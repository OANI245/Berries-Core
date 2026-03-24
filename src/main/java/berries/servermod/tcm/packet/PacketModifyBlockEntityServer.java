package berries.servermod.tcm.packet;

import org.mtr.mapping.holder.Direction;
import org.mtr.mapping.holder.World;
import org.mtr.mod.block.BlockStationNameEntrance;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.mtr.mod.block.BlockStationNameEntrance;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static berries.servermod.tcm.block.blockentity.MixinBlockEntityHelper.*;

public class PacketModifyBlockEntityServer {
    public static void receiveModifyBlockEntityC2S(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf packet, PacketSender responseSender) {
        String signal = packet.readUtf();
        BlockPos pos = packet.readBlockPos();

        Level level = player.level();

        long selectedId = packet.readLong();
        server.tell(new TickTask(server.getTickCount(), () -> {
            BlockEntity entity = level.getBlockEntity(pos);

            switch (signal) {
                case "SNE_EXIT_ZONE" -> {
                    if (!(entity instanceof BlockStationNameEntrance.BlockEntity)) return;

                    invokeSetMethodInBlockEntity(entity, "setSelectedExitZone", selectedId);
                    invokeSetMethodInBlockEntity(entity, "writeCompoundTag", new org.mtr.mapping.holder.CompoundTag());

                    BlockState state = level.getBlockState(pos);
                    BlockStationNameEntrance block = (BlockStationNameEntrance) state.getBlock();
                    block.propagate(new World(level), new org.mtr.mapping.holder.BlockPos(pos), Direction.convert(state.getValue(HorizontalDirectionalBlock.FACING).getClockWise()),
                            (offsetPos) -> {
                                BlockEntity newEntity = (BlockEntity) level.getBlockEntity(offsetPos.data);
                                if (newEntity instanceof BlockStationNameEntrance.BlockEntity) {
                                    invokeSetMethodInBlockEntity(newEntity, "setSelectedExitZone", selectedId);
                                }
                            }, 1);
                    block.propagate(new World(level), new org.mtr.mapping.holder.BlockPos(pos), Direction.convert(state.getValue(HorizontalDirectionalBlock.FACING).getCounterClockWise()),
                            (offsetPos) -> {
                                BlockEntity newEntity = (BlockEntity) level.getBlockEntity(offsetPos.data);
                                if (newEntity instanceof BlockStationNameEntrance.BlockEntity) {
                                    invokeSetMethodInBlockEntity(newEntity, "setSelectedExitZone", selectedId);
                                }
                            }, 1);
                }
                default -> {
                    return;
                }
            }
        }));
    }
}
