package com.agarrity.dynamic_economy.common.listeners;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.common.economy.resources.WorldResourceTracker;
import com.agarrity.dynamic_economy.common.world.entity.npc.AnimalVillager;
import com.agarrity.dynamic_economy.init.ItemInit;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

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
        processShulkerBoxContents(itemStack, false);

        if (itemStack.getItem() == ItemInit.COIN_BAG.get()) {
            // Cancel the event so it never hits the ground
            event.setCanceled(true);
            event.getPlayer().addItem(itemStack);
            return;
        }

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
        final var itemStack = event.getStack();
        processShulkerBoxContents(itemStack, true);


        WorldResourceTracker.addItemsToEconomy(itemStack);
    }

    private static void processShulkerBoxContents(ItemStack itemStack, boolean isPickUp) {
        if (itemStack.hasTag()) {
            final var nbtTag = itemStack.getTag();
            if (nbtTag == null) {
                return;
            }
            if (!nbtTag.contains("BlockEntityTag")) {
                return;
            }
            final var blockEntityTag = nbtTag.getCompound("BlockEntityTag");
            final var itemsTag = blockEntityTag.getList("Items", Tag.TAG_COMPOUND);
            for (var i = 0; i < itemsTag.size(); ++i) {
                final var itemTag = itemsTag.getCompound(i);
                final var count = itemTag.getInt("Count");
                final var id = itemTag.getString("id");

                if (id.isBlank() || count == 0) {
                    continue;
                }

                if (id.equals("minecraft:air")) {
                    continue;
                }

                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
                if (item == null) {
                    return;
                }

                if (isPickUp) {
                    WorldResourceTracker.addItemsToEconomy(item, count);
                } else {
                    WorldResourceTracker.removeItemsFromEconomy(item, count);
                }
            }
        }
    }
}
