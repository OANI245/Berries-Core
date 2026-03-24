package berries.servermod.tcm.client.packet;

import berries.servermod.tcm.TCM;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class PacketTeleportClient {
    public static void sendTeleportC2S(Vec3 pos) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeDouble(pos.x);
        packet.writeDouble(pos.y);
        packet.writeDouble(pos.z);
        ClientPlayNetworking.send(TCM.PACKET_TELEPORT, packet);
    }
}
