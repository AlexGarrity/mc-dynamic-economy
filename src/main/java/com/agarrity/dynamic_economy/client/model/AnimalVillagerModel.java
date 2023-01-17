package com.agarrity.dynamic_economy.client.model;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class AnimalVillagerModel<T extends Entity> extends EntityModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DynamicEconomy.MOD_ID, "trader_villager_model"), "main");
    private final ModelPart nose;
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart hatRim;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart leftEar;
    private final ModelPart rightEar;

    public AnimalVillagerModel(final ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.hat = this.head.getChild("hat");
        this.hatRim = this.hat.getChild("hat_rim");
        this.nose = this.head.getChild("nose");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.leftEar = this.head.getChild("extras").getChild("rabbit_ear_l");
        this.rightEar = this.head.getChild("extras").getChild("rabbit_ear_r");
    }

    public static LayerDefinition createBodyLayer() {
        final MeshDefinition meshdefinition = new MeshDefinition();
        final PartDefinition partdefinition = meshdefinition.getRoot();

        final var head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F), PartPose.ZERO);

        final var extras = head.addOrReplaceChild("extras", CubeListBuilder.create().texOffs(12, 23).addBox(-2.0F, -3.0F, -5.0F, 4.0F, 2.0F, 1.0F)
                .texOffs(0, 18).addBox(4.0F, -7.0F, 0.0F, 1.0F, 2.0F, 2.0F)
                .texOffs(29, 44).addBox(-7.0F, -6.0F, -1.0F, 3.0F, 1.0F, 2.0F)
                .texOffs(38, 18).addBox(-2.0F, -3.0F, -7.0F, 4.0F, 2.0F, 3.0F)
                .texOffs(53, 18).addBox(4.0F, -6.0F, -1.0F, 3.0F, 1.0F, 2.0F)
                .texOffs(36, 38).addBox(-1.5F, -5.5F, -5.0F, 3.0F, 2.0F, 1.0F)
                .texOffs(7, 18).addBox(-3.0F, -3.0F, -5.0F, 6.0F, 3.0F, 1.0F)
                .texOffs(36, 0).addBox(-5.0F, -9.0F, -1.0F, 1.0F, 3.0F, 1.0F)
                .texOffs(36, 0).addBox(4.0F, -9.0F, -1.0F, 1.0F, 3.0F, 1.0F)
                .texOffs(0, 38).addBox(-5.0F, -7.0F, 0.0F, 1.0F, 2.0F, 2.0F)
                .texOffs(22, 38).addBox(-2.0F, -3.0F, -7.0F, 4.0F, 3.0F, 3.0F)
                .texOffs(58, 21).addBox(3.0F, -11.0F, 0.0F, 2.0F, 3.0F, 1.0F)
                .texOffs(34, 4).addBox(-5.0F, -11.0F, 0.0F, 2.0F, 3.0F, 1.0F), PartPose.offset(0.0F, 0.0F, 0.0F));

        extras.addOrReplaceChild("rabbit_ear_r", CubeListBuilder.create().texOffs(58, 0).addBox(-3.5F, -16.0F, 0.0F, 2.0F, 6.0F, 1.0F), PartPose.ZERO);

        extras.addOrReplaceChild("rabbit_ear_l", CubeListBuilder.create().texOffs(0, 0).addBox(1.5F, -16.0F, 0.0F, 2.0F, 6.0F, 1.0F), PartPose.ZERO);

        head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -28.0F, -6.0F, 2.0F, 4.0F, 2.0F), PartPose.ZERO);

        final var hat = head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.ZERO);

        hat.addOrReplaceChild("hat_rim", CubeListBuilder.create().texOffs(30, 47).addBox(-8.0F, -8.0F, -6.0F, 16.0F, 16.0F, 1.0F), PartPose.rotation((-(float) Math.PI / 2F), 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("arms", CubeListBuilder.create().texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F).texOffs(44, 22).addBox(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, true).texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 3.0F, -1.0F, -0.75F, 0.0F, 0.0F));

        final var body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F), PartPose.ZERO);

        body.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 18.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.ZERO);

        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(-2.0F, 12.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(2.0F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(@NotNull final T pEntity, final float pLimbSwing, final float pLimbSwingAmount, final float pAgeInTicks, final float pNetHeadYaw, final float pHeadPitch) {
        this.head.yRot = pNetHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = pHeadPitch * ((float) Math.PI / 180F);
        this.head.zRot = 0.0F;

        this.leftEar.xRot = Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount * 0.1F;
        this.rightEar.xRot = Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 1.4F * pLimbSwingAmount * 0.1F;

        this.rightLeg.xRot = Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount * 0.5F;
        this.leftLeg.xRot = Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 1.4F * pLimbSwingAmount * 0.5F;
        this.rightLeg.yRot = 0.0F;
        this.leftLeg.yRot = 0.0F;
    }

    @Override
    public void renderToBuffer(@NotNull final PoseStack poseStack, @NotNull  final VertexConsumer vertexConsumer, final int packedLight, final int packedOverlay, final float red, final float green, final float blue, final float alpha) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        hat.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        hatRim.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        nose.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}