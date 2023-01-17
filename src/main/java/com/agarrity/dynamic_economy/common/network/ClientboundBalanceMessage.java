package com.agarrity.dynamic_economy.common.network;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.client.gui.screens.inventory.ICashScreen;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyAmount;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundBalanceMessage implements IMessage {

    private final CurrencyAmount amount;

    public ClientboundBalanceMessage(final CurrencyAmount amount) {
        this.amount = amount;
    }

    public ClientboundBalanceMessage(final FriendlyByteBuf buffer) {
        this.amount = new CurrencyAmount(buffer.readLong());
    }

    @Override
    public void encode(final FriendlyByteBuf buffer) {
        buffer.writeLong(amount.asLong());
    }

    @Override
    public void handle(final Supplier<NetworkEvent.Context> context) {
        DynamicEconomy.LOGGER.debug("Handling balance packet of {}", amount);
        final var screen = Minecraft.getInstance().screen;
        if (screen instanceof final ICashScreen cashScreen) {
            cashScreen.setBalance(amount);
        }
        context.get().setPacketHandled(true);
    }
}
