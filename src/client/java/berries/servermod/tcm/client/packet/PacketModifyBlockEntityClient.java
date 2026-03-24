package berries.servermod.tcm.client.packet;

import berries.servermod.tcm.TCM;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PacketModifyBlockEntityClient {
    public static void sendModifyBlockEntityC2S(BlockPos blockPos, Set<Long> value) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        List<Long> valueToList = new ArrayList<>(value);
        packet.writeUtf("SNE_EXIT_ZONE");
        packet.writeBlockPos(blockPos);
        packet.writeLong(!valueToList.isEmpty() ? valueToList.get(0) : 0);
        ClientPlayNetworking.send(TCM.PACKET_MODIFY_BLOCK_ENTITY, packet);
    }
}
