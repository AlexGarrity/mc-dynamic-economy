package com.agarrity.dynamic_economy.init;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.common.world.entity.npc.AnimalVillager;
import com.agarrity.dynamic_economy.common.world.entity.npc.AnimalVillagerProfession;
import com.agarrity.dynamic_economy.common.world.entity.npc.AnimalVillagerSpecies;
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
                    .<AnimalVillager>of((type, level) -> new AnimalVillager(type, level, AnimalVillagerSpecies.getRandomSpecies(), AnimalVillagerProfession.NONE), MobCategory.MISC)
                    .clientTrackingRange(10)
                    .sized(0.6F, 1.8F)
                    .build("animal_villager")
    );

    public static final RegistryObject<EntityType<AnimalVillager>> BANKER_ANIMAL_VILLAGER = ENTITIES_REGISTRY.register(
            "banker_animal_villager",
            () -> EntityType.Builder
                    .<AnimalVillager>of((type, level) -> new AnimalVillager(type, level, AnimalVillagerSpecies.getRandomSpecies(), AnimalVillagerProfession.BANKER), MobCategory.MISC)
                    .clientTrackingRange(10)
                    .sized(0.6F, 1.8F)
                    .build("banker_animal_villager")
    );

    public static final RegistryObject<EntityType<AnimalVillager>> TRADER_ANIMAL_VILLAGER = ENTITIES_REGISTRY.register(
            "trader_animal_villager",
            () -> EntityType.Builder
                    .<AnimalVillager>of((type, level) -> new AnimalVillager(type, level, AnimalVillagerSpecies.getRandomSpecies(), AnimalVillagerProfession.TRADER), MobCategory.MISC)
                    .clientTrackingRange(10)
                    .sized(0.6F, 1.8F)
                    .build("trader_animal_villager")
    );

    public static final RegistryObject<EntityType<AnimalVillager>> ASSESSOR_ANIMAL_VILLAGER = ENTITIES_REGISTRY.register(
            "assessor_animal_villager",
            () -> EntityType.Builder
                    .<AnimalVillager>of((type, level) -> new AnimalVillager(type, level, AnimalVillagerSpecies.getRandomSpecies(), AnimalVillagerProfession.ASSESSOR), MobCategory.MISC)
                    .clientTrackingRange(10)
                    .sized(0.6F, 1.8F)
                    .build("assessor_animal_villager")
    );

    public static final RegistryObject<EntityType<AnimalVillager>> PLAYER_TRADER_ANIMAL_VILLAGER = ENTITIES_REGISTRY.register(
            "player_trader_animal_villager",
            () -> EntityType.Builder
                    .<AnimalVillager>of((type, level) -> new AnimalVillager(type, level, AnimalVillagerSpecies.getRandomSpecies(), AnimalVillagerProfession.PLAYER_TRADER), MobCategory.MISC)
                    .clientTrackingRange(10)
                    .sized(0.6F, 1.8F)
                    .build("player_trader_animal_villager")
    );

    public static void registerSpawnPlacements() {
        SpawnPlacements.register(EntityInit.ANIMAL_VILLAGER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityInit.BANKER_ANIMAL_VILLAGER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityInit.TRADER_ANIMAL_VILLAGER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityInit.PLAYER_TRADER_ANIMAL_VILLAGER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityInit.ASSESSOR_ANIMAL_VILLAGER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Animal::checkAnimalSpawnRules);

    }
}
