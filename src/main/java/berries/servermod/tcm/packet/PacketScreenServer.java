package berries.servermod.tcm.packet;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.TCMRegistry;
import berries.servermod.tcm.block.blockentity.MixinBlockEntityHelper;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public class PacketScreenServer {
    public static void sendScreenS2C(ServerPlayer player, BlockEntity entity, BlockPos blockCurrentPos, String screenId) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeUtf(screenId);
        packet.writeBlockPos(blockCurrentPos);
        if (entity != null) {
            packet.writeLong(
                    Optional.ofNullable(
                            MixinBlockEntityHelper.invokeGetMethodInBlockEntity(entity, "getSelectedExitZone", Long.TYPE)
                    ).orElse(0L)
            );
        }
        TCMRegistry.sendToPlayer(player, TCM.PACKET_SCREEN, packet);
    }
}
