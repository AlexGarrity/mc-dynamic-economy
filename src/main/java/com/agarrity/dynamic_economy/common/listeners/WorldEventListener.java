package com.agarrity.dynamic_economy.common.listeners;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.DynamicEconomyConfig;
import com.agarrity.dynamic_economy.common.economy.bank.Bank;
import com.agarrity.dynamic_economy.common.economy.bank.BankSavedData;
import com.agarrity.dynamic_economy.common.economy.resources.WorldResourceTracker;
import com.agarrity.dynamic_economy.common.economy.resources.WorldSavedData;
import com.agarrity.dynamic_economy.init.EntityInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.world.*;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = DynamicEconomy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldEventListener {

    private static WorldSavedData SAVED_DATA;

    public static void setSavedData(@NotNull WorldSavedData savedData) {
        SAVED_DATA = savedData;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onWorldLoad(final WorldEvent.Load event) {
        if (event.getWorld() instanceof ServerLevel serverLevel) {
            if (serverLevel.dimension() != Level.OVERWORLD) {
                return;
            }

            final var worldSavedData = serverLevel.getDataStorage().computeIfAbsent(WorldSavedData::load, WorldSavedData::create, "dynamic_economy_resources");
            WorldResourceTracker.setSavedData(worldSavedData);
            PlayerEventListener.setSavedData(worldSavedData);
            WorldEventListener.setSavedData(worldSavedData);

            DynamicEconomyConfig.ALLOWED_BLOCKS.get().forEach(WorldResourceTracker::registerFiniteBlock);

            final var bankSavedData = serverLevel.getDataStorage().computeIfAbsent(BankSavedData::load, BankSavedData::create, "dynamic_economy_bank");
            Bank.setSavedData(bankSavedData);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onChunkGenerate(final ChunkEvent.Load event) {
        if (event.getWorld().isClientSide()) {
            return;
        }
        final var chunk = event.getChunk();

        if (SAVED_DATA.getChunksGenerated().contains(chunk.getPos().toLong())) {
            return;
        }

        WorldResourceTracker.addChunkToWorld(chunk);
        SAVED_DATA.getChunksGenerated().add(chunk.getPos().toLong());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBreak(final BlockEvent.BreakEvent event) {
        if (event.getWorld().isClientSide()) {
            return;
        }

        final var serverLevel = (ServerLevel) event.getPlayer().getLevel();
        final var block = event.getState().getBlock();

        final var blockEntity = serverLevel.getBlockEntity(event.getPos());
        if (blockEntity != null) {
            if (blockEntity instanceof Container container) {
                final var containerSize = container.getContainerSize();
                for (var i = 0; i < containerSize; ++i) {
                    WorldResourceTracker.removeItemsFromEconomy(container.getItem(i));
                }
            }
        }

        WorldResourceTracker.removeBlockFromWorld(block);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockPlace(final BlockEvent.EntityPlaceEvent event) {
        if (event.getWorld().isClientSide()) {
            return;
        }

        final var block = event.getPlacedBlock().getBlock();
        final var stackToRemove = new ItemStack(block.asItem(), 1);
        WorldResourceTracker.removeItemsFromEconomy(stackToRemove);
        WorldResourceTracker.addBlockToWorld(block);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityDestroysBlock(final LivingDestroyBlockEvent event) {
        if (event.getEntityLiving().getLevel().isClientSide()) {
            return;
        }

        final var block = event.getState().getBlock();
        if (event.getEntity() instanceof Zombie) {
            if (!(event.getState().getBlock() instanceof DoorBlock)) {
                return;
            }
        }

        WorldResourceTracker.removeBlockFromWorld(block);
    }

    // Make animal villagers spawn in all biomes in groups of up to 4
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRegisterBiomes(final BiomeLoadingEvent event) {
        event.getSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityInit.ANIMAL_VILLAGER.get(), 6, 1, 4));
    }

    @SubscribeEvent
    public static void onExplosion(final ExplosionEvent event) {
        if (event.getWorld().isClientSide()) {
            return;
        }

        final var explosion = event.getExplosion();
        final var destroyedBlocks = explosion.getToBlow();
        for (final var blockPosition : destroyedBlocks) {
            final var block = event.getWorld().getBlockState(blockPosition).getBlock();
            WorldResourceTracker.removeBlockFromWorld(block);
        }
    }

}
