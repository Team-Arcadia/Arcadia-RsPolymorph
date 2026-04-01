package com.vyrriox.rspolymorph.client;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import com.refinedmods.refinedstorage.common.grid.screen.AbstractGridScreen;
import net.minecraft.world.inventory.Slot;

/**
 * Client-only initialisation — loaded exclusively when Dist == CLIENT.
 *
 * Isolating every client-class reference here prevents the JVM verifier from
 * trying to resolve client-only types (AbstractGridScreen, PolymorphWidgets, …)
 * when loading the main @Mod class on a dedicated server.
 *
 * Author: vyrriox
 */
public final class ClientSetup {

    private ClientSetup() {}

    public static void init() {
        PolymorphWidgets.getInstance().registerWidget(screen -> {
            if (!(screen instanceof AbstractGridScreen<?> gridScreen)) return null;

            Slot resultSlot = PolymorphWidgets.getInstance().findResultSlot(gridScreen);

            // Fallback: scan the slot list for a result-like slot if Polymorph didn't find one.
            if (resultSlot == null) {
                for (Slot slot : gridScreen.getMenu().slots) {
                    if (slot.isActive()) {
                        String name = slot.getClass().getName();
                        if (name.contains("ResultSlot")
                                || name.contains("DisabledSlot")
                                || name.contains("ResourceSlot")) {
                            resultSlot = slot;
                            break;
                        }
                    }
                }
            }

            return resultSlot != null ? new RsGridRecipeWidget(gridScreen, resultSlot) : null;
        });
    }
}
