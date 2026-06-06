package com.vyrriox.rspolymorph;

import net.minecraft.world.item.Item;

/**
 * Two debug items, {@code test_stick_1} and {@code test_stick_2}, that share the SAME crafting
 * inputs (two vanilla sticks) but produce different outputs. In a vanilla crafting table only the
 * first match is reachable, but in a Refined Storage crafting grid this is exactly the multi-recipe
 * case RS Polymorph handles: the side button lights up and the popup offers both outputs to pick.
 *
 * Loader-agnostic holder — contains no {@code ResourceLocation}/{@code Identifier}, so it is shared
 * verbatim to the MC 26.1.2 build through the common-261 source remap with no fork needed. Each
 * loader registers the items under {@code rspolymorph:test_stick_1/2} and injects them here.
 *
 * Author: vyrriox
 */
public final class TestItems {

    private TestItems() {}

    private static volatile Item testStick1;
    private static volatile Item testStick2;

    public static void setTestStick1(Item item) { testStick1 = item; }
    public static void setTestStick2(Item item) { testStick2 = item; }

    public static Item testStick1() {
        Item i = testStick1;
        if (i == null) throw new IllegalStateException("test_stick_1 accessed before loader registration");
        return i;
    }

    public static Item testStick2() {
        Item i = testStick2;
        if (i == null) throw new IllegalStateException("test_stick_2 accessed before loader registration");
        return i;
    }
}
