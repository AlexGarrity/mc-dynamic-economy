package com.agarrity.dynamic_economy.common.economy.bank;

import com.agarrity.dynamic_economy.DynamicEconomy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class BankSavedData extends SavedData {

    private static final String ACCOUNTS_TAG_NAME = "accounts";
    private final HashMap<UUID, Account> accounts;

    public BankSavedData() {
        accounts = new HashMap<>();
    }

    public static BankSavedData load(final CompoundTag rootTag) {
        DynamicEconomy.LOGGER.debug("Loading bank saved data");

        final var bankData = create();

        final var accountsTag = rootTag.getCompound(ACCOUNTS_TAG_NAME);
        for (final var key : accountsTag.getAllKeys()) {
            final var balance = accountsTag.getInt(key);
            final var currencyAmount = new CurrencyAmount(balance);
            bankData.accounts.put(UUID.fromString(key), new Account(currencyAmount));
        }

        return bankData;
    }

    public static BankSavedData create() {
        DynamicEconomy.LOGGER.debug("Creating bank saved data");
        return new BankSavedData();
    }

    @Override
    public @NotNull CompoundTag save(@NotNull final CompoundTag pCompoundTag) {
        DynamicEconomy.LOGGER.debug("Saving bank saved data");

        final var accountsTag = new CompoundTag();

        for (final var account : accounts.keySet()) {
            final var balance = accounts.get(account).getBalance().asLong();
            accountsTag.putLong(account.toString(), balance);
        }

        pCompoundTag.put(ACCOUNTS_TAG_NAME, accountsTag);
        return pCompoundTag;
    }

    /**
     * Add an account to the bank saved data, provided that it does not already exist
     * @param uuid The UUID of the account
     * @param account The account data
     */
    public void addAccount(final UUID uuid, final Account account) {
        if (!accounts.containsKey(uuid)) {
            accounts.put(uuid, account);
            this.setDirty();
        }
    }

    /**
     * Delete an account from the bank saved data
     * @param uuid The UUID of the account to delete
     */
    public void deleteAccount(final UUID uuid) {
        if (accounts.containsKey(uuid)) {
            accounts.remove(uuid);
            this.setDirty();
        }
    }

    /**
     * Get an account with the specified UUID
     * @param uuid The UUID of the account
     * @return An optional which may contain an Account representing the account, provided that it exists
     */
    public Optional<Account> getAccount(final UUID uuid) {
        if (accounts.containsKey(uuid)) {
            return Optional.of(accounts.get(uuid));
        }
        return Optional.empty();
    }
}
