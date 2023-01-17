package com.agarrity.dynamic_economy.init;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.common.world.entity.npc.AnimalVillager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES_REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITIES, DynamicEconomy.MOD_ID);

    public static final RegistryObject<EntityType<AnimalVillager>> ANIMAL_VILLAGER = ENTITIES_REGISTRY.register(
            "animal_villager",
            () -> EntityType.Builder
                    .<AnimalVillager>of(AnimalVillager::new, MobCategory.MISC)
                    .clientTrackingRange(10)
                    .sized(0.6F, 1.8F)
                    .build("animal_villager")
    );

    public static void registerSpawnPlacements() {
        SpawnPlacements.register(EntityInit.ANIMAL_VILLAGER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Animal::checkAnimalSpawnRules);
    }
}