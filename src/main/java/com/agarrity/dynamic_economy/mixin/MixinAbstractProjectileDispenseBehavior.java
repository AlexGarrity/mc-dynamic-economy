package com.agarrity.dynamic_economy.mixin;

import com.agarrity.dynamic_economy.common.economy.resources.WorldResourceTracker;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractProjectileDispenseBehavior.class)
public class MixinAbstractProjectileDispenseBehavior {

    @Inject(at=@At("HEAD"), method="execute")
    private void onExecute(BlockSource pSource, ItemStack pStack, CallbackInfoReturnable<ItemStack> cir) {
        WorldResourceTracker.removeItemsFromEconomy(pStack.getItem(), 1);
    }

}
