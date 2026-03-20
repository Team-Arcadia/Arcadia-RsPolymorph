package com.vyrriox.rspolymorph.mixin;

import com.refinedmods.refinedstorage.common.support.RecipeMatrix;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RecipeMatrix.class, remap = false)
public interface AccessorRecipeMatrix<T extends Recipe<I>, I extends RecipeInput> {
    @Accessor("currentRecipe")
    void rspolymorph$setCurrentRecipe(RecipeHolder<T> recipe);
    
    @Accessor("currentRecipe")
    RecipeHolder<T> rspolymorph$getCurrentRecipe();
}
