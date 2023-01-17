package com.agarrity.dynamic_economy.common.world.item;

import com.agarrity.dynamic_economy.init.ItemInit;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CurrencyTab extends CreativeModeTab {

    public CurrencyTab(int index, String label) {
        super(index, label);
    }

    @Override
    public @NotNull ItemStack makeIcon() {
        return new ItemStack(ItemInit.FIXED_CURRENCY.get());
    }

    public static final CurrencyTab instance = new CurrencyTab(CurrencyTab.TABS.length, "currency");
}
