package com.vyrriox.rspolymorph.mixin;

import com.vyrriox.rspolymorph.RsPolymorph;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridBlockEntity;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PatternGridBlockEntity.class, remap = false)
public abstract class MixinPatternGrid {
    
    @Shadow abstract RecipeMatrixContainer getCraftingMatrix();
    @Shadow abstract RecipeMatrixContainer getSmithingTableMatrix();

    @Inject(method = "<init>(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At("RETURN"), remap = false)
    private void RSPOLYMORPH_init(BlockPos pos, BlockState state, CallbackInfo ci) {
        PatternGridBlockEntity be = (PatternGridBlockEntity) (Object) this;
        RecipeMatrixContainer c = this.getCraftingMatrix();
        if (c != null) {
            RsPolymorph.registerContainerBlockEntity(c, be);
        }
        RecipeMatrixContainer st = this.getSmithingTableMatrix();
        if (st != null) {
            RsPolymorph.registerContainerBlockEntity(st, be);
        }
    }

    /**
     * Tags the newly-created crafting pattern ItemStack with the Polymorph-selected recipe ID
     * so that PatternResolver can prefer the correct recipe at autocrafting time.
     */
    @Inject(method = "createCraftingPattern()Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), remap = false)
    private void RSPOLYMORPH_tagWithSelectedRecipe(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        if (result == null || result.isEmpty()) return;
        ResourceLocation selectedId = RsPolymorph.getSelectedRecipeId();
        if (selectedId != null) {
            result.set(RsPolymorph.SELECTED_RECIPE_COMPONENT.get(), selectedId);
        }
    }
}
