package berries.servermod.tcm;

import berries.servermod.tcm.commands.server.SudoCommand;
import berries.servermod.tcm.commands.server.TCMServerCommand;
import berries.servermod.tcm.packet.PacketModVersionCheckServer;
import berries.servermod.tcm.packet.PacketSyncServerConfigServer;
import berries.servermod.tcm.web.WebMain;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class TCMServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ServerConfig.INSTANCE.readConfig();
        ServerConfig.INSTANCE.saveConfig();
        TCMRegistry.registerPlayerJoinEvent((player) -> {
            PacketSyncServerConfigServer.sendSyncDirectionIPS2C(player);
            PacketModVersionCheckServer.sendVersionCheckS2C(player);
            PacketSyncServerConfigServer.sendClearServerTeleportsS2C(player);
            PacketSyncServerConfigServer.sendClearServerTimeMessagesS2C(player);
            for (int i = 0; i < ServerConfig.INSTANCE.teleports.size(); i++) {
                PacketSyncServerConfigServer.sendSyncServerTeleportsS2C(player, i);
            }
            ServerConfig.INSTANCE.timeMessages.keySet().forEach((time) -> {
                PacketSyncServerConfigServer.sendSyncServerTimeMessagesS2C(player, time);
            });
        });
        TCMServerCommand.register();
        SudoCommand.register();

        WebMain.init();
        ServerLifecycleEvents.SERVER_STARTING.register((server) -> {
            WebMain.callback = server::execute;
            WebMain.start();
        });
        ServerLifecycleEvents.SERVER_STOPPING.register((server) ->
                WebMain.stop());
    }
}
