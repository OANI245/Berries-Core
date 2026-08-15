package berries.servermod.tcm.client.packet;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.UFEInfo;
import berries.servermod.tcm.client.TCMClient;
import berries.servermod.tcm.client.screen.TCMDialogScreen;
import berries.servermod.tcm.util.TCMComponent;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Tuple;

import java.util.function.BiConsumer;

public class PacketServerVersionQuery {
    public static void sendVersionQueryC2S(boolean openScreen) {
        final FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeBoolean(openScreen);
        ClientPlayNetworking.send(TCM.PACKET_SERVER_VERSION_QUERY, packet);
    }

    public static void receiveVersionValueS2C(FriendlyByteBuf packet) {
        String serverVer = packet.readUtf();
        int serverPNB = packet.readInt();
        Minecraft mc = Minecraft.getInstance();
        if (packet.readBoolean() && UFEInfo.PNB_VERSION < serverPNB) {
            var cll = packet.readInt();
            String[] changelogs = new String[Math.min(cll, 5)];
            for (int i = 0; i < changelogs.length; i++)  {
                changelogs[i] = packet.readUtf();
                if (i >= changelogs.length - 1) {
                    changelogs[i] += "...";
                }
            }
            mc.tell(() -> {
                StringBuilder changeLogs = new StringBuilder();
                for (int i = 0; i < changelogs.length; i++) {
                    if (!changeLogs.isEmpty()) {
                        changeLogs.append("|");
                    }
                    changeLogs.append(i + 1);
                    changeLogs.append(". ");
                    changeLogs.append(changelogs[i]);
                    if (changelogs.length < cll && i >= changelogs.length - 1) {
                        changeLogs.append("......");
                    }
                }
                var instance = new TCMDialogScreen(TCMComponent.translatable("gui.tcm.update_dialog.title"),
                        TCMComponent.translatable("gui.tcm.update_dialog.content", changeLogs.toString()), true);
                instance.dialogWidth = 250;
                instance.dialogHeight = 173;
                instance.isUpdateDialog = true;
                instance.newVersion = serverVer + " #" + serverPNB;
                instance.buttonSettings.addFirst(new ObjectObjectImmutablePair<>(new ObjectObjectImmutablePair<>(TCMComponent.translatable("gui.tcm.update_dialog.updateButton.text"), true), ($, btn) -> {
                    Util.getPlatform().openUri("http://" + TCMClient.syncDirectionIp);
                }));
                mc.setScreen(instance);
            });
        } else {
            if (mc.player != null) {
                mc.player.sendSystemMessage(TCMComponent.translatable("gui.tcm.output.no_updates").copy().withStyle(Style.EMPTY.withColor(0x10893E)));
            }
        }
    }
}
