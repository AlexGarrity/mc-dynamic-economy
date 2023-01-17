package com.agarrity.dynamic_economy.common.network.syncher;

import com.agarrity.dynamic_economy.common.world.entity.npc.AnimalVillagerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import org.jetbrains.annotations.NotNull;

public class DEEntityDataSerializers {
    public static final EntityDataSerializer<AnimalVillagerData> ANIMAL_VILLAGER_DATA = new EntityDataSerializer<>() {
        public void write(final FriendlyByteBuf buffer, final AnimalVillagerData animalVillagerData) {
            buffer.writeInt(animalVillagerData.getSpecies());
            buffer.writeInt(animalVillagerData.getProfession());
        }

        public @NotNull AnimalVillagerData read(final FriendlyByteBuf buffer) {
            return new AnimalVillagerData(buffer.readInt(), buffer.readInt());
        }

        public @NotNull AnimalVillagerData copy(@NotNull final AnimalVillagerData animalVillagerData) {
            return animalVillagerData;
        }
    };

    public static void registerSerializers() {
        EntityDataSerializers.registerSerializer(ANIMAL_VILLAGER_DATA);
    }
}
