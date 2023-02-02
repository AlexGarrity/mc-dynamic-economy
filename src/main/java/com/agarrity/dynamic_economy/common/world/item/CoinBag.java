package com.agarrity.dynamic_economy.common.world.item;

import com.agarrity.dynamic_economy.common.economy.bag.CoinBagSavedData;
import com.agarrity.dynamic_economy.common.world.inventory.CoinBagMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CoinBag extends Item {

    public CoinBag() {
        super(new Properties().tab(CurrencyTab.instance).stacksTo(1).fireResistant());
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player pPlayer, @NotNull InteractionHand pUsedHand) {
        final var itemStack = pPlayer.getItemInHand(pUsedHand);
        if (itemStack.getTag() == null) {
            final var tag = new CompoundTag();
            tag.putUUID("uuid", UUID.randomUUID());
            itemStack.setTag(tag);
        }

        final var uuidTag = itemStack.getTag().getUUID("uuid");

        if (pPlayer instanceof ServerPlayer serverPlayer) {
            final var inventory = CoinBagSavedData.instance.getBag(uuidTag);
            NetworkHooks.openGui(
                    serverPlayer,
                    new SimpleMenuProvider(
                            (id, playerInventory, player) -> new CoinBagMenu(id, playerInventory, inventory),
                            new TranslatableComponent("gui.dynamic_economy.coin_bag.title")
                    )
            );
        }
        return InteractionResultHolder.sidedSuccess(itemStack, pLevel.isClientSide);
    }
}
