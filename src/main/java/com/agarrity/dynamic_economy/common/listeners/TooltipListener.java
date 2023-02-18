package com.agarrity.dynamic_economy.common.listeners;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.client.economy.ClientResourceTracker;
import com.agarrity.dynamic_economy.client.gui.screens.inventory.ICashScreen;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyAmount;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DynamicEconomy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TooltipListener {

    /**
     * Called when a player hovers over an item in the inventory
     *
     * @param event The event data
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltip(final ItemTooltipEvent event) {

        if (!(event.getFlags().isAdvanced() || (Minecraft.getInstance().screen instanceof ICashScreen))) {
            return;
        }

        final var itemStack = event.getItemStack();
        final var item = itemStack.getItem();
        final var optValue = CurrencyHelper.getCurrencyValue(itemStack);
        final var isSpecial = CurrencyHelper.isCurrencySpecial(itemStack);

        // Item is commemorative currency
        if (isSpecial) {
            final var textSingle = new TranslatableComponent("gui.dynamic_economy.tooltip.value");
            textSingle.withStyle(ChatFormatting.WHITE);
            final var textPriceless = new TranslatableComponent("gui.dynamic_economy.tooltip.priceless");
            textPriceless.withStyle(ChatFormatting.DARK_PURPLE);
            textSingle
                    .append(": ")
                    .append(textPriceless);

            event.getToolTip().add(textSingle);
        }

        // Item is some form of currency
        if (optValue.isPresent()) {
            final var textSingle = new TranslatableComponent("gui.dynamic_economy.tooltip.value");
            if (itemStack.getCount() > 1) {
                textSingle.append(" (1)");
            }
            textSingle
                    .append(String.format(": %s", optValue.get()))
                    .withStyle(ChatFormatting.WHITE);
            event.getToolTip().add(textSingle);

            if (itemStack.getCount() > 1) {
                final var textMultiple = new TranslatableComponent("gui.dynamic_economy.tooltip.value");
                textMultiple.append(String.format(" (%d): %s", itemStack.getCount(), new CurrencyAmount(optValue.get().asLong() * itemStack.getCount()))
                ).withStyle(ChatFormatting.WHITE);
                event.getToolTip().add(textMultiple);
            }

        } else {
            if (item.getRegistryName() == null) {
                return;
            }

            final var itemStats = ClientResourceTracker.itemStats.get(item);
            if (itemStats == null) {
                return;
            }

            final var textSingle = new TranslatableComponent("gui.dynamic_economy.tooltip.value");
            if (itemStack.getCount() > 1) {
                textSingle.append(" (1)");
            }
            textSingle.append(String.format(": %s", itemStats.value)
            ).withStyle(ChatFormatting.WHITE);
            event.getToolTip().add(textSingle);

            if (itemStack.getCount() > 1) {
                final var textMultiple = new TranslatableComponent("gui.dynamic_economy.tooltip.value");
                textMultiple.append(String.format(" (%d): %s", itemStack.getCount(), new CurrencyAmount(itemStats.value.asLong() * itemStack.getCount()))
                ).withStyle(ChatFormatting.WHITE);
                event.getToolTip().add(textMultiple);
            }
        }
    }

}
