package com.agarrity.dynamic_economy.common.listeners;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.common.economy.bank.Bank;
import com.agarrity.dynamic_economy.common.economy.resources.WorldResourceTracker;
import com.agarrity.dynamic_economy.common.economy.resources.WorldSavedData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DynamicEconomy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEventListener {

    private static WorldSavedData SAVED_DATA;

    public static void setSavedData(WorldSavedData data) {
        SAVED_DATA = data;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerJoin(EntityJoinWorldEvent event) {
        if (event.getWorld().isClientSide()) {
            return;
        }

        if ((event.getEntity() instanceof ServerPlayer player)) {
            if (!SAVED_DATA.getPlayersInWorld().contains(player.getUUID())) {
                SAVED_DATA.incrementPlayersInWorld();
                SAVED_DATA.getPlayersInWorld().add(player.getUUID());
            }

            if (!Bank.hasAccount(player.getUUID())) {
                Bank.createPlayerAccount(player.getUUID());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerEatFood(final PlayerInteractEvent event) {
        final var player = event.getPlayer();
        if (player.getLevel().isClientSide()) {
            return;
        }

        final var hand = event.getHand();
        final var itemInHand = player.getItemInHand(hand);

        if (itemInHand.isEdible()) {
            WorldResourceTracker.removeItemsFromEconomy(itemInHand, 1);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerBreakTool(final PlayerDestroyItemEvent event) {
        final var player = event.getPlayer();
        if (player.getLevel().isClientSide()) {
            return;
        }

        final var item = event.getOriginal();
        WorldResourceTracker.removeItemsFromEconomy(item, 1);
    }

}
