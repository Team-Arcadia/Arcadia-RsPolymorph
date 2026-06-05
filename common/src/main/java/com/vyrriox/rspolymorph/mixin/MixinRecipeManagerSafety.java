package com.vyrriox.rspolymorph.mixin;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Safety net for cross-type recipe leaks from upstream recipe-manager mixins.
 *
 * Polymorph 1.1.0 ships a {@code MixinRecipeManager} that injects on
 * {@code getRecipeFor(RecipeType, RecipeInput, Level, RecipeHolder)} and can resolve a
 * stored recipe from per-block-entity Polymorph data. Under specific cross-block-entity
 * tick orderings (observed with Create 6.0.10's encased fan blasting / smoking processing),
 * the injected return value can be a {@link RecipeHolder} whose recipe is of a different
 * {@link RecipeType} than the one requested. The caller (here Create's
 * {@code AllFanProcessingTypes$BlastingType.process}) holds the result as
 * {@code Optional<RecipeHolder<SmokingRecipe>>} via Java type erasure and crashes with a
 * {@link ClassCastException} at the first use-site cast.
 *
 * This injector runs at every {@code RETURN} of the four-arg overload, after the upstream
 * injection has had a chance to populate the value. It validates that the held recipe's
 * runtime type matches the requested {@link RecipeType}; on mismatch it falls back to a
 * fresh {@link RecipeManager#getRecipesFor} lookup scoped to the requested type. This
 * preserves Polymorph behavior on the matching-type path while neutralizing the
 * cross-type leak.
 *
 * Author: vyrriox
 */
@Mixin(value = RecipeManager.class)
public abstract class MixinRecipeManagerSafety {

    @Inject(
        method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/crafting/RecipeHolder;)Ljava/util/Optional;",
        at = @At("RETURN"),
        cancellable = true
    )
    private <I extends RecipeInput, T extends Recipe<I>> void RSPOLYMORPH_validateRecipeType(
            RecipeType<T> type,
            I input,
            Level level,
            RecipeHolder<T> previous,
            CallbackInfoReturnable<Optional<RecipeHolder<T>>> cir) {
        Optional<RecipeHolder<T>> result = cir.getReturnValue();
        if (result == null || result.isEmpty()) return;

        RecipeHolder<T> holder = result.get();
        if (holder.value().getType() == type) return;

        RecipeManager self = (RecipeManager) (Object) this;
        Optional<RecipeHolder<T>> safe = self.getRecipesFor(type, input, level)
                .stream()
                .findFirst();
        cir.setReturnValue(safe);
    }
}
