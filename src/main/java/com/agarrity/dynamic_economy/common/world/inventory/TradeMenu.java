package com.agarrity.dynamic_economy.common.world.inventory;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.common.economy.bag.CoinBagSavedData;
import com.agarrity.dynamic_economy.common.economy.bank.Bank;
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
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.network.NetworkDirection;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.TreeMap;
import java.util.UUID;

public class TradeMenu extends AbstractContainerMenu {

    private final Player player;
    private final ITrader traderVillager;
    private final TraderItemStackHandler traderItemStackHandler;

    public TradeMenu(final int containerId, final Inventory playerInventory) {
        this(containerId, playerInventory, new TraderItemStackHandler(AnimalVillager.INVENTORY_SIZE), new ClientSideTrader(playerInventory.player));
    }

    public TradeMenu(final int containerId, final Inventory playerInventory, final TraderItemStackHandler traderItemStackHandler, final ITrader trader) {
        super(MenuInit.TRADER_MENU.get(), containerId);
        this.traderVillager = trader;
        this.player = playerInventory.player;
        this.traderItemStackHandler = traderItemStackHandler;

        // Stores all the items that are going to need to have their values sent to the player
        final var playerInventoryItems = new HashSet<Item>();

        // Add trader inventory items
        for (var y = 0; y < 3; ++y) {
            for (var x = 0; x < 4; ++x) {
                if (this.traderVillager.isClientSide()) {
                    addSlot(new SlotItemHandler(traderItemStackHandler, x + (y * 4), 90 + (x * 21), 7 + (y * 21)));
                } else {
                    addSlot(new SlotItemHandler(traderItemStackHandler, x + (y * 4), 90 + (x * 21), 7 + (y * 21)) {
                        @Override
                        public @NotNull ItemStack remove(int amount) {
                            final int slotIndex = x + (y * 4);
                            playerBuyItemFromTrader(this.getItem(), amount, traderItemStackHandler.getSellerOfSlot(slotIndex));
                            return super.remove(amount);
                        }

                        @Override
                        public void set(@NotNull ItemStack stack) {
                            super.set(stack);
                            final int slotIndex = x + (y * 4);
                            traderItemStackHandler.setSellerOfSlot(slotIndex, player.getUUID());
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
                            final int slotIndex = x + (y * 4);
                            final var sellerUUID = traderItemStackHandler.getSellerOfSlot(slotIndex);
                            return playerCanAffordItem(this.getItem(), this.getItem().getCount()) || player.getUUID() == sellerUUID;
                        }
                    });

                    final var itemStack = this.traderItemStackHandler.getStackInSlot(x + (y * 4));
                    if (itemStack == ItemStack.EMPTY) {
                        continue;
                    }

                    playerInventoryItems.add(this.traderItemStackHandler.getStackInSlot(x + (y * 4)).getItem());
                }
            }
        }

        // Add player inventory items
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

        // Add hotbar items
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

        // Send the value of all items in the player and trader inventory to the player
        for (final var item : playerInventoryItems) {
            final var optValue = WorldResourceTracker.estimateItemsValue(new ItemStack(item));
            final var optRarity = WorldResourceTracker.getItemFrequency(new ItemStack(item));
            DynamicEconomyPacketHandler.INSTANCE.sendTo(new ClientboundItemValueMessage(item, optValue.orElse(null), optRarity.orElse(-1)), ((ServerPlayer) player).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
        }
    }


    /**
     * Determines whether supplied player can use this container
     *
     * @param pPlayer The player using the container
     * @return True if the menu is valid, false otherwise
     */
    @Override
    public boolean stillValid(@NotNull final Player pPlayer) {
        return true;
    }

    /**
     * Called when the container is closed
     *
     * @param pPlayer The player that closed the menu
     */
    @Override
    public void removed(@NotNull final Player pPlayer) {
        super.removed(pPlayer);
        this.traderVillager.setTradingPlayer(null);
    }

    /**
     * Shift-click move a stack
     *
     * @param pPlayer The player moving the slot
     * @param pIndex  The index of the slot the player shift-clicked
     * @return The remaining stack after attempting to move it
     */
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull final Player pPlayer, final int pIndex) {
        var itemStack = ItemStack.EMPTY;
        final var slot = this.slots.get(pIndex);
        if (slot.hasItem()) {
            final var itemStack1 = slot.getItem();
            itemStack = itemStack1.copy();
            // Item is being quick moved from the trader inventory
            if (pIndex < this.traderItemStackHandler.getSlots()) {
                final var sellerUUID = traderItemStackHandler.getSellerOfSlot(pIndex);
                if (this.commerceMoveItemStackTo(itemStack1, this.traderItemStackHandler.getSlots(), this.slots.size(), TransactionDirection.BuyFromTrader, sellerUUID)) {
                    return ItemStack.EMPTY;
                }
                // Item is being quick moved from the player inventory
            } else if (this.commerceMoveItemStackTo(itemStack1, 0, this.traderItemStackHandler.getSlots(), TransactionDirection.SellToTrader, pPlayer.getUUID())) {
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
     * Move an ItemStack between the player and trader inventory, checking whether they can be afforded / sold as required
     *
     * @param newStack             The stack to move
     * @param firstSlotIndex       The index of the first slot in the inventory
     * @param finalSlotIndex       The index of the last slot in the inventory
     * @param transactionDirection The direction of the transaction
     * @param seller               The UUID of the seller of the items in the slot
     * @return True if the ItemStack could be moved, false otherwise
     */
    protected boolean commerceMoveItemStackTo(@NotNull final ItemStack newStack, final int firstSlotIndex, final int finalSlotIndex, final TransactionDirection transactionDirection, UUID seller) {
        boolean flag = false;
        int i = firstSlotIndex;

        // Item can be stacked
        if (newStack.isStackable()) {
            while (!newStack.isEmpty()) {
                if (i >= finalSlotIndex) {
                    break;
                }

                final var slot = this.slots.get(i);
                final var existingStack = slot.getItem();
                if (!existingStack.isEmpty() && ItemStack.isSameItemSameTags(newStack, existingStack)) {
                    int countWhenStackIsAddedToExistingStack = existingStack.getCount() + newStack.getCount();
                    int maxSize = Math.min(slot.getMaxStackSize(), newStack.getMaxStackSize());
                    if (countWhenStackIsAddedToExistingStack <= maxSize) {
                        if (!this.traderVillager.isClientSide()) {
                            if (transactionDirection == TransactionDirection.BuyFromTrader) {
                                playerBuyItemFromTrader(newStack, newStack.getCount(), seller);
                            }
                        }
                        newStack.setCount(0);
                        existingStack.setCount(countWhenStackIsAddedToExistingStack);
                        slot.setChanged();
                        flag = true;
                    } else if (existingStack.getCount() < maxSize) {
                        final var quantityOfItemSold = maxSize - existingStack.getCount();
                        if (!this.traderVillager.isClientSide()) {
                            if (transactionDirection == TransactionDirection.BuyFromTrader) {
                                playerBuyItemFromTrader(newStack, quantityOfItemSold, seller);
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

        // Item cannot be stacked, and isn't empty
        if (!newStack.isEmpty()) {
            i = firstSlotIndex;

            while (true) {
                if (i >= finalSlotIndex) {
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

    /**
     * Check if a player can afford to buy an item from the trader
     *
     * @param stack The stack of items to purchase
     * @param count The number of items to purchase
     * @return True if the player can afford x items
     */
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

    /**
     * Check whether a player can sell an item
     *
     * @param stack The stack of items to sell
     * @param count The number of items to sell
     * @return True if the item can be sold, false otherwise
     */
    public boolean playerCanSellItem(final ItemStack stack, final int count) {
        final var newStack = new ItemStack(stack.getItem(), count);
        final var optValue = WorldResourceTracker.estimateItemsValue(newStack);
        return optValue.isPresent();
    }

    /**
     * Remove an amount of currency from the active player in exchange for an item stack, and gives the currency to the seller's account
     *
     * @param stack  The stack of items the player is buying
     * @param count  The number of items the player is buying
     * @param seller The seller of the items
     */
    public void playerBuyItemFromTrader(final ItemStack stack, final int count, UUID seller) {
        final var newStack = new ItemStack(stack.getItem(), count);
        final var optValue = WorldResourceTracker.estimateItemsValue(newStack);
        if (optValue.isEmpty()) {
            return;
        }

        if (!removeCurrencyFromPlayerInventory(optValue.get())) {
            return;
        }

        Bank.addCurrencyToAccount(seller, optValue.get());
        DynamicEconomy.LOGGER.debug("{} bought {} for {} from {}", player.getDisplayName().getString(), stack, optValue.get(), seller);
    }

    /**
     * Remove an amount of currency from the current player's inventory, and gives them a refund if they don't have correct change
     *
     * @param amount The amount of currency to remove
     * @return True if the currency could be removed, false otherwise
     */
    public boolean removeCurrencyFromPlayerInventory(final CurrencyAmount amount) {
        var valueToMakeUp = amount.asLong();
        var totalValue = 0;

        final var fixedMap = new TreeMap<Long, Integer>();
        final var currencyRequired = new TreeMap<Long, Integer>();
        final var currencySlotIndices = new OrderedHashSet<Integer>();

        final var coinBagSlot = findCoinBag();
        final var coinBag = (coinBagSlot != -1) ? player.getInventory().items.get(coinBagSlot) : null;
        ItemStackHandler coinBagInventory = null;
        if (coinBag != null) {
            final var coinBagTag = coinBag.getTag();
            if (coinBagTag != null) {
                final var coinBagUUID = coinBagTag.getUUID("uuid");
                coinBagInventory = CoinBagSavedData.instance.getBag(coinBagUUID);
            }
        }

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

        // First, remove coins from the player's inventory
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

        // First, try to remove coins from the player inventory
        for (final var slotIndex : currencySlotIndices) {
            final var slot = player.getInventory().items.get(slotIndex);
            if (tryToRemoveCurrencyFromSlot(currencyRequired, slot)) break;
        }

        // Then try to remove them from a coin bag, if it exists
        if (coinBagInventory != null) {
            for (var i = 0; i < coinBagInventory.getSlots(); ++i) {
                final var slot = coinBagInventory.getStackInSlot(i);
                if (tryToRemoveCurrencyFromSlot(currencyRequired, slot)) break;
            }
        }


        if (valueToMakeUp >= 0) {
            return true;
        }

        // The player is owed a refund
        final var currencyRefund = CurrencyHelper.calculateCurrencyRequired(new CurrencyAmount(-valueToMakeUp));
        for (final var stack : currencyRefund) {
            final var valueOfCoinToRefund = CurrencyHelper.getCurrencyValue(stack);

            // First, try to add it to the coin bag
            if (coinBagInventory != null) {
                for (var i = 0; i < coinBagInventory.getSlots(); ++i) {
                    final var bagStack = coinBagInventory.getStackInSlot(i);
                    final var optValue = CurrencyHelper.getCurrencyValue(bagStack);

                    assert (optValue.isPresent());
                    assert (valueOfCoinToRefund.isPresent());

                    if (optValue.get().asLong() == valueOfCoinToRefund.get().asLong()) {
                        final var countAvailableForPlacement = ItemInit.FIXED_CURRENCY.get().getItemStackLimit(bagStack) - bagStack.getCount();
                        if (countAvailableForPlacement > 0) {
                            bagStack.grow(countAvailableForPlacement);
                            stack.shrink(countAvailableForPlacement);
                        }
                        if (stack.isEmpty()) {
                            break;
                        }
                    }
                }
            }

            // If it does not fit in the coin bag, put it in the player inventory
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
                DynamicEconomy.LOGGER.debug("Failed to insert a stack of {} into player inventory", stack);
            }
        }

        return true;
    }

    /**
     * Try to remove an amount of currency from a slot
     *
     * @param currencyRequired A map containing the number of each type of currency required
     * @param slot             The slot to remove currency from
     * @return True if no more currency is required, false otherwise
     */
    private boolean tryToRemoveCurrencyFromSlot(TreeMap<Long, Integer> currencyRequired, ItemStack slot) {
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
        return currencyRequired.isEmpty();
    }

    /**
     * Find the slot that the player has a coin bag in
     *
     * @return An integer index representing the slot the player has their coin bag in
     */
    private int findCoinBag() {
        var slotIndex = 0;
        for (final var slot : player.getInventory().items) {
            if (slot.getItem() == ItemInit.COIN_BAG.get()) {
                return slotIndex;
            }
            ++slotIndex;
        }
        return -1;
    }

    /**
     * Calculate the value of a player's inventory, including coins in coin bags
     *
     * @return A CurrencyAmount containing the value of the currency in the inventory
     */
    public CurrencyAmount calculateInventoryCoinValue() {
        long total = 0;
        for (final var slot : player.getInventory().items) {
            if (slot.getItem() == ItemInit.FIXED_CURRENCY.get()) {
                total += CurrencyHelper.getStackValue(slot).orElse(new CurrencyAmount()).asLong();
            } else if (slot.getItem() == ItemInit.COIN_BAG.get()) {
                final var nbtTag = slot.getOrCreateTag();
                final var uuid = nbtTag.getUUID("uuid");
                final var bagInventory = CoinBagSavedData.instance.getBag(uuid);
                total += getValueOfBagContents(bagInventory).asLong();
            }
        }

        return new CurrencyAmount(total);
    }

    /**
     * Get the value of all of the coins in a coin bag's inventory
     *
     * @param bagInventory The inventory to check
     * @return A CurrencyAmount containing the value of the currency in the inventory
     */
    public CurrencyAmount getValueOfBagContents(ItemStackHandler bagInventory) {
        long total = 0;
        for (var i = 0; i < bagInventory.getSlots(); ++i) {
            final var optValue = CurrencyHelper.getCurrencyValue(bagInventory.getStackInSlot(i));
            if (optValue.isEmpty()) {
                continue;
            }
            total += optValue.get().asLong();
        }

        return new CurrencyAmount(total);
    }

    private enum TransactionDirection {
        SellToTrader, BuyFromTrader,
    }
}