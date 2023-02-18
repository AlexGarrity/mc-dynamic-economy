package com.agarrity.dynamic_economy.mixin;

import com.agarrity.dynamic_economy.common.economy.resources.WorldResourceTracker;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(HopperBlockEntity.class)
public abstract class MixinHopperBlockEntity {

    @Inject(locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;"), method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/item/ItemEntity;)Z")
    private static void onAddItem(Container pContainer, ItemEntity pItem, CallbackInfoReturnable<Boolean> cir, boolean haveAllItemsBeenPlaced, ItemStack originalItemStack) {
        final var itemCount = pItem.getItem().getCount() - originalItemStack.getCount();
        WorldResourceTracker.addItemsToEconomy(pItem.getItem().getItem(), itemCount);
    }


}
