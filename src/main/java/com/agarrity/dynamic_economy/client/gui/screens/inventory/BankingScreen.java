package com.agarrity.dynamic_economy.client.gui.screens.inventory;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyAmount;
import com.agarrity.dynamic_economy.common.network.DynamicEconomyPacketHandler;
import com.agarrity.dynamic_economy.common.network.ServerboundBalanceMessage;
import com.agarrity.dynamic_economy.common.network.ServerboundDepositMessage;
import com.agarrity.dynamic_economy.common.network.ServerboundWithdrawMessage;
import com.agarrity.dynamic_economy.common.world.inventory.BankingMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class BankingScreen extends AbstractContainerScreen<BankingMenu> implements ICashScreen {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(DynamicEconomy.MOD_ID, "textures/gui/container/banking.png");

    private CurrencyAmount balance;
    private float quantityBoxAmount;
    private EditBox quantityBox;
    private Button depositButton;
    private Button withdrawButton;

    public BankingScreen(BankingMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.inventoryLabelX = 7;
        this.quantityBoxAmount = 0.0F;
        setBalance(new CurrencyAmount());
    }

    @Override
    protected void init() {
        super.init();
        quantityBox = new EditBox(this.font, this.leftPos + 68, this.topPos + 49, 102, 16, new TranslatableComponent("gui.dynamic_economy.banking.quantity"));
        quantityBox.setValue("0");
        quantityBox.setFilter((String str) -> {
            DynamicEconomy.LOGGER.debug(str);
            try {
                this.quantityBoxAmount = Float.parseFloat(str);
            } catch (Exception ex) {
                this.quantityBoxAmount = 0.0F;
                this.quantityBox.setValue("0");
                return false;
            }
            return true;
        });
        quantityBox.setTextColor(-1);

        depositButton = new Button(this.leftPos + 6, this.topPos + 29, 56, 15, new TranslatableComponent("gui.dynamic_economy.banking.deposit"),
                (Button button) -> DynamicEconomyPacketHandler.INSTANCE.sendToServer(new ServerboundDepositMessage())
        );

        withdrawButton = new Button(this.leftPos + 6, this.topPos + 49, 56, 15, new TranslatableComponent("gui.dynamic_economy.banking.withdraw"),
                (Button button) -> {
                    final var amount = new CurrencyAmount((int) (Math.floor(this.quantityBoxAmount * 100.0F)));
                    DynamicEconomyPacketHandler.INSTANCE.sendToServer(new ServerboundWithdrawMessage(amount));
                }
        );

        DynamicEconomyPacketHandler.INSTANCE.sendToServer(new ServerboundBalanceMessage());

        this.addWidget(quantityBox);
        this.addWidget(depositButton);
        this.addWidget(withdrawButton);
    }

    @Override
    protected void renderLabels(@NotNull PoseStack pPoseStack, int pMouseX, int pMouseY) {
        super.renderLabels(pPoseStack, pMouseX, pMouseY);
        final var balanceComponent = new TranslatableComponent("gui.dynamic_economy.banking.balance");

        balanceComponent.append(": ");
        balanceComponent.append(this.balance.toString());

        this.font.draw(pPoseStack, balanceComponent, 8, 17, ChatFormatting.DARK_GRAY.getColor());
    }

    protected void renderFg(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        quantityBox.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        depositButton.renderButton(pPoseStack, pMouseX, pMouseY, pPartialTick);
        withdrawButton.renderButton(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    protected void renderBg(@NotNull PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);

        // Draw background
        this.blit(pPoseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // Draw buttons
        this.blit(pPoseStack, this.leftPos + 6, this.topPos + 29, 0, 166, 55, 16);
        this.blit(pPoseStack, this.leftPos + 6, this.topPos + 49, 0, 166, 55, 16);
    }

    @Override
    public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTick);
        RenderSystem.disableBlend();
        this.renderFg(pose, mouseX, mouseY, partialTick);
        this.renderTooltip(pose, mouseX, mouseY);
    }

    @Override
    public void setBalance(@NotNull final CurrencyAmount amount) {
        this.balance = amount;
    }
}
