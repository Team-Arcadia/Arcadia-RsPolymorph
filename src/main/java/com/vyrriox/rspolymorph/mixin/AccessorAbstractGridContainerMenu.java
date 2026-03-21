package com.vyrriox.rspolymorph.mixin;

import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for AbstractGridContainerMenu to expose grid field.
 * Author: vyrriox
 */
@Mixin(value = AbstractGridContainerMenu.class, remap = false)
public interface AccessorAbstractGridContainerMenu {
    @Accessor(value = "grid", remap = false)
    Grid rspolymorph$getGrid();

    @Accessor(value = "playerInventory", remap = false)
    net.minecraft.world.entity.player.Inventory rspolymorph$getPlayerInventory();
}
