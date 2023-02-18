package com.agarrity.dynamic_economy.common.world.inventory;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class TraderSlotItemHandler extends SlotItemHandler {
    final int slotIndex;

    public TraderSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.slotIndex = index;
    }

    public int getSlotIndex() {
        return slotIndex;
    }
}
