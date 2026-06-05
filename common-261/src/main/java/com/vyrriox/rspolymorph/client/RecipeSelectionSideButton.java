package com.vyrriox.rspolymorph.client;

import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.List;

/**
 * Native RS grid side button that opens the recipe-selection popup.
 * Uses RS's sprite system for proper theming. No Polymorph dependency.
 * Author: vyrriox
 */
public class RecipeSelectionSideButton extends AbstractSideButtonWidget {

    public RecipeSelectionSideButton() {
        super(button -> {
            RsGridRecipeWidget widget = RsGridRecipeWidget.getActiveInstance();
            if (widget != null) {
                widget.triggerSelection();
            }
        });
    }

    @Override
    protected Identifier getSprite() {
        return Identifier.fromNamespaceAndPath("rspolymorph", "widget/side_button/polymorph");
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
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        RsGridRecipeWidget rsWidget = RsGridRecipeWidget.getActiveInstance();
        this.active = (rsWidget != null && rsWidget.hasMultipleRecipes());
        this.visible = true;
        super.extractContents(graphics, mouseX, mouseY, partialTicks);
    }
}
