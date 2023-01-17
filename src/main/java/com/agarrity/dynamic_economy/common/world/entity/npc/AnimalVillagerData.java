package com.agarrity.dynamic_economy.common.world.entity.npc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class AnimalVillagerData {

    private final int species;
    private final int profession;

    public static final Codec<AnimalVillagerData> CODEC = RecordCodecBuilder.create((villagerDataInstance) ->
        villagerDataInstance.group(
                Codec.INT.fieldOf("species").forGetter(AnimalVillagerData::getSpecies),
                Codec.INT.fieldOf("profession").forGetter(AnimalVillagerData::getProfession)
                ).apply(villagerDataInstance, AnimalVillagerData::new)
    );

    public AnimalVillagerData(final int speciesIndex, final int professionIndex) {
        this.species = speciesIndex;
        this.profession = professionIndex;
    }

    public int getSpecies() {
        return this.species;
    }

    public int getProfession() {
        return this.profession;
    }


    public AnimalVillagerData setSpecies(final int pType) {
        return new AnimalVillagerData(pType, this.profession);
    }

    public AnimalVillagerData setProfession(final int pProfession) {
        return new AnimalVillagerData(this.species, pProfession);
    }

}
