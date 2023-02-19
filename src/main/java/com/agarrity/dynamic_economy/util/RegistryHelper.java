package com.agarrity.dynamic_economy.util;

import net.minecraft.ResourceLocationException;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class RegistryHelper {

    public static String getRegistryNameOrThrow(final Item item) throws ResourceLocationException {
        final var registryName = item.getRegistryName();
        if (registryName == null) {
            throw new ResourceLocationException(String.format("Could not find a resource location for %s (%s)", item.getDescription(), item.getDescriptionId()));
        }

        return registryName.toString();
    }

    public static String getRegistryNameOrThrow(final ItemStack itemStack) throws ResourceLocationException {
        return getRegistryNameOrThrow(itemStack.getItem());
    }

    public static String getRegistryNameOrThrow(final Block block) throws ResourceLocationException {
        return getRegistryNameOrThrow(block.asItem());
    }
}
