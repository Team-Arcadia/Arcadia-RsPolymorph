package com.vyrriox.rspolymorph.client;

import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;

/**
 * Native RS2 side button to trigger Polymorph recipe selection.
 * Uses RS2's sprite system for proper theming.
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
        return ResourceLocation.fromNamespaceAndPath("rspolymorph", "widget/side_button/polymorph");
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
        this.active = (rsWidget != null && rsWidget.hasMultipleRecipes());
        this.visible = true;
        super.renderWidget(graphics, mouseX, mouseY, partialTicks);
    }
}
