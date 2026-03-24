package berries.servermod.tcm.client.datafix;

import berries.servermod.tcm.UFEInfo;
import berries.servermod.tcm.client.screen.overlay.PrepOverlay;
import icyllis.modernui.mc.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("all")
@Environment(EnvType.CLIENT)
public final class ClientDataFixes {
    private static final Map<Integer, Runnable> dataFixes = new HashMap<>();

    static {
        dataFixes.put(249, () -> {
            Config.CLIENT.mFirstFontFamily.set("MiSans Latin Medium");
            List<String> fallbackDefault = new ArrayList<>(Config.CLIENT.mFallbackFontFamilyList.getDefault());
            if (!fallbackDefault.contains("Source Han Sans CN Medium")) {
                fallbackDefault.add(0, "Source Han Sans CN Medium");
            }
            Config.CLIENT.mFallbackFontFamilyList.set(fallbackDefault);
        });
    }

    public static void runDataFix(int pnb) {
        dataFixes.getOrDefault(pnb, () -> {}).run();
    }

    public static void runUpdateFix() {
        dataFixes.forEach((key, value) -> {
            if (key <= UFEInfo.PNB_VERSION && key > berries.servermod.tcm.client.Config.INSTANCE.lastBuildVersion) {
                runDataFix(key);
            }
        });
    }

    private static void showPrep(AtomicBoolean isClose) {
        Minecraft.getInstance().tell(() -> {
            Minecraft.getInstance().setOverlay(new PrepOverlay(
                    Minecraft.getInstance(),
                    isClose,
                    (throwable) -> {
                        Minecraft.getInstance().setScreen(new TitleScreen(true));
                        Minecraft.getInstance().setOverlay(null);
                    },
                    true,
                    false
            ));
        });
    }
}
