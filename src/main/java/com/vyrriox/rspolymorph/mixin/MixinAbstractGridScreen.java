package com.vyrriox.rspolymorph.mixin;

import com.vyrriox.rspolymorph.RsPolymorph;
import com.vyrriox.rspolymorph.client.RsGridRecipeWidget;
import com.refinedmods.refinedstorage.common.grid.screen.AbstractGridScreen;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to manually inject the Polymorph button into RS2 Grid screens.
 * Author: vyrriox
 */
@Mixin(value = AbstractGridScreen.class, remap = false)
public abstract class MixinAbstractGridScreen {

    @Inject(method = "init", at = @At("RETURN"), remap = false)
    private void RSPOLYMORPH_init(CallbackInfo ci) {
        AbstractGridScreen<?> screen = (AbstractGridScreen<?>) (Object) this;
        
        // Use Polymorph API to find the result slot
        Slot resultSlot = PolymorphWidgets.getInstance().findResultSlot(screen);
        
        // If not found by API, try manual fallback for RS2 specific slots
        if (resultSlot == null) {
            for (Slot slot : screen.getMenu().slots) {
                if (!slot.isActive()) continue;
                String className = slot.getClass().getName();
                if (className.contains("ResultSlot") || 
                    className.contains("DisabledSlot") || 
                    className.contains("ResourceSlot")) {
                    resultSlot = slot;
                    break;
                }
            }
        }

        // If we found a potential slot, force the widget creation
        if (resultSlot != null) {
            // Polymorph handles the actual button rendering and logic via the widget
            // We just need to make sure the widget is registered/active for this screen
            new RsGridRecipeWidget(screen, resultSlot);
        }
    }
}
