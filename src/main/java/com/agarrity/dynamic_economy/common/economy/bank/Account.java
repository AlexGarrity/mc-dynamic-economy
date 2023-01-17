package com.agarrity.dynamic_economy.common.economy.bank;

import org.jetbrains.annotations.NotNull;

public class Account {

    private final CurrencyAmount balance;

    /**
     * Create an account with a balance of zero
     */
    public Account() {
        this.balance = new CurrencyAmount();
    }

    /**
     * Create an account with the specified balance
     * @param balance The balance to give to the account
     */
    public Account(@NotNull final CurrencyAmount balance) {
        this.balance = balance;
    }

    /**
     * Adds an amount of currency to this account's balance
     * @param amount The amount of currency to add
     */
    public void addBalance(@NotNull final CurrencyAmount amount) {
        balance.add(amount);
    }

    /**
     * Subtracts an amount of currency from this account's balance.
     * If the account does not have sufficient balance, it will subtract as much as possible down to 0.0
     * @param amount The amount of currency to subtract
     */
    public void subtractBalance(@NotNull final CurrencyAmount amount) {
        balance.subtract(amount);
    }

    /**
     * Check if a balance is greater than or equal to an amount of currency
     * @param amount The amount of currency to compare the balance to
     * @return true if the balance is greater than or equal to the amount, false otherwise
     */
    public boolean checkBalanceLargeEnough(@NotNull final CurrencyAmount amount) {
        return balance.isGreaterThan(amount) || balance.isEqualTo(amount);
    }

    /**
     * Get a new copy of the balance, which will not modify the account's balance if changed
     * @return A copy of the account's balance
     */
    public CurrencyAmount getBalance() {
        return new CurrencyAmount(balance);
    }

}
