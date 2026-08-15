package berries.servermod.tcm.packet;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.signal.ItemGettingSignal;
import berries.servermod.tcm.util.TCMComponent;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.UUID;

public class PacketGetItemServer {
    public static void receiveGetItemC2S(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf packet, PacketSender responseSender) {
        String signalString = packet.readUtf();
        try {
            ItemGettingSignal signal = ItemGettingSignal.valueOf(signalString);
            int count = packet.readInt();

            Item item = BuiltInRegistries.ITEM.get(signal.getLocation());
            ItemStack stack = new ItemStack(item, count);

            giveItem(stack, player, count);
        } catch (Exception e) {
            TCM.LOGGER.warn(String.format("%s can't get item, because this player getting item is not found.", player.getName().getString()));
            TCM.LOGGER.warn("Signal: " + signalString);
        }
    }

    private static void giveItem(ItemStack stack, ServerPlayer serverPlayer, int i) {
        int j = stack.getItem().getMaxStackSize();
        int k = j * 100;
        if (i > k) {
            serverPlayer.sendSystemMessage(TCMComponent.translatable("commands.give.failed.toomanyitems").copy().withStyle(ChatFormatting.RED));
            return;
        } else {
            int l = i;

            while (l > 0) {
                int m = Math.min(j, l);
                l -= m;
                boolean bl = serverPlayer.getInventory().add(stack);
                if (bl && stack.isEmpty()) {
                    stack.setCount(1);
                    serverPlayer.containerMenu.broadcastChanges();
                }
            }
        }
    }
}
