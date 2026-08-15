package berries.servermod.tcm.packet;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.TCMRegistry;
import berries.servermod.tcm.block.CRHTicketBarrierBlock;
import berries.servermod.tcm.block.StationsNameInfoBlock;
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
        if ((screenId.equals("SELECT_EXIT_ZONES")) && entity != null) {
            packet.writeInt(1);
            packet.writeLong(
                    Optional.ofNullable(
                            MixinBlockEntityHelper.invokeGetMethodInBlockEntity(entity, "getSelectedExitZone", Long.TYPE)
                    ).orElse(0L)
            );
        } else if (screenId.equals("SELECT_PLATFORMS") && entity instanceof StationsNameInfoBlock.StationsNameInfoBlockEntity el) {
            packet.writeInt(1);
            packet.writeLong(el.selectedPlatformId);
        } else if (screenId.equals("SELECT_PLATFORM_NUMBERS") && entity instanceof CRHTicketBarrierBlock.CRHTicketBarrierBlockEntity el) {
            packet.writeInt(el.selectedPlatforms.size());
            el.selectedPlatforms.forEach(packet::writeLong);
        }else {
            packet.writeInt(1);
            packet.writeLong(0L);
        }
        TCMRegistry.sendToPlayer(player, TCM.PACKET_SCREEN, packet);
    }
}
