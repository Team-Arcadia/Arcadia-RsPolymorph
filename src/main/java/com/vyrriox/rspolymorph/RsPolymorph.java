package com.vyrriox.rspolymorph;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.vyrriox.rspolymorph.mixin.AccessorAbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridBlockEntity;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.CraftingGridBlockEntity;
import com.refinedmods.refinedstorage.common.grid.screen.CraftingGridScreen;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridScreen;
import com.refinedmods.refinedstorage.common.support.RecipeMatrix;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import com.vyrriox.rspolymorph.client.RsGridRecipeWidget;

/**
 * vyrriox: Main class for RS Polymorph integration.
 */
@Mod(RsPolymorph.MOD_ID)
public class RsPolymorph {
    public static final String MOD_ID = "rspolymorph";
    private static final Logger LOGGER = LogManager.getLogger();
    
    private static final Map<RecipeMatrixContainer, BlockEntity> CONTAINER_TO_BE = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<RecipeMatrixContainer, RecipeMatrix<?, ?>> CONTAINER_TO_MATRIX = Collections.synchronizedMap(new WeakHashMap<>());

    public RsPolymorph(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            registerWidget();
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("RS Polymorph (vyrriox) initializing...");
        event.enqueueWork(() -> {
            com.illusivesoulworks.polymorph.api.PolymorphApi.getInstance().registerBlockEntity(
                com.refinedmods.refinedstorage.common.grid.CraftingGridBlockEntity.class,
                (net.minecraft.world.level.block.entity.BlockEntity be) -> new RsGridRecipeData(be)
            );
            com.illusivesoulworks.polymorph.api.PolymorphApi.getInstance().registerBlockEntity(
                com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridBlockEntity.class,
                (net.minecraft.world.level.block.entity.BlockEntity be) -> new RsGridRecipeData(be)
            );
            com.illusivesoulworks.polymorph.api.PolymorphApi.getInstance().registerContainer2BlockEntity(menu -> {
                if (menu instanceof AbstractGridContainerMenu gridMenu) {
                    Grid grid = ((AccessorAbstractGridContainerMenu) gridMenu).rs_getGrid();
                    if (grid instanceof BlockEntity be) {
                        return be;
                    }
                }
                return null;
            });
        });
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

    public static void registerMatrixToContainer(RecipeMatrixContainer container, RecipeMatrix<?, ?> matrix) {
        CONTAINER_TO_MATRIX.put(container, matrix);
    }

    @SuppressWarnings("unchecked")
    public static RecipeHolder<?> getRecipe(RecipeMatrix<?, ?> matrix, Level level) {
        if (matrix.getMatrix().isEmpty()) {
            return null;
        }

        BlockEntity be = CONTAINER_TO_BE.get(matrix.getMatrix());
        if (be != null) {
            com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData data = com.illusivesoulworks.polymorph.api.PolymorphApi.getInstance().getBlockEntityRecipeData(be);
            if (data instanceof RsGridRecipeData rsData) {
                if (matrix instanceof IRsRecipeMatrix<?, ?>) {
                    IRsRecipeMatrix<?, ?> rsMatrix = (IRsRecipeMatrix<?, ?>) matrix;
                    RecipeType<?> matrixType = rsMatrix.rspolymorph$getRecipeType();
                    RecipeHolder<?> selected = rsData.getSelectedRecipe(matrixType);
                    
                    if (selected != null) {
                        Recipe<RecipeInput> recipe = (Recipe<RecipeInput>) selected.value();
                        RecipeInput input = (RecipeInput) rsMatrix.rspolymorph$getInputProvider().apply(matrix.getMatrix());
                        if (recipe.matches(input, level)) {
                            return selected;
                        }
                    }
                }
            }
        }
        return null;
    }

    private void registerWidget() {
        com.illusivesoulworks.polymorph.api.client.PolymorphWidgets.getInstance().registerWidget(screen -> {
            if (screen instanceof CraftingGridScreen || screen instanceof PatternGridScreen) {
                // Try Polymorph's own helper first
                net.minecraft.world.inventory.Slot found = com.illusivesoulworks.polymorph.api.client.PolymorphWidgets.getInstance().findResultSlot(screen);
                if (found != null && found.isActive()) {
                    return new RsGridRecipeWidget(screen, found);
                }

                // Fallback to manual search
                for (net.minecraft.world.inventory.Slot slot : screen.getMenu().slots) {
                    if (!slot.isActive()) continue;
                    
                    String className = slot.getClass().getName();
                    if (slot instanceof net.minecraft.world.inventory.ResultSlot || 
                        className.contains("ResultSlot") ||
                        className.contains("DisabledSlot") ||
                        className.contains("ProcessingMatrixResourceSlot") ||
                        className.contains("DisabledResourceSlot")) {
                        return new RsGridRecipeWidget(screen, slot);
                    }
                }
            }
            return null;
        });
    }
}
