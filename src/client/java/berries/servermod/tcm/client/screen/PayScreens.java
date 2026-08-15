package berries.servermod.tcm.client.screen;

import berries.servermod.tcm.UFEInfo;
import berries.servermod.tcm.client.flueroui.FlueroUI;
import berries.servermod.tcm.client.flueroui.widget.FlueroButton;
import berries.servermod.tcm.client.screen.widget.MetroTileType2;
import berries.servermod.tcm.client.util.PayClient;
import berries.servermod.tcm.util.TCMComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public final class PayScreens {
    private final Screen perviousScreen;
    public final FirstScreen firstScreen;
    public final CashPayScreen cashPayScreen;

    public final float money;

    private static final int PANEL_WIDTH = 380;
    private static final int PANEL_HEIGHT = 280;
    private static final int PANEL_PADDING = 24;
    private static final int TILE_WIDTH = 150;
    private static final int TILE_HEIGHT = 140;
    private static final int TILE_GAP = 20;

    private PayScreens(Screen perviousScreen, float money) {
        this.perviousScreen = perviousScreen;
        this.firstScreen = new FirstScreen();
        this.cashPayScreen = new CashPayScreen();
        this.money = money;
    }

    public static PayScreens newInstance(Screen perviousScreen, float money) {
        return new PayScreens(perviousScreen, money);
    }

    public final class FirstScreen extends Screen {
        FirstScreen() {
            super(TCMComponent.translatable("gui.tcm.pay_screen.first_screen.title"));
        }

        @Override
        protected void init() {
            super.init();

            if (money <= 0 && minecraft != null) {
                minecraft.setScreen(perviousScreen);
                return;
            }

            int panelX = width / 2 - PANEL_WIDTH / 2;
            int panelY = height / 2 - PANEL_HEIGHT / 2;

            int tilesStartX = panelX + PANEL_PADDING;
            int tilesStartY = panelY + PANEL_PADDING + 60;

            int tileCenterX = panelX + PANEL_WIDTH / 2;

            addRenderableWidget(new FlueroButton(
                    panelX + PANEL_WIDTH - 60 - PANEL_PADDING,
                    panelY + PANEL_PADDING + 4,
                    60, 18,
                    TCMComponent.text("返回"),
                    (button) -> {
                        if (minecraft != null) {
                            minecraft.setScreen(PayScreens.this.perviousScreen);
                        }
                    }));

            addRenderableWidget(new MetroTileType2(
                    tileCenterX - TILE_WIDTH - TILE_GAP / 2,
                    tilesStartY,
                    TILE_WIDTH,
                    TILE_HEIGHT,
                    new ResourceLocation(UFEInfo.MOD_ID, "textures/item/tcy_10.png"),
                    TCMComponent.text("现金支付"),
                    (button) -> {
                        if (minecraft != null) {
                            minecraft.setScreen(cashPayScreen);
                        }
                    }));

            addRenderableWidget(new MetroTileType2(
                    tileCenterX + TILE_GAP / 2,
                    tilesStartY,
                    TILE_WIDTH,
                    TILE_HEIGHT,
                    new ResourceLocation(UFEInfo.MOD_ID, "textures/item/credit_card.png"),
                    TCMComponent.text("银行卡支付"),
                    (button) -> {
                        if (minecraft != null) {
                            minecraft.setScreen(perviousScreen);
                        }
                    }) {{this.active = false; this.setTooltip(Tooltip.create(TCMComponent.text("此功能暂时无法使用"))); this.sendStyleChanged();}});
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
            renderBackground(guiGraphics);

            if (minecraft == null) {
                return;
            }

            int panelX = width / 2 - PANEL_WIDTH / 2;
            int panelY = height / 2 - PANEL_HEIGHT / 2;
            int panelMaxX = panelX + PANEL_WIDTH;
            int panelMaxY = panelY + PANEL_HEIGHT;

            guiGraphics.fill(panelX, panelY, panelMaxX, panelMaxY, FlueroUI.argb(220, 32, 32, 32));

            guiGraphics.fill(panelX, panelY, panelMaxX, panelY + 1, FlueroUI.rgb(60, 60, 60));
            guiGraphics.fill(panelX, panelY, panelX + 1, panelMaxY, FlueroUI.rgb(60, 60, 60));
            guiGraphics.fill(panelX, panelMaxY - 1, panelMaxX, panelMaxY, FlueroUI.rgb(60, 60, 60));
            guiGraphics.fill(panelMaxX - 1, panelY, panelMaxX, panelMaxY, FlueroUI.rgb(60, 60, 60));

            guiGraphics.fill(panelX + 1, panelY + 1, panelMaxX - 1, panelY + 1 + 40, FlueroUI.argb(180, 45, 45, 45));

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(2, 2, 2);
            guiGraphics.drawCenteredString(this.minecraft.font,
                    this.title,
                    width / 4,
                    (panelY + PANEL_PADDING) / 2,
                    0xFFFFFF);
            guiGraphics.pose().popPose();

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(1.2f, 1.2f, 1.2f);
            guiGraphics.drawCenteredString(this.minecraft.font,
                    TCMComponent.text("支付金额: ").copy().append(
                            TCMComponent.text(money + "฿").copy().withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD))
                    ),
                    (int)(width / 2 / 1.2f),
                    (int)((panelY + PANEL_PADDING + 35) / 1.2f),
                    0xCCCCCC);
            guiGraphics.pose().popPose();

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(0.85f, 0.85f, 0.85f);
            guiGraphics.drawCenteredString(this.minecraft.font,
                    TCMComponent.text("请选择支付方式"),
                    (int)(width / 2 / 0.85f),
                    (int)((panelY + PANEL_PADDING + 58) / 0.85f),
                    0x999999);
            guiGraphics.pose().popPose();

            super.render(guiGraphics, i, j, f);
        }

        @Override
        public void onClose() {
            if (minecraft != null) {
                minecraft.setScreen(PayScreens.this.perviousScreen);
            }
        }
    }

    public final class CashPayScreen extends Screen {
        private static final int DIALOG_WIDTH = 320;
        private static final int DIALOG_HEIGHT = 240;

        CashPayScreen() {
            super(TCMComponent.text(""));
        }

        @Override
        protected void init() {
            super.init();

            int dialogX = width / 2 - DIALOG_WIDTH / 2;
            int dialogY = height / 2 - DIALOG_HEIGHT / 2;
            int buttonWidth = 100;
            int buttonHeight = 22;
            int buttonY = dialogY + DIALOG_HEIGHT - 36;

            addRenderableWidget(new FlueroButton(
                    width / 2 - buttonWidth - 8,
                    buttonY,
                    buttonWidth,
                    buttonHeight,
                    TCMComponent.text("取消"),
                    (button) -> {
                        if (minecraft != null) {
                            minecraft.setScreen(PayScreens.this.firstScreen);
                        }
                    }));

            addRenderableWidget(new FlueroButton(
                    width / 2 + 8,
                    buttonY,
                    buttonWidth,
                    buttonHeight,
                    TCMComponent.text("确认支付"),
                    true,
                    (button) -> {
                        if (minecraft != null) {
                            PayClient.cashPay(PayScreens.this.money);
                        }
                    }));
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
            if (minecraft == null) {
                return;
            }

            renderBackground(guiGraphics);

            int dialogX = width / 2 - DIALOG_WIDTH / 2;
            int dialogY = height / 2 - DIALOG_HEIGHT / 2;
            int dialogMaxX = dialogX + DIALOG_WIDTH;
            int dialogMaxY = dialogY + DIALOG_HEIGHT;

            guiGraphics.fill(dialogX, dialogY, dialogMaxX, dialogMaxY, FlueroUI.argb(240, 32, 32, 32));

            guiGraphics.fill(dialogX, dialogY, dialogMaxX, dialogY + 1, FlueroUI.rgb(70, 70, 70));
            guiGraphics.fill(dialogX, dialogY, dialogX + 1, dialogMaxY, FlueroUI.rgb(70, 70, 70));
            guiGraphics.fill(dialogX, dialogMaxY - 1, dialogMaxX, dialogMaxY, FlueroUI.rgb(70, 70, 70));
            guiGraphics.fill(dialogMaxX - 1, dialogY, dialogMaxX, dialogMaxY, FlueroUI.rgb(70, 70, 70));

            guiGraphics.fill(dialogX + 1, dialogY + 1, dialogMaxX - 1, dialogY + 1 + 48, FlueroUI.argb(180, 50, 50, 50));

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(1.5f, 1.5f, 1.5f);
            guiGraphics.drawCenteredString(this.minecraft.font,
                    TCMComponent.text("确认支付"),
                    (int)(width / 2 / 1.5f),
                    (int)((dialogY + 18) / 1.5f),
                    0xFFFFFF);
            guiGraphics.pose().popPose();

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(3f, 3f, 3f);
            guiGraphics.drawCenteredString(this.minecraft.font,
                    TCMComponent.text(PayScreens.this.money + "฿").copy().withStyle(
                            Style.EMPTY
                                    .withFont(new ResourceLocation("tcm:nsc"))
                                    .withColor(ChatFormatting.GOLD)
                    ),
                    (int)(width / 2 / 3),
                    (int)((dialogY + 70) / 3),
                    0xFFFFFF);
            guiGraphics.pose().popPose();

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(1.1f, 1.1f, 1.1f);
            guiGraphics.drawCenteredString(this.minecraft.font,
                    TCMComponent.text("确认使用现金支付？").copy().withStyle(
                            Style.EMPTY.withFont(new ResourceLocation("tcm:nsc"))
                    ),
                    (int)(width / 2 / 1.1f),
                    (int)((dialogY + 130) / 1.1f),
                    0xCCCCCC);
            guiGraphics.pose().popPose();

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(0.75f, 0.75f, 0.75f);
            guiGraphics.drawCenteredString(this.minecraft.font,
                    TCMComponent.text("安全提示：支付前请确认金额无误").copy().withStyle(
                            Style.EMPTY.withColor(ChatFormatting.GRAY)
                    ),
                    (int)(width / 2 / 0.75f),
                    (int)((dialogY + 165) / 0.75f),
                    0x888888);
            guiGraphics.drawCenteredString(this.minecraft.font,
                    TCMComponent.text("支付完成后将无法撤销").copy().withStyle(
                            Style.EMPTY.withColor(ChatFormatting.GRAY)
                    ),
                    (int)(width / 2 / 0.75f),
                    (int)((dialogY + 178) / 0.75f),
                    0x888888);
            guiGraphics.pose().popPose();

            super.render(guiGraphics, i, j, f);
        }

        @Override
        public void onClose() {
            if (minecraft != null) {
                minecraft.setScreen(PayScreens.this.perviousScreen);
            }
        }
    }
}