package com.vyrriox.rspolymorph.mixin;

import com.vyrriox.rspolymorph.client.PolymorphSideButton;
import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.common.grid.screen.AbstractGridScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to inject Polymorph side button into RS2 crafting grid screens.
 * Author: vyrriox
 */
@Mixin(value = AbstractBaseScreen.class, remap = false)
public abstract class MixinAbstractBaseScreen {

    @Inject(method = "init", at = @At("RETURN"), remap = false)
    private void RSPOLYMORPH_init(CallbackInfo ci) {
        AbstractBaseScreen<?> screen = (AbstractBaseScreen<?>) (Object) this;

        // Only add the button on crafting-capable grid screens
        if (!(screen instanceof AbstractGridScreen<?>)) {
            return;
        }

        screen.addSideButton(new PolymorphSideButton());
    }
}
