package com.agarrity.dynamic_economy.common.network;

import com.agarrity.dynamic_economy.common.world.inventory.BankingMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundDepositMessage implements IMessage {
    public ServerboundDepositMessage() {
    }

    public ServerboundDepositMessage(final FriendlyByteBuf buffer) {
    }

    public void encode(final FriendlyByteBuf buffer) {

    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
                    DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
                        final var serverPlayer = context.get().getSender();
                        if (serverPlayer.containerMenu instanceof final BankingMenu bankingMenu) {
                            bankingMenu.doDeposit();
                        }
                    });

                    context.get().setPacketHandled(true);
                }
        );
    }
}
