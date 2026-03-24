package berries.servermod.tcm.client.packet;

import berries.servermod.tcm.TCM;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;

public class PacketClearItemClient {
    public static void sendClearItemC2S() {
        ClientPlayNetworking.send(TCM.PACKET_CLEAR_ITEM, new FriendlyByteBuf(Unpooled.buffer()));
    }
}
