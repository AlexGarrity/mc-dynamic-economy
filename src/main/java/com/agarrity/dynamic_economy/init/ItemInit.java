package com.agarrity.dynamic_economy.init;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyHelper;
import com.agarrity.dynamic_economy.common.world.item.CoinBag;
import com.agarrity.dynamic_economy.common.world.item.CurrencyTab;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class ItemInit {

    public static final DeferredRegister<Item> ITEMS_REGISTRY =
            DeferredRegister.create(ForgeRegistries.ITEMS, DynamicEconomy.MOD_ID);

    public static final TagKey<Item> CURRENCY_TAG =
            ITEMS_REGISTRY.createTagKey(new ResourceLocation(DynamicEconomy.MOD_ID, "currency"));
    public static final RegistryObject<Item> DYNAMIC_CURRENCY = ITEMS_REGISTRY.register(
            "dynamic_currency",
            () -> new Item(new Item.Properties().tab(CurrencyTab.instance).stacksTo(1))
    );
    public static final RegistryObject<Item> COIN_BAG = ITEMS_REGISTRY.register(
            "coin_bag",
            CoinBag::new
    );

    static {
        ITEMS_REGISTRY.register(
                "animal_villager_spawn_egg",
                () -> new ForgeSpawnEggItem(EntityInit.ANIMAL_VILLAGER, 5051507, 12365937, new Item.Properties().tab(CreativeModeTab.TAB_MISC))
        );
        ITEMS_REGISTRY.register(
                "banker_animal_villager_spawn_egg",
                () -> new ForgeSpawnEggItem(EntityInit.BANKER_ANIMAL_VILLAGER, 5251507, 12365937, new Item.Properties().tab(CreativeModeTab.TAB_MISC))
        );
        ITEMS_REGISTRY.register(
                "trader_animal_villager_spawn_egg",
                () -> new ForgeSpawnEggItem(EntityInit.TRADER_ANIMAL_VILLAGER, 5451507, 12365937, new Item.Properties().tab(CreativeModeTab.TAB_MISC))
        );
        ITEMS_REGISTRY.register(
                "assessor_animal_villager_spawn_egg",
                () -> new ForgeSpawnEggItem(EntityInit.ASSESSOR_ANIMAL_VILLAGER, 5651507, 12365937, new Item.Properties().tab(CreativeModeTab.TAB_MISC))
        );
        ITEMS_REGISTRY.register(
                "player_trader_animal_villager_spawn_egg",
                () -> new ForgeSpawnEggItem(EntityInit.PLAYER_TRADER_ANIMAL_VILLAGER, 5851507, 12365937, new Item.Properties().tab(CreativeModeTab.TAB_MISC))
        );
    }

    public static final RegistryObject<Item> FIXED_CURRENCY = ITEMS_REGISTRY.register(
            "fixed_currency",
            () -> new Item(new Item.Properties().tab(CurrencyTab.instance).stacksTo(64).fireResistant()) {
                @Override
                public boolean isFoil(@NotNull ItemStack pStack) {
                    return CurrencyHelper.isCurrencySpecial(pStack);
                }

                @Override
                public int getItemStackLimit(ItemStack stack) {
                    return CurrencyHelper.isCurrencySpecial(stack) ? 1 : 64;
                }
            }
    );


}
