package com.vyrriox.rspolymorph;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import com.illusivesoulworks.polymorph.api.client.widgets.PlayerRecipesWidget;
import com.vyrriox.rspolymorph.mixin.AccessorAbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridBlockEntity;
import com.refinedmods.refinedstorage.common.grid.CraftingGridBlockEntity;
import com.refinedmods.refinedstorage.common.grid.CraftingGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.screen.CraftingGridScreen;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridScreen;
import com.refinedmods.refinedstorage.common.support.RecipeMatrix;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.Collections;

@Mod(RsPolymorph.MOD_ID)
public class RsPolymorph {
    public static final String MOD_ID = "rspolymorph";
    
    private static final Map<RecipeMatrix<?, ?>, BlockEntity> MATRIX_TO_BE = Collections.synchronizedMap(new WeakHashMap<>());

    public RsPolymorph(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        PolymorphApi api = PolymorphApi.getInstance();
        
        api.registerBlockEntity(CraftingGridBlockEntity.class, blockEntity -> {
            synchronized (MATRIX_TO_BE) {
                for (Map.Entry<RecipeMatrix<?, ?>, BlockEntity> entry : MATRIX_TO_BE.entrySet()) {
                    if (entry.getValue() == blockEntity) {
                        return new RsGridRecipeData(blockEntity, entry.getKey());
                    }
                }
            }
            return null;
        });
        
        api.registerBlockEntity(PatternGridBlockEntity.class, blockEntity -> {
             synchronized (MATRIX_TO_BE) {
                for (Map.Entry<RecipeMatrix<?, ?>, BlockEntity> entry : MATRIX_TO_BE.entrySet()) {
                    if (entry.getValue() == blockEntity) {
                        return new RsGridRecipeData(blockEntity, entry.getKey());
                    }
                }
            }
            return null;
        });

        api.registerMenu(menu -> {
            if (menu instanceof AccessorAbstractGridContainerMenu accessor) {
                Grid grid = accessor.rspolymorph$getGrid();
                if (grid instanceof BlockEntity be) {
                    return be;
                }
            }
            return null;
        });
    }
    
    public static void registerMatrix(RecipeMatrix<?, ?> matrix, BlockEntity blockEntity) {
        MATRIX_TO_BE.put(matrix, blockEntity);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Recipe<I>, I extends RecipeInput> RecipeHolder<T> getRecipe(RecipeMatrix<T, I> matrix, Level level) {
        BlockEntity be = MATRIX_TO_BE.get(matrix);
        if (be != null && !level.isClientSide()) {
            return (RecipeHolder<T>) PolymorphApi.getInstance().getBlockEntityRecipeData(be).getSelectedRecipe();
        }
        return null;
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        PolymorphWidgets.getInstance().registerWidget(screen -> {
            if (screen instanceof CraftingGridScreen || screen instanceof PatternGridScreen) {
                for (Slot slot : screen.getMenu().slots) {
                    if (slot.getClass().getName().contains("ResultSlot") || 
                        slot.getClass().getName().contains("DisabledSlot")) {
                         return new PlayerRecipesWidget(screen, slot);
                    }
                }
            }
            return null;
        });
    }
}
