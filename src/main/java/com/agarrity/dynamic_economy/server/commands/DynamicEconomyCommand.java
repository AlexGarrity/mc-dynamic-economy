package com.agarrity.dynamic_economy.server.commands;

import com.agarrity.dynamic_economy.common.economy.bank.Bank;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyAmount;
import com.agarrity.dynamic_economy.common.economy.resources.WorldResourceTracker;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DynamicEconomyCommand {

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("dynamiceconomy").requires(
                (commandSourceStack) -> commandSourceStack.hasPermission(2)
                )
                .then(Commands.literal("resource")
                        .then(Commands.literal("set").then(Commands.argument("item", ItemArgument.item()).then(Commands.argument("count", IntegerArgumentType.integer(0)).executes((commandContext) -> setResourceCount(commandContext.getSource(), ItemArgument.getItem(commandContext, "item"), IntegerArgumentType.getInteger(commandContext, "count"))))))
                        .then(Commands.literal("get").then(Commands.argument("item", ItemArgument.item()).executes((commandContext) -> getResourceCount(commandContext.getSource(), ItemArgument.getItem(commandContext, "item")))))
                        .then(Commands.literal("modify").then(Commands.argument("item", ItemArgument.item()).then(Commands.argument("count", IntegerArgumentType.integer()).executes(((context) -> modifyResourceCount(context.getSource(), ItemArgument.getItem(context, "item"), IntegerArgumentType.getInteger(context, "count"))))))))
                .then(Commands.literal("bank")
                        .then(Commands.literal("get").then(Commands.argument("player", EntityArgument.player()).executes((commandContext) -> getBankBalance(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "player")))))
                        .then(Commands.literal("pay").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("amount", FloatArgumentType.floatArg(0.01F)).executes((context) -> payOtherPlayer(context.getSource(), EntityArgument.getPlayer(context, "player"), FloatArgumentType.getFloat(context, "amount"))))))
                ));
    }

    private static int setResourceCount(CommandSourceStack commandSourceStack, ItemInput item, int count) {
        WorldResourceTracker.setItemsInEconomy(item.getItem(), count);
        final var itemName = item.getItem().getDescriptionId();
        commandSourceStack.sendSuccess(new TranslatableComponent("command.dynamic_economy.resource.set", itemName, count), true);
        return count;
    }

    private static int getResourceCount(CommandSourceStack commandSourceStack, ItemInput item) {
        final var itemName = item.getItem().getDescriptionId();

        if (!WorldResourceTracker.itemExistsInEconomy(item.getItem())) {
            commandSourceStack.sendSuccess(new TranslatableComponent("command.dynamic_economy.resource.get.none", itemName), false);
            return 0;
        }

        final var count = WorldResourceTracker.getItemsInEconomy(item.getItem());
        final var optRarity = WorldResourceTracker.getItemFrequency(new ItemStack(item.getItem()));
        final var optValue = WorldResourceTracker.estimateItemsValue(new ItemStack(item.getItem()));
        commandSourceStack.sendSuccess(
                new TranslatableComponent(
                        "command.dynamic_economy.resource.get",
                        count,
                        itemName,
                        (optRarity.isPresent()? optRarity.get() : new TranslatableComponent("gui.dynamic_economy.assess.rarity.unknown")),
                        (optValue.isPresent()? optValue.get() : new TranslatableComponent("gui.dynamic_economy.assess.rarity.unknown"))
                ), false
        );
        return count;
    }

    private static int modifyResourceCount(CommandSourceStack commandSourceStack, ItemInput item, int count) {
        if (count < 0) {
            WorldResourceTracker.removeItemsFromEconomy(item.getItem(), count);
        }
        else {
            WorldResourceTracker.addItemsToEconomy(item.getItem(), count);
        }

        return getResourceCount(commandSourceStack, item);
    }

    private static int getBankBalance(CommandSourceStack commandSourceStack, Player player) {
        final var balance = Bank.getAccountBalance(player.getUUID());
        commandSourceStack.sendSuccess(new TranslatableComponent("command.dynamic_economy.bank.get", player.getDisplayName(), balance.orElse(new CurrencyAmount()).toString()), false);
        return (int) balance.orElse(new CurrencyAmount()).asLong();
    }

    private static int payOtherPlayer(CommandSourceStack commandSourceStack, Player player, float amount) {
        final var longAmount = (long) (amount * 100.0F);
        final var currencyAmount = new CurrencyAmount(longAmount);
        try {
            if (player.getUUID().equals(commandSourceStack.getPlayerOrException().getUUID())) {
                commandSourceStack.sendFailure(new TranslatableComponent("command.dynamic_economy.bank.pay.self_pay"));
                return 0;
            }

            if (Bank.transferCurrencyBetweenAccounts(commandSourceStack.getPlayerOrException().getUUID(), player.getUUID(), currencyAmount)) {
                commandSourceStack.sendSuccess(new TranslatableComponent("command.dynamic_economy.bank.pay", currencyAmount.toString(), player.getDisplayName()), true);
                return (int) longAmount;
            }
            else {
                commandSourceStack.sendFailure(new TranslatableComponent("command.dynamic_economy.bank.pay.insufficient_balance"));
                return 0;
            }
        }
        catch (CommandSyntaxException exception) {
            commandSourceStack.sendFailure(new TranslatableComponent("command.failed"));
        }
        return 0;
    }

}
