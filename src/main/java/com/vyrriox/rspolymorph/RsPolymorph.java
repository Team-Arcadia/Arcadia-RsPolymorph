package com.vyrriox.rspolymorph;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridBlockEntity;
import com.refinedmods.refinedstorage.common.grid.CraftingGridBlockEntity;
import com.refinedmods.refinedstorage.common.support.RecipeMatrix;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.vyrriox.rspolymorph.network.SelectRecipePacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main class for RS Polymorph — integrates Polymorph recipe selection with RS2 grids.
 *
 * IMPORTANT: This class must NEVER reference client-only types (screens, widgets, Minecraft class)
 * directly — not even inside lambdas — because the JVM verifier resolves types in ALL methods
 * (including compiler-generated lambda bodies) at class-loading time. On a dedicated server the
 * client classes do not exist, so any reference here causes a ClassNotFoundException at startup.
 *
 * All client code lives in {@code com.vyrriox.rspolymorph.client.ClientSetup}.
 *
 * Author: vyrriox
 */
@Mod(RsPolymorph.MOD_ID)
public class RsPolymorph {

    public static final String MOD_ID = "rspolymorph";
    private static final Logger LOGGER = LogManager.getLogger();

    // ── Data component ────────────────────────────────────────────────────────
    /** Stored on crafting pattern items to persist the user's Polymorph recipe selection. */
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> SELECTED_RECIPE_COMPONENT =
            DATA_COMPONENT_TYPES.register("selected_recipe", () ->
                    DataComponentType.<ResourceLocation>builder()
                            .persistent(ResourceLocation.CODEC)
                            .build());

    // ── Registry maps ─────────────────────────────────────────────────────────
    /** RecipeMatrixContainer → the BlockEntity that owns it. Populated on BE construction. */
    private static final Map<RecipeMatrixContainer, BlockEntity> CONTAINER_TO_BE =
            new ConcurrentHashMap<>();

    /** RecipeMatrixContainer → the RecipeMatrix wrapping it. Populated on RecipeMatrix construction. */
    private static final Map<RecipeMatrixContainer, RecipeMatrix<?, ?>> CONTAINER_TO_MATRIX =
            new ConcurrentHashMap<>();

    /**
     * RecipeMatrix → the recipe selection chosen for it, for grids that have NO BlockEntity
     * (e.g. the Quartz Arsenal wireless crafting grid). The BlockEntity-keyed Polymorph
     * capability cannot persist a selection for these, so we keep it here keyed by the matrix.
     *
     * Reclamation is deterministic via {@link #unregisterMatrix} on menu close — that is the
     * load-bearing mechanism, because {@code CONTAINER_TO_MATRIX} strong-pins each matrix and
     * would otherwise keep this entry reachable forever. The WeakHashMap with weak keys is kept
     * only as a best-effort safety net (e.g. if a close hook is ever missed). Wrapped in a
     * synchronized map because it is touched from the server thread (packet handler), the matrix
     * mixin's updateResult, and the menu-close hook.
     */
    private static final Map<RecipeMatrix<?, ?>, ResourceLocation> MATRIX_SELECTION =
            Collections.synchronizedMap(new WeakHashMap<>());

    // ── Selection state ───────────────────────────────────────────────────────
    /**
     * Recipe ID chosen by the client via the Polymorph popup.
     * In singleplayer the JVM is shared, so server-side code reads this directly.
     * Volatile for cross-thread visibility.
     */
    private static volatile ResourceLocation selectedRecipeId = null;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    public RsPolymorph(IEventBus modEventBus) {
        DATA_COMPONENT_TYPES.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(
                        SelectRecipePacket.TYPE,
                        SelectRecipePacket.STREAM_CODEC,
                        SelectRecipePacket::handleOnServer
                );
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("RS Polymorph initializing...");

        event.enqueueWork(() -> {
            // IMPORTANT: Polymorph's createBlockEntityRecipeData iterates a flat
            // List<IRecipeDataFactory> and accepts the first factory that returns non-null
            // — the Class<?> argument we pass is metadata only, not enforced. A factory
            // that always returns a value would attach RsGridRecipeData to EVERY block
            // entity in the world (Create encased fans, hoppers, anything). That, combined
            // with Polymorph's per-data RecipeCache being keyed by input alone (not by
            // RecipeType), causes cross-type recipe leaks: a SMOKING query primes the
            // cache, the next SMELTING query with the same input hits the cache and
            // returns SMOKING recipes, and the caller crashes on ClassCastException.
            // Filter by runtime class so non-RS2 BEs fall through to the next factory.
            PolymorphApi.getInstance().registerBlockEntity(
                    CraftingGridBlockEntity.class,
                    be -> be instanceof CraftingGridBlockEntity ? new RsGridRecipeData(be) : null);
            PolymorphApi.getInstance().registerBlockEntity(
                    PatternGridBlockEntity.class,
                    be -> be instanceof PatternGridBlockEntity ? new RsGridRecipeData(be) : null);
        });

        // Client widget registration lives in a separate class to keep all client-only
        // type references out of this class's constant pool / lambda methods.
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            com.vyrriox.rspolymorph.client.ClientSetup.init();
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public static void setSelectedRecipeId(ResourceLocation id) {
        selectedRecipeId = id;
    }

    public static ResourceLocation getSelectedRecipeId() {
        return selectedRecipeId;
    }

    public static Map<RecipeMatrixContainer, BlockEntity> getMatrixMap() {
        return CONTAINER_TO_BE;
    }

    public static Map<RecipeMatrixContainer, RecipeMatrix<?, ?>> getContainerToMatrixMap() {
        return CONTAINER_TO_MATRIX;
    }

    public static void registerContainerBlockEntity(RecipeMatrixContainer container, BlockEntity be) {
        CONTAINER_TO_BE.put(container, be);
    }

    public static BlockEntity getBlockEntityForContainer(RecipeMatrixContainer container) {
        return CONTAINER_TO_BE.get(container);
    }

    public static void registerMatrixToContainer(RecipeMatrixContainer container, RecipeMatrix<?, ?> matrix) {
        CONTAINER_TO_MATRIX.put(container, matrix);
    }

    // ── Per-matrix selection (BlockEntity-free grids, e.g. wireless crafting grid) ──

    /**
     * Persists a recipe selection for a grid that has no BlockEntity. {@code null} clears it.
     *
     * Guarded so the per-matrix store can NEVER shadow a BlockEntity-backed matrix: if the
     * matrix's container is registered to a BlockEntity, the selection must go through the
     * Polymorph capability ({@link RsGridRecipeData}) instead, and we ignore the store request.
     * This keeps path-2 in {@code MixinRecipeMatrix} exclusively the non-BE channel.
     */
    public static void setMatrixSelection(RecipeMatrix<?, ?> matrix, ResourceLocation recipeId) {
        if (matrix == null) return;
        if (recipeId == null) {
            MATRIX_SELECTION.remove(matrix);
            return;
        }
        // BE-backed matrix → use the Polymorph capability path, not this store.
        if (CONTAINER_TO_BE.get(matrix.getMatrix()) != null) return;
        MATRIX_SELECTION.put(matrix, recipeId);
    }

    /** Returns the per-matrix recipe selection, or {@code null} if none was stored. */
    public static ResourceLocation getMatrixSelection(RecipeMatrix<?, ?> matrix) {
        if (matrix == null) return null;
        return MATRIX_SELECTION.get(matrix);
    }

    /**
     * Deterministically reclaims the registrations for a BlockEntity-free grid's crafting matrix
     * when its menu closes. Removes the strong {@code CONTAINER_TO_MATRIX} entry (the actual leak
     * root — the WeakHashMap selection store alone cannot collect because this map strong-pins the
     * matrix) and the per-matrix selection in one go.
     *
     * CONTRACT: only call for grids that have NO BlockEntity. For BE-backed grids the mapping is
     * owned by the BlockEntity lifecycle and must not be dropped on menu close.
     */
    public static void unregisterMatrix(RecipeMatrixContainer container) {
        if (container == null) return;
        RecipeMatrix<?, ?> m = CONTAINER_TO_MATRIX.remove(container);
        if (m != null) MATRIX_SELECTION.remove(m);
    }

    /**
     * Returns the Polymorph-persisted recipe for the given matrix, if one was selected
     * and still matches the current grid inputs. Returns null otherwise.
     */
    @SuppressWarnings("unchecked")
    public static RecipeHolder<?> getRecipe(RecipeMatrix<?, ?> matrix, Level level) {
        if (matrix.getMatrix().isEmpty()) return null;

        BlockEntity be = CONTAINER_TO_BE.get(matrix.getMatrix());
        if (be == null) return null;

        var data = PolymorphApi.getInstance().getBlockEntityRecipeData(be);
        if (!(data instanceof RsGridRecipeData rsData)) return null;
        if (!(matrix instanceof IRsRecipeMatrix<?, ?> rsMatrix)) return null;

        RecipeHolder<?> selected = rsData.getSelectedRecipe(rsMatrix.rspolymorph$getRecipeType());
        if (selected == null) return null;

        // Validate the selection still matches the current inputs.
        Recipe<RecipeInput> recipe = (Recipe<RecipeInput>) selected.value();
        RecipeInput input = (RecipeInput) rsMatrix.rspolymorph$getInputProvider().apply(matrix.getMatrix());
        return (input != null && recipe.matches(input, level)) ? selected : null;
    }
}
