package com.agarrity.dynamic_economy.common.world.entity.npc;

import java.util.concurrent.ThreadLocalRandom;

public class AnimalVillagerSpecies {
    public static final int TEDDY_BEAR = 0;
    public static final int CAT = 1;
    public static final int WOLF = 2;
    public static final int RABBIT = 3;
    public static final int FOX = 4;
    public static final int SHEEP = 5 ;
    public static final int COW = 6;
    public static final int OCELOT = 7;
    public static final int CORGI = 8;
    public static final int DUTCH_RABBIT = 9;
    public static final int RACCOON = 10;

    public static int getRandomSpecies() {
        final var random = ThreadLocalRandom.current();
        return random.nextInt(0, 11);
    }
}