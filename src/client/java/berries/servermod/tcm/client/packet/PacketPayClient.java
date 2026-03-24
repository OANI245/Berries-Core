package berries.servermod.tcm.client.packet;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.client.screen.PayScreens;
import berries.servermod.tcm.client.util.PayClient;
import berries.servermod.tcm.client.util.PayStatus;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import org.mtr.mapping.holder.MinecraftClient;

public class PacketPayClient {
    public static void sendPayC2S(int type, float money) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeInt(type);
        packet.writeFloat(money);
        ClientPlayNetworking.send(TCM.PACKET_PAY, packet);
    }

    public static void receivePayScreenS2C(FriendlyByteBuf buf) {
        float money = buf.readFloat();
        Minecraft mc = Minecraft.getInstance();
        PayScreens screens = PayScreens.newInstance(mc.screen, money);
        mc.tell(() -> {
            mc.setScreen(screens.firstScreen);
        });
    }

    public static void receivePayResultS2C(FriendlyByteBuf buf) {
        int statusInt = buf.readInt();

        if (statusInt == 0) {
            PayClient.status = PayStatus.SUCCESS;
        } else if (statusInt == 1) {
            PayClient.status = PayStatus.INSU_BALANCE;
        } else {
            PayClient.status = PayStatus.PAYING_ERROR;
        }
    }
}
