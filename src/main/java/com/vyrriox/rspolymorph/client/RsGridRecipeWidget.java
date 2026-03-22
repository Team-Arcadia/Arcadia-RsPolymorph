package com.vyrriox.rspolymorph.client;

import com.illusivesoulworks.polymorph.api.client.base.PersistentRecipesWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Custom widget for RS2 Grids to target Block Entity data.
 * Author: vyrriox
 */
public class RsGridRecipeWidget extends PersistentRecipesWidget {
    public static final Map<AbstractContainerScreen<?>, RsGridRecipeWidget> ACTIVE_WIDGETS = new WeakHashMap<>();
    private final Slot outputSlot;

    public RsGridRecipeWidget(AbstractContainerScreen<?> screen, Slot outputSlot) {
        super(screen);
        this.outputSlot = outputSlot;
        ACTIVE_WIDGETS.put(screen, this);
    }

    @Override
    public Slot getOutputSlot() {
        return outputSlot;
    }


    @Override
    public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Ensure it stays invisible during render
        if (this.openButton != null) {
            this.openButton.visible = false;
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /**
     * vyrriox: Exposes the internal selection button's active state.
     */
    public boolean isSelectionActive() {
        return this.openButton != null && this.openButton.active;
    }

    /**
     * vyrriox: Exposes the internal selection button's click action to trigger the UI.
     */
    public void triggerSelection() {
        if (this.openButton != null && this.openButton.active) {
            this.openButton.onPress();
        } else if (this.openButton != null) {
            // Force onPress in case the button was disabled but the side button was clicked
            this.openButton.onPress();
        }
    }
}
