package com.agarrity.dynamic_economy.common.economy.bank;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.DynamicEconomyConfig;
import com.agarrity.dynamic_economy.init.ItemInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class CurrencyHelper {

    private static final int MAX_CURRENCY_ITEMS_PER_TRANSACTION = 5;
    private static CurrencyAmount SMALLEST_CURRENCY = new CurrencyAmount(1);
    private static List<Integer> AVAILABLE_CURRENCY_SIZES;

    public static void setAvailableCurrencySizes(List<Integer> currencySizes) {
        AVAILABLE_CURRENCY_SIZES = currencySizes;
        AVAILABLE_CURRENCY_SIZES.sort((a, b) -> -a.compareTo(b));
        SMALLEST_CURRENCY = new CurrencyAmount(AVAILABLE_CURRENCY_SIZES.get(AVAILABLE_CURRENCY_SIZES.size() - 1));
    }

    public static List<Integer> getAvailableCurrencySizes() {
        return AVAILABLE_CURRENCY_SIZES;
    }

    private static CompoundTag createValueTag(long value) {
        final var tag = new CompoundTag();
        tag.putLong("value", value);
        return tag;
    }

    /**
     * Calculate the amount of currency required to make up an amount of money, based on the available currency sizes
     * @param amount The amount of currency to mint
     * @return An ordered set containing ItemStacks representing the fixed-value currency, in value descending order
     */
    public static Set<ItemStack> calculateCurrencyRequired(final CurrencyAmount amount) {
        DynamicEconomy.LOGGER.debug("Creating currency to the amount of {}", amount);
        final var currency = new OrderedHashSet<ItemStack>();

        // Don't create any currency for an amount of 0
        if (amount.isZero()) {
            return currency;
        }

        // If the amount is over the credit note threshold, create a credit note instead
        if (amount.isGreaterThan(new CurrencyAmount(DynamicEconomyConfig.CREDIT_NOTE_THRESHOLD.get()))) {
            final var stack = createCreditNote(amount);
            stack.ifPresent(currency::add);
            return currency;
        }

        var mutAmount = new CurrencyAmount(amount);
        for (final var coinValue : AVAILABLE_CURRENCY_SIZES) {
            // We have only one slot left, but we're on the smallest currency
            final boolean flag1 = (coinValue == SMALLEST_CURRENCY.asLong());
            // We have only one slot left, but the remainder is a multiple of the coin value
            final boolean flag2 = (coinValue % SMALLEST_CURRENCY.asLong() == 0);
            if (currency.size() == (MAX_CURRENCY_ITEMS_PER_TRANSACTION - 1)) {
                if (!(flag1 || flag2)) {
                    continue;
                }
            }

            // Add items whilst the amount required is larger than the size of the coin
            int total = 0;
            while (mutAmount.asLong() >= coinValue) {
                mutAmount.subtract(new CurrencyAmount(coinValue));
                ++total;
            }

            // Return a credit note if we need more than 64 of a currency item
            if (total > 64) {
                final var optCreditNote = createCreditNote(amount);
                currency.clear();
                optCreditNote.ifPresent(currency::add);
                return currency;
            }

            // If we have any coins of this type, add them
            if (total > 0) {
                final var stack = new ItemStack(ItemInit.FIXED_CURRENCY.get(), total);
                stack.setTag(createValueTag(coinValue));
                currency.add(stack);
                DynamicEconomy.LOGGER.debug("Issuing {} of {} value currency", stack.getCount(), coinValue);
            }
        }

        return currency;
    }

    /**
     * Create a credit note with the specified balance
     * @param amount The value of the credit note to create
     * @return An optional that will contain the ItemStack representing the Credit Note
     */
    public static Optional<ItemStack> createCreditNote(final CurrencyAmount amount) {
        final var itemStack = new ItemStack(ItemInit.DYNAMIC_CURRENCY.get(), 1);
        itemStack.setTag(createValueTag(amount.asLong()));

        return Optional.of(itemStack);
    }

    /**
     * Create a credit note, checking that the account to create it for has sufficient balance and then removing the
     * amount required to create the note
     * @param amount The value of the credit note to create
     * @param account The account to remove money from
     * @return An optional which may contain an ItemStack representing the credit note, if the account exists and had
     * sufficient balance
     */
    public static Optional<ItemStack> createCreditNote(final CurrencyAmount amount, final UUID account) {
        if (Bank.hasAccount(account)) {
            return Optional.empty();
        }

        if (!Bank.removeCurrencyFromAccount(account, amount)) {
            return Optional.empty();
        }

        final var itemStack = new ItemStack(ItemInit.DYNAMIC_CURRENCY.get(), 1);
        itemStack.setTag(createValueTag(amount.asLong()));

        return Optional.of(itemStack);
    }

    public static boolean isCurrencySpecial(@NotNull final ItemStack stack) {
        if (!(stack.getItem() == ItemInit.FIXED_CURRENCY.get() || stack.getItem() == ItemInit.DYNAMIC_CURRENCY.get())) {
            return false;
        }

        final var tag = stack.getTag();
        if (tag == null) {
            return false;
        }

        if (!tag.contains("special")) {
            return false;
        }

        return tag.getBoolean("special");
    }

    public static Optional<CurrencyAmount> getCurrencyValue(@NotNull final ItemStack stack) {
        if (!(stack.getItem() == ItemInit.FIXED_CURRENCY.get() || stack.getItem() == ItemInit.DYNAMIC_CURRENCY.get())) {
            return Optional.empty();
        }

        final var tag = stack.getTag();
        if (tag == null) {
            return Optional.empty();
        }

        if (!tag.contains("value")) {
            return Optional.empty();
        }

        final var value = tag.getLong("value");
        return Optional.of(new CurrencyAmount(value));
    }

    public static Optional<CurrencyAmount> getStackValue(@NotNull final ItemStack stack) {
        final var optValue = getCurrencyValue(stack);
        return optValue.map(currencyAmount -> new CurrencyAmount(currencyAmount.asLong() * stack.getCount()));
    }

    public static CurrencyAmount getLargestCurrency() {
        return new CurrencyAmount(AVAILABLE_CURRENCY_SIZES.get(AVAILABLE_CURRENCY_SIZES.size() - 1));
    }

}
