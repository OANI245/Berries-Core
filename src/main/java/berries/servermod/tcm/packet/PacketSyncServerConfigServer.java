package berries.servermod.tcm.packet;

import berries.servermod.tcm.ServerConfig;
import berries.servermod.tcm.TCM;
import berries.servermod.tcm.util.Packets;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class PacketSyncServerConfigServer {
    public static void sendSyncServerTeleportsS2C(ServerPlayer player, int index) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());

        packet.writeInt(index);
        packet.writeUtf(ServerConfig.INSTANCE.teleports.get(index).getA().getA());
        packet.writeUtf(ServerConfig.INSTANCE.teleports.get(index).getA().getB());
        Packets.writeVec3(packet, ServerConfig.INSTANCE.teleports.get(index).getB());

        ServerPlayNetworking.send(player, TCM.PACKET_SYNC_TELEPORTS, packet);
    }

    public static void sendSyncServerTimeMessagesS2C(ServerPlayer player, long time) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());

        if (!ServerConfig.INSTANCE.timeMessages.containsKey(time)) {
            return;
        }

        packet.writeLong(time);
        packet.writeComponent(ServerConfig.INSTANCE.timeMessages.get(time));

        ServerPlayNetworking.send(player, TCM.PACKET_SYNC_TIME_MESSAGES, packet);
    }

    public static void sendSyncDirectionIPS2C(ServerPlayer player) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());

        if (ServerConfig.INSTANCE.directionIP.isEmpty()) {
            return;
        }

        packet.writeUtf(ServerConfig.INSTANCE.directionIP);
        ServerPlayNetworking.send(player, TCM.PACKET_SYNC_DIRECTION_IP, packet);
    }

    public static void sendClearServerTeleportsS2C(ServerPlayer player) {
        ServerPlayNetworking.send(player, TCM.PACKET_CLEAR_TELEPORTS, new FriendlyByteBuf(Unpooled.buffer()));
    }

    public static void sendClearServerTimeMessagesS2C(ServerPlayer player) {
        ServerPlayNetworking.send(player, TCM.PACKET_CLEAR_TIME_MESSAGES, new FriendlyByteBuf(Unpooled.buffer()));
    }
}
