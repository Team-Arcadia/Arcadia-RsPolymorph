package com.vyrriox.rspolymorph.mixin;

import com.refinedmods.refinedstorage.common.grid.AbstractCraftingGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.CraftingGrid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for AbstractCraftingGridContainerMenu to expose the craftingGrid field.
 *
 * The wired CraftingGridBlockEntity and the Quartz Arsenal WirelessCraftingGrid both
 * implement RS2's {@link CraftingGrid} interface, but only the wired one is a BlockEntity.
 * The wireless grid is a transient, player-bound object with no BlockEntity, so the
 * BlockEntity-keyed selection path cannot reach it. This accessor lets the server-side
 * packet handler resolve the open grid's {@link CraftingGrid} directly from the menu and
 * drive its RecipeMatrix without needing a BlockEntity.
 *
 * Targets the RS2 base class (not any Quartz Arsenal class), so it applies to every
 * crafting grid menu and keeps Quartz Arsenal an optional, soft dependency.
 *
 * Author: vyrriox
 */
@Mixin(value = AbstractCraftingGridContainerMenu.class, remap = false)
public interface AccessorAbstractCraftingGridContainerMenu {
    @Accessor(value = "craftingGrid", remap = false)
    CraftingGrid rspolymorph$getCraftingGrid();
}
