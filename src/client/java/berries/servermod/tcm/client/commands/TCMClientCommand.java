package berries.servermod.tcm.client.commands;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.UFEInfo;
import berries.servermod.tcm.client.Config;
import berries.servermod.tcm.client.TCMClient;
import berries.servermod.tcm.client.data.TCMDynamicResourceCacheV2;
import berries.servermod.tcm.client.packet.PacketModVersionCheckClient;
import berries.servermod.tcm.client.packet.PacketServerVersionQuery;
import berries.servermod.tcm.client.screen.*;
import berries.servermod.tcm.client.screen.fragments.MainFragment;
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
import icyllis.modernui.mc.MuiModApi;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
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
                                                                TCMComponent.translatable("gui.tcm.update_dialog.content", "nothing", 0),
                                                                true
                                                        );
                                                        instance.dialogWidth = 240;
                                                        instance.buttonSettings.add(0, new Tuple<Component, BiConsumer<Minecraft, AbstractButton>>(TCMComponent.translatable("gui.tcm.update_dialog.updateButton.text"), ($, btn) -> {
                                                            Util.getPlatform().openUri("http://" + TCMClient.syncDirectionIp);
                                                        }));
                                                        mc.setScreen(instance);
                                                    }
                                                    case 2 -> {
                                                        mc.setScreen(
                                                                new VersionLowScreen("nothing", 0, "nothing", 0)
                                                        );
                                                    }
                                                    case 3 -> {
                                                        MuiModApi.openScreen(new MainFragment());
                                                    }
                                                    case 4 -> {
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
                                                    case 5 -> {
                                                        PayClient.pay(null, 5.0F, (status) -> {});
                                                    }
                                                    case 6 -> {
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
