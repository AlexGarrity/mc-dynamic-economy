package com.agarrity.dynamic_economy.common.world.entity.npc;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientSideTrader implements ITrader, InventoryCarrier {
    private final Player source;
    private final SimpleContainer inventory = new SimpleContainer(12);


    public ClientSideTrader(final Player player) {
        this.source = player;
    }

    @Override
    public void setTradingPlayer(@Nullable Player pTradingPlayer) {

    }

    @Override
    public Player getTradingPlayer() {
        return this.source;
    }

    @Override
    public boolean isClientSide() {
        return this.source.level.isClientSide;
    }

    @Override
    public void openTradingScreen(@NotNull final Player pPlayer, @NotNull final Component pDisplayName) {

    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_TRADE;
    }

    @Override
    public Container getInventory() {
        return inventory;
    }
}