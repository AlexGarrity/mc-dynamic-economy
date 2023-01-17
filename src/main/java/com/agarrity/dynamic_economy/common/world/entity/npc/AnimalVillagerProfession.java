package com.agarrity.dynamic_economy.common.world.entity.npc;

import java.util.concurrent.ThreadLocalRandom;

public class AnimalVillagerProfession {
    public static final int NONE = 0;
    public static final int BANKER = 1;
    public static final int TRADER = 2;
    public static final int ASSESSOR = 3;


    public static final float TRADER_THRESHOLD = 0.7F;
    public static final float BANKER_THRESHOLD = 0.85F;
    public static final float ASSESSOR_THRESHOLD = 1.0F;

    public static int getRandomProfession() {
        final var random = ThreadLocalRandom.current();
        final var v = random.nextFloat();
        if (v < TRADER_THRESHOLD) {
            return TRADER;
        } else if (v < BANKER_THRESHOLD) {
            return BANKER;
        }
        return ASSESSOR;
    }
}