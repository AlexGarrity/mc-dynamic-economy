package com.agarrity.dynamic_economy;

import com.agarrity.dynamic_economy.client.gui.screens.inventory.AssessScreen;
import com.agarrity.dynamic_economy.client.gui.screens.inventory.BankingScreen;
import com.agarrity.dynamic_economy.client.gui.screens.inventory.TradeScreen;
import com.agarrity.dynamic_economy.client.model.AnimalVillagerModel;
import com.agarrity.dynamic_economy.client.renderer.entity.AnimalVillagerRenderer;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyHelper;
import com.agarrity.dynamic_economy.common.misc.DispenserOverride;
import com.agarrity.dynamic_economy.common.network.DynamicEconomyPacketHandler;
import com.agarrity.dynamic_economy.common.network.syncher.DEEntityDataSerializers;
import com.agarrity.dynamic_economy.common.world.entity.npc.AnimalVillager;
import com.agarrity.dynamic_economy.init.EntityInit;
import com.agarrity.dynamic_economy.init.ItemInit;
import com.agarrity.dynamic_economy.init.MenuInit;
import com.agarrity.dynamic_economy.init.TriggerInit;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.FileUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("dynamic_economy")
public class DynamicEconomy {
    public static final String MOD_ID = "dynamic_economy";

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public DynamicEconomy() {
        // Register the mod config
        FileUtils.getOrCreateDirectory(FMLPaths.CONFIGDIR.get().resolve(MOD_ID), MOD_ID);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, DynamicEconomyConfig.GENERAL_SPEC, MOD_ID + "/config.toml");

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register mod content
        EntityInit.ENTITIES_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
        MenuInit.MENUS_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
        ItemInit.ITEMS_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {

        @SubscribeEvent
        public static void onCommonSetup(final FMLCommonSetupEvent event) {
            event.enqueueWork(DynamicEconomyPacketHandler::registerPackets);

            event.enqueueWork(DEEntityDataSerializers::registerSerializers);

            event.enqueueWork(TriggerInit::registerTriggers);
            event.enqueueWork(EntityInit::registerSpawnPlacements);

            event.enqueueWork(() -> {
                        final List<Integer> currencySizes = new ArrayList<>();
                        DynamicEconomyConfig.FIXED_CURRENCY_SIZES.get().forEach(
                                (value) -> currencySizes.add(value.intValue())
                        );
                        CurrencyHelper.setAvailableCurrencySizes(currencySizes);
                    }
            );
        }

        @SubscribeEvent
        public static void onClientSetup(final FMLClientSetupEvent event) {
            event.enqueueWork(
                    () -> {
                        MenuScreens.register(MenuInit.BANKING_MENU.get(), BankingScreen::new);
                        MenuScreens.register(MenuInit.TRADER_MENU.get(), TradeScreen::new);
                        MenuScreens.register(MenuInit.ASSESS_MENU.get(), AssessScreen::new);
                    }
            );

            event.enqueueWork(
                    () -> ItemProperties.register(ItemInit.FIXED_CURRENCY.get(), new ResourceLocation(MOD_ID, "value"), (stack, level, living, id) -> {
                        final var tag = stack.getTag();
                        if (tag == null) {
                            return 0.0F;
                        }
                        if (!tag.contains("value")) {
                            return 0.0F;
                        }
                        return (float) tag.getInt("value");
                    })
            );
            event.enqueueWork(
                    () -> ItemProperties.register(ItemInit.FIXED_CURRENCY.get(), new ResourceLocation(MOD_ID, "decoration"), (stack, level, living, id) -> {
                        final var tag = stack.getTag();
                        if (tag == null) {
                            return 0.0F;
                        }
                        if (!tag.contains("decoration")) {
                            return -1.0F;
                        }
                        return (float) tag.getInt("decoration");
                    })
            );
        }

        @SubscribeEvent
        public static void onRegisterLayerDefinitions(final EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(AnimalVillagerModel.LAYER_LOCATION, AnimalVillagerModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void onRegisterEntityRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(EntityInit.ANIMAL_VILLAGER.get(), AnimalVillagerRenderer::new);
        }

        @SubscribeEvent
        public static void onEntityAttributeCreation(final EntityAttributeCreationEvent event) {
            event.put(EntityInit.ANIMAL_VILLAGER.get(), AnimalVillager.createAttributes().build());
        }
    }
}
