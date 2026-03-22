package com.vyrriox.rspolymorph.client;

import com.illusivesoulworks.polymorph.api.client.base.PersistentRecipesWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;


/**
 * Custom widget for RS2 Grids to target Block Entity data.
 * Author: vyrriox
 */
public class RsGridRecipeWidget extends PersistentRecipesWidget {
    private static RsGridRecipeWidget activeInstance = null;
    private final Slot outputSlot;

    public RsGridRecipeWidget(AbstractContainerScreen<?> screen, Slot outputSlot) {
        super(screen);
        this.outputSlot = outputSlot;
        activeInstance = this;
    }
    
    public static RsGridRecipeWidget getActiveInstance() {
        return activeInstance;
    }

    @Override
    public Slot getOutputSlot() {
        return outputSlot;
    }


    @Override
    public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Let Polymorph do its internal updates (recipe detection, openButton state)
        super.render(graphics, mouseX, mouseY, partialTick);
        // Hide the default Polymorph open button — we use the RS2 native side button instead
        if (this.openButton != null) {
            this.openButton.visible = false;
        }
    }

    /**
     * Returns true if Polymorph detected multiple matching recipes for the current grid input.
     */
    public boolean hasMultipleRecipes() {
        return this.openButton != null && this.openButton.active;
    }

    /**
     * Triggers Polymorph's recipe selection popup via the RS2 side button.
     */
    public void triggerSelection() {
        if (this.openButton == null) return;
        this.openButton.active = true;
        this.openButton.visible = true;
        this.openButton.onPress();
        this.openButton.visible = false;
    }
}
