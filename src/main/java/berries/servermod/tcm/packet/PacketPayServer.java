package berries.servermod.tcm.packet;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.util.PayServer;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.function.Consumer;

public class PacketPayServer {
    public static void receivePayC2S(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl ignored, FriendlyByteBuf packet, PacketSender ignored1) {
        int type = packet.readInt();
        float money = packet.readFloat();

        PayServer.pay(server, player, money);
    }

    public static void sendPayScreenS2C(ServerPlayer player, float money, Consumer<Integer> onFinish) {
        PayServer.onFinishPlayersMap.put(player.getGameProfile(), onFinish);
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeFloat(money);
        ServerPlayNetworking.send(player, TCM.PACKET_PAY_SCREEN, packet);
    }

    public static void sendPayResultS2C(ServerPlayer player, int status) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeInt(status);
        ServerPlayNetworking.send(player, TCM.PACKET_PAY_RESULT, packet);
    }
}
