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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Mixin for RS2 RecipeMatrix to integrate Polymorph.
 * Author: vyrriox
 */
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

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void RSPOLYMORPH_init(Runnable r, Supplier<Level> levelSupplier, int w, int h, Function<RecipeMatrixContainer, I> provider, RecipeType<T> type, CallbackInfo ci) {
        RsPolymorph.registerMatrixToContainer(this.matrix, (RecipeMatrix<?, ?>) (Object) this);
    }

    @Inject(method = "updateResult", at = @At("HEAD"), remap = false)
    private void RSPOLYMORPH_updateResultHead(Level level, CallbackInfo ci) {
        // vyrriox: Handle the Polymorph recipe swap at the very beginning of RS2 update
        RecipeHolder<T> polymorphRecipe = (RecipeHolder<T>) RsPolymorph.getRecipe((RecipeMatrix<?, ?>) (Object) this, level);
        if (polymorphRecipe != null) {
            ((AccessorRecipeMatrix<T, I>) this).rspolymorph$setCurrentRecipe(polymorphRecipe);
        }
    }
}
