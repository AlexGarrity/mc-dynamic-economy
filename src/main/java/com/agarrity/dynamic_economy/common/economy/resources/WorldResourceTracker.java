package com.agarrity.dynamic_economy.common.economy.resources;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyAmount;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;

public class WorldResourceTracker {

    private static final HashSet<String> FINITE_BLOCKS = new HashSet<>();

    private static WorldSavedData SAVED_DATA;

    public static void setSavedData(WorldSavedData data) {
        SAVED_DATA = data;
    }

    /**
     * Register a block as finite (there are a fixed quantity in the world)
     * @param blockName The registry name of the block to register
     */
    public static void registerFiniteBlock(@NotNull final String blockName) {
        if (!FINITE_BLOCKS.contains(blockName)) {
            DynamicEconomy.LOGGER.debug("Registered {} as finite", blockName);
            FINITE_BLOCKS.add(blockName);
            SAVED_DATA.getBlocksInWorld().put(blockName, 0);
        }
    }

    /**
     * Add a block to the world without logging it, provided that it's finite
     * @param block The block to add to the world
     * @return true if the block was added, false otherwise
     */
    private static boolean addBlockToWorldUnlogged(@NotNull final Block block) {
        if (block.getRegistryName() == null) {
            return false;
        }

        SAVED_DATA.incrementBlocksInWorld();
        SAVED_DATA.setDirty();

        final var blockName = block.getRegistryName().toString();
        if (!FINITE_BLOCKS.contains(blockName)) {
            return false;
        }

        if (SAVED_DATA.getBlocksInWorld().containsKey(blockName)) {
            final var existingCount = SAVED_DATA.getBlocksInWorld().get(blockName);
            SAVED_DATA.getBlocksInWorld().put(blockName, existingCount + 1);
        }
        else {
            SAVED_DATA.getBlocksInWorld().put(blockName, 1);
        }

        return true;
    }

    /**
     * Add a block to the resource tracker's virtual world
     * @param block The block to add to the world
     */
    public static void addBlockToWorld(@NotNull final Block block) {
        if (addBlockToWorldUnlogged(block)) {
            DynamicEconomy.LOGGER.debug("Added a {} to the world", block.getName().getString());
        }
    }

    /**
     * Remove a block from the world without logging it, provided that the block is finite
     * @param block The block to remove from the world
     * @return true if the block was removed, false otherwise
     */
    private static boolean removeBlockFromWorldUnlogged(@NotNull final Block block) {
        if (block.getRegistryName() == null) {
            return false;
        }

        SAVED_DATA.decrementBlocksInWorld();
        SAVED_DATA.setDirty();

        final var blockName = block.getRegistryName().toString();
        if (!FINITE_BLOCKS.contains(blockName)) {
            return false;
        }

        if (!SAVED_DATA.getBlocksInWorld().containsKey(blockName)) {
            return false;
        }

        final var existingCount = SAVED_DATA.getBlocksInWorld().get(blockName);
        if (existingCount == 0) {
            return false;
        }

        if (existingCount == 1) {
            SAVED_DATA.getBlocksInWorld().remove(blockName);
        }
        else {
            SAVED_DATA.getBlocksInWorld().put(blockName, existingCount - 1);
        }

        return true;
    }

    /**
     * Remove a block from the resource tracker's virtual world
     * @param block The block to remove
     */
    public static void removeBlockFromWorld(@NotNull final Block block) {
        if (removeBlockFromWorldUnlogged(block)) {
            DynamicEconomy.LOGGER.debug("Removed a {} from the world", block.getName().getString());
        }
    }

    /**
     * Add a chunk of blocks to the resource tracker's virtual world
     * @param chunk The chunk to add
     */
    public static void addChunkToWorld(final ChunkAccess chunk) {
        for (var z = 0; z < 16; ++z) {
            for (var x = 0; x < 16; ++x) {
                for (var y = -64; y < 256; ++y) {
                    final var block = chunk.getBlockState(new BlockPos(x,y,z)).getBlock();
                    addBlockToWorldUnlogged(block);
                }
            }
        }
        SAVED_DATA.incrementChunksInWorld();
    }

    /**
     * Add an item to the resource tracker's economy
     * @param stack The item to add
     */
    public static void addItemsToEconomy(@NotNull final ItemStack stack) {
        addItemsToEconomy(stack, stack.getCount());
    }

    /**
     * Add a stack of items to the resource tracker's economy
     * @param stack The stack of items to add
     * @param count The number of items to add
     */
    public static void addItemsToEconomy(@NotNull final ItemStack stack, int count) {
        if (stack.getItem().getRegistryName() == null) {
            return;
        }

        final var itemName = stack.getItem().getRegistryName().toString();
        if (SAVED_DATA.getItemsInWorld().containsKey(itemName)) {
            final var existingCount = SAVED_DATA.getItemsInWorld().get(itemName);
            SAVED_DATA.getItemsInWorld().put(itemName, existingCount + count);
        }
        else {
            SAVED_DATA.getItemsInWorld().put(itemName, count);
        }


        DynamicEconomy.LOGGER.debug("ADDED {} of '{}' TO THE ECONOMY", stack.getCount(), itemName);
        SAVED_DATA.setDirty();
    }

    /**
     * Remove an item from the resource tracker's economy
     * @param stack The item to remove
     */
    public static void removeItemsFromEconomy(@NotNull final ItemStack stack) {
        removeItemsFromEconomy(stack, stack.getCount());
    }

    /**
     * Remove a stack of items from the resource tracker's economy
     * @param stack The stack of items to remove
     * @param count The number of items to remove
     */
    public static void removeItemsFromEconomy(@NotNull final ItemStack stack, final int count) {
        if (stack.getItem().getRegistryName() == null) {
            return;
        }

        if (stack.isEmpty()) {
            return;
        }

        final var itemName = stack.getItem().getRegistryName().toString();

        if (!SAVED_DATA.getItemsInWorld().containsKey(itemName)) {
            DynamicEconomy.LOGGER.debug("Tried to remove {} from the economy, but there is none to remove", stack);
            return;
        }

        final var existingCount = SAVED_DATA.getItemsInWorld().get(itemName);
        if (existingCount < count) {
            SAVED_DATA.getItemsInWorld().remove(itemName);
            DynamicEconomy.LOGGER.debug("Tried to remove {} of '{}' from the economy, but there were only {} to remove", count, itemName, existingCount);
            return;
        }

        if (existingCount.equals(count)) {
            SAVED_DATA.getItemsInWorld().remove(itemName);
        }
        else {
            SAVED_DATA.getItemsInWorld().put(itemName, existingCount - count);
        }

        DynamicEconomy.LOGGER.debug("Removed {} of '{}' from the economy", count, itemName);
        SAVED_DATA.setDirty();
    }

    /**
     * Get the total pool size per resource type
     * @return The size of the pool
     */
    private static int getPoolSize() {
        return (int) (SAVED_DATA.getPoolSize() * 0.5F);
    }

    /**
     * Estimate the value of an item
     * @param stack The item to estimate the value of
     * @return An empty optional if none of the item type legitimately exist, or the estimated value otherwise
     */
    public static Optional<CurrencyAmount> estimateItemValue(@NotNull final ItemStack stack) {
        final var optCounts = getItemCounts(stack);
        if (optCounts.isEmpty()) {
            return Optional.empty();
        }


        final var counts = optCounts.get();
        final var value = getPoolSize() / counts.total();
        return Optional.of(new CurrencyAmount(value));
    }

    /**
     * Estimate the value of a stack of items
     * @param stack The item stack to estimate the value of
     * @return An empty optional if none of the item type legitimately exist, or the estimated value otherwise
     */
    public static Optional<CurrencyAmount> estimateItemsValue(@NotNull final ItemStack stack) {
        final var singleItemValue = estimateItemValue(stack);
        if (singleItemValue.isEmpty()) {
            return singleItemValue;
        }

        final var value = singleItemValue.get().asLong();
        return Optional.of(new CurrencyAmount(value * stack.getCount()));
    }

    private static class ItemCounts {
        int virtual;
        int economy;

        public ItemCounts(int v, int e) {
            this.virtual = v;
            this.economy = e;
        }

        public int total() {
            return economy + virtual;
        }
    }

    private static Optional<ItemCounts> getItemCounts(@NotNull final ItemStack stack) {
        if (stack.getItem().getRegistryName() == null) {
            return Optional.empty();
        }

        final var itemName = stack.getItem().getRegistryName().toString();
        final var economyCount = SAVED_DATA.getItemsInWorld().getOrDefault(itemName, 0);
        final var virtualCount = SAVED_DATA.getItemsInWorld().getOrDefault(itemName, 0);

        final var itemCounts = new ItemCounts(virtualCount, economyCount);
        if (itemCounts.total() == 0) {
            return Optional.empty();
        }
        return Optional.of(itemCounts);
    }

    /**
     * Get the logarithmic frequency of an item based on the number that exist in the economy
     * (ie. if 10 exist then the frequency is 1, if 100 exist then the frequency is 2, ...)
     * @param stack The item type to get the frequency of
     * @return An empty optional if none of the item legitimately exist in the economy, otherwise the frequency
     */
    public static Optional<Integer> getItemFrequency(@NotNull final ItemStack stack) {
        if (stack.getItem().getRegistryName() == null) {
            return Optional.empty();
        }

        final var itemName = stack.getItem().getRegistryName().toString();
        if (!SAVED_DATA.getItemsInWorld().containsKey(itemName)) {
            return Optional.empty();
        }

        final var existingCount = SAVED_DATA.getItemsInWorld().get(itemName);
        return Optional.of((int) Math.log10(existingCount));
    }

}
