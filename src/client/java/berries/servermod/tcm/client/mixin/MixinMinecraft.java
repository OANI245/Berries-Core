package berries.servermod.tcm.client.mixin;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.client.Config;
import berries.servermod.tcm.client.TCMClient;
import berries.servermod.tcm.client.screen.CrashedScreen;
import berries.servermod.tcm.util.TCMComponent;
import icyllis.modernui.core.Clipboard;
import icyllis.modernui.mc.ModernUIClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.MemoryReserve;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(value = Minecraft.class)
public abstract class MixinMinecraft {
    @Shadow
    public ClientLevel level;

    @Shadow
    public LocalPlayer player;

    @Final
    @Shadow
    public LevelRenderer levelRenderer;

    @Shadow
    private boolean isLocalServer;

    @Shadow
    private IntegratedServer singleplayerServer;

    @Final
    @Shadow
    public File gameDirectory;

    @Shadow
    public abstract void tick();

    @Shadow public abstract void setScreen(@Nullable Screen screen);

    @Unique
    private long showedTimeMessageTime = 0;

    @Unique
    private static CrashReport crashReport = null;

    @Inject(
            method = "setScreen",
            at = @At("HEAD"),
            cancellable = true)
    private void injected01(Screen screen, CallbackInfo ci) {
        if (TCM.lockMinecraftScreen && screen != null) {
            ci.cancel();
        }
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void injected02(CallbackInfo ci) {
        if (this.level == null) return;

        long tick = getDayTime(this.level.getDayTime());
        if (Config.INSTANCE.sendMorningAndNightMessage) {
            if (showedTimeMessageTime != 0 && !Config.INSTANCE.timeMessages.containsKey(tick)) {
                this.showedTimeMessageTime = 0;
            } else if (TCMClient.syncTimeMessages.isEmpty() && Config.INSTANCE.timeMessages.containsKey(tick) && showedTimeMessageTime != tick) {
                this.showedTimeMessageTime = tick;
                MutableComponent text = Config.INSTANCE.timeMessages.get(tick);
                this.player.sendSystemMessage(text);
            } else if (TCMClient.syncTimeMessages.containsKey(tick) && showedTimeMessageTime != tick) {
                this.showedTimeMessageTime = tick;
                MutableComponent text = TCMClient.syncTimeMessages.get(tick);
                this.player.sendSystemMessage(text);
            }
        }
    }

    @Inject(
            method = "onGameLoadFinished",
            at = @At("TAIL")
    )
    public void init(CallbackInfo ci) {
        Config.INSTANCE.readConfig();
        if (Config.INSTANCE.isFirstUse) {
            TCM.LOGGER.info("Setting ModernUI Config......");
            icyllis.modernui.mc.Config.TEXT.mUseTextShadersInWorld.set(false);
            icyllis.modernui.mc.Config.TEXT.mMinPixelDensityForSDF.set(8);
            icyllis.modernui.mc.Config.CLIENT.mBlurEffect.set(false);

            setDefaultFont();

            Config.INSTANCE.isFirstUse = false;
            Config.INSTANCE.saveConfig();
        }

        if (icyllis.modernui.mc.Config.CLIENT.mFirstFontFamily.get().contains("Frozen")) {
            setDefaultFont();
        }

        TCMClient.loadedRunnable$1.run();
    }

    @Unique
    private static void setDefaultFont() {
        icyllis.modernui.mc.Config.CLIENT.mFirstFontFamily.set("MiSans Latin Medium");
        List<String> fallbackDefault = new ArrayList<>(icyllis.modernui.mc.Config.CLIENT.mFallbackFontFamilyList.getDefault());
        if (!fallbackDefault.contains("Source Han Sans CN Medium")) {
            fallbackDefault.add(0, "Source Han Sans CN Medium");
        }
        icyllis.modernui.mc.Config.CLIENT.mFallbackFontFamilyList.set(fallbackDefault);
        ModernUIClient.getInstance().reloadFontStrike();
        ModernUIClient.getInstance().reloadTypeface();
    }

    @Redirect(
            method = "run",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;emergencySave()V")
    )
    public void injected03(Minecraft instance) {
        try {
            MemoryReserve.release();
            instance.levelRenderer.clear();
        } catch (Throwable var3) {
        }

        try {
            System.gc();
            if (this.isLocalServer && this.singleplayerServer != null) {
                this.singleplayerServer.halt(true);
            }

            instance.clearLevel(new GenericDirtMessageScreen(TCMComponent.translatable("gui.tcm.crash.message")));
        } catch (Throwable var2) {
        }

        System.gc();
    }

    @Inject(
            method = "crash",
            at = @At("HEAD"),
            cancellable = true)
    private static void injected04(CrashReport crashReport, CallbackInfo ci) {
        MixinMinecraft.crashReport = crashReport;
        Minecraft.getInstance().tell(() -> {
            openCrashedScreen(crashReport);
        });
        openCrashedScreen(crashReport);
        ci.cancel();
    }

    @Unique
    private static void crashLegacy(CrashReport crashReport) {
        File file = new File(Minecraft.getInstance().gameDirectory, "crash-reports");
        File file2 = new File(file, "crash-" + Util.getFilenameFormattedDateTime() + "-client.txt");
        Bootstrap.realStdoutPrintln(crashReport.getFriendlyReport());
        if (crashReport.getSaveFile() != null) {
            Bootstrap.realStdoutPrintln("#@!@# Game crashed! Crash report saved to: #@!@# " + crashReport.getSaveFile());
            System.exit(-1);
        } else if (crashReport.saveToFile(file2)) {
            Bootstrap.realStdoutPrintln("#@!@# Game crashed! Crash report saved to: #@!@# " + file2.getAbsolutePath());
            System.exit(-1);
        } else {
            Bootstrap.realStdoutPrintln("#@?@# Game crashed! Crash report could not be saved. #@?@#");
            System.exit(-2);
        }
    }

    @Unique
    private static void openCrashedScreen(CrashReport crashReport) {
        Minecraft.getInstance().setScreen(new CrashedScreen(() -> {
            File file = new File(Minecraft.getInstance().gameDirectory, "crash-reports");
            File file2 = new File(file, "crash-" + Util.getFilenameFormattedDateTime() + "-client.txt");
            Bootstrap.realStdoutPrintln(crashReport.getFriendlyReport());
            Clipboard.setText(crashReport.getFriendlyReport());
            if (crashReport.getSaveFile() != null) {
                Bootstrap.realStdoutPrintln("#@!@# Game crashed! Crash report saved to: #@!@# " + crashReport.getSaveFile());
                System.exit(-1);
            } else if (crashReport.saveToFile(file2)) {
                Bootstrap.realStdoutPrintln("#@!@# Game crashed! Crash report saved to: #@!@# " + file2.getAbsolutePath());
                System.exit(-1);
            } else {
                Bootstrap.realStdoutPrintln("#@?@# Game crashed! Crash report could not be saved. #@?@#");
                System.exit(-2);
            }
        }));
    }

    @Inject(
            method = "destroy",
            at = @At(value = "INVOKE", target = "Ljava/lang/System;exit(I)V")
    )
    public void injected05(CallbackInfo ci) {
        if (crashReport != null) {
            crashLegacy(crashReport);
            return;
        }

        if (Config.INSTANCE.isInstalling) {
            File tmpJava = new File(gameDirectory.getPath() + File.separator + "Uninstaller.java");
            if (tmpJava.exists()) {
                tmpJava.delete();
            }

            try {
                new ProcessBuilder()
                        .command("java", "Uninstaller", TCM.class.getProtectionDomain().getCodeSource().getLocation()
                                .toURI().getPath().replaceAll("\\s", "?"))
                        .directory(new File(Minecraft.getInstance().gameDirectory.getPath()))
                        .start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.exit(0);
    }

    @Unique
    private long getDayTime(long time) {
        return (time - 24000 * (long) Math.floor((double) time / 24000));
    }
}
