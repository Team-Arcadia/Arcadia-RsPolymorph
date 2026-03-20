package com.vyrriox.rspolymorph;

import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.function.Function;

public interface IRsRecipeMatrix<T extends Recipe<I>, I extends RecipeInput> {
    RecipeType<T> rspolymorph$getRecipeType();
    Function<RecipeMatrixContainer, I> rspolymorph$getInputProvider();
}
