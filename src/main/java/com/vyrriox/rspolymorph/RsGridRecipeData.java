package com.vyrriox.rspolymorph;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import com.illusivesoulworks.polymorph.common.capability.AbstractBlockEntityRecipeData;
import com.refinedmods.refinedstorage.common.support.RecipeMatrix;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Custom recipe data for RS2 Grids.
 * Author: vyrriox
 */
public class RsGridRecipeData extends com.illusivesoulworks.polymorph.common.capability.AbstractBlockEntityRecipeData<BlockEntity> implements com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData {
    private final Map<RecipeType<?>, RecipeHolder<?>> selections = new HashMap<>();

    public RsGridRecipeData(BlockEntity owner) {
        super(owner);
    }

    private List<RecipeMatrix<?, ?>> getMatrices() {
        List<RecipeMatrix<?, ?>> matrices = new ArrayList<>();
        Map<RecipeMatrixContainer, BlockEntity> map = RsPolymorph.getMatrixMap();
        Map<RecipeMatrixContainer, RecipeMatrix<?, ?>> matrixMap = RsPolymorph.getContainerToMatrixMap();
        synchronized (map) {
            for (Map.Entry<RecipeMatrixContainer, BlockEntity> entry : map.entrySet()) {
                if (entry.getValue() == getOwner()) {
                    RecipeMatrix<?, ?> matrix = matrixMap.get(entry.getKey());
                    if (matrix != null) {
                        matrices.add(matrix);
                    }
                }
            }
        }
        return matrices;
    }

    @Override
    protected NonNullList<ItemStack> getInput() {
        NonNullList<ItemStack> stacks = NonNullList.create();
        for (RecipeMatrix<?, ?> matrix : getMatrices()) {
            RecipeMatrixContainer container = matrix.getMatrix();
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (!stack.isEmpty()) {
                    stacks.add(stack.copy());
                }
            }
        }
        return stacks;
    }

    public RecipeHolder<?> getSelectedRecipe(RecipeType<?> type) {
        return selections.get(type);
    }

    @Override
    public void selectRecipe(@NotNull RecipeHolder<?> recipe) {
        if (recipe != null) {
            selections.put(recipe.value().getType(), recipe);
            super.setSelectedRecipe(recipe); 
            
            // Notify RS2 matrices to update
            Level level = getOwner().getLevel();
            if (level != null && !level.isClientSide()) {
                for (RecipeMatrix<?, ?> matrix : getMatrices()) {
                    if (((IRsRecipeMatrix<?, ?>) matrix).rspolymorph$getRecipeType() == recipe.value().getType()) {
                        matrix.updateResult(level);
                    }
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public boolean isEmpty() {
        for (RecipeMatrix<?, ?> matrix : getMatrices()) {
            if (!matrix.getMatrix().isEmpty()) return false;
        }
        return true;
    }
}
