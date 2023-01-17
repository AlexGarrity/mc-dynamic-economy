package com.agarrity.dynamic_economy.common.network;

import com.agarrity.dynamic_economy.common.economy.resources.WorldResourceTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ServerboundItemValueMessage implements IMessage {

    final String itemName;

    public ServerboundItemValueMessage(final String itemName) {
        this.itemName = itemName;
    }

    public ServerboundItemValueMessage(FriendlyByteBuf buffer) {
        itemName = buffer.readUtf();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(itemName);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
            final var sender = context.get().getSender();
            if (sender == null) {
                return;
            }

            final var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
            if (item == null) {
                return;
            }

            final var stack = new ItemStack(item, 1);
            final var optValue = WorldResourceTracker.estimateItemValue(stack);
            final var optRarity = WorldResourceTracker.getItemFrequency(stack);

            DynamicEconomyPacketHandler.INSTANCE.sendTo(
                    new ClientboundItemValueMessage(
                            item,
                            optValue.orElse(null),
                            optRarity.orElse(-1)
                    ),
                    sender.connection.getConnection(),
                    NetworkDirection.PLAY_TO_CLIENT
            );

        }));
        context.get().setPacketHandled(true);
    }
}