package com.vyrriox.rspolymorph.mixin;

import com.vyrriox.rspolymorph.RsPolymorph;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridBlockEntity;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
}
