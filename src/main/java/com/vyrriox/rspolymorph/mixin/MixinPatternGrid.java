package com.vyrriox.rspolymorph.mixin;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.vyrriox.rspolymorph.RsGridRecipeData;
import com.vyrriox.rspolymorph.RsPolymorph;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridBlockEntity;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
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
     *
     * Reads the selection from two sources (in priority order):
     *  1. Static selectedRecipeId — fast path for singleplayer (shared JVM).
     *  2. RsGridRecipeData.selections — persisted selection from SelectRecipePacket,
     *     works on dedicated servers where the static field may already be cleared
     *     by the time RS2's createCraftingPattern packet arrives.
     */
    @Inject(method = "createCraftingPattern()Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), remap = false)
    private void RSPOLYMORPH_tagWithSelectedRecipe(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        if (result == null || result.isEmpty()) return;

        // 1) Static fast path (singleplayer)
        ResourceLocation selectedId = RsPolymorph.getSelectedRecipeId();

        // 2) Persisted selection from RsGridRecipeData (dedicated server)
        if (selectedId == null) {
            PatternGridBlockEntity be = (PatternGridBlockEntity) (Object) this;
            var data = PolymorphApi.getInstance().getBlockEntityRecipeData(be);
            if (data instanceof RsGridRecipeData rsData) {
                RecipeHolder<?> selected = rsData.getSelectedRecipe(RecipeType.CRAFTING);
                if (selected != null) {
                    selectedId = selected.id();
                }
            }
        }

        if (selectedId != null) {
            result.set(RsPolymorph.SELECTED_RECIPE_COMPONENT.get(), selectedId);
        }
    }
}
