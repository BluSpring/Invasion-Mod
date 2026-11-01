package com.invasion.block.container;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;

public class OutputSlot extends Slot {
    public OutputSlot(Container iinventory, int i, int j, int k) {
        super(iinventory, i, j, k);
    }

    public boolean isItemValid(ItemStack itemstack) {
        return false;
    }
}