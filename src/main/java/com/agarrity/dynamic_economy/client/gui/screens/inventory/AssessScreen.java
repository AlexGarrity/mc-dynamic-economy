package com.agarrity.dynamic_economy.client.gui.screens.inventory;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.client.economy.ClientResourceTracker;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyAmount;
import com.agarrity.dynamic_economy.common.world.inventory.AssessMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class AssessScreen extends AbstractContainerScreen<AssessMenu> {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(DynamicEconomy.MOD_ID, "textures/gui/container/assess.png");

    public AssessScreen(AssessMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.inventoryLabelX = 7;
    }

    @Override
    protected void renderLabels(@NotNull PoseStack pPoseStack, int pMouseX, int pMouseY) {
        super.renderLabels(pPoseStack, pMouseX, pMouseY);
        final var singleValueComponent = new TranslatableComponent("gui.dynamic_economy.assess.value");
        final var multipleValueComponent = new TranslatableComponent("gui.dynamic_economy.assess.value");
        final var rarityComponent = new TranslatableComponent("gui.dynamic_economy.assess.rarity");

        final var itemStats = ClientResourceTracker.itemStats.get(
                this.getMenu().getItems().get(0).getItem()
        );
        if (itemStats == null) {
            return;
        }

        singleValueComponent.append(": ");
        singleValueComponent.append(itemStats.value.toString());
        this.font.draw(pPoseStack, singleValueComponent, 8, 17, ChatFormatting.DARK_GRAY.getColor());

        final var itemCount = this.getMenu().getItems().get(0).getCount();
        if (itemCount > 1) {
            multipleValueComponent.append(String.format(" (%d): ", itemCount));
            multipleValueComponent.append(new CurrencyAmount(itemStats.value.asLong() * itemCount).toString());
            this.font.draw(pPoseStack, multipleValueComponent, 8, 29, ChatFormatting.DARK_GRAY.getColor());
        }

        rarityComponent.append(": ");
        switch (itemStats.rarity) {
            case -1 -> rarityComponent.append(new TranslatableComponent("gui.dynamic_economy.assess.rarity.unknown").withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.OBFUSCATED));
            case 0 -> rarityComponent.append(new TranslatableComponent("gui.dynamic_economy.assess.rarity.legendary").withStyle(ChatFormatting.GOLD));
            case 1 -> rarityComponent.append(new TranslatableComponent("gui.dynamic_economy.assess.rarity.epic").withStyle(ChatFormatting.DARK_PURPLE));
            case 2 -> rarityComponent.append(new TranslatableComponent("gui.dynamic_economy.assess.rarity.rare").withStyle(ChatFormatting.BLUE));
            case 3 -> rarityComponent.append(new TranslatableComponent("gui.dynamic_economy.assess.rarity.uncommon").withStyle(ChatFormatting.GREEN));
            case 4 -> rarityComponent.append(new TranslatableComponent("gui.dynamic_economy.assess.rarity.common").withStyle(ChatFormatting.WHITE));
            default -> rarityComponent.append(new TranslatableComponent("gui.dynamic_economy.assess.rarity.poor").withStyle(ChatFormatting.GRAY));
        }
        this.font.draw(pPoseStack, rarityComponent, 8, 41, ChatFormatting.DARK_GRAY.getColor());
    }

    @Override
    protected void renderBg(@NotNull PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);

        // Draw background
        this.blit(pPoseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTick);
        RenderSystem.disableBlend();
        this.renderTooltip(pose, mouseX, mouseY);
    }

}
