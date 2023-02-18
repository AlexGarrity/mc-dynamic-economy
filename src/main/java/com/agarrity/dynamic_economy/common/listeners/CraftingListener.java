package com.agarrity.dynamic_economy.common.listeners;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.common.economy.resources.WorldResourceTracker;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DynamicEconomy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CraftingListener {

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
            WorldResourceTracker.removeItemsFromEconomy(itemStack.getItem(), 1);
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

}
