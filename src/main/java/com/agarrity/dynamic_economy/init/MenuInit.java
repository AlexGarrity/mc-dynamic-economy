package com.agarrity.dynamic_economy.init;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.common.world.inventory.AssessMenu;
import com.agarrity.dynamic_economy.common.world.inventory.BankingMenu;
import com.agarrity.dynamic_economy.common.world.inventory.TradeMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MenuInit {

    public static final DeferredRegister<MenuType<?>> MENUS_REGISTRY = DeferredRegister.create(ForgeRegistries.CONTAINERS, DynamicEconomy.MOD_ID);

    public static final RegistryObject<MenuType<AssessMenu>> ASSESS_MENU = MENUS_REGISTRY.register(
            "assess",
            () -> new MenuType<>(AssessMenu::new)
    );

    public static final RegistryObject<MenuType<TradeMenu>> TRADER_MENU = MENUS_REGISTRY.register(
            "trade",
            () -> new MenuType<>(TradeMenu::new)
    );

    public static final RegistryObject<MenuType<BankingMenu>> BANKING_MENU = MENUS_REGISTRY.register(
            "banking",
            () -> new MenuType<>(BankingMenu::new)
    );

}
