package com.vyrriox.rspolymorph;

import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import com.illusivesoulworks.polymorph.common.capability.AbstractRecipeData;
import com.refinedmods.refinedstorage.common.support.RecipeMatrix;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Collections;

public class RsGridRecipeData extends AbstractRecipeData<BlockEntity> implements IBlockEntityRecipeData {
    private final RecipeMatrix<?, ?> recipeMatrix;

    public RsGridRecipeData(BlockEntity owner, RecipeMatrix<?, ?> recipeMatrix) {
        super(owner);
        this.recipeMatrix = recipeMatrix;
    }

    @Override
    public void tick() {
        if (getOwner().getLevel() != null && !getOwner().getLevel().isClientSide()) {
            updatePolymorph();
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Recipe<I>, I extends RecipeInput> void updatePolymorph() {
        IRsRecipeMatrix<T, I> rsMatrix = (IRsRecipeMatrix<T, I>) recipeMatrix;
        RecipeMatrixContainer container = recipeMatrix.getMatrix();
        
        if (container.isEmpty()) {
            return;
        }

        I input = rsMatrix.rspolymorph$getInputProvider().apply(container);
        this.getRecipe(rsMatrix.rspolymorph$getRecipeType(), input, getOwner().getLevel(), Collections.emptyList());
    }

    @Override
    public boolean isEmpty() {
        return recipeMatrix.getMatrix().isEmpty();
    }
}
