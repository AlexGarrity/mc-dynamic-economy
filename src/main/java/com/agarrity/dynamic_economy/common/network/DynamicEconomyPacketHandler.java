package com.agarrity.dynamic_economy.common.network;

import com.agarrity.dynamic_economy.DynamicEconomy;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class DynamicEconomyPacketHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(DynamicEconomy.MOD_ID, "general_channel"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registerPackets() {
        int messageId = 0;

        INSTANCE.registerMessage(
                messageId++,
                ServerboundWithdrawMessage.class,
                ServerboundWithdrawMessage::encode,
                ServerboundWithdrawMessage::new,
                ServerboundWithdrawMessage::handle
        );

        INSTANCE.registerMessage(
                messageId++,
                ServerboundDepositMessage.class,
                ServerboundDepositMessage::encode,
                ServerboundDepositMessage::new,
                ServerboundDepositMessage::handle
        );

        INSTANCE.registerMessage(
                messageId++,
                ClientboundItemValueMessage.class,
                ClientboundItemValueMessage::encode,
                ClientboundItemValueMessage::new,
                ClientboundItemValueMessage::handle
        );

        INSTANCE.registerMessage(
                messageId++,
                ServerboundItemValueMessage.class,
                ServerboundItemValueMessage::encode,
                ServerboundItemValueMessage::new,
                ServerboundItemValueMessage::handle
        );

        INSTANCE.registerMessage(
                messageId++,
                ClientboundBalanceMessage.class,
                ClientboundBalanceMessage::encode,
                ClientboundBalanceMessage::new,
                ClientboundBalanceMessage::handle
        );

        INSTANCE.registerMessage(
                messageId++,
                ServerboundBalanceMessage.class,
                ServerboundBalanceMessage::encode,
                ServerboundBalanceMessage::new,
                ServerboundBalanceMessage::handle
        );

        INSTANCE.registerMessage(
                messageId++,
                ClientboundCurrencySizesMessage.class,
                ClientboundCurrencySizesMessage::encode,
                ClientboundCurrencySizesMessage::new,
                ClientboundCurrencySizesMessage::handle
        );
    }

}
