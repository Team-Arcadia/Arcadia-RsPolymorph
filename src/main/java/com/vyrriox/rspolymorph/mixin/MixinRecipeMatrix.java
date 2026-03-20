package com.vyrriox.rspolymorph.mixin;

import com.vyrriox.rspolymorph.IRsRecipeMatrix;
import com.vyrriox.rspolymorph.RsPolymorph;
import com.refinedmods.refinedstorage.common.support.RecipeMatrix;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(value = RecipeMatrix.class, remap = false)
public abstract class MixinRecipeMatrix<T extends Recipe<I>, I extends RecipeInput> implements IRsRecipeMatrix<T, I> {

    @Shadow @Final private RecipeType<T> recipeType;
    @Shadow @Final private RecipeMatrixContainer matrix;
    @Shadow @Final private Function<RecipeMatrixContainer, I> inputProvider;

    @Override
    public RecipeType<T> rspolymorph$getRecipeType() {
        return recipeType;
    }

    @Override
    public Function<RecipeMatrixContainer, I> rspolymorph$getInputProvider() {
        return inputProvider;
    }

    @Inject(method = "loadRecipe", at = @At("HEAD"), cancellable = true)
    private void RSPOLYMORPH_loadRecipe(Level level, CallbackInfoReturnable<RecipeHolder<T>> cir) {
        RecipeHolder<T> recipe = RsPolymorph.getRecipe((RecipeMatrix<T, I>) (Object) this, level);
        if (recipe != null) {
            cir.setReturnValue(recipe);
        }
    }
}
