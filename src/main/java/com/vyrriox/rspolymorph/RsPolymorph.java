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

/**
 * vyrriox: Main class for RS Polymorph integration.
 */
@Mod(RsPolymorph.MOD_ID)
public class RsPolymorph {
    public static final String MOD_ID = "rspolymorph";
    private static final Logger LOGGER = LogManager.getLogger();
    
    private static final Map<RecipeMatrix<?, ?>, BlockEntity> MATRIX_TO_BE = Collections.synchronizedMap(new WeakHashMap<>());

    public RsPolymorph(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
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
        });
    }

    public static Map<RecipeMatrix<?, ?>, BlockEntity> getMatrixMap() {
        return MATRIX_TO_BE;
    }

    public static <T extends Recipe<I>, I extends RecipeInput> void registerMenu(RecipeMatrix<T, I> matrix, net.minecraft.world.inventory.AbstractContainerMenu menu) {
        if (menu instanceof AccessorAbstractGridContainerMenu accessor) {
            com.refinedmods.refinedstorage.common.api.grid.Grid grid = accessor.rspolymorph$getGrid();
            if (grid instanceof com.refinedmods.refinedstorage.common.grid.CraftingGridBlockEntity be) {
                MATRIX_TO_BE.put(matrix, be);
            } else if (grid instanceof com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridBlockEntity be) {
                MATRIX_TO_BE.put(matrix, be);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Recipe<I>, I extends RecipeInput> RecipeHolder<T> getRecipe(RecipeMatrix<T, I> matrix, Level level) {
        if (matrix.getMatrix().isEmpty()) {
            return null;
        }

        BlockEntity be = MATRIX_TO_BE.get(matrix);
        if (be != null) {
            com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData data = com.illusivesoulworks.polymorph.api.PolymorphApi.getInstance().getBlockEntityRecipeData(be);
            if (data instanceof RsGridRecipeData rsData) {
                IRsRecipeMatrix<T, I> rsMatrix = (IRsRecipeMatrix<T, I>) matrix;
                RecipeType<T> matrixType = rsMatrix.rspolymorph$getRecipeType();
                RecipeHolder<?> selected = rsData.getSelectedRecipe(matrixType);
                
                if (selected != null) {
                    Recipe<I> recipe = (Recipe<I>) selected.value();
                    I input = rsMatrix.rspolymorph$getInputProvider().apply(matrix.getMatrix());
                    if (recipe.matches(input, level)) {
                        return (RecipeHolder<T>) selected;
                    }
                }
            }
        }
        return null;
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        com.illusivesoulworks.polymorph.api.client.PolymorphWidgets.getInstance().registerWidget(screen -> {
            if (screen instanceof CraftingGridScreen || screen instanceof PatternGridScreen) {
                // Attempt to find the appropriate slot for the Polymorph button (Crafting Output)
                for (Slot slot : screen.getMenu().slots) {
                    if (!slot.isActive()) continue;
                    
                    String className = slot.getClass().getSimpleName();
                    if (slot instanceof net.minecraft.world.inventory.ResultSlot || 
                        className.contains("ResultSlot") ||
                        (screen instanceof PatternGridScreen && className.contains("DisabledSlot"))) {
                        return new com.illusivesoulworks.polymorph.api.client.widgets.PlayerRecipesWidget(screen, slot);
                    }
                }
            }
            return null;
        });
    }
}
