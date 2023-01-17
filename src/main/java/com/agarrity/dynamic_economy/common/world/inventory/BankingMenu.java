package com.agarrity.dynamic_economy.common.world.inventory;

import com.agarrity.dynamic_economy.common.economy.bank.Bank;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyAmount;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyHelper;
import com.agarrity.dynamic_economy.common.network.ClientboundBalanceMessage;
import com.agarrity.dynamic_economy.common.network.DynamicEconomyPacketHandler;
import com.agarrity.dynamic_economy.common.world.entity.npc.ClientSideTrader;
import com.agarrity.dynamic_economy.common.world.entity.npc.ITrader;
import com.agarrity.dynamic_economy.init.ItemInit;
import com.agarrity.dynamic_economy.init.MenuInit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.network.NetworkDirection;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BankingMenu extends AbstractContainerMenu {

    private final Player player;
    private final UUID playerUUID;

    private final ITrader bankerVillager;
    private final IItemHandler container = new ItemStackHandler(5);


    public BankingMenu(final int containerId, final Inventory playerInventory) {
        this(containerId, playerInventory, new ClientSideTrader(playerInventory.player));
    }

    public BankingMenu(final int containerId, final Inventory playerInventory, final ITrader banker) {
        super(MenuInit.BANKING_MENU.get(), containerId);

        this.player = playerInventory.player;
        this.playerUUID = player.getUUID();
        this.bankerVillager = banker;

        for (var i = 0; i < container.getSlots(); ++i) {
            addSlot(new SlotItemHandler(container, i, 69 + (i * 21), 29) {
                @Override
                public boolean mayPlace(@NotNull ItemStack pStack) {
                    if (!pStack.is(ItemInit.CURRENCY_TAG)) {
                        return false;
                    }
                    var tag = pStack.getTag();
                    if (tag == null) {
                        return false;
                    }
                    var value = tag.getLong("value");
                    return value > 0;
                }
            });
        }

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
    public @NotNull ItemStack quickMoveStack(@NotNull final Player pPlayer, final int pIndex) {
        var itemStack = ItemStack.EMPTY;
        final var slot = this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            final var itemStack1 = slot.getItem();
            itemStack = itemStack1.copy();
            if (pIndex < this.container.getSlots()) {
                if (!this.moveItemStackTo(itemStack1, this.container.getSlots(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack1, 0, this.container.getSlots(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }

    /**
     * Play a sound indicating that a transaction has occurred
     */
    public void playTransactionSound() {
        if (!this.bankerVillager.isClientSide()) {
            final var entity = (Entity) this.bankerVillager;
            entity.getLevel().playLocalSound(entity.getX(), entity.getY(), entity.getZ(), this.bankerVillager.getNotifyTradeSound(), SoundSource.NEUTRAL, 1.0F, 1.0F, false);
        }
    }

    @Override
    public void removed(@NotNull final Player pPlayer) {
        super.removed(pPlayer);
        this.bankerVillager.setTradingPlayer(null);
        for (var i = 0; i < container.getSlots(); ++i) {
            this.player.drop(this.container.getStackInSlot(i), false);
        }
    }

    /**
     * Remove an amount of currency from the account and convert it into physical currency
     * @param amount The amount to withdraw
     */
    public void doWithdrawal(final CurrencyAmount amount) {
        for (var i = 0; i < container.getSlots(); ++i) {
            if (container.getStackInSlot(i) != ItemStack.EMPTY) {
                return;
            }
        }

        if (!Bank.removeCurrencyFromAccount(playerUUID, amount)) {
            return;
        }

        final var currencyRequired = CurrencyHelper.calculateCurrencyRequired(amount);
        var slotIndex = 0;
        for (var currency : currencyRequired) {
            container.insertItem(slotIndex++, currency, false);
        }

        final var balance = Bank.getAccountBalance(playerUUID);
        balance.ifPresent(currencyAmount -> DynamicEconomyPacketHandler.INSTANCE.sendTo(new ClientboundBalanceMessage(currencyAmount), ((ServerPlayer) player).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT));
        playTransactionSound();
    }

    /**
     * Convert the contents of the menu's slots into virtual currency
     */
    public void doDeposit() {
        if (!Bank.hasAccount(playerUUID)) {
            Bank.createPlayerAccount(playerUUID);
        }

        for (var i = 0; i < container.getSlots(); ++i) {
            final var stack = container.getStackInSlot(i);
            final var item = stack.getItem();
            if (item == ItemInit.FIXED_CURRENCY.get() || item == ItemInit.DYNAMIC_CURRENCY.get()) {
                final var tag = stack.getTag();
                if (tag == null) {
                    continue;
                }
                final var value = tag.getLong("value");
                Bank.addCurrencyToAccount(player, new CurrencyAmount(value * stack.getCount()));
            }
            clearSlot(i);
        }

        final var balance = Bank.getAccountBalance(playerUUID);
        balance.ifPresent(amount -> DynamicEconomyPacketHandler.INSTANCE.sendTo(new ClientboundBalanceMessage(amount), ((ServerPlayer) player).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT));
        playTransactionSound();
    }

    private void clearSlot(final int index) {
        if (index >= 0 && index < container.getSlots()) {
            this.container.extractItem(index, 64, false);
        }
    }
}