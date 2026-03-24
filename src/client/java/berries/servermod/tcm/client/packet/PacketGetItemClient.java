package berries.servermod.tcm.client.packet;

import berries.servermod.tcm.TCM;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;

public class PacketGetItemClient {
    public static void sendGetItemC2S(String signal, int count) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeUtf(signal);
        packet.writeInt(count);
        ClientPlayNetworking.send(TCM.PACKET_GET_ITEM, packet);
    }
}
