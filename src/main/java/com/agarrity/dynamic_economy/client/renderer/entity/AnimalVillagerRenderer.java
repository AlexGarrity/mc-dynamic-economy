package com.agarrity.dynamic_economy.client.renderer.entity;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.client.model.AnimalVillagerModel;
import com.agarrity.dynamic_economy.common.world.entity.npc.AnimalVillager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class AnimalVillagerRenderer extends MobRenderer<AnimalVillager, AnimalVillagerModel<AnimalVillager>> {
    private static final ResourceLocation[] TEXTURES = {
            new ResourceLocation(DynamicEconomy.MOD_ID, "textures/entity/villager/species/teddy_bear.png"),
            new ResourceLocation(DynamicEconomy.MOD_ID, "textures/entity/villager/species/cat.png"),
            new ResourceLocation(DynamicEconomy.MOD_ID, "textures/entity/villager/species/wolf.png"),
            new ResourceLocation(DynamicEconomy.MOD_ID, "textures/entity/villager/species/rabbit.png"),
            new ResourceLocation(DynamicEconomy.MOD_ID, "textures/entity/villager/species/fox.png"),
            new ResourceLocation(DynamicEconomy.MOD_ID, "textures/entity/villager/species/sheep.png"),
            new ResourceLocation(DynamicEconomy.MOD_ID, "textures/entity/villager/species/cow.png"),
            new ResourceLocation(DynamicEconomy.MOD_ID, "textures/entity/villager/species/ocelot.png"),
            new ResourceLocation(DynamicEconomy.MOD_ID, "textures/entity/villager/species/corgi.png"),
            new ResourceLocation(DynamicEconomy.MOD_ID, "textures/entity/villager/species/dutch_rabbit.png"),
            new ResourceLocation(DynamicEconomy.MOD_ID, "textures/entity/villager/species/raccoon.png")
    };

    public AnimalVillagerRenderer(final EntityRendererProvider.Context pContext) {
        super(pContext, new AnimalVillagerModel<>(pContext.bakeLayer(AnimalVillagerModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new CrossedArmsItemLayer<>(this));
    }

    public static int getTextureCount() {
        return TEXTURES.length;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull final AnimalVillager pEntity) {
        return TEXTURES[pEntity.getAnimalVillagerData().getSpecies()];
    }

    protected void scale(final AnimalVillager pLivingEntity, @NotNull final PoseStack pMatrixStack, final float pPartialTickTime) {
        float f = 0.9375F;
        if (pLivingEntity.isBaby()) {
            f *= 0.5F;
            this.shadowRadius = 0.25F;
        } else {
            this.shadowRadius = 0.5F;
        }

        pMatrixStack.scale(f, f, f);
    }
}
