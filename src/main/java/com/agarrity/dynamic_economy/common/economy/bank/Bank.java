package com.agarrity.dynamic_economy.common.economy.bank;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.init.TriggerInit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class Bank {

    private static BankSavedData SAVED_DATA;

    /**
     * Set the BankSavedData that the Bank should use
     *
     * @param savedData The BankSavedData to use
     */
    public static void setSavedData(@NotNull final BankSavedData savedData) {
        SAVED_DATA = savedData;
    }

    /**
     * Create a new account in the global bank with a balance of zero, provided that one does not already exist
     *
     * @param player The player UUID to create an account for
     */
    public static void createPlayerAccount(@NotNull final UUID player) {
        createPlayerAccount(player, new CurrencyAmount());
    }

    /**
     * Create a new account in the global bank with the given balance, provided that one does not already exist
     *
     * @param player  The player UUID to create an account for
     * @param balance The balance to start the player with
     */
    public static void createPlayerAccount(@NotNull final UUID player, @NotNull final CurrencyAmount balance) {
        DynamicEconomy.LOGGER.debug("Creating account for {}", player);
        SAVED_DATA.addAccount(player, new Account(balance));
    }

    /**
     * Add currency to an account with a given UUID, provided that it exists
     *
     * @param accountUUID   The account UUID to add the currency to
     * @param quantity The amount of currency to add
     */
    public static void addCurrencyToAccount(@NotNull final UUID accountUUID, @NotNull final CurrencyAmount quantity) {
        if (quantity.isZero()) {
            return;
        }

        final var optAccount = SAVED_DATA.getAccount(accountUUID);
        optAccount.ifPresent(
                (Account account) -> {
                    account.addBalance(quantity);
                    SAVED_DATA.setDirty();
                }
        );
    }

    /**
     * Add currency to a player's account, provided that it exists
     *
     * @param player   The account number to add the currency to
     * @param quantity The amount of currency to add
     */
    public static void addCurrencyToAccount(@NotNull final Player player, @NotNull final CurrencyAmount quantity) {
        if (quantity.isZero()) {
            return;
        }

        final var uuid = player.getUUID();
        final var optAccount = SAVED_DATA.getAccount(uuid);
        optAccount.ifPresent(
                (Account account) -> {
                    account.addBalance(quantity);
                    SAVED_DATA.setDirty();
                    TriggerInit.BALANCE_TRIGGER.trigger((ServerPlayer) player, account.getBalance());
                }
        );
    }

    /**
     * Remove currency to a player's account, provided that it exists and the player has sufficient balance
     *
     * @param player   The account number to remove the currency from
     * @param quantity The amount of currency to try to remove
     * @return true if the amount was removed successfully, or false if the player doesn't have an account or has insufficient balance
     */
    public static boolean removeCurrencyFromAccount(final @NotNull UUID player, final @NotNull CurrencyAmount quantity) {
        if (quantity.isZero()) {
            return true;
        }

        var optAccount = SAVED_DATA.getAccount(player);
        if (optAccount.isEmpty()) {
            return false;
        }

        final var account = optAccount.get();
        if (!account.checkBalanceLargeEnough(quantity)) {
            return false;
        }

        account.subtractBalance(quantity);

        SAVED_DATA.setDirty();
        return true;
    }

    /**
     * Transfers an amount of currency from account A to account B
     *
     * @param sender   The account to remove the currency from (A)
     * @param receiver The account to add the currency to (B)
     * @param quantity The amount of currency to transfer
     * @return true if the transfer completed successfully, false otherwise
     */
    public static boolean transferCurrencyBetweenAccounts(@NotNull final UUID sender, @NotNull final UUID receiver, @NotNull final CurrencyAmount quantity) {
        final var optSenderAccount = SAVED_DATA.getAccount(sender);
        if (optSenderAccount.isEmpty()) {
            return false;
        }

        final var senderAccount = optSenderAccount.get();
        if (!senderAccount.checkBalanceLargeEnough(quantity)) {
            return false;
        }

        final var optReceiverAccount = SAVED_DATA.getAccount(receiver);
        if (optReceiverAccount.isEmpty()) {
            return false;
        }

        final var receiverAccount = optReceiverAccount.get();
        senderAccount.subtractBalance(quantity);
        receiverAccount.addBalance(quantity);

        SAVED_DATA.setDirty();
        return true;
    }

    /**
     * Get the balance of a given account
     *
     * @param player The player UUID to query the account of
     * @return An empty Optional if the account doesn't exist, otherwise a new CurrencyAmount containing the balance
     */
    public static Optional<CurrencyAmount> getAccountBalance(@NotNull final UUID player) {
        DynamicEconomy.LOGGER.debug("Querying account balance of {}", player);

        final var optAccount = SAVED_DATA.getAccount(player);

        if (optAccount.isEmpty()) {
            return Optional.empty();
        }

        final var account = optAccount.get();
        return Optional.of(account.getBalance());
    }

    /**
     * Get whether a player UUID has an account
     *
     * @param player The player UUID to query the account of
     * @return true if the UUID has an account assigned, false otherwise
     */
    public static boolean hasAccount(@NotNull final UUID player) {
        return SAVED_DATA.getAccount(player).isPresent();
    }

    public static boolean balanceIsSufficient(@NotNull final UUID player, @NotNull final CurrencyAmount amount) {
        if (!hasAccount(player)) {
            return false;
        }

        return SAVED_DATA.getAccount(player).orElse(new Account()).checkBalanceLargeEnough(amount);
    }

}
