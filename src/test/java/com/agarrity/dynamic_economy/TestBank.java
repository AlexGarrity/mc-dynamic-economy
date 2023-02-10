package com.agarrity.dynamic_economy;

import com.agarrity.dynamic_economy.common.economy.bank.Bank;
import com.agarrity.dynamic_economy.common.economy.bank.BankSavedData;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyAmount;
import net.minecraft.nbt.CompoundTag;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class TestBank {

    final UUID accountUUID1 = UUID.randomUUID();
    final UUID accountUUID2 = UUID.randomUUID();
    final BankSavedData bankSavedData = BankSavedData.create();

    @Before
    public void setupBank() {
        Bank.setSavedData(bankSavedData);
    }

    @Test
    public void createAccounts() {
        Bank.createPlayerAccount(accountUUID1);
        Bank.createPlayerAccount(accountUUID2);

        assertTrue(Bank.hasAccount(accountUUID1));
        assertTrue(Bank.hasAccount(accountUUID2));

        Bank.createPlayerAccount(accountUUID1, new CurrencyAmount(500, 0));

        assertEquals(CurrencyAmount.ZERO, Bank.getAccountBalance(accountUUID1).orElseThrow());
    }

    @Test
    public void addCurrencyToAccounts() {
        final var INITIAL_VALUE_A = new CurrencyAmount(1, 50);
        final var INITIAL_VALUE_B = new CurrencyAmount(2, 50);

        Bank.createPlayerAccount(accountUUID1);
        Bank.createPlayerAccount(accountUUID2);

        Bank.addCurrencyToAccount(accountUUID1, INITIAL_VALUE_A);
        Bank.addCurrencyToAccount(accountUUID2, INITIAL_VALUE_B);

        assertEquals(INITIAL_VALUE_A, Bank.getAccountBalance(accountUUID1).orElseThrow());
        assertEquals(INITIAL_VALUE_B, Bank.getAccountBalance(accountUUID2).orElseThrow());
    }

    @Test
    public void removeCurrencyFromAccounts() {
        final var INITIAL_VALUE_A = new CurrencyAmount(1, 50);
        final var INITIAL_VALUE_B = new CurrencyAmount(2, 50);

        final var SUBTRACT_VALUE_A = new CurrencyAmount(0, 60);
        final var SUBTRACT_VALUE_B = new CurrencyAmount(2, 80);

        final var FINAL_VALUE_A = new CurrencyAmount(0, 90);

        Bank.createPlayerAccount(accountUUID1, INITIAL_VALUE_A);
        Bank.createPlayerAccount(accountUUID2, INITIAL_VALUE_B);

        assertTrue(Bank.removeCurrencyFromAccount(accountUUID1, SUBTRACT_VALUE_A));
        assertFalse(Bank.removeCurrencyFromAccount(accountUUID2, SUBTRACT_VALUE_B));

        assertEquals(FINAL_VALUE_A, Bank.getAccountBalance(accountUUID1).orElseThrow());
        assertEquals(INITIAL_VALUE_B, Bank.getAccountBalance(accountUUID2).orElseThrow());
    }

    @Test
    public void balanceIsSufficient() {
        final var INITIAL_BALANCE = new CurrencyAmount(1, 50);
        Bank.createPlayerAccount(accountUUID1, INITIAL_BALANCE);

        assertTrue(Bank.balanceIsSufficient(accountUUID1, new CurrencyAmount(0, 51)));
        assertTrue(Bank.balanceIsSufficient(accountUUID1, new CurrencyAmount(1, 49)));
        assertTrue(Bank.balanceIsSufficient(accountUUID1, new CurrencyAmount(1, 50)));
        assertFalse(Bank.balanceIsSufficient(accountUUID1, new CurrencyAmount(1, 51)));
        assertFalse(Bank.balanceIsSufficient(accountUUID1, new CurrencyAmount(2, 0)));
    }

    @Test
    public void transferBetweenAccounts() {
        final var INITIAL_VALUE_A = new CurrencyAmount(1, 50);
        final var INITIAL_VALUE_B = new CurrencyAmount(2, 50);
        final var TRANSFER_VALUE = new CurrencyAmount(0, 50);
        final var FINAL_VALUE_A = new CurrencyAmount(1, 0);
        final var FINAL_VALUE_B = new CurrencyAmount(3, 0);

        Bank.createPlayerAccount(accountUUID1, INITIAL_VALUE_A);
        Bank.createPlayerAccount(accountUUID2, INITIAL_VALUE_B);

        Bank.transferCurrencyBetweenAccounts(accountUUID1, accountUUID2, TRANSFER_VALUE);

        assertEquals(FINAL_VALUE_A, Bank.getAccountBalance(accountUUID1).orElseThrow());
        assertEquals(FINAL_VALUE_B, Bank.getAccountBalance(accountUUID2).orElseThrow());
    }

    @Test
    public void addRemoveNegativeAmount() {
        final var TARGET_AMOUNT = new CurrencyAmount(1, 0);
        Bank.createPlayerAccount(accountUUID1, TARGET_AMOUNT);

        Bank.addCurrencyToAccount(accountUUID1, new CurrencyAmount(-1, 0));
        Bank.addCurrencyToAccount(accountUUID1, new CurrencyAmount(0, -50));
        Bank.removeCurrencyFromAccount(accountUUID1, new CurrencyAmount(-1, 0));
        Bank.removeCurrencyFromAccount(accountUUID1, new CurrencyAmount(0, -50));

        assertEquals(TARGET_AMOUNT, Bank.getAccountBalance(accountUUID1).orElseThrow());
    }

    @Test
    public void saveBankData() {
        final var INITIAL_VALUE_A = new CurrencyAmount(1, 50);
        final var INITIAL_VALUE_B = new CurrencyAmount(2, 50);

        Bank.createPlayerAccount(accountUUID1, INITIAL_VALUE_A);
        Bank.createPlayerAccount(accountUUID2, INITIAL_VALUE_B);

        final var compoundTag = bankSavedData.save(new CompoundTag());
        final var newBankSavedData = BankSavedData.load(compoundTag);
        Bank.setSavedData(newBankSavedData);

        assertEquals(INITIAL_VALUE_B, Bank.getAccountBalance(accountUUID2).orElseThrow());
        assertEquals(INITIAL_VALUE_A, Bank.getAccountBalance(accountUUID1).orElseThrow());
    }


}


