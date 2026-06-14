package com.vyrriox.rspolymorph.mixin;

import com.vyrriox.rspolymorph.client.RsGridRecipeWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MC 26.1.2 fork. Draws the recipe-selection popup over the RS grid screen.
 *
 * Z-ORDER FIX: the 26.x render pipeline extracts in four sequential phases driven by vanilla
 * {@code AbstractContainerScreen.extractRenderState}:
 *   extractContents -> extractCarriedItem -> extractSnapbackItem -> extractTooltip.
 * Each later phase advances the render stack via {@code GuiGraphicsExtractor.nextStratum()} and
 * composites ABOVE earlier strata. The popup used to be drawn at the RETURN of
 * {@code AbstractGridScreen.extractContents} (the FIRST phase), so the carried item and the
 * hovered-slot layer extracted afterwards painted on top of it and the recipe icons appeared buried
 * under the grid — making them unreadable/unclickable.
 *
 * Fix: inject at the RETURN of {@code extractRenderState}, the one method that runs strictly after
 * all four sub-phases. It is declared only on vanilla {@code AbstractContainerScreen} (no RS grid
 * screen overrides it), so this single mixin covers every grid screen via inheritance. The popup's
 * own {@code nextStratum()} in {@link com.vyrriox.rspolymorph.client.RecipeSelectorPopup#render}
 * then places it on a fresh top stratum above the carried-item stratum.
 *
 * Author: vyrriox
 */
@Mixin(AbstractContainerScreen.class)
public abstract class MixinScreenPopupRender {

    @Inject(
            method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
            at = @At("RETURN")
    )
    private void rspolymorph$renderPopup(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        RsGridRecipeWidget widget = RsGridRecipeWidget.getActiveInstance();
        if (widget != null && widget.isOpenForScreen((AbstractContainerScreen<?>) (Object) this)) {
            widget.renderPopup(graphics, mouseX, mouseY);
        }
    }
}
