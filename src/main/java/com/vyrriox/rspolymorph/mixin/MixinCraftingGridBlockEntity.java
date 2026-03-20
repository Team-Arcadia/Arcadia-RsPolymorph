package com.vyrriox.rspolymorph.mixin;

import com.vyrriox.rspolymorph.RsPolymorph;
import com.refinedmods.refinedstorage.common.grid.CraftingGridBlockEntity;
import com.refinedmods.refinedstorage.common.support.RecipeMatrix;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftingGridBlockEntity.class, remap = false)
public abstract class MixinCraftingGridBlockEntity {
    @Shadow @Final private RecipeMatrix<?, ?> craftingRecipe;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void RSPOLYMORPH_init(BlockPos pos, BlockState state, CallbackInfo ci) {
        RsPolymorph.registerMatrix(craftingRecipe, (BlockEntity) (Object) this);
    }
}
