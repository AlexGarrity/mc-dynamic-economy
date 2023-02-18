package com.agarrity.dynamic_economy.common.world.inventory;

import com.agarrity.dynamic_economy.DynamicEconomy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class OwnedItemStack {
    public static final OwnedItemStack EMPTY = new OwnedItemStack();
    private final ItemStack itemStack;
    private final UUID seller;

    public OwnedItemStack() {
        this(ItemStack.EMPTY, null);
    }

    public OwnedItemStack(ItemStack stack) {
        this(stack, null);
    }

    public OwnedItemStack(ItemStack stack, UUID sellerUUID) {
        this.itemStack = stack;
        this.seller = sellerUUID;
    }

    public OwnedItemStack(final CompoundTag compoundTag) {
        this.itemStack = ItemStack.of(compoundTag);
        this.seller = compoundTag.getUUID("seller");
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

    public UUID getSeller() {
        return this.seller;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public Item getItem() {
        return this.itemStack.getItem();
    }
}
