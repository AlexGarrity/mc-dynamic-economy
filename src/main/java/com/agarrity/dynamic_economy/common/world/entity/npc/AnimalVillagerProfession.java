package com.agarrity.dynamic_economy.common.world.entity.npc;

import java.util.concurrent.ThreadLocalRandom;

public class AnimalVillagerProfession {
    public static final int NONE = 0;
    public static final int BANKER = 1;
    public static final int TRADER = 2;
    public static final int ASSESSOR = 3;
    public static final int PLAYER_TRADER = 4;


    public static final float PLAYER_TRADER_THRESHOLD = 0.5F;
    public static final float TRADER_THRESHOLD = 0.7f;
    public static final float BANKER_THRESHOLD = 0.85F;
    public static final float ASSESSOR_THRESHOLD = 0.95F;

    public static int getRandomProfession() {
        final var random = ThreadLocalRandom.current();
        final var v = random.nextFloat();
        if (v < PLAYER_TRADER_THRESHOLD) {
            return PLAYER_TRADER;
        } else if (v < TRADER_THRESHOLD) {
            return TRADER;
        } else if (v < BANKER_THRESHOLD) {
            return BANKER;
        } else if (v < ASSESSOR_THRESHOLD) {
            return ASSESSOR;
        }
        return NONE;
    }
}