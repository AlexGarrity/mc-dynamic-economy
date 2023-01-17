package com.agarrity.dynamic_economy.common.listeners;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.client.economy.ClientResourceTracker;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyAmount;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyHelper;
import com.agarrity.dynamic_economy.common.economy.resources.WorldResourceTracker;
import com.agarrity.dynamic_economy.common.world.entity.npc.AnimalVillager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DynamicEconomy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InventoryEventListener {
    /**
     * Called when an entity drops items because they died.  In our case, we care about players dropping items or
     * animal villagers dropping items, as these represent "containers" in the economy
     *
     * @param event The event data
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeathDrops(final LivingDropsEvent event) {
        final var entity = event.getEntity();
        if (event.getEntity().getLevel().isClientSide()) {
            return;
        }

        final boolean isPlayerOrVillager = (entity instanceof Player) || (entity instanceof AnimalVillager);
        if (!isPlayerOrVillager) {
            return;
        }

        if (event.getEntity() instanceof Player) {
            if (event.getEntity().getLevel().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                return;
            }
        }

        DynamicEconomy.LOGGER.debug("Drop items event");

        final var dropEntities = event.getDrops();
        for (final var dropEntity : dropEntities) {
            final var itemStack = dropEntity.getItem();
            WorldResourceTracker.removeItemsFromEconomy(itemStack);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onVillagerTrade(VillagerTradesEvent event) {

    }

    /**
     * Called when a player drops an item
     *
     * @param event The event data
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDropItem(final ItemTossEvent event) {
        if (event.getPlayer().getLevel().isClientSide()) {
            return;
        }

        final var itemStack = event.getEntityItem().getItem();
        WorldResourceTracker.removeItemsFromEconomy(itemStack);
    }

    /**
     * Called when a player picks up an item
     *
     * @param event The event data
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPickupItem(final PlayerEvent.ItemPickupEvent event) {
        if (event.getPlayer().getLevel().isClientSide()) {
            return;
        }

        DynamicEconomy.LOGGER.debug("Pickup items event");
        final var item = event.getStack();
        WorldResourceTracker.addItemsToEconomy(item);
    }

    /**
     * Called when a player crafts an item, once for each instance of the item crafted (a stack of 37 calls this 37 times)
     *
     * @param event The event data
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCraftItem(final PlayerEvent.ItemCraftedEvent event) {
        if (event.getPlayer().getLevel().isClientSide()) {
            return;
        }

        final var craftedItem = event.getCrafting();
        final var matrix = event.getInventory();

        final var containerSize = matrix.getContainerSize();
        for (var slotIndex = 0; slotIndex < containerSize; ++slotIndex) {
            final var itemStack = matrix.getItem(slotIndex);
            WorldResourceTracker.removeItemsFromEconomy(itemStack, 1);
        }

        DynamicEconomy.LOGGER.debug("Crafted items event");
        WorldResourceTracker.addItemsToEconomy(craftedItem);
    }

    /**
     * Called when a player smelts and item
     *
     * @param event The event data
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSmeltItem(final PlayerEvent.ItemSmeltedEvent event) {
        if (event.getPlayer().getLevel().isClientSide()) {
            return;
        }

        DynamicEconomy.LOGGER.debug("Smelted items event");
        final var smeltedItem = event.getSmelting();
        WorldResourceTracker.addItemsToEconomy(smeltedItem);
    }

    /**
     * Called when a player hovers over an item in the inventory
     *
     * @param event The event data
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltip(final ItemTooltipEvent event) {

        if (!event.getFlags().isAdvanced()) {
            return;
        }

        final var itemStack = event.getItemStack();
        final var item = itemStack.getItem();
        final var optValue = CurrencyHelper.getCurrencyValue(itemStack);
        final var isSpecial = CurrencyHelper.isCurrencySpecial(itemStack);

        // Item is fixed-value currency


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