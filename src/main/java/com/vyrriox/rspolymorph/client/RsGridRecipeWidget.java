package com.vyrriox.rspolymorph.client;

import com.illusivesoulworks.polymorph.api.client.base.PersistentRecipesWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.List;

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


    @Override
    public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // vyrriox: strictly hide the default polymorph buttons so only the side button remains
        if (this.openButton != null) {
            this.openButton.visible = false;
        }
    }


    /**
     * vyrriox: Returns true if there are actually multiple recipes to select from.
     */
    @SuppressWarnings("unchecked")
    public boolean hasMultipleRecipes() {
        // If the internal button is active, definitely show it
        if (this.openButton != null && this.openButton.active) return true;

        // Otherwise, manually check RS2 matrices for collisions
        net.minecraft.client.multiplayer.ClientLevel level = net.minecraft.client.Minecraft.getInstance().level;
        if (level == null) return false;

        for (com.refinedmods.refinedstorage.common.support.RecipeMatrix<?, ?> matrix : com.vyrriox.rspolymorph.RsPolymorph.getContainerToMatrixMap().values()) {
            if (matrix.getMatrix().isEmpty()) continue;
            
            if (matrix instanceof com.vyrriox.rspolymorph.IRsRecipeMatrix<?, ?> rsMatrix) {
                net.minecraft.world.item.crafting.RecipeType<?> type = rsMatrix.rspolymorph$getRecipeType();
                net.minecraft.world.item.crafting.RecipeInput input = (net.minecraft.world.item.crafting.RecipeInput) rsMatrix.rspolymorph$getInputProvider().apply(matrix.getMatrix());
                if (input != null) {
                    List<? extends net.minecraft.world.item.crafting.RecipeHolder<?>> matching = level.getRecipeManager().getRecipesFor((net.minecraft.world.item.crafting.RecipeType<net.minecraft.world.item.crafting.Recipe<net.minecraft.world.item.crafting.RecipeInput>>) type, input, level);
                    if (matching.size() > 1) return true;
                }
            }
        }
        return false;
    }

    /**
     * vyrriox: Exposes the internal selection button's click action to trigger the UI.
     */
    public void triggerSelection() {
        if (this.openButton != null) {
            // Force the button to be active before pressing so Polymorph opens the UI
            this.openButton.active = true;
            this.openButton.onPress();
        }
    }
}
