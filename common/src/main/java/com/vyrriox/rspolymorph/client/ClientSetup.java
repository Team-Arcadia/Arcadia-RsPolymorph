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
            // Uses instanceof checks to correctly match anonymous inner classes that extend
            // DisabledSlot/FilterSlot (e.g. PatternGridContainerMenu$5 extends DisabledSlot).
            if (resultSlot == null) {
                for (Slot slot : gridScreen.getMenu().slots) {
                    if (slot.isActive()) {
                        if (slot instanceof com.refinedmods.refinedstorage.common.support.containermenu.DisabledSlot
                                || slot instanceof net.minecraft.world.inventory.ResultSlot) {
                            resultSlot = slot;
                            break;
                        }
                    }
                }
            }
            // Last resort: find any DisabledSlot even if inactive (e.g. crafting tab not yet synced).
            if (resultSlot == null) {
                for (Slot slot : gridScreen.getMenu().slots) {
                    if (slot instanceof com.refinedmods.refinedstorage.common.support.containermenu.DisabledSlot) {
                        resultSlot = slot;
                        break;
                    }
                }
            }

            return resultSlot != null ? new RsGridRecipeWidget(gridScreen, resultSlot) : null;
        });
    }
}
