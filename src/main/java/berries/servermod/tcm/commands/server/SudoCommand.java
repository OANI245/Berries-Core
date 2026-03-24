package berries.servermod.tcm.commands.server;

import berries.servermod.tcm.packet.PacketSudoServer;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySelector;

import java.util.Collection;

public class SudoCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((d, b, a) -> {
            d.register(LiteralArgumentBuilder.<CommandSourceStack>literal("sudo").
                    requires((s) -> Permissions.check(s, "tcm.commands.sudo", s.hasPermission(2)))
                    .then(RequiredArgumentBuilder.<CommandSourceStack, net.minecraft.commands.arguments.selector.EntitySelector>argument("players", EntityArgument.players())
                            .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("command", StringArgumentType.string())
                                    .executes((ctx) -> {
                                        Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
                                        String command = StringArgumentType.getString(ctx, "command");
                                        players.forEach((player) -> {
                                            PacketSudoServer.sendSudoS2C(player, command);
                                        });
                                        return players.size();
                                    }))));
        });
    }
}
