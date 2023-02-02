package com.agarrity.dynamic_economy.common.economy.bag;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.init.ItemInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class CoinBagSavedData extends SavedData {

    private static final String BAGS_TAG_NAME = "bags";
    private final HashMap<UUID, ItemStackHandler> bags;

    public CoinBagSavedData() {
        bags = new HashMap<>();
    }

    public static CoinBagSavedData load(final CompoundTag rootTag) {
        DynamicEconomy.LOGGER.debug("Loading bag saved data");

        final var bagData = create();

        final var bagsTag = rootTag.getCompound(BAGS_TAG_NAME);
        for (final var key : bagsTag.getAllKeys()) {
            final var inventory = new ItemStackHandler(27);
            inventory.deserializeNBT(bagsTag.getCompound(key));
            bagData.bags.put(UUID.fromString(key), inventory);
        }

        return bagData;
    }

    public static CoinBagSavedData create() {
        DynamicEconomy.LOGGER.debug("Creating bag saved data");
        return new CoinBagSavedData();
    }

    @Override
    public @NotNull CompoundTag save(@NotNull final CompoundTag pCompoundTag) {
        DynamicEconomy.LOGGER.debug("Saving bag saved data");

        final var bagsTag = new CompoundTag();

        for (final var bag : bags.keySet()) {
            final var inventory = bags.get(bag);
            final var tag = inventory.serializeNBT();
            bagsTag.put(bag.toString(), tag);
        }

        pCompoundTag.put(BAGS_TAG_NAME, bagsTag);
        return pCompoundTag;
    }

    public ItemStackHandler getBag(UUID uuid) {
        if (!bags.containsKey(uuid)) {
            final var handler = new ItemStackHandler(27) {
                @Override
                public void setStackInSlot(int slot, @NotNull ItemStack stack) {
                    if (!stack.is(ItemInit.CURRENCY_TAG)) {
                        return;
                    }
                    super.setStackInSlot(slot, stack);
                }

                @NotNull
                @Override
                public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                    if (!stack.is(ItemInit.CURRENCY_TAG)) {
                        return stack;
                    }
                    return super.insertItem(slot, stack, simulate);
                }
            };

            bags.put(uuid, handler);
        }

        this.setDirty();
        return bags.get(uuid);
    }

    public static CoinBagSavedData instance;
}
