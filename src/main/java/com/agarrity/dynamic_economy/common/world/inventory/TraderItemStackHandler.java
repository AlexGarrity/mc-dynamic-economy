package com.agarrity.dynamic_economy.common.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.UUID;


public class TraderItemStackHandler implements IItemHandler, IItemHandlerModifiable, INBTSerializable<CompoundTag> {

    protected NonNullList<OwnedItemStack> stacks;

    public TraderItemStackHandler() {
        this(1);
    }

    public TraderItemStackHandler(int size) {
        stacks = NonNullList.withSize(size, OwnedItemStack.EMPTY);
    }

    public void setSize(int size) {
        stacks = NonNullList.withSize(size, OwnedItemStack.EMPTY);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        validateSlotIndex(slot);
        this.stacks.set(slot, new OwnedItemStack(stack));
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return this.stacks.get(slot).getItemStack();
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty())
            return ItemStack.EMPTY;

        if (!isItemValid(slot, stack))
            return stack;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot).getItemStack();

        int limit = getStackLimit(slot, stack);

        if (!existing.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
                return stack;

            limit -= existing.getCount();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                this.stacks.set(slot, reachedLimit ? new OwnedItemStack(ItemHandlerHelper.copyStackWithSize(stack, limit)) : new OwnedItemStack(stack));
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0)
            return ItemStack.EMPTY;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot).getItemStack();

        if (existing.isEmpty())
            return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                this.stacks.set(slot, OwnedItemStack.EMPTY);
                return existing;
            } else {
                return existing.copy();
            }
        } else {
            if (!simulate) {
                this.stacks.set(slot, new OwnedItemStack(ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract)));
            }

            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public CompoundTag serializeNBT() {
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                stacks.get(i).save(itemTag);
                nbtTagList.add(itemTag);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", stacks.size());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        setSize(nbt.contains("Size", Tag.TAG_INT) ? nbt.getInt("Size") : stacks.size());
        ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            if (slot >= 0 && slot < stacks.size()) {
                stacks.set(slot, OwnedItemStack.of(itemTags));
            }
        }
    }

    public UUID getSellerOfSlot(final int index) {
        validateSlotIndex(index);
        return stacks.get(index).getSeller();
    }

    public void setSellerOfSlot(final int index, UUID sellerUUID) {
        validateSlotIndex(index);
        stacks.set(index, new OwnedItemStack(stacks.get(index).getItemStack(), sellerUUID));
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= stacks.size())
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
    }

}
