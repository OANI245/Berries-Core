package berries.servermod.tcm.packet;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.TCMRegistry;
import berries.servermod.tcm.UFEInfo;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class PacketModVersionCheckServer {
    public static void sendVersionCheckS2C(ServerPlayer player) {
        final FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeUtf(UFEInfo.MOD_VERSION);
        packet.writeVarIntArray(new int[]{UFEInfo.LOWEST_PNB, UFEInfo.PNB_VERSION});
        try {
            var changelogs = (String[]) UFEInfo.class.getField("CHANGE_LOGS").get(null);
            packet.writeInt(changelogs.length);
            for (String changelog : changelogs) {
                packet.writeUtf(changelog);
            }
        } catch (Exception ignored) {}
        TCMRegistry.sendToPlayer(player, TCM.PACKET_MOD_VERSION_CHECK, packet);
    }
}
