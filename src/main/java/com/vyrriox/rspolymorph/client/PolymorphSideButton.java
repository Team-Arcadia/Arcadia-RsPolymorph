package com.vyrriox.rspolymorph.client;

import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

import java.util.Collections;
import java.util.List;

/**
 * Native RS2 side button to trigger Polymorph selection.
 * Author: vyrriox
 */
public class PolymorphSideButton extends AbstractSideButtonWidget {
    private final AbstractContainerScreen<?> screen;

    public PolymorphSideButton(AbstractContainerScreen<?> screen, Slot resultSlot) {
        super(button -> {
            // Search for Polymorph's widget directly in the screen's components
            for (Object widget : screen.renderables) {
                if (widget instanceof RsGridRecipeWidget rsWidget) {
                    rsWidget.triggerSelection();
                    return;
                }
            }
        });
        this.screen = screen;
    }

    protected ResourceLocation getSprite() {
        // Use a standard RS2 side button sprite that is guaranteed to exist
        // Note: rs2 sprites are registered without the textures/ prefix
        return ResourceLocation.fromNamespaceAndPath("refinedstorage", "side_button/config"); 
    }

    @Override
    protected MutableComponent getTitle() {
        return Component.translatable("rspolymorph.gui.recipe_selection");
    }

    @Override
    protected List<MutableComponent> getSubText() {
        return Collections.singletonList(Component.translatable("rspolymorph.gui.click_to_select"));
    }

    @Override
    public void renderWidget(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Find the widget each frame to know if we should be active
        RsGridRecipeWidget rsWidget = null;
        for (Object widget : screen.renderables) {
            if (widget instanceof RsGridRecipeWidget w) {
                rsWidget = w;
                break;
            }
        }
        
        // The button is only 'active' (clickable) if there are multiple recipes detected
        this.active = (rsWidget != null && rsWidget.hasMultipleRecipes());
        this.visible = true; 
        super.renderWidget(graphics, mouseX, mouseY, partialTicks);
    }
}
