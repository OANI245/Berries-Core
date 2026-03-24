package berries.servermod.tcm.client.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

public class PacketSudoClient {
    public static void receiveSudoS2C(FriendlyByteBuf packet) {
        String command = packet.readUtf();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.connection.sendCommand(command);
        }
    }
}
