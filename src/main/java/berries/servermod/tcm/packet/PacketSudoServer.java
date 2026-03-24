package berries.servermod.tcm.packet;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.TCMRegistry;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class PacketSudoServer {
    public static void sendSudoS2C(ServerPlayer player, String command) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(command);
        TCMRegistry.sendToPlayer(player, TCM.PACKET_SUDO, buf);
    }
}
