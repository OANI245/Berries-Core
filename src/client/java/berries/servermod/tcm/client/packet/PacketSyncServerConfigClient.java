package berries.servermod.tcm.client.packet;

import berries.servermod.tcm.client.TCMClient;
import berries.servermod.tcm.util.Packets;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec3;

public class PacketSyncServerConfigClient {
    public static void receiveSyncServerTeleportsS2C(FriendlyByteBuf packet) {
        int index = packet.readInt();
        String title = packet.readUtf();
        String description = packet.readUtf();
        Vec3 pos = Packets.readVec3(packet);

        TCMClient.syncTeleports.add(index, new Tuple<>(new Tuple<>(title, description), pos));
    }

    public static void receiveSyncServerTimeMessagesS2C(FriendlyByteBuf packet) {
        long time = packet.readLong();
        Component text = packet.readComponent();

        TCMClient.syncTimeMessages.put(time, text.copy());
    }

    public static void receiveSyncDirectionIPS2C(FriendlyByteBuf packet) {
        TCMClient.syncDirectionIp = packet.readUtf();
    }

    public static void receiveClearServerTeleportsS2C(FriendlyByteBuf packet) {
        TCMClient.syncTeleports.clear();
    }

    public static void receiveClearServerTimeMessagesS2C(FriendlyByteBuf packet) {
        TCMClient.syncTimeMessages.clear();
    }
}
