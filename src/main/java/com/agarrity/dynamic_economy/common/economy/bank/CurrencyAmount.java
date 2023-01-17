package com.agarrity.dynamic_economy.common.economy.bank;

import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

public class CurrencyAmount {
    private int whole;
    private int decimal;

    private static final TranslatableComponent CURRENCY_SYMBOL_MAJOR = new TranslatableComponent("gui.dynamic_economy.symbol.currency_major");
    private static final TranslatableComponent CURRENCY_SYMBOL_MINOR = new TranslatableComponent("gui.dynamic_economy.symbol.currency_minor");

    /**
     * Construct a new CurrencyAmount with a value of 0.0
     */
    public CurrencyAmount() {
        whole = 0;
        decimal = 0;
    }

    /**
     * Construct a new CurrencyAmount using integer xxxxxyy where the whole part is xxxxx and the decimal part is yy
     *
     * @param value The value to construct the CurrencyAmount with
     */
    public CurrencyAmount(long value) {
        if (value < 0) {
            value = 0;
        }

        whole = (int) (value / 100);
        decimal = (int) (value % 100);
    }

    /**
     * Construct a new CurrencyAmount by cloning an existing CurrencyAmount
     *
     * @param amount The CurrencyAmount to copy the values of
     */
    public CurrencyAmount(@NotNull CurrencyAmount amount) {
        whole = amount.getWhole();
        decimal = amount.getDecimal();
    }

    /**
     * Construct a new CurrencyAmount, specifying the whole and decimal component.
     * If the given decimal part is > 100, the overflow is automatically applied to the whole component.
     *
     * @param whole   The whole component of the amount
     * @param decimal The decimal component of the amount
     */
    public CurrencyAmount(int whole, int decimal) {
        final var decimalOverflow = (decimal > 100)? decimal / 100 : 0;
        this.whole = Math.max(whole + decimalOverflow, 0);
        this.decimal = Math.max(decimal % 100, 0);

    }

    /**
     * Add a CurrencyAmount to this CurrencyAmount
     *
     * @param quantity The CurrencyAmount to add
     */
    public void add(@NotNull CurrencyAmount quantity) {
        whole += quantity.getWhole();
        decimal += quantity.getDecimal();
    }

    /**
     * Subtract a CurrencyAmount from this CurrencyAmount, down to a minimum of 0.0
     *
     * @param quantity The CurrencyAmount to subtract
     */
    public void subtract(@NotNull CurrencyAmount quantity) {
        whole = Math.max(whole - quantity.getWhole(), 0);
        decimal = Math.max(decimal - quantity.getDecimal(), 0);
    }

    /**
     * Get the whole component of the CurrencyAmount
     */
    public int getWhole() {
        return whole;
    }

    /**
     * Set the whole component of the CurrencyAmount, provided that the given value is greater than zero
     *
     * @param value The value to set the whole component to
     */
    public void setWhole(int value) {
        if (value < 0) {
            return;
        }

        whole = value;
    }

    /**
     * Get the decimal component of the CurrencyAmount
     */
    public int getDecimal() {
        return decimal;
    }

    /**
     * Set the decimal component of the CurrencyAmount, profivided that the given value is greater than zero.
     * If the value is greater than 100, the overflow is automatically added to the whole component.
     *
     * @param value The value to add to the decimal component
     */
    public void setDecimal(int value) {
        if (decimal < 0) {
            return;
        }

        if (decimal > 100) {
            decimal = value;
        } else {
            final var overflow = value / 100;
            decimal = value % 100;
            whole += overflow;
        }
    }

    /**
     * Check if this CurrencyAmount is greater than another CurrencyAmount
     *
     * @param amount The CurrencyAmount to compare this one to
     * @return true if this is explicitly larger (this > amount), false otherwise
     */
    public boolean isGreaterThan(@NotNull CurrencyAmount amount) {
        if (amount.getWhole() > whole) {
            return false;
        }

        if (amount.getWhole() == whole) {
            return amount.getDecimal() <= decimal;
        }

        return true;
    }

    /**
     * Check if this CurrencyAmount is lesser than another CurrencyAmount
     *
     * @param amount The CurrencyAmount to compare this one to
     * @return true if this is explicitly less than (this < amount), false otherwise
     */
    public boolean isLessThan(@NotNull CurrencyAmount amount) {
        if (amount.getWhole() > whole) {
            return true;
        }

        if (amount.getWhole() == whole) {
            return amount.getDecimal() > decimal;
        }

        return false;
    }

    /**
     * Check if this CurrencyAmount is equal to another CurrencyAmount
     *
     * @param amount The CurrencyAmount to compare this one to
     * @return true if the two amounts are the same, false otherwise
     */
    public boolean isEqualTo(@NotNull CurrencyAmount amount) {
        return (amount.getWhole() == whole) && (amount.getDecimal() == decimal);
    }

    /**
     * Check if this CurrencyAmount is equal to 0.0
     *
     * @return true if this represents 0.0, false otherwise
     */
    public boolean isZero() {
        return (whole == 0) && (decimal == 0);
    }

    /**
     * Get the currency amount in long format (ie. 12.34 becomes 1234)
     * @return A long representing the amount
     */
    public long asLong() {
        return ((long) whole * 100) + (long) decimal;
    }

    /**
     * Print the currency amount in standard currency format
     * @return A string representing the amount
     */
    @Override
    public String toString() {
        switch ((((whole > 0) ? 0x01 : 0x00) + ((decimal > 0) ? 0x02 : 0x00))) {
            case 0x00 -> {
                return String.format("%s%d", CURRENCY_SYMBOL_MAJOR.getString(), 0);
            }
            case 0x01 -> {
                return String.format("%s%d", CURRENCY_SYMBOL_MAJOR.getString(), whole);
            }
            case 0x02 -> {
                return String.format("%d%s", decimal, CURRENCY_SYMBOL_MINOR.getString());
            }
            default -> {
                return String.format("%s%d.%02d", CURRENCY_SYMBOL_MAJOR.getString(), whole, decimal);
            }
        }
    }
}
