package com.vyrriox.rspolymorph.client;

import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;
import com.illusivesoulworks.polymorph.api.client.widgets.PlayerRecipesWidget;
import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
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
    private static final ResourceLocation SPRITE = ResourceLocation.fromNamespaceAndPath("polymorph", "textures/gui/recipe_button.png");
    private final AbstractContainerScreen<?> screen;
    private final Slot resultSlot;

    public PolymorphSideButton(AbstractContainerScreen<?> screen, Slot resultSlot) {
        super(button -> {
            // Trigger the selection UI directly on our managed widget
            RsGridRecipeWidget rsWidget = RsGridRecipeWidget.ACTIVE_WIDGETS.get(screen);
            if (rsWidget != null) {
                rsWidget.triggerSelection();
            }
        });
        this.screen = screen;
        this.resultSlot = resultSlot;
    }

    @Override
    protected ResourceLocation getSprite() {
        // Use a standard RS2 side button sprite that is guaranteed to exist
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
        // Only active if there are multiple recipes detected by Polymorph
        RsGridRecipeWidget rsWidget = RsGridRecipeWidget.ACTIVE_WIDGETS.get(this.screen);
        this.active = (rsWidget != null && rsWidget.isSelectionActive());
        this.visible = true; // Optimization: for testing, we keep it visible
        super.renderWidget(graphics, mouseX, mouseY, partialTicks);
    }
}
