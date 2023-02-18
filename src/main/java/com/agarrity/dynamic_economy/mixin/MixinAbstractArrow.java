package com.agarrity.dynamic_economy.mixin;

import com.agarrity.dynamic_economy.common.economy.resources.WorldResourceTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AbstractArrow.class)
public abstract class MixinAbstractArrow {

    @Shadow public AbstractArrow.Pickup pickup;

    @Shadow protected abstract ItemStack getPickupItem();

    @Inject(at=@At(value="TAIL"), method="tryPickup", locals = LocalCapture.CAPTURE_FAILHARD)
    public void onTryPickup(Player pPlayer, CallbackInfoReturnable<Boolean> cir) {
        if (pPlayer.getLevel().isClientSide) {
            return;
        }

        if (this.pickup == AbstractArrow.Pickup.ALLOWED && cir.getReturnValue()) {
            WorldResourceTracker.addItemsToEconomy(this.getPickupItem().getItem(), 1);
        }
    }

}
