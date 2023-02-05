package com.agarrity.dynamic_economy.common.world.inventory;

import com.agarrity.dynamic_economy.init.ItemInit;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class CoinBagMenu extends AbstractContainerMenu {


    public CoinBagMenu(final int containerID, final Inventory playerInventory, final IItemHandler bagInventory) {
        super(MenuType.GENERIC_9x3, containerID);

        int i = -18;

        for (int rowIndex = 0; rowIndex < 3; ++rowIndex) {
            for (int columnIndex = 0; columnIndex < 9; ++columnIndex) {
                this.addSlot(new SlotItemHandler(bagInventory, columnIndex + rowIndex * 9, 8 + columnIndex * 18, 18 + rowIndex * 18) {
                                 @Override
                                 public boolean mayPlace(@NotNull ItemStack pStack) {
                                     return pStack.getItem() == ItemInit.FIXED_CURRENCY.get();
                                 }
                             }
                );
            }
        }

        for (int rowIndex = 0; rowIndex < 3; ++rowIndex) {
            for (int columnIndex = 0; columnIndex < 9; ++columnIndex) {
                this.addSlot(new Slot(playerInventory, columnIndex + rowIndex * 9 + 9, 8 + columnIndex * 18, 103 + rowIndex * 18 + i));
            }
        }

        for (int hotbarIndex = 0; hotbarIndex < 9; ++hotbarIndex) {
            this.addSlot(new Slot(playerInventory, hotbarIndex, 8 + hotbarIndex * 18, 161 + i));
        }
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return true;
    }
}
