package com.vyrriox.rspolymorph.mixin;

import com.vyrriox.rspolymorph.client.RsGridRecipeWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses the vanilla hovered-slot tooltip while the recipe-selection popup is open AND the
 * cursor is over it. The popup draws its own recipe-name tooltip; without this the screen would
 * also draw the underlying slot's tooltip at the same cursor, producing the overlapping double
 * text seen in-game. Vanilla target → remapped. {@code require = 0}: best-effort, never fails the
 * load if the obfuscated signature differs on a given loader.
 *
 * Author: vyrriox
 */
@Mixin(AbstractContainerScreen.class)
public abstract class MixinScreenTooltipSuppress {

    @Inject(
            method = "renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void rspolymorph$suppressSlotTooltip(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        RsGridRecipeWidget widget = RsGridRecipeWidget.getActiveInstance();
        if (widget != null && widget.isPopupOpen() && widget.isMouseOverPopup(mouseX, mouseY)) {
            ci.cancel();
        }
    }
}
