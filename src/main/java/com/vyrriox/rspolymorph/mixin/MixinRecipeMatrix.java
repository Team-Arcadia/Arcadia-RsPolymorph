package com.vyrriox.rspolymorph.mixin;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import com.vyrriox.rspolymorph.IRsRecipeMatrix;
import com.vyrriox.rspolymorph.RsPolymorph;
import com.refinedmods.refinedstorage.common.support.RecipeMatrix;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Mixin for RS2 RecipeMatrix — integrates Polymorph recipe selection.
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
    private void RSPOLYMORPH_init(Runnable r, Supplier<Level> levelSupplier, int w, int h,
                                  Function<RecipeMatrixContainer, I> provider, RecipeType<T> type,
                                  CallbackInfo ci) {
        RsPolymorph.registerMatrixToContainer(this.matrix, (RecipeMatrix<?, ?>) (Object) this);
    }

    /**
     * After RS2 has resolved its default recipe, override with Polymorph's selection if one is
     * active for this matrix.
     *
     * Strategy (in priority order):
     *  1. Static selectedRecipeId (client→server singleplayer fast path).
     *  2. Polymorph BlockEntity recipe data (persisted selection via Polymorph's capability).
     *
     * Early-exit: if the current recipe already matches the selection, skip the search entirely.
     * This is the common case after the initial selection is applied.
     */
    @SuppressWarnings("unchecked")
    @Inject(method = "updateResult", at = @At("RETURN"), remap = false)
    private void RSPOLYMORPH_afterUpdateResult(Level level, CallbackInfo ci) {

        // ── 1) Static selection (singleplayer fast path) ──────────────────────
        ResourceLocation selectedId = RsPolymorph.getSelectedRecipeId();
        if (selectedId != null) {
            // Skip the search if RS2 already resolved the right recipe.
            RecipeHolder<T> current = ((AccessorRecipeMatrix<T, I>) this).rspolymorph$getCurrentRecipe();
            if (current != null && current.id().equals(selectedId)) {
                syncRecipesIfServer(level);
                return;
            }

            I input = this.inputProvider.apply(this.matrix);
            if (input != null) {
                List<RecipeHolder<T>> matches = level.getRecipeManager().getRecipesFor(this.recipeType, input, level);
                for (RecipeHolder<T> holder : matches) {
                    if (holder.id().equals(selectedId)) {
                        ItemStack output = holder.value().assemble(input, level.registryAccess());
                        ((AccessorRecipeMatrix<T, I>) this).rspolymorph$invokeSetResult(holder, output);
                        syncRecipesIfServer(level);
                        return;
                    }
                }
            }
            // selectedId didn't match this matrix's inputs — fall through to Polymorph fallback.
        }

        // ── 2) Polymorph BlockEntity data (persisted selection) ───────────────
        RecipeHolder<T> polymorphRecipe = (RecipeHolder<T>) RsPolymorph.getRecipe((RecipeMatrix<?, ?>) (Object) this, level);
        if (polymorphRecipe != null) {
            // Early-exit if the result is already correct.
            RecipeHolder<T> current = ((AccessorRecipeMatrix<T, I>) this).rspolymorph$getCurrentRecipe();
            if (current == null || !current.id().equals(polymorphRecipe.id())) {
                I input = this.inputProvider.apply(this.matrix);
                if (input != null) {
                    ItemStack output = polymorphRecipe.value().assemble(input, level.registryAccess());
                    ((AccessorRecipeMatrix<T, I>) this).rspolymorph$invokeSetResult(polymorphRecipe, output);
                }
            }
        }

        syncRecipesIfServer(level);
    }

    /**
     * Pushes the current recipe list to Polymorph's sync mechanism (server side only).
     * Throttling is handled inside RsGridRecipeData.forceUpdateRecipes().
     * Wrapped in try-catch so an unexpected Polymorph issue never crashes the server.
     */
    private void syncRecipesIfServer(Level level) {
        if (level.isClientSide()) return;
        try {
            BlockEntity be = RsPolymorph.getBlockEntityForContainer(this.matrix);
            if (be == null) return;
            IBlockEntityRecipeData data = PolymorphApi.getInstance().getBlockEntityRecipeData(be);
            if (data instanceof com.vyrriox.rspolymorph.RsGridRecipeData rsData) {
                rsData.forceUpdateRecipes(level);
            }
        } catch (Exception e) {
            org.apache.logging.log4j.LogManager.getLogger("RSPolymorph")
                    .warn("Failed to sync recipes to Polymorph", e);
        }
    }
}
