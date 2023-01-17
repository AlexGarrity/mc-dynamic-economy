package com.agarrity.dynamic_economy.common.network;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.client.gui.screens.inventory.ICashScreen;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyAmount;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClientboundCurrencySizesMessage implements IMessage {
    private final List<Integer> currencySizes;

    public ClientboundCurrencySizesMessage(final List<Integer> currencySizes) {
        this.currencySizes = currencySizes;
    }

    public ClientboundCurrencySizesMessage(final FriendlyByteBuf buffer) {
        final var sizes = buffer.readVarIntArray();
        this.currencySizes = new ArrayList<>();
        for (final var size : sizes) {
            this.currencySizes.add(size);
        }
    }

    @Override
    public void encode(final FriendlyByteBuf buffer) {
        final var values = new int[this.currencySizes.size()];
        for (var i = 0; i < this.currencySizes.size(); ++i) {
            values[i] = this.currencySizes.get(i);
        }
        buffer.writeVarIntArray(values);
    }

    @Override
    public void handle(final Supplier<NetworkEvent.Context> context) {
        CurrencyHelper.setAvailableCurrencySizes(this.currencySizes);
        final var sizesString = new StringBuilder();
        for (final var value : this.currencySizes) {
            sizesString.append(String.format("%d, ", value));
        }
        DynamicEconomy.LOGGER.debug("Received the following currency sizes from the server: {}", sizesString);
        context.get().setPacketHandled(true);
    }
}
