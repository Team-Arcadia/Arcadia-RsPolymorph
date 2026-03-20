package com.vyrriox.rspolymorph;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import com.illusivesoulworks.polymorph.api.client.widgets.PlayerRecipesWidget;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridContainerMenu;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridScreen;
import com.refinedmods.refinedstorage.common.grid.CraftingGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.screen.CraftingGridScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(RsPolymorph.MODID)
public class RsPolymorph {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "rspolymorph";

    public RsPolymorph(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        
        // Register client setup if we are on the physical client
        if (net.neoforged.fml.loading.FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::clientSetup);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Use player recipe data for grids so that selection is stored for the player
        PolymorphApi.getInstance().registerMenu(menu -> {
            if (menu instanceof CraftingGridContainerMenu || menu instanceof PatternGridContainerMenu) {
                // Return null to fall back to PlayerRecipeData, 
                // but registering the menu classes helps Polymorph identify them.
                return null;
            }
            return null;
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // Register the widget for RS2 Screens
        PolymorphWidgets.getInstance().registerWidget((AbstractContainerScreen<?> screen) -> {
            if (screen instanceof CraftingGridScreen || screen instanceof PatternGridScreen) {
                // Find the crafting result slot in the RS2 container.
                // Both screens use menus that have slots. We look for a ResultSlot or similar.
                for (Slot slot : screen.getMenu().slots) {
                    if (slot instanceof net.minecraft.world.inventory.ResultSlot || 
                        slot.getClass().getName().contains("ResultSlot") ||
                        slot.getClass().getName().contains("PatternGridResultSlot")) {
                        return new PlayerRecipesWidget(screen, slot);
                    }
                }
            }
            return null;
        });
    }
}
