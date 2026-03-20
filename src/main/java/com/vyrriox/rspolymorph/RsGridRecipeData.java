package com.vyrriox.rspolymorph;

import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import com.illusivesoulworks.polymorph.common.capability.AbstractRecipeData;
import com.refinedmods.refinedstorage.common.support.RecipeMatrix;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

public class RsGridRecipeData extends AbstractRecipeData<BlockEntity> implements IBlockEntityRecipeData {

    public RsGridRecipeData(BlockEntity owner) {
        super(owner);
    }

    private List<RecipeMatrix<?, ?>> getMatrices() {
        List<RecipeMatrix<?, ?>> matrices = new ArrayList<>();
        Map<RecipeMatrix<?, ?>, BlockEntity> map = RsPolymorph.getMatrixMap();
        synchronized (map) {
            for (Map.Entry<RecipeMatrix<?, ?>, BlockEntity> entry : map.entrySet()) {
                if (entry.getValue() == getOwner()) {
                    matrices.add(entry.getKey());
                }
            }
        }
        return matrices;
    }

    @Override
    public void setSelectedRecipe(RecipeHolder<?> recipe) {
        super.setSelectedRecipe(recipe);
        if (getOwner().getLevel() != null && !getOwner().getLevel().isClientSide()) {
            for (RecipeMatrix<?, ?> matrix : getMatrices()) {
                matrix.updateResult(getOwner().getLevel());
            }
        }
    }

    @Override
    public void tick() {
        if (getOwner().getLevel() != null && !getOwner().getLevel().isClientSide()) {
            updatePolymorph();
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Recipe<I>, I extends RecipeInput> void updatePolymorph() {
        for (RecipeMatrix<?, ?> recipeMatrix : getMatrices()) {
            IRsRecipeMatrix<T, I> rsMatrix = (IRsRecipeMatrix<T, I>) recipeMatrix;
            RecipeMatrixContainer container = recipeMatrix.getMatrix();
            if (container.isEmpty()) continue;

            I input = rsMatrix.rspolymorph$getInputProvider().apply(container);
            this.getRecipe(rsMatrix.rspolymorph$getRecipeType(), input, getOwner().getLevel(), Collections.emptyList());
        }
    }

    @Override
    public boolean isEmpty() {
        for (RecipeMatrix<?, ?> matrix : getMatrices()) {
            if (!matrix.getMatrix().isEmpty()) return false;
        }
        return true;
    }
}
