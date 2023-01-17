package com.agarrity.dynamic_economy.common.world.inventory;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.common.economy.resources.WorldResourceTracker;
import com.agarrity.dynamic_economy.common.network.ClientboundItemValueMessage;
import com.agarrity.dynamic_economy.common.network.DynamicEconomyPacketHandler;
import com.agarrity.dynamic_economy.common.world.entity.npc.ClientSideTrader;
import com.agarrity.dynamic_economy.common.world.entity.npc.ITrader;
import com.agarrity.dynamic_economy.init.MenuInit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.network.NetworkDirection;
import org.jetbrains.annotations.NotNull;

public class AssessMenu extends AbstractContainerMenu {

    private final Player player;
    private final ITrader traderVillager;
    private final IItemHandler traderInventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            if (traderVillager.isClientSide()) {
                return;
            }
            final var itemStack = this.getStackInSlot(slot);
            if (itemStack == ItemStack.EMPTY) {
                return;
            }

            final var itemValue = WorldResourceTracker.estimateItemValue(itemStack);
            final var itemRarity = WorldResourceTracker.getItemFrequency(itemStack);


            final var value = itemValue.orElse(null);
            final var rarity = itemRarity.orElse(-1);
            DynamicEconomy.LOGGER.debug("Sending the value of {} as {}, with a rarity of {}", itemStack.getItem().getRegistryName().toString(), itemValue, itemRarity);

            DynamicEconomyPacketHandler.INSTANCE.sendTo(
                    new ClientboundItemValueMessage(itemStack.getItem(), value, rarity),
                    ((ServerPlayer) player).connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT);
        }
    };

    public AssessMenu(final int containerId, final Inventory playerInventory) {
        this(containerId, playerInventory, new ClientSideTrader(playerInventory.player));
    }

    public AssessMenu(final int containerId, final Inventory playerInventory, final ITrader trader) {
        super(MenuInit.ASSESS_MENU.get(), containerId);
        this.player = playerInventory.player;
        this.traderVillager = trader;

        addSlot(new SlotItemHandler(traderInventory, 0, 142, 18));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }


    @Override
    public boolean stillValid(@NotNull final Player pPlayer) {
        return true;
    }

    @Override
    public void removed(@NotNull final Player pPlayer) {
        super.removed(pPlayer);
        this.traderVillager.setTradingPlayer(null);
        this.player.drop(this.getItems().get(0), false);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull final Player pPlayer, final int pIndex) {
        var itemstack = ItemStack.EMPTY;
        final var slot = this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            final var itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (pIndex < this.traderInventory.getSlots()) {
                if (!this.moveItemStackTo(itemstack1, this.traderInventory.getSlots(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, this.traderInventory.getSlots(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void slotsChanged(Container pContainer) {
        super.slotsChanged(pContainer);
    }

    @Override
    public void clicked(final int pSlotId, final int pButton, final @NotNull ClickType pClickType, final @NotNull Player pPlayer) {
        super.clicked(pSlotId, pButton, pClickType, pPlayer);
    }
}