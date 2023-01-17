package com.agarrity.dynamic_economy;

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
            Blocks.SPONGE.getRegistryName().toString(),
            Blocks.WET_SPONGE.getRegistryName().toString(),
            // From Ancient Debris
            Blocks.ANCIENT_DEBRIS.getRegistryName().toString(),
            Blocks.NETHERITE_BLOCK.getRegistryName().toString(),
            Blocks.LODESTONE.getRegistryName().toString(),
            // From Calcite
            Blocks.CALCITE.getRegistryName().toString(),
            // From Deepslate
            Blocks.DEEPSLATE.getRegistryName().toString(),
            Blocks.COBBLED_DEEPSLATE.getRegistryName().toString(),
            Blocks.COBBLED_DEEPSLATE_SLAB.getRegistryName().toString(),
            Blocks.COBBLED_DEEPSLATE_STAIRS.getRegistryName().toString(),
            Blocks.COBBLED_DEEPSLATE_WALL.getRegistryName().toString(),
            Blocks.POLISHED_DEEPSLATE.getRegistryName().toString(),
            Blocks.POLISHED_DEEPSLATE_SLAB.getRegistryName().toString(),
            Blocks.POLISHED_DEEPSLATE_STAIRS.getRegistryName().toString(),
            Blocks.POLISHED_DEEPSLATE_WALL.getRegistryName().toString(),
            Blocks.CHISELED_DEEPSLATE.getRegistryName().toString(),
            Blocks.DEEPSLATE_BRICKS.getRegistryName().toString(),
            Blocks.DEEPSLATE_BRICK_SLAB.getRegistryName().toString(),
            Blocks.DEEPSLATE_BRICK_STAIRS.getRegistryName().toString(),
            Blocks.DEEPSLATE_BRICK_WALL.getRegistryName().toString(),
            Blocks.CRACKED_DEEPSLATE_BRICKS.getRegistryName().toString(),
            Blocks.DEEPSLATE_TILES.getRegistryName().toString(),
            Blocks.DEEPSLATE_TILE_SLAB.getRegistryName().toString(),
            Blocks.DEEPSLATE_TILE_STAIRS.getRegistryName().toString(),
            Blocks.DEEPSLATE_TILE_WALL.getRegistryName().toString(),
            Blocks.CRACKED_DEEPSLATE_TILES.getRegistryName().toString(),
            // From Dead Bush
            Blocks.DEAD_BUSH.getRegistryName().toString(),
            // From Diamond Ore
            Blocks.DIAMOND_ORE.getRegistryName().toString(),
            Blocks.DEEPSLATE_DIAMOND_ORE.getRegistryName().toString(),
            Blocks.DIAMOND_BLOCK.getRegistryName().toString(),
            Blocks.ENCHANTING_TABLE.getRegistryName().toString(),
            Blocks.JUKEBOX.getRegistryName().toString(),
            // From Emerald Ore
            Blocks.EMERALD_ORE.getRegistryName().toString(),
            Blocks.DEEPSLATE_EMERALD_ORE.getRegistryName().toString(),
            // From Nether Ores
            Blocks.NETHER_QUARTZ_ORE.getRegistryName().toString(),
            Blocks.NETHER_GOLD_ORE.getRegistryName().toString(),
            // From Ores
            Blocks.COAL_ORE.getRegistryName().toString(),
            Blocks.IRON_ORE.getRegistryName().toString(),
            Blocks.REDSTONE_ORE.getRegistryName().toString(),
            Blocks.LAPIS_ORE.getRegistryName().toString(),
            Blocks.GOLD_ORE.getRegistryName().toString(),
            Blocks.COPPER_ORE.getRegistryName().toString(),
            Blocks.DEEPSLATE_COAL_ORE.getRegistryName().toString(),
            Blocks.DEEPSLATE_IRON_ORE.getRegistryName().toString(),
            Blocks.DEEPSLATE_REDSTONE_ORE.getRegistryName().toString(),
            Blocks.DEEPSLATE_LAPIS_ORE.getRegistryName().toString(),
            Blocks.DEEPSLATE_GOLD_ORE.getRegistryName().toString(),
            Blocks.DEEPSLATE_COPPER_ORE.getRegistryName().toString(),
            // From Spore Blossom
            Blocks.SPORE_BLOSSOM.getRegistryName().toString(),
            // From Tuff
            Blocks.TUFF.getRegistryName().toString()
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
