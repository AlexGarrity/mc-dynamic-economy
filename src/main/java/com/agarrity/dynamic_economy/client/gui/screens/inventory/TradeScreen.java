package com.agarrity.dynamic_economy.client.gui.screens.inventory;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyAmount;
import com.agarrity.dynamic_economy.common.network.DynamicEconomyPacketHandler;
import com.agarrity.dynamic_economy.common.network.ServerboundBalanceMessage;
import com.agarrity.dynamic_economy.common.world.inventory.TradeMenu;
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

public class TradeScreen extends AbstractContainerScreen<TradeMenu> {
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(DynamicEconomy.MOD_ID, "textures/gui/container/trader.png");
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    public TradeScreen(TradeMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.inventoryLabelX = 7;
    }

    @Override
    protected void init() {
        super.init();
        DynamicEconomyPacketHandler.INSTANCE.sendToServer(new ServerboundBalanceMessage());
    }

    @Override
    protected void renderLabels(@NotNull PoseStack pPoseStack, int pMouseX, int pMouseY) {
        super.renderLabels(pPoseStack, pMouseX, pMouseY);
        final var balanceComponent = new TranslatableComponent("gui.dynamic_economy.trading.balance");

        balanceComponent.append(": ");
        balanceComponent.append(this.getMenu().calculateInventoryCoinValue().toString());

        this.font.draw(pPoseStack, balanceComponent, 8, 17, ChatFormatting.DARK_GRAY.getColor());
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
