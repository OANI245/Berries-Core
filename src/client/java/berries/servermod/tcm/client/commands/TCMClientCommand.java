package berries.servermod.tcm.client.commands;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.UFEInfo;
import berries.servermod.tcm.client.Config;
import berries.servermod.tcm.client.TCMClient;
import berries.servermod.tcm.client.data.TCMDynamicResourceCacheV2;
import berries.servermod.tcm.client.packet.PacketModVersionCheckClient;
import berries.servermod.tcm.client.packet.PacketServerVersionQuery;
import berries.servermod.tcm.client.screen.*;
import berries.servermod.tcm.client.screen.overlay.PrepOverlay;
import berries.servermod.tcm.client.util.ModUpdate;
import berries.servermod.tcm.client.util.PayClient;
import berries.servermod.tcm.util.TCMComponent;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class TCMClientCommand {
    private static final boolean DEBUG_MODE = false;

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(
                (d, c) -> {
                    d.register(
                            ClientCommandManager.literal("tcm").then(
                                    ClientCommandManager.literal("checkupdate").executes(TCMClientCommand::checkUpdates)
                            ).then(ClientCommandManager.literal("testscreen").then(
                                    ClientCommandManager.argument("screen", IntegerArgumentType.integer()).executes((ctx) -> {
                                        if (!Config.INSTANCE.developerMode) {
                                            ctx.getSource().sendError(TCMComponent.text("You are not open the developer mode."));
                                            return 0;
                                        }

                                        int argument = ctx.getArgument("screen", Integer.TYPE);
                                        Minecraft mc = Minecraft.getInstance();
                                        mc.tell(() -> {
                                            try {
                                                switch (argument) {
                                                    case 0 -> {
                                                        TCMMainScreen.open(mc, null);
                                                    }
                                                    case 1 -> {
                                                        TCMDialogScreen instance = new TCMDialogScreen(
                                                                TCMComponent.translatable("gui.tcm.update_dialog.title"),
                                                                TCMComponent.translatable("gui.tcm.update_dialog.content", "1. Nothing|2. Nothing|3. Nothing"),
                                                                true
                                                        );
                                                        instance.dialogWidth = 250;
                                                        instance.dialogHeight = 173;
                                                        instance.isUpdateDialog = true;
                                                        instance.newVersion = "nothing";
                                                        instance.buttonSettings.addFirst(new ObjectObjectImmutablePair<>(new ObjectObjectImmutablePair<>(TCMComponent.translatable("gui.tcm.update_dialog.updateButton.text"), true), ($, btn) -> {
                                                            Util.getPlatform().openUri("http://" + TCMClient.syncDirectionIp);
                                                        }));
                                                        mc.setScreen(instance);
                                                    }
                                                    case 2 -> {
                                                        TCMDialogScreen instance = new TCMDialogScreen(
                                                                TCMComponent.translatable("gui.tcm.version_check_disconnect.title"),
                                                                TCMComponent.translatable("gui.tcm.version_check_disconnect.description", UFEInfo.MOD_VERSION, UFEInfo.PNB_VERSION, UFEInfo.MOD_VERSION, UFEInfo.PNB_VERSION, UFEInfo.MOD_VERSION, UFEInfo.PNB_VERSION),
                                                                false
                                                        );
                                                        instance.dialogWidth = 250;
                                                        instance.dialogHeight = 173;
                                                        instance.isUpdateDialog = true;
                                                        instance.newVersion = "nothing";
                                                        instance.screenOnClose = new JoinMultiplayerScreen(null);
                                                        instance.buttonSettings.addFirst(new ObjectObjectImmutablePair<>(new ObjectObjectImmutablePair<>(TCMComponent.translatable("gui.tcm.version_check_disconnect.dnvButton.text"), true), ($, btn) -> {
                                                            Util.getPlatform().openUri("http://" + TCMClient.syncDirectionIp);
                                                        }));
                                                        instance.buttonSettings.addFirst(new ObjectObjectImmutablePair<>(new ObjectObjectImmutablePair<>(TCMComponent.translatable("gui.tcm.version_check_disconnect.updateButton.text"), false), ($, btn) -> {
                                                            ModUpdate.screens(false);
                                                        }));
                                                        mc.setScreen(instance);
                                                    }
                                                    case 3 -> {
                                                        new Thread(() -> {
                                                            try {
                                                                Thread.sleep(20000);
                                                                mc.tell(() -> {
                                                                    mc.setOverlay(null);
                                                                });
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        }).start();
                                                        mc.setOverlay(
                                                                new PrepOverlay(
                                                                        mc, new AtomicBoolean(false),
                                                                        (o) -> {
                                                                            mc.setOverlay(null);
                                                                            mc.setScreen(null);
                                                                        },
                                                                        true, true
                                                                )
                                                        );
                                                    }
                                                    case 4 -> {
                                                        PayClient.pay(null, 5.0F, (status) -> {});
                                                    }
                                                    case 5 -> {
                                                        Minecraft.getInstance().tell(() -> {
                                                            Throwable t = new AssertionError("Testing Screen 6");
                                                            Minecraft.getInstance().emergencySave();
                                                            Minecraft.crash(Minecraft.getInstance().fillReport(new CrashReport("Testing Screen", t)));
                                                        });
                                                    }
                                                    default -> {
                                                        ctx.getSource().sendFeedback(TCMComponent.text("Unknown screen"));
                                                    }
                                                }
                                            } catch (Throwable e) {
                                                ctx.getSource().sendError(TCMComponent.text("Failed to load screen"));
                                                ctx.getSource().sendError(TCMComponent.text("StackTrace: " + e.getLocalizedMessage()));
                                                Arrays.stream(e.getStackTrace()).forEach((element) -> {
                                                    ctx.getSource().sendError(TCMComponent.text(element.toString()));
                                                });
                                            }
                                        });
                                        return 1;
                                    })
                            )).then(ClientCommandManager.literal("refreshresources").executes((ctx) -> {
                                TCMDynamicResourceCacheV2.instance.reload();
                                TCMDynamicResourceCacheV2.instance.refresh();
                                return 1;
                            })).executes((ctx) -> {
                                Minecraft mc = Minecraft.getInstance();
                                mc.tell(() -> {
                                    TCMMainScreen.open(mc, null);
                                });
                                return 1;
                            }).then(ClientCommandManager.literal("debugupdate").executes((ctx) -> {
                                if (!Config.INSTANCE.developerMode) {
                                    ctx.getSource().sendError(TCMComponent.text("You are not open the developer mode."));
                                    return 0;
                                }
                                ModUpdate.screens(false);
                                return 1;
                            }))
                    );
                }
        );
    }

    private static <T> int checkUpdates(CommandContext<T> ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (DEBUG_MODE) {
            mc.tell(() -> {
                System.out.println("Show Dialog");
                mc.setScreen(new TCMDialogScreen(TCMComponent.translatable("gui.tcm.update_dialog.title"), TCMComponent.translatable("gui.tcm.update_dialog.content", UFEInfo.MOD_VERSION, UFEInfo.PNB_VERSION), true));
            });
        } else {
            PacketServerVersionQuery.sendVersionQueryC2S(true);
        }
        return 1;
    }
}
