package com.vyrriox.rspolymorph.mixin;

import com.illusivesoulworks.polymorph.api.client.widgets.children.SelectionWidget;
import com.refinedmods.refinedstorage.common.grid.screen.AbstractGridScreen;
import com.vyrriox.rspolymorph.client.RsGridRecipeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancels SelectionWidget rendering when the RS2 popup is not intentionally open.
 *
 * AbstractRecipesWidget.render() re-activates the SelectionWidget every frame based
 * on the openButton's toggle state, making client-side setActive(false) calls
 * ineffective. This mixin intercepts at the deepest level to guarantee stale recipes
 * never appear after the grid input changes.
 *
 * IMPORTANT: Only intercepts on RS2 grid screens. Without this guard, the static
 * activeInstance left from a previously open RS2 grid would cancel Polymorph's
 * rendering on vanilla crafting tables and other non-RS2 screens.
 *
 * Author: vyrriox
 */
@Mixin(value = SelectionWidget.class, remap = false)
public class MixinSelectionWidget {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private void RSPOLYMORPH_cancelStaleRender(
            GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        // Only manage the SelectionWidget for RS2 grid screens.
        // On vanilla crafting tables or other screens, let Polymorph handle rendering normally.
        if (!(Minecraft.getInstance().screen instanceof AbstractGridScreen<?>)) return;

        RsGridRecipeWidget widget = RsGridRecipeWidget.getActiveInstance();
        if (widget != null && !widget.isPopupOpen()) {
            ci.cancel();
        }
    }
}
