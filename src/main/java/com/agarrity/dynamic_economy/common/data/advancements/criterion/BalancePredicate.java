package com.agarrity.dynamic_economy.common.data.advancements.criterion;

import com.agarrity.dynamic_economy.common.economy.bank.CurrencyAmount;
import java.util.function.Predicate;

public class BalancePredicate implements Predicate<CurrencyAmount> {
    final CurrencyAmount amount;

    public BalancePredicate(final CurrencyAmount amount) {
        this.amount = amount;
    }

    @Override
    public boolean test(final CurrencyAmount currencyAmount) {
        return (this.amount.isGreaterThan(currencyAmount) || this.amount.isEqualTo(currencyAmount));
    }
}