package com.agarrity.dynamic_economy;

import com.agarrity.dynamic_economy.util.RegistryHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class DynamicEconomyConfig {

    private static final List<Integer> DEFAULT_FIXED_CURRENCY_SIZES = List.of(
            // Coins
            1,
            2,
            5,
            10,
            20,
            50,
            100,
            200,
            // Notes
            500,
            1000,
            2000,
            5000,
            10000
    );

    private static final List<String> DEFAULT_NON_RENEWABLE_BLOCKS = List.of(
            // From Sponge
            RegistryHelper.getRegistryNameOrThrow(Blocks.SPONGE),
            RegistryHelper.getRegistryNameOrThrow(Blocks.WET_SPONGE),
            // From Ancient Debris
            RegistryHelper.getRegistryNameOrThrow(Blocks.ANCIENT_DEBRIS),
            RegistryHelper.getRegistryNameOrThrow(Blocks.NETHERITE_BLOCK),
            RegistryHelper.getRegistryNameOrThrow(Blocks.LODESTONE),
            // From Calcite
            RegistryHelper.getRegistryNameOrThrow(Blocks.CALCITE),
            // From Deepslate
            RegistryHelper.getRegistryNameOrThrow(Blocks.DEEPSLATE),
            RegistryHelper.getRegistryNameOrThrow(Blocks.COBBLED_DEEPSLATE),
            RegistryHelper.getRegistryNameOrThrow(Blocks.COBBLED_DEEPSLATE_SLAB),
            RegistryHelper.getRegistryNameOrThrow(Blocks.COBBLED_DEEPSLATE_STAIRS),
            RegistryHelper.getRegistryNameOrThrow(Blocks.COBBLED_DEEPSLATE_WALL),
            RegistryHelper.getRegistryNameOrThrow(Blocks.POLISHED_DEEPSLATE),
            RegistryHelper.getRegistryNameOrThrow(Blocks.POLISHED_DEEPSLATE_SLAB),
            RegistryHelper.getRegistryNameOrThrow(Blocks.POLISHED_DEEPSLATE_STAIRS),
            RegistryHelper.getRegistryNameOrThrow(Blocks.POLISHED_DEEPSLATE_WALL),
            RegistryHelper.getRegistryNameOrThrow(Blocks.CHISELED_DEEPSLATE),
            RegistryHelper.getRegistryNameOrThrow(Blocks.DEEPSLATE_BRICKS),
            RegistryHelper.getRegistryNameOrThrow(Blocks.DEEPSLATE_BRICK_SLAB),
            RegistryHelper.getRegistryNameOrThrow(Blocks.DEEPSLATE_BRICK_STAIRS),
            RegistryHelper.getRegistryNameOrThrow(Blocks.DEEPSLATE_BRICK_WALL),
            RegistryHelper.getRegistryNameOrThrow(Blocks.CRACKED_DEEPSLATE_BRICKS),
            RegistryHelper.getRegistryNameOrThrow(Blocks.DEEPSLATE_TILES),
            RegistryHelper.getRegistryNameOrThrow(Blocks.DEEPSLATE_TILE_SLAB),
            RegistryHelper.getRegistryNameOrThrow(Blocks.DEEPSLATE_TILE_STAIRS),
            RegistryHelper.getRegistryNameOrThrow(Blocks.DEEPSLATE_TILE_WALL),
            RegistryHelper.getRegistryNameOrThrow(Blocks.CRACKED_DEEPSLATE_TILES),
            // From Dead Bush
            RegistryHelper.getRegistryNameOrThrow(Blocks.DEAD_BUSH),
            // From Diamond Ore
            RegistryHelper.getRegistryNameOrThrow(Blocks.DIAMOND_ORE),
            RegistryHelper.getRegistryNameOrThrow(Blocks.DEEPSLATE_DIAMOND_ORE),
            RegistryHelper.getRegistryNameOrThrow(Blocks.DIAMOND_BLOCK),
            RegistryHelper.getRegistryNameOrThrow(Blocks.ENCHANTING_TABLE),
            RegistryHelper.getRegistryNameOrThrow(Blocks.JUKEBOX),
            // From Emerald Ore
            RegistryHelper.getRegistryNameOrThrow(Blocks.EMERALD_ORE),
            RegistryHelper.getRegistryNameOrThrow(Blocks.DEEPSLATE_EMERALD_ORE),
            // From Nether Ores
            RegistryHelper.getRegistryNameOrThrow(Blocks.NETHER_QUARTZ_ORE),
            RegistryHelper.getRegistryNameOrThrow(Blocks.NETHER_GOLD_ORE),
            // From Ores
            RegistryHelper.getRegistryNameOrThrow(Blocks.COAL_ORE),
            RegistryHelper.getRegistryNameOrThrow(Blocks.IRON_ORE),
            RegistryHelper.getRegistryNameOrThrow(Blocks.REDSTONE_ORE),
            RegistryHelper.getRegistryNameOrThrow(Blocks.LAPIS_ORE),
            RegistryHelper.getRegistryNameOrThrow(Blocks.GOLD_ORE),
            RegistryHelper.getRegistryNameOrThrow(Blocks.COPPER_ORE),
            RegistryHelper.getRegistryNameOrThrow(Blocks.DEEPSLATE_COAL_ORE),
            RegistryHelper.getRegistryNameOrThrow(Blocks.DEEPSLATE_IRON_ORE),
            RegistryHelper.getRegistryNameOrThrow(Blocks.DEEPSLATE_REDSTONE_ORE),
            RegistryHelper.getRegistryNameOrThrow(Blocks.DEEPSLATE_LAPIS_ORE),
            RegistryHelper.getRegistryNameOrThrow(Blocks.DEEPSLATE_GOLD_ORE),
            RegistryHelper.getRegistryNameOrThrow(Blocks.DEEPSLATE_COPPER_ORE),
            // From Spore Blossom
            RegistryHelper.getRegistryNameOrThrow(Blocks.SPORE_BLOSSOM),
            // From Tuff
            RegistryHelper.getRegistryNameOrThrow(Blocks.TUFF)
    );

    public static final ForgeConfigSpec GENERAL_SPEC;

    public static ForgeConfigSpec.ConfigValue<List<? extends String>> ALLOWED_BLOCKS;

    public static ForgeConfigSpec.LongValue ECONOMY_POOL_SIZE;

    public static ForgeConfigSpec.ConfigValue<List<? extends Integer>> FIXED_CURRENCY_SIZES;
    public static ForgeConfigSpec.IntValue CREDIT_NOTE_THRESHOLD;

    public static ForgeConfigSpec.IntValue TRADER_MAX_BUY_PRICE;

    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        setupConfig(configBuilder);
        GENERAL_SPEC = configBuilder.build();
    }

    private static void setupConfig(final ForgeConfigSpec.Builder builder) {
        // Economy configuration
        ALLOWED_BLOCKS = builder
                .comment("Defines the list of blocks that are non-renewable (and therefore exist finitely)")
                .defineList("non_renewable_blocks", DEFAULT_NON_RENEWABLE_BLOCKS, entry -> true);

        // Economy configuration
        ECONOMY_POOL_SIZE = builder
                .comment("Sets the size of the currency pool per resource (default: 100000000)")
                .defineInRange("economy_pool_size", 100000000, 0, Long.MAX_VALUE);

        FIXED_CURRENCY_SIZES = builder
                .comment("Sets the values of all the available fixed-size currency (ie. notes & coins)")
                .defineList("fixed_currency_sizes", DEFAULT_FIXED_CURRENCY_SIZES, entry -> true);

        CREDIT_NOTE_THRESHOLD = builder
                .comment("Sets the threshold at which a withdrawal will create a credit note rather than issue fixed-size currency")
                .defineInRange("credit_note_threshold", 100000, 0, Integer.MAX_VALUE);

        TRADER_MAX_BUY_PRICE = builder
                .comment("The maximum price that a trader will buy an item for")
                .defineInRange("trader_max_buy_price", 50000, 0, Integer.MAX_VALUE);
    }

}
