package com.vyrriox.rspolymorph.client;

import com.illusivesoulworks.polymorph.api.client.base.PersistentRecipesWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

/**
 * Custom widget for RS2 Grids to target Block Entity data.
 * Author: vyrriox
 */
public class RsGridRecipeWidget extends PersistentRecipesWidget {
    private final Slot outputSlot;

    public RsGridRecipeWidget(AbstractContainerScreen<?> screen, Slot outputSlot) {
        super(screen);
        this.outputSlot = outputSlot;
    }

    @Override
    public Slot getOutputSlot() {
        return outputSlot;
    }
}
