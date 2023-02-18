package com.agarrity.dynamic_economy.mixin;

import com.agarrity.dynamic_economy.common.economy.resources.WorldResourceTracker;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DefaultDispenseItemBehavior.class)
public class MixinDefaultDispenseItemBehavior {

    @Inject(at=@At("TAIL"), method="spawnItem")
    private static void onSpawnItem(Level pLevel, ItemStack pStack, int pSpeed, Direction pFacing, Position pPosition, CallbackInfo ci) {
        WorldResourceTracker.removeItemsFromEconomy(pStack.getItem(), 1);
    }

}
