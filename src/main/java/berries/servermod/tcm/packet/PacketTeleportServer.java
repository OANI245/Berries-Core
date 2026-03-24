package berries.servermod.tcm.packet;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

public class PacketTeleportServer {
    public static void receiveTeleportC2S(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf packet, PacketSender responseSender) {
        double x = packet.readDouble();
        double y = packet.readDouble();
        double z = packet.readDouble();
        Set<RelativeMovement> set = EnumSet.noneOf(RelativeMovement.class);
        performTeleport(player, (ServerLevel) player.level(), x, y, z, set);
    }

    private static void performTeleport(ServerPlayer entity, ServerLevel serverLevel, double d, double e, double f, Set<RelativeMovement> set) {
        BlockPos blockPos = BlockPos.containing(d, e, f);
        if (!Level.isInSpawnableBounds(blockPos)) {
            return;
        } else {
            if (entity.teleportTo(serverLevel, d, e, f, set, entity.getYRot(), entity.getXRot())) {
                label23: {
                    if (((LivingEntity) entity).isFallFlying()) {
                        break label23;
                    }

                    entity.setDeltaMovement(entity.getDeltaMovement().multiply((double)1.0F, (double)0.0F, (double)1.0F));
                    entity.setOnGround(true);
                }
            }
        }
    }
}
