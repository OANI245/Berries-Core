package berries.servermod.tcm.client.util;

import berries.servermod.tcm.client.packet.PacketPayClient;
import berries.servermod.tcm.client.screen.PayScreens;
import berries.servermod.tcm.util.TCMComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public final class PayClient {
    public static final int CASH_PAY = 0;
    public static final int CREDIT_CARD_PAY = 1;
    public static final int PHONE_PAY = 2;

    public static Consumer<PayStatus> onFinish = (status) -> {};

    public static PayStatus status = PayStatus.NO_PAYING;

    public static void pay(Screen screen, float money, Consumer<PayStatus> onFinish) {
        Minecraft mc = Minecraft.getInstance();
        PayScreens screens = PayScreens.newInstance(screen, money);

        status = PayStatus.PAYING;

        mc.tell(() -> {
            mc.setScreen(screens.firstScreen);
        });

        PayClient.onFinish = onFinish;
    }

    public static void cashPay(float money) {
        Minecraft mc = Minecraft.getInstance();
        AtomicReference<GenericDirtMessageScreen> processScreen = new AtomicReference<>(new GenericDirtMessageScreen(TCMComponent.text("正在支付")));

        ExecutorService payExecutor = Executors.newSingleThreadExecutor();

        mc.tell(() -> {
           mc.setScreen(processScreen.get());
        });

        PacketPayClient.sendPayC2S(CASH_PAY, money);

        payExecutor.submit(() -> {
            while(status == PayStatus.PAYING || status == PayStatus.NO_PAYING) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            switch(status) {
                case SUCCESS -> {
                    processScreen.set(new GenericDirtMessageScreen(TCMComponent.text("支付成功")));
                    mc.tell(() -> {
                        mc.setScreen(processScreen.get());
                    });

                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    mc.tell(() -> {
                        mc.setScreen(null);
                    });
                }
                case INSU_BALANCE -> {
                    processScreen.set(new GenericDirtMessageScreen(TCMComponent.text("余额不足")));
                    mc.tell(() -> {
                        mc.setScreen(processScreen.get());
                    });

                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    mc.tell(() -> {
                        mc.setScreen(null);
                    });
                }
                default -> {
                    processScreen.set(new GenericDirtMessageScreen(TCMComponent.text("支付失败")));
                    mc.tell(() -> {
                        mc.setScreen(processScreen.get());
                    });

                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    mc.tell(() -> {
                        mc.setScreen(null);
                    });
                }
            }
            onFinish.accept(status);
            onFinish = (status) -> {};
            status = PayStatus.NO_PAYING;
        });
    }
}
