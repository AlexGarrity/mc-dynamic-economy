package com.agarrity.dynamic_economy.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public interface IMessage {

    void encode(final FriendlyByteBuf buffer);
    void handle(final Supplier<NetworkEvent.Context> context);


}
