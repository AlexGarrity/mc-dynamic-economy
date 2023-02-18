package com.agarrity.dynamic_economy.common.network;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.common.economy.bank.Bank;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundBalanceMessage implements IMessage {

    public ServerboundBalanceMessage() {
    }

    public ServerboundBalanceMessage(final FriendlyByteBuf buffer) {
    }

    @Override
    public void encode(final FriendlyByteBuf buffer) {
    }

    @Override
    public void handle(final Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
            DynamicEconomy.LOGGER.debug("Sending balance back");
            final var sender = context.get().getSender();
            if (sender == null) {
                return;
            }
            final var optBalance = Bank.getAccountBalance(sender.getUUID());
            optBalance.ifPresent(
                    (balance) -> DynamicEconomyPacketHandler.INSTANCE.sendTo(new ClientboundBalanceMessage(balance), sender.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT)
            );
        }));
        context.get().setPacketHandled(true);
    }
}
