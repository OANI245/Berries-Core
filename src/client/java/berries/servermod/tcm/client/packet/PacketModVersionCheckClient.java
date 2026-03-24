package berries.servermod.tcm.client.packet;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.UFEInfo;
import berries.servermod.tcm.client.Config;
import berries.servermod.tcm.client.screen.TCMDialogScreen;
import berries.servermod.tcm.client.screen.VersionLowScreen;
import berries.servermod.tcm.util.TCMComponent;
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

import java.lang.reflect.Field;

public class PacketModVersionCheckClient {
    public static void receiveVersionCheckS2C(FriendlyByteBuf packet) {
        String serverVersion = packet.readUtf();
        int[] builds = packet.readVarIntArray();
        if (builds.length < 2) return;
        int serverLowestBuild = builds[0];
        int serverBuild = builds[1];
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (UFEInfo.PNB_VERSION < serverLowestBuild) {
                final ClientPacketListener connection = mc.getConnection();
                if (connection != null) {
                    mc.setScreen(new VersionLowScreen(UFEInfo.MOD_VERSION, UFEInfo.PNB_VERSION, serverVersion, serverBuild));
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
                    mc.setScreen(new TCMDialogScreen(TCMComponent.translatable("gui.tcm.update_dialog.title"), TCMComponent.translatable("gui.tcm.update_dialog.content", serverVersion, serverBuild), true));
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
