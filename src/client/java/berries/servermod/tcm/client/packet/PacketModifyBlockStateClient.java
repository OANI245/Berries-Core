package berries.servermod.tcm.client.packet;

import berries.servermod.tcm.TCM;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class PacketModifyBlockStateClient {
    public static void sendModifyPSDTopBlockStateC2S(BlockPos blockPos, int st, Object value) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeInt(st);
        packet.writeBlockPos(blockPos);
        if (value instanceof Integer) {
            packet.writeInt((Integer) value);
        }
        ClientPlayNetworking.send(TCM.PACKET_MODIFY_BLOCK_STATE, packet);
    }
}
