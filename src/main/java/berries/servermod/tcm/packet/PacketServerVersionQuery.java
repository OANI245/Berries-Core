package berries.servermod.tcm.packet;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.TCMRegistry;
import berries.servermod.tcm.UFEInfo;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class PacketServerVersionQuery {
    public static void receiveVersionQueryC2S(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf packet, PacketSender responseSender) {
        sendVersionValueS2C(player, packet.readBoolean());
    }

    public static void sendVersionValueS2C(ServerPlayer player, boolean openScreen) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeUtf(UFEInfo.MOD_VERSION);
        packet.writeInt(UFEInfo.PNB_VERSION);
        packet.writeBoolean(openScreen);
        TCMRegistry.sendToPlayer(player, TCM.PACKET_SERVER_VERSION_QUERY, packet);
    }
}
