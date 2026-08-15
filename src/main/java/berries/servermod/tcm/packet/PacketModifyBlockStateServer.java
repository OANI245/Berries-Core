package berries.servermod.tcm.packet;

import berries.servermod.tcm.block.MixinStates;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.mtr.mapping.holder.DirectionProperty;
import org.mtr.mapping.holder.Property;
import org.mtr.mapping.holder.World;
import org.mtr.mod.block.BlockPSDTop;
import org.mtr.mod.block.IBlock;

public class PacketModifyBlockStateServer {
    public static void receiveModifyPSDTopBlockStateC2S(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf packet, PacketSender responseSender) {
        var st = packet.readInt();
        try {
            IntegerProperty temp0;
            temp0 = MixinStates.PSD_TOP_CONTENT_STYLE;

            var pos = packet.readBlockPos();
            var level = player.level();
            var state = level.getBlockState(pos);
            var block = state.getBlock();
            if (!(block instanceof BlockPSDTop)) return;
            var sv = packet.readInt();
            state = state.setValue((IntegerProperty)temp0, sv);
            level.setBlockAndUpdate(pos, state);
            if (!level.isClientSide()) {
                ((BlockPSDTop) block).propagate(new World(level), new org.mtr.mapping.holder.BlockPos(pos), IBlock.getStatePropertySafe(new org.mtr.mapping.holder.BlockState(state), new DirectionProperty(HorizontalDirectionalBlock.FACING)).rotateYClockwise(), (offsetPos) -> {
                    level.setBlockAndUpdate(offsetPos.data, level.getBlockState(offsetPos.data).setValue(temp0, sv));
                }, 1);
                ((BlockPSDTop) block).propagate(new World(level), new org.mtr.mapping.holder.BlockPos(pos), IBlock.getStatePropertySafe(new org.mtr.mapping.holder.BlockState(state), new DirectionProperty(HorizontalDirectionalBlock.FACING)).rotateYCounterclockwise(), (offsetPos) -> {
                    level.setBlockAndUpdate(offsetPos.data, level.getBlockState(offsetPos.data).setValue(temp0, sv));
                }, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
