package com.vyrriox.rspolymorph.mixin;

import com.vyrriox.rspolymorph.client.AccessorScreenOffset;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Exposes {@code AbstractContainerScreen}'s protected {@code leftPos}/{@code topPos} via the
 * {@link AccessorScreenOffset} duck-type interface, so the recipe popup can be anchored in screen
 * coordinates. Vanilla target → remapped (default {@code remap=true}).
 *
 * Author: vyrriox
 */
@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreenOffset implements AccessorScreenOffset {

    @Shadow protected int leftPos;
    @Shadow protected int topPos;

    @Override
    public int rspolymorph$leftPos() {
        return leftPos;
    }

    @Override
    public int rspolymorph$topPos() {
        return topPos;
    }
}
