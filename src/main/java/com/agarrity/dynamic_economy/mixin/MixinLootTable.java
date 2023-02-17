package com.agarrity.dynamic_economy.mixin;

import com.agarrity.dynamic_economy.common.economy.resources.WorldResourceTracker;
import net.minecraft.world.Container;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootTable.class)
public class MixinLootTable {

    @Inject(at=@At("TAIL"), method = "fill")
    public void onFillContainer(Container pContainer, LootContext pContext, CallbackInfo ci) {
        final var slotCount = pContainer.getContainerSize();
        for (var i = 0; i < slotCount; ++i) {
            final var slot = pContainer.getItem(i);
            WorldResourceTracker.addItemsToEconomy(slot);
        }
    }

}
