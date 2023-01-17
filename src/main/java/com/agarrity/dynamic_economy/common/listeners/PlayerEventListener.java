package com.agarrity.dynamic_economy.common.listeners;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyHelper;
import com.agarrity.dynamic_economy.common.economy.resources.WorldSavedData;
import com.agarrity.dynamic_economy.common.economy.bank.Bank;
import com.agarrity.dynamic_economy.common.network.ClientboundCurrencySizesMessage;
import com.agarrity.dynamic_economy.common.network.DynamicEconomyPacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

@Mod.EventBusSubscriber(modid= DynamicEconomy.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
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

}
