package com.vyrriox.rspolymorph.mixin;

import com.vyrriox.rspolymorph.client.PolymorphSideButton;
import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to inject Polymorph side button into RS2 screens.
 * Author: vyrriox
 */
@Mixin(value = AbstractBaseScreen.class, remap = false)
public abstract class MixinAbstractBaseScreen {

    @Inject(method = "init", at = @At("RETURN"), remap = false)
    private void RSPOLYMORPH_init(CallbackInfo ci) {
        AbstractBaseScreen<?> screen = (AbstractBaseScreen<?>) (Object) this;
        
        // Find result slot for Polymorph logic
        Slot resultSlot = PolymorphWidgets.getInstance().findResultSlot(screen);
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

        // Add side button if a result slot exists
        if (resultSlot != null) {
            screen.addSideButton(new PolymorphSideButton(screen, resultSlot));
        }
    }
}
