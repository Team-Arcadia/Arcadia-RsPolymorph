package com.vyrriox.rspolymorph.client;

import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;

/**
 * Native RS2 side button to trigger Polymorph selection.
 * Author: vyrriox
 */
public class PolymorphSideButton extends AbstractSideButtonWidget {

    public PolymorphSideButton() {
        super(button -> {
            RsGridRecipeWidget widget = RsGridRecipeWidget.getActiveInstance();
            if (widget != null) {
                widget.triggerSelection();
            }
        });
    }

    @Override
    protected ResourceLocation getSprite() {
        // Use a guaranteed vanilla sprite to avoid the pink square issue
        return ResourceLocation.withDefaultNamespace("recipe_book/button_all");
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
        RsGridRecipeWidget rsWidget = RsGridRecipeWidget.getActiveInstance();
        // The button is only 'active' (clickable) if there are multiple recipes detected
        this.active = (rsWidget != null && rsWidget.hasMultipleRecipes());
        this.visible = true;
        
        // Draw the standard RS2 side button background
        super.renderWidget(graphics, mouseX, mouseY, partialTicks);
        
        // Draw the Polymorph icon on top
        if (this.visible) {
            ResourceLocation icon = ResourceLocation.fromNamespaceAndPath("polymorph", "textures/gui/recipe_button.png");
            graphics.blit(icon, this.getX() + 1, this.getY() + 1, 0, 0, 16, 16, 16, 16);
        }
    }
}
