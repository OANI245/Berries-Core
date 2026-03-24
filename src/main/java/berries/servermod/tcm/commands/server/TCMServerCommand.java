package berries.servermod.tcm.commands.server;

import berries.servermod.tcm.ServerConfig;
import berries.servermod.tcm.TCM;
import berries.servermod.tcm.UFEInfo;
import berries.servermod.tcm.packet.PacketSyncServerConfigServer;
import berries.servermod.tcm.util.TCMComponent;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class TCMServerCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((d, b, a) -> {
            d.register(
                    LiteralArgumentBuilder.<CommandSourceStack>literal("tcms")
                            .then(
                                    LiteralArgumentBuilder.<CommandSourceStack>literal("reload")
                                            .requires((s) -> Permissions.check(s, "tcm.commands.reload", s.hasPermission(3)))
                                            .executes(
                                                    (stack) -> {
                                                        ServerConfig.INSTANCE.readConfig();
                                                        ServerConfig.INSTANCE.saveConfig();
                                                        ServerConfig.INSTANCE.timeMessages.keySet().forEach((time) -> {
                                                            for (ServerPlayer player : stack.getSource().getServer().getPlayerList().getPlayers()) {
                                                                PacketSyncServerConfigServer.sendSyncServerTimeMessagesS2C(player, time);
                                                            }
                                                        });
                                                        stack.getSource().sendSuccess(() -> TCMComponent.text("Tiancheng Mod Server Side Reload Successful."), true);
                                                        return 1;
                                                    }
                                            )
                            ).executes(
                                    (stack) -> {
                                        stack.getSource().getPlayerOrException().sendSystemMessage(
                                                TCMComponent.text("Tiancheng Mod").copy()
                                                        .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withFont(new ResourceLocation(UFEInfo.MOD_ID, "vga")))
                                                        .append("\n")
                                                        .append(
                                                                TCMComponent.text("Version: " + TCM.getFullVersion()).copy()
                                                                        .withStyle(Style.EMPTY.withBold(false).withColor(0xFFFFFF))
                                                        )
                                        );
                                        return 1;
                                    }
                            )
            );
        });
    }
}
