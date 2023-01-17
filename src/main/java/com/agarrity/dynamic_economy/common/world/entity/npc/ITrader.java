package com.agarrity.dynamic_economy.common.world.entity.npc;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public interface ITrader {

    void setTradingPlayer(@Nullable final Player pTradingPlayer);

    Player getTradingPlayer();

    boolean isClientSide();

    void openTradingScreen(@NotNull final Player pPlayer, @NotNull final Component pDisplayName);

    SoundEvent getNotifyTradeSound();

}
