package berries.servermod.tcm.client;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.UFEInfo;
import berries.servermod.tcm.block.Blocks;
import berries.servermod.tcm.block.blockentity.BlockEntityTypes;
import berries.servermod.tcm.client.commands.TCMClientCommand;
import berries.servermod.tcm.client.datafix.ClientDataFixes;
import berries.servermod.tcm.client.packet.*;
import berries.servermod.tcm.client.render.StationsNameInfoBlockEntityRenderer;
import berries.servermod.tcm.client.screen.TCMMainScreen;
import berries.servermod.tcm.client.vehicle.processing.ContentProcessing;
import berries.servermod.tcm.client.vehicle.processing.Processors;
import berries.servermod.tcm.data.vehicle.VehicleDataCache;
import berries.servermod.tcm.util.TCMComponent;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import org.mtr.mapping.holder.MinecraftClient;
import org.mtr.mod.Keys;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class TCMClient implements ClientModInitializer {
    public static List<Tuple<Tuple<String, String>, Vec3>> syncTeleports = new ArrayList<>(0);
    public static Map<Long, MutableComponent> syncTimeMessages = new HashMap<>(0);
    public static String syncDirectionIp = "localhost";
    public static Runnable loadedRunnable$1 = () -> {};

    public static KeyMapping tcmKeyMapping = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                    "key.tcm.open_gui",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_N,
                    "gui.tcm.key_binding.title"
            )
    );

    @Override
    public void onInitializeClient() {
        try {
            TCM.LOGGER.info("Client is loaded, MTR Version is: " + Keys.class.getField("MOD_VERSION").get(null));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        Config.INSTANCE.readConfig();
        final boolean installingCopy = Config.INSTANCE.isInstalling;
        loadedRunnable$1 = () -> {
            if (installingCopy) {
                ClientDataFixes.runUpdateFix();
            }
        };
        Config.INSTANCE.isInstalling = false;
        Config.INSTANCE.saveConfig();

        File tmpClass = new File(Minecraft.getInstance().gameDirectory.getPath() + File.separator + "Uninstaller.class");
        if (tmpClass.exists()) {
            tmpClass.delete();
        }

        ModContainer container = FabricLoader.getInstance().getModContainer(UFEInfo.MOD_ID).get();
        ResourceManagerHelper.registerBuiltinResourcePack(
                new ResourceLocation(UFEInfo.MOD_ID, "mtr_main"),
                container,
                TCMComponent.text("TCM模组资源"),
                ResourcePackActivationType.ALWAYS_ENABLED
        );
        if (UFEInfo.OPEN_TRIALS) {
            ResourceManagerHelper.registerBuiltinResourcePack(
                    new ResourceLocation(UFEInfo.MOD_ID, "trials"),
                    container,
                    TCMComponent.text("实验性内容"),
                    ResourcePackActivationType.NORMAL
            );
        }

        //BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), Blocks.STATIONS_NAME_INFO_BLOCK);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(),
                Blocks.CR_TICKET_BARRIER_ENTRANCE_BLOCK.get(),
                Blocks.CR_TICKET_BARRIER_EXIT_BLOCK.get());

        BlockEntityRenderers.register(BlockEntityTypes.STATIONS_NAME_INFO_BLOCK_ENTITY, StationsNameInfoBlockEntityRenderer::new);

        TCMClientCommand.register();
        ClientPlayNetworking.registerGlobalReceiver(TCM.PACKET_MOD_VERSION_CHECK, (client, handler, packet, responseSender) -> PacketModVersionCheckClient.receiveVersionCheckS2C(packet));
        ClientPlayNetworking.registerGlobalReceiver(TCM.PACKET_SERVER_VERSION_QUERY, (client, handler, packet, responseSender) -> PacketServerVersionQuery.receiveVersionValueS2C(packet));
        ClientPlayNetworking.registerGlobalReceiver(TCM.PACKET_SCREEN, (client, handler, packet, responseSender) -> PacketScreenClient.receiveScreenS2C(packet));
        ClientPlayNetworking.registerGlobalReceiver(TCM.PACKET_SYNC_TELEPORTS, (client, handler, packet, responseSender) -> PacketSyncServerConfigClient.receiveSyncServerTeleportsS2C(packet));
        ClientPlayNetworking.registerGlobalReceiver(TCM.PACKET_CLEAR_TELEPORTS, (client, handler, packet, responseSender) -> PacketSyncServerConfigClient.receiveClearServerTeleportsS2C(packet));
        ClientPlayNetworking.registerGlobalReceiver(TCM.PACKET_SYNC_TIME_MESSAGES, (client, handler, packet, responseSender) -> PacketSyncServerConfigClient.receiveSyncServerTimeMessagesS2C(packet));
        ClientPlayNetworking.registerGlobalReceiver(TCM.PACKET_CLEAR_TIME_MESSAGES, (client, handler, packet, responseSender) -> PacketSyncServerConfigClient.receiveClearServerTimeMessagesS2C(packet));
        ClientPlayNetworking.registerGlobalReceiver(TCM.PACKET_SYNC_DIRECTION_IP, (client, handler, packet, responseSender) -> PacketSyncServerConfigClient.receiveSyncDirectionIPS2C(packet));
        ClientPlayNetworking.registerGlobalReceiver(TCM.PACKET_SUDO, (client, handler, packet, responseSender) -> PacketSudoClient.receiveSudoS2C(packet));
        ClientPlayNetworking.registerGlobalReceiver(TCM.PACKET_PAY_RESULT, (client, handler, packet, responseSender) -> PacketPayClient.receivePayResultS2C(packet));
        ClientPlayNetworking.registerGlobalReceiver(TCM.PACKET_PAY_SCREEN, (client, handler, packet, responseSender) -> PacketPayClient.receivePayScreenS2C(packet));

        Processors.init();
        ContentProcessing.register();

        ClientTickEvents.END_CLIENT_TICK.register((mc) -> {
            if (tcmKeyMapping.isDown() && mc.screen == null) {
                mc.execute(() -> {
                    TCMMainScreen.open(mc, null);
                });
            }
        });

        ClientTickEvents.START_CLIENT_TICK.register((minecraftServer) -> {
            ContentProcessing.tick();
            VehicleDataCache.tick();

            if (MinecraftClient.getInstance().getWorldMapped() == null) {
                VehicleDataCache.clearData();
            }
        });
    }
}
