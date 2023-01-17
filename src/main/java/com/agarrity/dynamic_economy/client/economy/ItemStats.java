package com.agarrity.dynamic_economy.client.economy;

import com.agarrity.dynamic_economy.common.economy.bank.CurrencyAmount;

public class ItemStats {
    public final CurrencyAmount value;
    public final int rarity;

    public ItemStats(CurrencyAmount value, final int rarity) {
        this.rarity = rarity;
        this.value = value;
    }
}
