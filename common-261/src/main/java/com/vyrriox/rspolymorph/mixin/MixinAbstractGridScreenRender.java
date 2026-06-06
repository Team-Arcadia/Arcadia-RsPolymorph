package com.vyrriox.rspolymorph.mixin;

import com.refinedmods.refinedstorage.common.grid.screen.AbstractGridScreen;
import com.vyrriox.rspolymorph.client.RsGridRecipeWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Draws the standalone recipe-selection popup over the RS grid screen and routes clicks to it.
 *
 * IMPORTANT (MC 26.x): mixin {@code @Inject} only resolves methods DECLARED in the target class,
 * not inherited ones. {@code AbstractGridScreen} overrides {@code extractContents} (the 26.x content
 * render phase) and {@code mouseClicked(MouseButtonEvent, boolean)}, so we target those — NOT the
 * vanilla {@code extractRenderState} / old {@code mouseClicked(DDI)Z}, which are not declared here
 * and would fail to apply (critical injection error at class load).
 *
 * Author: vyrriox
 */
@Mixin(value = AbstractGridScreen.class, remap = false)
public abstract class MixinAbstractGridScreenRender {

    @Inject(
            method = "extractContents(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
            at = @At("RETURN"),
            remap = true
    )
    private void rspolymorph$renderPopup(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        RsGridRecipeWidget widget = RsGridRecipeWidget.getActiveInstance();
        if (widget != null && widget.isOpenForScreen((AbstractContainerScreen<?>) (Object) this)) {
            widget.renderPopup(graphics, mouseX, mouseY);
        }
    }

    @Inject(
            method = "mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = true
    )
    private void rspolymorph$popupClick(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        RsGridRecipeWidget widget = RsGridRecipeWidget.getActiveInstance();
        if (widget == null || !widget.isPopupOpen()) return;
        if (widget.handleClick(event.x(), event.y())) {
            cir.setReturnValue(true); // consumed — don't let the click reach slots
        }
    }
}
