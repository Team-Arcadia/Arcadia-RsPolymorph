package com.vyrriox.rspolymorph.mixin;

import com.vyrriox.rspolymorph.client.RsGridRecipeWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MC 26.1.2 fork. The 26.x render pipeline is a two-phase extract/submit flow with no
 * {@code renderTooltip(GuiGraphics,int,int)} entry on the grid screens, and the 26.x popup already
 * draws its recipe name as a fixed label above the box (so it never collides with the cursor
 * slot-tooltip) — the overlap bug is 1.21.1-only. This fork is therefore a fail-soft no-op:
 * {@code require = 0} on a best-effort vanilla target that may not exist in the extract phase, so it
 * neither suppresses anything nor risks the client load on 26.x.
 *
 * Author: vyrriox
 */
@Mixin(AbstractContainerScreen.class)
public abstract class MixinScreenTooltipSuppress {

    @Inject(
            method = "renderTooltip(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void rspolymorph$suppressSlotTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        RsGridRecipeWidget widget = RsGridRecipeWidget.getActiveInstance();
        if (widget != null && widget.isPopupOpen() && widget.isMouseOverPopup(mouseX, mouseY)) {
            ci.cancel();
        }
    }
}
