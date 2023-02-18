package com.agarrity.dynamic_economy.common.world.inventory;

import com.agarrity.dynamic_economy.DynamicEconomy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class OwnedItemStack {
    public static final OwnedItemStack EMPTY = new OwnedItemStack();
    ItemStack itemStack;
    UUID seller;

    public OwnedItemStack() {
        itemStack = ItemStack.EMPTY;
        seller = null;
    }

    public OwnedItemStack(final CompoundTag compoundTag) {
        itemStack = ItemStack.of(compoundTag);
    }

    public static OwnedItemStack of(CompoundTag compoundTag) {
        try {
            return new OwnedItemStack(compoundTag);
        } catch (RuntimeException runtimeexception) {
            DynamicEconomy.LOGGER.debug("Tried to load invalid item: {}", compoundTag, runtimeexception);
            return EMPTY;
        }
    }

    public void save(final CompoundTag compoundTag) {
        itemStack.save(compoundTag);
        compoundTag.putUUID("seller", seller);
    }

    public boolean isEmpty() {
        return itemStack.isEmpty();
    }
}
