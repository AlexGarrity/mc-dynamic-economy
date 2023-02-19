package com.agarrity.dynamic_economy.init;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.common.world.entity.npc.AnimalVillager;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.sensing.TemptingSensor;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;

public class SensorInit {
    @ObjectHolder(value="dynamic_economy:animal_villager_temptations")
    public static final SensorType<TemptingSensor> ANIMAL_VILLAGER_TEMPTATIONS = null;

    public static void registerSensors(final RegistryEvent.Register<SensorType<?>> event) {
        event.getRegistry().register(new SensorType<>(
                () -> new TemptingSensor(AnimalVillager.getTemptations())
        ).setRegistryName(DynamicEconomy.MOD_ID, "animal_villager_temptations"));
    }


}
