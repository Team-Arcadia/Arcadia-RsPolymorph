package com.vyrriox.rspolymorph.network;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.vyrriox.rspolymorph.RsGridRecipeData;
import com.vyrriox.rspolymorph.RsPolymorph;
import com.refinedmods.refinedstorage.common.grid.CraftingGrid;
import com.refinedmods.refinedstorage.common.support.RecipeMatrix;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import com.vyrriox.rspolymorph.mixin.AccessorAbstractCraftingGridContainerMenu;
import com.vyrriox.rspolymorph.mixin.AccessorAbstractGridContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * Client → Server packet to communicate Polymorph recipe selection to the dedicated server.
 *
 * In singleplayer the JVM is shared, so the client can directly schedule work on the
 * integrated server thread (see RsGridRecipeWidget.selectRecipe). On a dedicated server
 * the client must send this packet instead.
 *
 * Author: vyrriox
 */
public record SelectRecipePacket(ResourceLocation recipeId) implements CustomPacketPayload {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final Type<SelectRecipePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(RsPolymorph.MOD_ID, "select_recipe"));

    public static final StreamCodec<FriendlyByteBuf, SelectRecipePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC,
                    SelectRecipePacket::recipeId,
                    SelectRecipePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Server-side handler. Looks up the recipe by ID and applies it to the BlockEntity
     * backing the player's currently open crafting grid.
     *
     * Strategy 1: scan menu slots for RecipeMatrixContainer (works for CraftingGrid).
     * Strategy 2: use the menu accessor to get the Grid field (works for PatternGrid
     *             where slots are phantom/filter and don't expose RecipeMatrixContainer).
     */
    public static void handleOnServer(SelectRecipePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            Level level = player.level();
            Optional<RecipeHolder<?>> recipeOpt = level.getRecipeManager().byKey(packet.recipeId());
            if (recipeOpt.isEmpty()) {
                LOGGER.warn("[RS Polymorph] Server received unknown recipe ID: {}", packet.recipeId());
                return;
            }
            RecipeHolder<?> recipe = recipeOpt.get();

            BlockEntity targetBe = findBlockEntity(player.containerMenu);
            if (targetBe != null) {
                var data = PolymorphApi.getInstance().getBlockEntityRecipeData(targetBe);
                if (data instanceof RsGridRecipeData rsData) {
                    // Set the static selectedRecipeId so MixinPatternGrid.createCraftingPattern()
                    // can tag the pattern with the chosen recipe on the server side.
                    RsPolymorph.setSelectedRecipeId(packet.recipeId());
                    rsData.selectRecipe(recipe);
                    RsPolymorph.setSelectedRecipeId(null);
                }
                return;
            }

            // No BlockEntity — this is a BlockEntity-free grid (e.g. the Quartz Arsenal
            // wireless crafting grid). Drive the selection through its RecipeMatrix directly.
            if (applyToBlockEntityFreeGrid(player, packet.recipeId())) {
                return;
            }

            LOGGER.warn("[RS Polymorph] Could not find target grid for recipe selection (player: {})", player.getName().getString());
        });
    }

    /**
     * Applies the selection to a crafting grid that has no BlockEntity.
     *
     * Resolves the open menu's {@link com.refinedmods.refinedstorage.common.grid.CraftingGrid}
     * via the menu accessor, looks up the {@link RecipeMatrix} that wraps its crafting matrix
     * container (registered by MixinRecipeMatrix on construction), persists the choice in the
     * per-matrix selection store so it survives later updateResult calls, then drives
     * {@code updateResult}. The static selectedRecipeId provides the fast path for this first
     * apply; the per-matrix store keeps it sticky afterwards.
     *
     * The override itself is applied by MixinRecipeMatrix at updateResult RETURN, which writes
     * the chosen output into the grid's ResultContainer via setResult. The result reaches the
     * client through the menu's crafting result slot (added over craftingGrid.getCraftingResult()
     * in the AbstractCraftingGridContainerMenu constructor) on the next broadcastChanges() tick —
     * the same path the wired grid uses. RecipeMatrix.setResult does NOT run the matrix listener,
     * so we do not rely on it for the client sync.
     *
     * @return true if a matrix was found and updated; false if this menu is not a crafting grid
     *         or its matrix is not registered.
     */
    private static boolean applyToBlockEntityFreeGrid(ServerPlayer player, ResourceLocation recipeId) {
        AbstractContainerMenu menu = player.containerMenu;
        if (!(menu instanceof AccessorAbstractCraftingGridContainerMenu craftingMenu)) return false;

        CraftingGrid craftingGrid = craftingMenu.rspolymorph$getCraftingGrid();
        if (craftingGrid == null) return false;

        RecipeMatrixContainer container = craftingGrid.getCraftingMatrix();
        if (container == null) return false;

        RecipeMatrix<?, ?> matrix = RsPolymorph.getContainerToMatrixMap().get(container);
        if (matrix == null) return false;

        Level level = player.level();

        // Persist for subsequent updateResult calls (input changes re-apply via MixinRecipeMatrix).
        RsPolymorph.setMatrixSelection(matrix, recipeId);

        // Fast path for this apply; MixinRecipeMatrix reads the static during updateResult and
        // writes the chosen output into the result slot, which broadcastChanges syncs to the client.
        RsPolymorph.setSelectedRecipeId(recipeId);
        try {
            matrix.updateResult(level);
        } finally {
            RsPolymorph.setSelectedRecipeId(null);
        }
        return true;
    }

    /**
     * Finds the BlockEntity backing the player's open grid menu.
     *
     * Both strategies are scoped to the OPEN menu, so the selection can only ever be applied
     * to the grid the player is actually looking at. (A previous global reverse-scan fallback
     * was removed: it returned the first registered grid with Polymorph data regardless of which
     * menu was open, which mis-routed wireless-grid selections onto an unrelated wired grid and
     * corrupted it.)
     *
     * Strategy 1: scan menu slots for RecipeMatrixContainer → CONTAINER_TO_BE lookup (CraftingGrid).
     * Strategy 2: accessor on AbstractGridContainerMenu to get the Grid field directly (PatternGrid).
     *
     * Returns null for grids with no BlockEntity (e.g. the wireless crafting grid); the caller
     * then routes through {@link #applyToBlockEntityFreeGrid}.
     */
    private static BlockEntity findBlockEntity(AbstractContainerMenu menu) {
        // Strategy 1: slot scan (works for CraftingGrid)
        for (Slot slot : menu.slots) {
            if (!(slot.container instanceof RecipeMatrixContainer rmc)) continue;
            BlockEntity be = RsPolymorph.getBlockEntityForContainer(rmc);
            if (be != null) return be;
        }

        // Strategy 2: accessor on the menu's Grid field (works for PatternGrid)
        if (menu instanceof AccessorAbstractGridContainerMenu accessor) {
            Object grid = accessor.rspolymorph$getGrid();
            if (grid instanceof BlockEntity be) return be;
        }

        return null;
    }
}
