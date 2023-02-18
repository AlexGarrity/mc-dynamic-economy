package com.agarrity.dynamic_economy;

import com.agarrity.dynamic_economy.common.economy.bank.CurrencyAmount;
import com.agarrity.dynamic_economy.common.economy.resources.WorldResourceTracker;
import com.agarrity.dynamic_economy.common.economy.resources.WorldSavedData;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class TestWorldResourceTracker {

    public final WorldSavedData worldSavedData = WorldSavedData.create();
    public long POOL_SIZE;

    @Before
    public void setWorldResourceTracker() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        WorldResourceTracker.setSavedData(worldSavedData);
        POOL_SIZE = DynamicEconomyConfig.ECONOMY_POOL_SIZE.get();
    }

    @Test
    public void addItems() {
        WorldResourceTracker.addItemsToEconomy(new ItemStack(Items.COD, 3));
        WorldResourceTracker.addItemsToEconomy(Items.COD, 7);

        assertEquals(1, WorldResourceTracker.getItemFrequency(new ItemStack(Items.COD, 10)).orElseThrow().intValue());
        assertEquals(new CurrencyAmount(POOL_SIZE / 10), WorldResourceTracker.estimateItemValue(new ItemStack(Items.COD, 1)).orElseThrow());
        assertEquals(new CurrencyAmount(POOL_SIZE), WorldResourceTracker.estimateItemsValue(new ItemStack(Items.COD, 10)).orElseThrow());
    }

    @Test
    public void removeItems() {
        WorldResourceTracker.addItemsToEconomy(new ItemStack(Items.COD, 3));
        WorldResourceTracker.addItemsToEconomy(Items.COD, 7);

        assertEquals(1, WorldResourceTracker.getItemFrequency(new ItemStack(Items.COD, 10)).orElseThrow().intValue());
        assertEquals(new CurrencyAmount(POOL_SIZE / 10), WorldResourceTracker.estimateItemValue(new ItemStack(Items.COD, 1)).orElseThrow());
        assertEquals(new CurrencyAmount(POOL_SIZE), WorldResourceTracker.estimateItemsValue(new ItemStack(Items.COD, 10)).orElseThrow());

        WorldResourceTracker.removeItemsFromEconomy(new ItemStack(Items.COD, 1));
        WorldResourceTracker.removeItemsFromEconomy(Items.COD, 1);

        assertEquals(0, WorldResourceTracker.getItemFrequency(new ItemStack(Items.COD, 8)).orElseThrow().intValue());
        assertEquals(new CurrencyAmount(POOL_SIZE / 8), WorldResourceTracker.estimateItemValue(new ItemStack(Items.COD, 1)).orElseThrow());
        assertEquals(new CurrencyAmount(POOL_SIZE), WorldResourceTracker.estimateItemsValue(new ItemStack(Items.COD, 8)).orElseThrow());
    }

    @Test
    public void estimateItemRarity() {
        WorldResourceTracker.addItemsToEconomy(new ItemStack(Items.COD, 1));
        assertEquals(0, WorldResourceTracker.getItemFrequency(new ItemStack(Items.COD, 10)).orElseThrow().intValue());

        WorldResourceTracker.addItemsToEconomy(new ItemStack(Items.COD, 9));
        assertEquals(1, WorldResourceTracker.getItemFrequency(new ItemStack(Items.COD, 10)).orElseThrow().intValue());

        WorldResourceTracker.addItemsToEconomy(new ItemStack(Items.COD, 90));
        assertEquals(2, WorldResourceTracker.getItemFrequency(new ItemStack(Items.COD, 10)).orElseThrow().intValue());

        WorldResourceTracker.addItemsToEconomy(new ItemStack(Items.COD, 900));
        assertEquals(3, WorldResourceTracker.getItemFrequency(new ItemStack(Items.COD, 10)).orElseThrow().intValue());

        WorldResourceTracker.addItemsToEconomy(new ItemStack(Items.COD, 9000));
        assertEquals(4, WorldResourceTracker.getItemFrequency(new ItemStack(Items.COD, 10)).orElseThrow().intValue());
    }
}
