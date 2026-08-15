package berries.servermod.tcm.client.packet;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.UFEInfo;
import berries.servermod.tcm.client.Config;
import berries.servermod.tcm.client.TCMClient;
import berries.servermod.tcm.client.screen.TCMDialogScreen;
import berries.servermod.tcm.client.util.ModUpdate;
import berries.servermod.tcm.util.TCMComponent;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.TellRawCommand;
import net.minecraft.server.commands.TitleCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;

import java.lang.reflect.Field;

public class PacketModVersionCheckClient {
    public static void receiveVersionCheckS2C(FriendlyByteBuf packet) {
        String serverVersion = packet.readUtf();
        int[] builds = packet.readVarIntArray();
        if (builds.length < 2) return;
        int serverLowestBuild = builds[0];
        int serverBuild = builds[1];
        Minecraft mc = Minecraft.getInstance();
        var cll = packet.readInt();
        String[] serverChangelogs = new String[Math.min(cll, 5)];
        for (int i = 0; i < serverChangelogs.length; i++) {
            serverChangelogs[i] = packet.readUtf();
        }
        mc.execute(() -> {
            if (UFEInfo.PNB_VERSION < serverLowestBuild) {
                final ClientPacketListener connection = mc.getConnection();
                if (connection != null) {
                    TCMDialogScreen instance = new TCMDialogScreen(
                            TCMComponent.translatable("gui.tcm.version_check_disconnect.title"),
                            TCMComponent.translatable("gui.tcm.version_check_disconnect.description", serverVersion, serverBuild, UFEInfo.MOD_VERSION, UFEInfo.PNB_VERSION, serverVersion, serverLowestBuild),
                            false
                    );
                    instance.dialogWidth = 250;
                    instance.dialogHeight = 173;
                    instance.isUpdateDialog = true;
                    instance.newVersion = serverVersion + " #" + serverBuild;
                    instance.screenOnClose = new JoinMultiplayerScreen(null);
                    instance.buttonSettings.addFirst(new ObjectObjectImmutablePair<>(new ObjectObjectImmutablePair<>(TCMComponent.translatable("gui.tcm.version_check_disconnect.dnvButton.text"), true), ($, btn) -> {
                        Util.getPlatform().openUri("http://" + TCMClient.syncDirectionIp);
                    }));
                    instance.buttonSettings.addFirst(new ObjectObjectImmutablePair<>(new ObjectObjectImmutablePair<>(TCMComponent.translatable("gui.tcm.version_check_disconnect.updateButton.text"), false), ($, btn) -> {
                        ModUpdate.screens(false);
                    }));
                    mc.setScreen(instance);
                    /*mc.setScreen(new VersionLowScreen(UFEInfo.MOD_VERSION, UFEInfo.PNB_VERSION, serverVersion, serverBuild));*/
                    TCM.lockMinecraftScreen = true;
                    new Thread(() -> {
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        TCM.lockMinecraftScreen = false;
                    }).start();
                    connection.getConnection().disconnect(null);
                }
            } else if (UFEInfo.PNB_VERSION < serverBuild) {
                if (!Config.INSTANCE.neverShowUpdateDialog) {
                    StringBuilder changeLogs = new StringBuilder();
                    for (int i = 0; i < serverChangelogs.length; i++) {
                        if (!changeLogs.isEmpty()) {
                            changeLogs.append("|");
                        }
                        changeLogs.append(i + 1);
                        changeLogs.append(". ");
                        changeLogs.append(serverChangelogs[i]);
                        if (serverChangelogs.length < cll && i >= serverChangelogs.length - 1) {
                            changeLogs.append("......");
                        }
                    }
                    var instance = new TCMDialogScreen(TCMComponent.translatable("gui.tcm.update_dialog.title"),
                            TCMComponent.translatable("gui.tcm.update_dialog.content", changeLogs.toString()), true);
                    instance.dialogWidth = 250;
                    instance.dialogHeight = 173;
                    instance.isUpdateDialog = true;
                    instance.newVersion = serverVersion + " #" + serverBuild;
                    instance.buttonSettings.addFirst(new ObjectObjectImmutablePair<>(new ObjectObjectImmutablePair<>(TCMComponent.translatable("gui.tcm.update_dialog.updateButton.text"), true), ($, btn) -> {
                        Util.getPlatform().openUri("http://" + TCMClient.syncDirectionIp);
                    }));
                    mc.setScreen(instance);
                }
                if (mc.player != null) {
                    mc.player.sendSystemMessage(
                            TCMComponent.translatable("gui.tcm.output.update_info.first").copy()
                                    .withStyle(Style.EMPTY)
                                    .append(TCMComponent.translatable("gui.tcm.output.update_info.second").copy()
                                            .withStyle(Style.EMPTY.withUnderlined(true)
                                                    .withColor(0xF7630C)
                                                    .withClickEvent(
                                                            new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                                    "/tcm checkupdate")
                                                    )
                                            )
                                    ));
                }
            }
        });
    }
}
