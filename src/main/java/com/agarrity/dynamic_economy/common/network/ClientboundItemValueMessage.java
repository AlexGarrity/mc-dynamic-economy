package com.agarrity.dynamic_economy.common.network;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.client.economy.ItemStats;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyAmount;
import com.agarrity.dynamic_economy.client.economy.ClientResourceTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ClientboundItemValueMessage implements IMessage {

    final String itemName;
    final boolean hasValue;
    final CurrencyAmount value;
    final int rarity;

    public ClientboundItemValueMessage(final Item item, @Nullable final CurrencyAmount value, int rarity) {
        assert(item.getRegistryName() != null);
        this.itemName = item.getRegistryName().toString();

        if (value != null) {
            this.value = value;
            this.hasValue = true;
            this.rarity = rarity;
        }
        else {
            this.value = new CurrencyAmount();
            this.hasValue = false;
            this.rarity = -1;
        }
    }

    public ClientboundItemValueMessage(FriendlyByteBuf buffer) {
        this.itemName = buffer.readUtf();
        this.hasValue = buffer.readBoolean();
        if (hasValue) {
            final var whole = buffer.readInt();
            final var decimal = buffer.readInt();
            this.value = new CurrencyAmount(whole, decimal);
            this.rarity = buffer.readInt();
        } else {
            this.value = new CurrencyAmount();
            this.rarity = -1;
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.itemName);
        buffer.writeBoolean(this.hasValue);
        if (this.value != null) {
            buffer.writeInt(this.value.getWhole());
            buffer.writeInt(this.value.getDecimal());
            buffer.writeInt(this.rarity);
        }
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            final var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(this.itemName));
            if (this.hasValue) {
                final var itemStats = new ItemStats(this.value, this.rarity);
                DynamicEconomy.LOGGER.debug("Received a value of {}, rarity {} for {}", value, rarity, this.itemName);
                ClientResourceTracker.itemStats.put(item, itemStats);
            }
            else {
                ClientResourceTracker.itemStats.remove(item);
            }

            context.get().setPacketHandled(true);
        });
    }


}
