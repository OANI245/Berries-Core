package berries.servermod.tcm.client.screen;

import berries.servermod.tcm.UFEInfo;
import berries.servermod.tcm.client.screen.widget.MetroTileType2;
import berries.servermod.tcm.client.util.PayClient;
import berries.servermod.tcm.util.TCMComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
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
            }

            addRenderableWidget(new MetroTileType2(
                    this.width / 2 / 8, this.height / 2 / 2,
                    this.width / 2 / 16 * 13, this.height / 2 / 16 * 13,
                    new ResourceLocation(UFEInfo.MOD_ID, "textures/item/tcy_10.png"),
                    TCMComponent.text("现金支付"),
                    (button) -> {
                        if (minecraft != null) {
                            minecraft.setScreen(cashPayScreen);
                        }
                    }));

            addRenderableWidget(new MetroTileType2(
                    this.width - (this.width / 2 / 8 + this.width / 2 / 16 * 13), this.height / 2 / 2,
                    this.width / 2 / 16 * 13, this.height / 2 / 16 * 13,
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

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(2, 2, 2);
            guiGraphics.drawCenteredString(this.minecraft.font, this.title, this.width / 2 / 2, 30 / 2, 0xFFFFFF);
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
        CashPayScreen() {
            super(TCMComponent.text(""));
        }

        @Override
        protected void init() {
            super.init();

            addRenderableWidget(Button.builder(TCMComponent.text("支付"), (button) -> {
                if (minecraft != null) {
                    PayClient.cashPay(PayScreens.this.money);
                }
            }).pos(this.width / 2 - 50, height / 4 + 85).size(100, 20).build());
            addRenderableWidget(Button.builder(TCMComponent.text("取消"), (button) -> {
                if (minecraft != null) {
                    minecraft.setScreen(PayScreens.this.perviousScreen);
                }
            }).pos(this.width / 2 - 50, height / 4 + 110).size(100, 20).build());
        }

        @Override
        public void render(GuiGraphics guiGraphics, int i, int j, float f) {
            if (minecraft == null) {
                return;
            }

            renderBackground(guiGraphics);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(3, 3, 3);
            guiGraphics.drawCenteredString(
                    this.minecraft.font,
                    TCMComponent.text("您需要花费").copy().withStyle(
                            Style.EMPTY
                                    .withFont(new ResourceLocation("tcm:nsc"))
                    ), width / 2 / 3, height / 4 / 3, 0xFFFFFF
            );
            guiGraphics.drawCenteredString(
                    this.minecraft.font,
                    TCMComponent.text(PayScreens.this.money + "฿").copy().withStyle(
                            Style.EMPTY
                                    .withFont(new ResourceLocation("tcm:nsc"))
                                    .withColor(ChatFormatting.GOLD)
                    ), width / 2 / 3, height / 4 / 3 + 10, 0xFFFFFF
            );
            guiGraphics.pose().popPose();
            guiGraphics.drawCenteredString(
                    this.minecraft.font,
                    TCMComponent.text("确认支付？").copy().withStyle(
                            Style.EMPTY
                                    .withFont(new ResourceLocation("tcm:nsc"))
                    ), width / 2, height / 4 + 60, 0xFFFFFF
            );
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
