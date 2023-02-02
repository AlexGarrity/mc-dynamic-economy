package com.agarrity.dynamic_economy.common.world.inventory;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.DynamicEconomyConfig;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyAmount;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyHelper;
import com.agarrity.dynamic_economy.common.economy.resources.WorldResourceTracker;
import com.agarrity.dynamic_economy.common.network.ClientboundItemValueMessage;
import com.agarrity.dynamic_economy.common.network.DynamicEconomyPacketHandler;
import com.agarrity.dynamic_economy.common.world.entity.npc.AnimalVillager;
import com.agarrity.dynamic_economy.common.world.entity.npc.ClientSideTrader;
import com.agarrity.dynamic_economy.common.world.entity.npc.ITrader;
import com.agarrity.dynamic_economy.init.ItemInit;
import com.agarrity.dynamic_economy.init.MenuInit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.network.NetworkDirection;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.TreeMap;

public class TradeMenu extends AbstractContainerMenu {

    private final Player player;
    private final ITrader traderVillager;
    private final IItemHandler traderInventory;

    public TradeMenu(final int containerId, final Inventory playerInventory) {
        this(containerId, playerInventory, new ItemStackHandler(AnimalVillager.INVENTORY_SIZE), new ClientSideTrader(playerInventory.player));
    }

    public TradeMenu(final int containerId, final Inventory playerInventory, final IItemHandler traderInventory, final ITrader trader) {
        super(MenuInit.TRADER_MENU.get(), containerId);
        this.traderVillager = trader;
        this.player = playerInventory.player;
        this.traderInventory = traderInventory;

        final var playerInventoryItems = new HashSet<Item>();

        for (var y = 0; y < 3; ++y) {
            for (var x = 0; x < 4; ++x) {
                if (this.traderVillager.isClientSide()) {
                    addSlot(new SlotItemHandler(traderInventory, x + (y * 4), 90 + (x * 21), 7 + (y * 21)));
                } else {
                    addSlot(new SlotItemHandler(traderInventory, x + (y * 4), 90 + (x * 21), 7 + (y * 21)) {
                        @Override
                        public @NotNull ItemStack remove(int amount) {
                            playerBuyItemFromTrader(this.getItem(), amount);
                            return super.remove(amount);
                        }

                        @Override
                        public void set(@NotNull ItemStack stack) {
                            super.set(stack);
                            playerSellItemToTrader(stack, stack.getCount());
                        }

                        @Override
                        public boolean mayPlace(@NotNull final ItemStack pStack) {
                            if (CurrencyHelper.getCurrencyValue(pStack).isPresent()) {
                                return false;
                            }

                            if (this.hasItem()) {
                                return false;
                            }

                            return playerCanSellItem(pStack, pStack.getCount());
                        }

                        @Override
                        public boolean mayPickup(Player playerIn) {
                            return playerCanAffordItem(this.getItem(), this.getItem().getCount());
                        }
                    });

                    final var itemStack = this.traderInventory.getStackInSlot(x + (y * 4));
                    if (itemStack == ItemStack.EMPTY) {
                        continue;
                    }

                    playerInventoryItems.add(this.traderInventory.getStackInSlot(x + (y * 4)).getItem());
                }
            }
        }

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));

                if (this.traderVillager.isClientSide()) {
                    continue;
                }

                final var itemStack = playerInventory.getItem(j + i * 9 + 9);
                if (itemStack == ItemStack.EMPTY) {
                    continue;
                }

                playerInventoryItems.add(playerInventory.getItem(j + i * 9 + 9).getItem());
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));

            if (this.traderVillager.isClientSide()) {
                continue;
            }

            final var itemStack = playerInventory.getItem(k);
            if (itemStack == ItemStack.EMPTY) {
                continue;
            }

            playerInventoryItems.add(playerInventory.getItem(k).getItem());
        }

        for (final var item : playerInventoryItems) {
            final var optValue = WorldResourceTracker.estimateItemsValue(new ItemStack(item));
            final var optRarity = WorldResourceTracker.getItemFrequency(new ItemStack(item));
            DynamicEconomyPacketHandler.INSTANCE.sendTo(
                    new ClientboundItemValueMessage(
                            item,
                            optValue.orElse(null),
                            optRarity.orElse(-1)
                    ),
                    ((ServerPlayer) player).connection.getConnection(),
                    NetworkDirection.PLAY_TO_CLIENT
            );
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
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull final Player pPlayer, final int pIndex) {
        var itemStack = ItemStack.EMPTY;
        final var slot = this.slots.get(pIndex);
        if (slot.hasItem()) {
            final var itemStack1 = slot.getItem();
            itemStack = itemStack1.copy();
            if (pIndex < this.traderInventory.getSlots()) {
                if (this.commerceMoveItemStackTo(itemStack1, this.traderInventory.getSlots(), this.slots.size(), TransactionDirection.BuyFromTrader)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.commerceMoveItemStackTo(itemStack1, 0, this.traderInventory.getSlots(), TransactionDirection.SellToTrader)) {
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

    protected boolean commerceMoveItemStackTo(@NotNull final ItemStack newStack, final int pStartIndex, final int pEndIndex, final TransactionDirection transactionDirection) {
        boolean flag = false;
        int i = pStartIndex;
        if (newStack.isStackable()) {
            while (!newStack.isEmpty()) {
                if (i >= pEndIndex) {
                    break;
                }

                final var slot = this.slots.get(i);
                final var existingStack = slot.getItem();
                if (!existingStack.isEmpty() && ItemStack.isSameItemSameTags(newStack, existingStack)) {
                    int countWhenStackIsAddedToExistingStack = existingStack.getCount() + newStack.getCount();
                    int maxSize = Math.min(slot.getMaxStackSize(), newStack.getMaxStackSize());
                    if (countWhenStackIsAddedToExistingStack <= maxSize) {
                        if (!this.traderVillager.isClientSide()) {
                            if (transactionDirection == TransactionDirection.SellToTrader) {
                                playerSellItemToTrader(newStack, newStack.getCount());
                            } else {
                                playerBuyItemFromTrader(newStack, newStack.getCount());
                            }
                        }
                        newStack.setCount(0);
                        existingStack.setCount(countWhenStackIsAddedToExistingStack);
                        slot.setChanged();
                        flag = true;
                    } else if (existingStack.getCount() < maxSize) {
                        final var quantityOfItemSold = maxSize - existingStack.getCount();
                        if (!this.traderVillager.isClientSide()) {
                            if (transactionDirection == TransactionDirection.SellToTrader) {
                                playerSellItemToTrader(newStack, quantityOfItemSold);
                            } else {
                                playerBuyItemFromTrader(newStack, quantityOfItemSold);
                            }
                        }
                        newStack.shrink(quantityOfItemSold);
                        existingStack.setCount(maxSize);
                        slot.setChanged();
                        flag = true;
                    }
                }

                ++i;
            }
        }

        if (!newStack.isEmpty()) {
            i = pStartIndex;

            while (true) {
                if (i >= pEndIndex) {
                    break;
                }

                final var slot = this.slots.get(i);
                final var existingItemStack = slot.getItem();
                if (existingItemStack.isEmpty() && slot.mayPlace(newStack)) {
                    if (newStack.getCount() > slot.getMaxStackSize()) {
                        slot.set(newStack.split(slot.getMaxStackSize()));
                    } else {
                        slot.set(newStack.split(newStack.getCount()));
                    }

                    slot.setChanged();
                    flag = true;
                    break;
                }

                ++i;
            }
        }

        return !flag;
    }

    public boolean playerCanAffordItem(final ItemStack stack, final int count) {
        final var newStack = new ItemStack(stack.getItem(), count);
        final var optValue = WorldResourceTracker.estimateItemsValue(newStack);
        if (optValue.isEmpty()) {
            return false;
        }

        final var inventoryValue = calculateInventoryCoinValue();

        final var value = optValue.get();
        return inventoryValue.isGreaterThan(value) || inventoryValue.isEqualTo(value);
    }

    public boolean playerCanSellItem(final ItemStack stack, final int count) {
        final var newStack = new ItemStack(stack.getItem(), count);
        final var optValue = WorldResourceTracker.estimateItemsValue(newStack);
        if (optValue.isEmpty()) {
            return false;
        }
        final var value = optValue.get();

        return value.asLong() <= DynamicEconomyConfig.TRADER_MAX_BUY_PRICE.get();
    }

    public void playerBuyItemFromTrader(final ItemStack stack, final int count) {
        final var newStack = new ItemStack(stack.getItem(), count);
        final var optValue = WorldResourceTracker.estimateItemsValue(newStack);
        if (optValue.isEmpty()) {
            return;
        }

        if (!removeCurrencyFromPlayerInventory(optValue.get())) {
            return;
        }

        DynamicEconomy.LOGGER.debug("{} bought {} for {}", player.getDisplayName().getString(), stack, optValue.get());
    }

    public void playerSellItemToTrader(final ItemStack stack, final int count) {
        if (traderVillager.isClientSide()) {
            return;
        }

        final var newStack = new ItemStack(stack.getItem(), count);
        final var optValue = WorldResourceTracker.estimateItemsValue(newStack);
        if (optValue.isEmpty()) {
            return;
        }

        final var changeRequired = CurrencyHelper.calculateCurrencyRequired(optValue.get());
        for (final var currencyItem : changeRequired) {
            if (!player.addItem(currencyItem)) {
                player.drop(currencyItem, false);
            }
        }

        DynamicEconomy.LOGGER.debug("{} sold {} for {}", player.getDisplayName().getString(), stack, optValue.get());
    }

    public boolean removeCurrencyFromPlayerInventory(final CurrencyAmount amount) {
        var valueToMakeUp = amount.asLong();
        var totalValue = 0;

        final var fixedMap = new TreeMap<Long, Integer>();
        final var currencyRequired = new TreeMap<Long, Integer>();
        final var currencySlotIndices = new OrderedHashSet<Integer>();

        for (var index = 0; index < player.getInventory().items.size(); ++index) {
            final var slot = player.getInventory().items.get(index);
            if (slot.getItem() == ItemInit.FIXED_CURRENCY.get()) {
                final var itemValue = CurrencyHelper.getCurrencyValue(slot).orElse(new CurrencyAmount()).asLong();
                fixedMap.put(itemValue, (fixedMap.containsKey(itemValue)) ? fixedMap.get(itemValue) + slot.getCount() : slot.getCount());
                totalValue += itemValue * slot.getCount();
                currencySlotIndices.add(index);
            }
        }

        if (valueToMakeUp > totalValue) {
            return false;
        }

        for (final var currencyAmount : fixedMap.keySet()) {
            var currencyCount = fixedMap.get(currencyAmount);
            while (valueToMakeUp > 0 && currencyCount != 0) {
                currencyCount -= 1;
                valueToMakeUp -= currencyAmount;
                if (currencyRequired.containsKey(currencyAmount)) {
                    final var existingValue = currencyRequired.get(currencyAmount);
                    currencyRequired.put(currencyAmount, existingValue + 1);
                } else {
                    currencyRequired.put(currencyAmount, 1);
                }
            }
        }

        for (final var slotIndex : currencySlotIndices) {
            final var slot = player.getInventory().items.get(slotIndex);
            final var itemValue = CurrencyHelper.getCurrencyValue(slot).orElse(new CurrencyAmount()).asLong();
            if (currencyRequired.containsKey(itemValue)) {
                final var quantityOfCurrencyRequired = currencyRequired.get(itemValue);
                if (slot.getCount() >= quantityOfCurrencyRequired) {
                    slot.setCount(slot.getCount() - quantityOfCurrencyRequired);
                    currencyRequired.remove(itemValue);
                } else {
                    currencyRequired.put(itemValue, quantityOfCurrencyRequired - slot.getCount());
                    slot.setCount(0);
                }
            }
            if (currencyRequired.isEmpty()) {
                break;
            }
        }

        if (valueToMakeUp < 0) {
            final var currencyRefund = CurrencyHelper.calculateCurrencyRequired(new CurrencyAmount(-valueToMakeUp));
            for (final var stack : currencyRefund) {
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                    DynamicEconomy.LOGGER.debug("Failed to insert a stack of {} into player inventory", stack);
                }
            }
        }

        return true;
    }

    public CurrencyAmount calculateInventoryCoinValue() {
        long total = 0;
        for (final var slot : player.getInventory().items) {
            if (slot.getItem() == ItemInit.FIXED_CURRENCY.get()) {
                total += CurrencyHelper.getStackValue(slot).orElse(new CurrencyAmount()).asLong();
            }
        }

        return new CurrencyAmount(total);
    }

    private enum TransactionDirection {
        SellToTrader,
        BuyFromTrader,
    }
}