package berries.servermod.tcm.client.packet;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.UFEInfo;
import berries.servermod.tcm.client.TCMClient;
import berries.servermod.tcm.client.screen.TCMDialogScreen;
import berries.servermod.tcm.util.TCMComponent;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Style;

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
            mc.tell(() -> {
                mc.setScreen(new TCMDialogScreen(TCMComponent.translatable("gui.tcm.update_dialog.title"), TCMComponent.translatable("gui.tcm.update_dialog.content", serverVer, serverPNB), true));
            });
        } else {
            if (mc.player != null) {
                mc.player.sendSystemMessage(TCMComponent.translatable("gui.tcm.output.no_updates").copy().withStyle(Style.EMPTY.withColor(0x10893E)));
            }
        }
    }
}
