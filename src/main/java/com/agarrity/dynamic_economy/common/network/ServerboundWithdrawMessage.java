package com.agarrity.dynamic_economy.common.network;

import com.agarrity.dynamic_economy.common.economy.bank.CurrencyAmount;
import com.agarrity.dynamic_economy.common.world.inventory.BankingMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundWithdrawMessage {
    public final CurrencyAmount value;

    public ServerboundWithdrawMessage(final CurrencyAmount value) {
        this.value = value;
    }

    public ServerboundWithdrawMessage(final FriendlyByteBuf buffer) {
        this.value = new CurrencyAmount(buffer.readLong());
    }

    public void encode(final FriendlyByteBuf buffer) {
        buffer.writeLong(value.asLong());
    }

    public void handle(final Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
                    DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
                        final var serverPlayer = context.get().getSender();
                        if (serverPlayer.containerMenu instanceof final BankingMenu bankingMenu) {
                            bankingMenu.doWithdrawal(this.value);
                        }
                    });

                    context.get().setPacketHandled(true);
                }
        );
    }
}
