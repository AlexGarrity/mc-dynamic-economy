package com.agarrity.dynamic_economy.common.economy.resources;

import com.agarrity.dynamic_economy.DynamicEconomy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class WorldSavedData extends SavedData {

    private static final String BLOCKS_TAG_NAME = "blocks";
    private static final String ITEMS_TAG_NAME = "items";
    private static final String VIRTUAL_ITEMS_TAG_NAME = "virtual_items";
    private static final String PLAYERS_TAG_NAME = "players";
    private static final String TOTAL_BLOCKS_TAG_NAME = "total_blocks";
    private static final String TOTAL_CHUNKS_TAG_NAME = "total_chunks";
    private static final String TOTAL_PLAYERS_TAG_NAME = "total_players";
    private static final String GENERATED_CHUNKS_TAG_NAME = "generated_chunks";
    private static final String ECONOMY_POOL_SIZE_TAG_NAME = "economy_pool_size";

    private final HashMap<String, Integer> BLOCKS_IN_WORLD;
    private final HashMap<String, Integer> ITEMS_IN_WORLD;
    private final HashMap<String, Integer> ITEMS_HELD_VIRTUALLY;
    private final HashSet<Long> GENERATED_CHUNKS;
    private final HashSet<UUID> PLAYERS_IN_WORLD;

    private int TOTAL_BLOCKS_IN_WORLD = 0;
    private int TOTAL_CHUNKS_IN_WORLD = 0;
    private int TOTAL_PLAYERS_IN_WORLD = 0;
    private long ECONOMY_POOL_SIZE = 100000000L;

    public WorldSavedData() {
        BLOCKS_IN_WORLD = new HashMap<>();
        ITEMS_IN_WORLD = new HashMap<>();
        ITEMS_HELD_VIRTUALLY = new HashMap<>();
        GENERATED_CHUNKS = new HashSet<>();
        PLAYERS_IN_WORLD = new HashSet<>();
    }

    public static WorldSavedData load(CompoundTag rootTag) {

        DynamicEconomy.LOGGER.debug("Loading economy saved data");

        final var worldData = create();
        worldData.ECONOMY_POOL_SIZE = rootTag.getLong(ECONOMY_POOL_SIZE_TAG_NAME);
        worldData.TOTAL_BLOCKS_IN_WORLD = rootTag.getInt(TOTAL_BLOCKS_TAG_NAME);
        worldData.TOTAL_CHUNKS_IN_WORLD = rootTag.getInt(TOTAL_CHUNKS_TAG_NAME);
        worldData.TOTAL_PLAYERS_IN_WORLD = rootTag.getInt(TOTAL_PLAYERS_TAG_NAME);

        final var blocksTag = rootTag.getCompound(BLOCKS_TAG_NAME);
        for (final var key : blocksTag.getAllKeys()) {
            final var count = blocksTag.getInt(key);
            worldData.BLOCKS_IN_WORLD.put(key, count);
        }

        final var itemsTag = rootTag.getCompound(ITEMS_TAG_NAME);
        for (final var key : itemsTag.getAllKeys()) {
            final var count = itemsTag.getInt(key);
            worldData.ITEMS_IN_WORLD.put(key, count);
        }

        final var virtualItemsTag = rootTag.getCompound(VIRTUAL_ITEMS_TAG_NAME);
        for (final var key : virtualItemsTag.getAllKeys()) {
            final var count = virtualItemsTag.getInt(key);
            worldData.ITEMS_HELD_VIRTUALLY.put(key, count);
        }

        final var playersTag = rootTag.getCompound(PLAYERS_TAG_NAME);
        for (final var key : playersTag.getAllKeys()) {
            worldData.PLAYERS_IN_WORLD.add(playersTag.getUUID(key));
        }

        for (final var chunkPos : rootTag.getLongArray(GENERATED_CHUNKS_TAG_NAME)) {
            worldData.GENERATED_CHUNKS.add(chunkPos);
        }

        return worldData;
    }

    public static WorldSavedData create() {
        DynamicEconomy.LOGGER.debug("Creating economy saved data");
        return new WorldSavedData();
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag pCompoundTag) {
        DynamicEconomy.LOGGER.debug("Saving economy saved data");

        final var blocksTag = new CompoundTag();
        final var itemsTag = new CompoundTag();
        final var virtualItemsTag = new CompoundTag();
        final var playersTag = new CompoundTag();
        final var chunksTag = new LongArrayTag(GENERATED_CHUNKS.stream().toList());

        for (final var blockName : getBlocksInWorld().keySet()) {
            final var count = getBlocksInWorld().get(blockName);
            blocksTag.putInt(blockName, count);
        }

        for (final var itemName : getItemsInWorld().keySet()) {
            final var count = getItemsInWorld().get(itemName);
            itemsTag.putInt(itemName, count);
        }

        for (final var virtualItemName : getVirtualItems().keySet()) {
            final var count = getVirtualItems().get(virtualItemName);
            virtualItemsTag.putInt(virtualItemName, count);
        }

        var i = 0;
        for (final var player : getPlayersInWorld()) {
            playersTag.putUUID(String.valueOf((i++)), player);
        }

        pCompoundTag.putLong(ECONOMY_POOL_SIZE_TAG_NAME, ECONOMY_POOL_SIZE);
        pCompoundTag.putInt(TOTAL_BLOCKS_TAG_NAME, TOTAL_BLOCKS_IN_WORLD);
        pCompoundTag.putInt(TOTAL_CHUNKS_TAG_NAME, TOTAL_CHUNKS_IN_WORLD);
        pCompoundTag.putInt(TOTAL_PLAYERS_TAG_NAME, TOTAL_PLAYERS_IN_WORLD);
        pCompoundTag.put(GENERATED_CHUNKS_TAG_NAME, chunksTag);
        pCompoundTag.put(BLOCKS_TAG_NAME, blocksTag);
        pCompoundTag.put(VIRTUAL_ITEMS_TAG_NAME, virtualItemsTag);
        pCompoundTag.put(ITEMS_TAG_NAME, itemsTag);
        pCompoundTag.put(PLAYERS_TAG_NAME, playersTag);
        return pCompoundTag;
    }

    public HashSet<Long> getChunksGenerated() {
        return GENERATED_CHUNKS;
    }

    public int getTotalPlayers() {
        return TOTAL_PLAYERS_IN_WORLD;
    }

    public long getPoolSize() {
        return ECONOMY_POOL_SIZE;
    }

    public HashMap<String, Integer> getBlocksInWorld() {
        return BLOCKS_IN_WORLD;
    }

    public HashMap<String, Integer> getItemsInWorld() {
        return ITEMS_IN_WORLD;
    }

    public HashMap<String, Integer> getVirtualItems() {
        return ITEMS_HELD_VIRTUALLY;
    }

    public HashSet<UUID> getPlayersInWorld() {
        return PLAYERS_IN_WORLD;
    }

    public void incrementBlocksInWorld() {
        ++TOTAL_BLOCKS_IN_WORLD;
    }

    public void decrementBlocksInWorld() {
        if (TOTAL_BLOCKS_IN_WORLD == 0) {
            return;
        }
        --TOTAL_BLOCKS_IN_WORLD;
    }

    public void incrementChunksInWorld() {
        ++TOTAL_CHUNKS_IN_WORLD;
    }

    public void incrementPlayersInWorld() {
        ++TOTAL_PLAYERS_IN_WORLD;
    }

}
