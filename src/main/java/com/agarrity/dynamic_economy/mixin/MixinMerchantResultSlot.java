package com.agarrity.dynamic_economy.mixin;

import com.agarrity.dynamic_economy.common.economy.resources.WorldResourceTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MerchantResultSlot.class)
public abstract class MixinMerchantResultSlot {

    @Shadow @Final private Merchant merchant;

    // Inject for shrinking Offer A
    @Inject(at=@At(value="INVOKE", target="Lnet/minecraft/world/item/trading/Merchant;notifyTrade(Lnet/minecraft/world/item/trading/MerchantOffer;)V"), method="onTake", locals = LocalCapture.CAPTURE_FAILHARD)
    public void onOnTake(Player pPlayer, ItemStack pStack, CallbackInfo ci, MerchantOffer merchantoffer, ItemStack itemstack, ItemStack itemstack1) {
        if (merchant.isClientSide()) {
            return;
        }

        WorldResourceTracker.removeItemsFromEconomy(merchantoffer.getCostA());
        WorldResourceTracker.removeItemsFromEconomy(merchantoffer.getCostB());
        WorldResourceTracker.addItemsToEconomy(merchantoffer.getResult());
    }

}
