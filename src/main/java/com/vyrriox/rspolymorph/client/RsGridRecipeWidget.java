package com.vyrriox.rspolymorph.client;

import com.illusivesoulworks.polymorph.api.client.base.PersistentRecipesWidget;
import com.illusivesoulworks.polymorph.api.client.widgets.children.SelectionWidget;
import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.vyrriox.rspolymorph.IRsRecipeMatrix;
import com.vyrriox.rspolymorph.RsPolymorph;
import com.vyrriox.rspolymorph.mixin.AccessorAbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.support.RecipeMatrix;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Custom Polymorph widget for RS2 grids.
 * Works on both singleplayer (shared JVM) and dedicated servers (separate JVMs).
 *
 * On dedicated servers, the menu slots may not directly expose RecipeMatrixContainer
 * (e.g., PatternGrid uses phantom/filter slots). In that case we fall back to:
 *  1. Accessing the Grid (BlockEntity) via the menu accessor
 *  2. Finding registered containers for that BE via CONTAINER_TO_BE reverse lookup
 *  3. Building CraftingInput directly as a last resort
 *
 * Author: vyrriox
 */
public class RsGridRecipeWidget extends PersistentRecipesWidget {

    private static RsGridRecipeWidget activeInstance = null;
    private final Slot outputSlot;
    private final AbstractContainerScreen<?> screen;

    /**
     * BlockEntity for the currently open grid — computed once at construction.
     * Found via menu slot scan or accessor fallback.
     * May be null if neither approach works.
     */
    private final BlockEntity activeBlockEntity;

    /** True only while the popup is intentionally open. */
    private boolean popupIsOpen = false;
    /** Grid contents hash at the moment the popup was opened. */
    private int popupOpenedAtHash = 0;

    /**
     * Cache for hasMultipleRecipes().
     * Recomputed only when the input hash changes, not every frame.
     */
    private boolean cachedHasMultiple = false;
    private int lastHashForMultipleCheck = Integer.MIN_VALUE;

    public RsGridRecipeWidget(AbstractContainerScreen<?> screen, Slot outputSlot) {
        super(screen);
        this.screen = screen;
        this.outputSlot = outputSlot;
        this.activeBlockEntity = findBlockEntity(screen);
        activeInstance = this;
    }

    /**
     * Finds the BlockEntity that backs this screen's crafting grid.
     *
     * Strategy 1: scan menu slots for RecipeMatrixContainer and look up CONTAINER_TO_BE.
     *   Works for CraftingGrid where slots directly reference the BE's containers.
     *
     * Strategy 2 (fallback): use the accessor on AbstractGridContainerMenu to get the
     *   Grid field, which IS the BlockEntity on both client and server.
     *   Works for PatternGrid where slots may be phantom/filter slots.
     */
    private static BlockEntity findBlockEntity(AbstractContainerScreen<?> screen) {
        // Strategy 1: slot scan
        for (Slot slot : screen.getMenu().slots) {
            if (slot.container instanceof RecipeMatrixContainer rmc) {
                BlockEntity be = RsPolymorph.getBlockEntityForContainer(rmc);
                if (be != null) return be;
            }
        }

        // Strategy 2: accessor on the menu's Grid field
        if (screen.getMenu() instanceof AccessorAbstractGridContainerMenu accessor) {
            Object grid = accessor.rspolymorph$getGrid();
            if (grid instanceof BlockEntity be) return be;
        }

        return null;
    }

    public static RsGridRecipeWidget getActiveInstance() {
        return activeInstance;
    }

    public boolean isPopupOpen() {
        return popupIsOpen;
    }

    @Override
    public Slot getOutputSlot() {
        return outputSlot;
    }

    // -------------------------------------------------------------------------
    // Container discovery — works for both CraftingGrid and PatternGrid
    // -------------------------------------------------------------------------

    /**
     * Returns the RecipeMatrixContainer instances relevant to this screen.
     *
     * Path 1: scan menu slots directly (CraftingGrid — slots reference the container).
     * Path 2: reverse-lookup CONTAINER_TO_BE for the active BlockEntity.
     *   This finds containers registered by MixinCraftingGrid/MixinPatternGrid
     *   on the CLIENT-SIDE BE construction (works even if slots are phantom).
     */
    private List<RecipeMatrixContainer> getContainers() {
        // Path 1: direct from menu slots
        List<RecipeMatrixContainer> fromSlots = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        for (Slot slot : screen.getMenu().slots) {
            if (slot.container instanceof RecipeMatrixContainer rmc) {
                if (seen.add(System.identityHashCode(rmc))) {
                    fromSlots.add(rmc);
                }
            }
        }
        if (!fromSlots.isEmpty()) return fromSlots;

        // Path 2: reverse-lookup from the BE
        if (activeBlockEntity != null) {
            Map<RecipeMatrixContainer, BlockEntity> beMap = RsPolymorph.getMatrixMap();
            List<RecipeMatrixContainer> fromBe = new ArrayList<>();
            synchronized (beMap) {
                for (Map.Entry<RecipeMatrixContainer, BlockEntity> entry : beMap.entrySet()) {
                    if (entry.getValue() == activeBlockEntity) {
                        fromBe.add(entry.getKey());
                    }
                }
            }
            if (!fromBe.isEmpty()) return fromBe;
        }

        return Collections.emptyList();
    }

    /**
     * Queries available recipes for the given container.
     *
     * Primary: uses RecipeMatrix from CONTAINER_TO_MATRIX (correct type + inputProvider).
     * Fallback: builds CraftingInput directly for 3x3 / 2x2 grids.
     */
    @SuppressWarnings("unchecked")
    private List<RecipeHolder<?>> queryRecipes(RecipeMatrixContainer container, ClientLevel level) {
        // Primary: use RecipeMatrix if registered
        RecipeMatrix<?, ?> matrix = RsPolymorph.getContainerToMatrixMap().get(container);
        if (matrix instanceof IRsRecipeMatrix<?, ?> rsMatrix) {
            RecipeInput input = (RecipeInput) rsMatrix.rspolymorph$getInputProvider().apply(container);
            if (input != null) {
                RecipeType<Recipe<RecipeInput>> type =
                        (RecipeType<Recipe<RecipeInput>>) rsMatrix.rspolymorph$getRecipeType();
                return (List<RecipeHolder<?>>) (List<?>) level.getRecipeManager().getRecipesFor(type, input, level);
            }
        }

        // Fallback: build CraftingInput directly
        int size = container.getContainerSize();
        if (size == 9) {
            List<ItemStack> items = new ArrayList<>(9);
            for (int i = 0; i < 9; i++) items.add(container.getItem(i));
            CraftingInput input = CraftingInput.of(3, 3, items);
            return (List<RecipeHolder<?>>) (List<?>) level.getRecipeManager()
                    .getRecipesFor(RecipeType.CRAFTING, input, level);
        } else if (size == 4) {
            List<ItemStack> items = new ArrayList<>(4);
            for (int i = 0; i < 4; i++) items.add(container.getItem(i));
            CraftingInput input = CraftingInput.of(2, 2, items);
            return (List<RecipeHolder<?>>) (List<?>) level.getRecipeManager()
                    .getRecipesFor(RecipeType.CRAFTING, input, level);
        }
        return List.of();
    }

    // -------------------------------------------------------------------------
    // Render
    // -------------------------------------------------------------------------

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (popupIsOpen) {
            if (computeInputHash() != popupOpenedAtHash) {
                closePopup();
            }
        }

        if (!popupIsOpen) {
            closeSelectionWidgetViaToggle();
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        if (!popupIsOpen) {
            SelectionWidget sel = this.getSelectionWidget();
            if (sel != null) sel.setActive(false);
        }

        if (this.openButton != null) {
            this.openButton.visible = false;
        }
    }

    private void closePopup() {
        popupIsOpen = false;
        RsPolymorph.setSelectedRecipeId(null);
        this.setRecipesList(new TreeSet<>(), null);
    }

    private void closeSelectionWidgetViaToggle() {
        SelectionWidget sel = this.getSelectionWidget();
        if (sel == null || !sel.isActive()) return;

        if (this.openButton != null) {
            this.openButton.active = true;
            this.openButton.onPress();
            this.openButton.active = false;
        }
        sel.setActive(false);
    }

    // -------------------------------------------------------------------------
    // Input hash
    // -------------------------------------------------------------------------

    private int computeInputHash() {
        List<RecipeMatrixContainer> containers = getContainers();
        containers = new ArrayList<>(containers); // mutable copy for sorting
        containers.sort(Comparator.comparingInt(System::identityHashCode));

        int hash = 1;
        for (RecipeMatrixContainer container : containers) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (stack.isEmpty()) {
                    hash = 31 * hash;
                } else {
                    hash = 31 * hash + stack.getItem().hashCode();
                    hash = 31 * hash + stack.getCount();
                }
            }
        }
        return hash;
    }

    // -------------------------------------------------------------------------
    // hasMultipleRecipes
    // -------------------------------------------------------------------------

    public boolean hasMultipleRecipes() {
        int hash = computeInputHash();
        if (hash != lastHashForMultipleCheck) {
            lastHashForMultipleCheck = hash;
            cachedHasMultiple = queryHasMultipleRecipes();
        }
        return cachedHasMultiple;
    }

    private boolean queryHasMultipleRecipes() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return false;

        for (RecipeMatrixContainer container : getContainers()) {
            if (container.isEmpty()) continue;
            if (queryRecipes(container, level).size() > 1) return true;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // triggerSelection
    // -------------------------------------------------------------------------

    public void triggerSelection() {
        if (this.openButton == null) return;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Set<IRecipePair> pairs = new TreeSet<>();
        for (RecipeMatrixContainer container : getContainers()) {
            if (container.isEmpty()) continue;
            for (RecipeHolder<?> holder : queryRecipes(container, level)) {
                ItemStack output = holder.value().getResultItem(level.registryAccess());
                pairs.add(new RecipePairEntry(holder.id(), output));
            }
        }

        if (pairs.size() < 2) return;

        popupOpenedAtHash = computeInputHash();
        popupIsOpen = true;

        this.setRecipesList(pairs, null);
        this.openButton.active = true;
        this.openButton.visible = true;
        this.openButton.onPress();
        this.openButton.visible = false;
    }

    // -------------------------------------------------------------------------
    // selectRecipe
    // -------------------------------------------------------------------------

    @Override
    public void selectRecipe(ResourceLocation resourceLocation) {
        popupIsOpen = false;
        RsPolymorph.setSelectedRecipeId(resourceLocation);

        final BlockEntity target = activeBlockEntity;

        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null) {
            server.execute(() -> {
                for (Map.Entry<RecipeMatrixContainer, RecipeMatrix<?, ?>> entry :
                        RsPolymorph.getContainerToMatrixMap().entrySet()) {
                    RecipeMatrixContainer container = entry.getKey();
                    if (container.isEmpty()) continue;
                    BlockEntity be = RsPolymorph.getBlockEntityForContainer(container);
                    if (be == null) continue;
                    if (target != null && be != target) continue;
                    if (be.getLevel() != null && !be.getLevel().isClientSide()) {
                        entry.getValue().updateResult(be.getLevel());
                    }
                }
            });
        } else {
            PacketDistributor.sendToServer(
                    new com.vyrriox.rspolymorph.network.SelectRecipePacket(resourceLocation));
        }
    }

    // -------------------------------------------------------------------------
    // RecipePairEntry
    // -------------------------------------------------------------------------

    private static final class RecipePairEntry implements IRecipePair {
        private final ResourceLocation id;
        private final ItemStack output;

        RecipePairEntry(ResourceLocation id, ItemStack output) {
            this.id = id;
            this.output = output;
        }

        @Override public ItemStack getOutput() { return output; }
        @Override public ResourceLocation getResourceLocation() { return id; }
        @Override public int compareTo(IRecipePair other) {
            return id.compareTo(other.getResourceLocation());
        }
    }
}
