package berries.servermod.tcm;

import berries.servermod.tcm.block.blockentity.BlockEntityTypes;
import berries.servermod.tcm.block.Blocks;
import berries.servermod.tcm.data.TCMGameRules;
import berries.servermod.tcm.packet.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.mtr.mod.CreativeModeTabs;
import org.mtr.mod.Init;
import org.mtr.mod.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static berries.servermod.tcm.item.Items.StaticFields.*;

public class TCM implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Tiancheng Client Mod");

    public static final ResourceLocation PACKET_MOD_VERSION_CHECK = new ResourceLocation(UFEInfo.MOD_ID, "packet_mod_version_check");
    public static final ResourceLocation PACKET_SCREEN = new ResourceLocation(UFEInfo.MOD_ID, "packet_screen");
    public static final ResourceLocation PACKET_MODIFY_BLOCK_ENTITY = new ResourceLocation(UFEInfo.MOD_ID, "packet_modify_block_entity");
    public static final ResourceLocation PACKET_MODIFY_BLOCK_STATE = new ResourceLocation(UFEInfo.MOD_ID, "packet_modify_block_state");
    public static final ResourceLocation PACKET_SERVER_VERSION_QUERY = new ResourceLocation(UFEInfo.MOD_ID, "packet_server_version_query");
    public static final ResourceLocation PACKET_SYNC_TELEPORTS = new ResourceLocation(UFEInfo.MOD_ID, "packet_sync_teleports");
    public static final ResourceLocation PACKET_CLEAR_TELEPORTS = new ResourceLocation(UFEInfo.MOD_ID, "packet_clear_teleports");
    public static final ResourceLocation PACKET_SYNC_TIME_MESSAGES = new ResourceLocation(UFEInfo.MOD_ID, "packet_sync_time_messages");
    public static final ResourceLocation PACKET_CLEAR_TIME_MESSAGES = new ResourceLocation(UFEInfo.MOD_ID, "packet_clear_time_messages");
    public static final ResourceLocation PACKET_SYNC_DIRECTION_IP = new ResourceLocation(UFEInfo.MOD_ID, "packet_sync_direction_ip");
    public static final ResourceLocation PACKET_TELEPORT = new ResourceLocation(UFEInfo.MOD_ID, "packet_teleport");
    public static final ResourceLocation PACKET_GET_ITEM = new ResourceLocation(UFEInfo.MOD_ID, "packet_get_item");
    public static final ResourceLocation PACKET_CLEAR_ITEM = new ResourceLocation(UFEInfo.MOD_ID, "packet_clear_item");
    public static final ResourceLocation PACKET_SUDO = new ResourceLocation(UFEInfo.MOD_ID, "packet_sudo");
    public static final ResourceLocation PACKET_PAY = new ResourceLocation(UFEInfo.MOD_ID, "packet_pay");
    public static final ResourceLocation PACKET_PAY_SCREEN = new ResourceLocation(UFEInfo.MOD_ID, "packet_pay_screen");
    public static final ResourceLocation PACKET_PAY_RESULT = new ResourceLocation(UFEInfo.MOD_ID, "packet_pay_result");

    public static boolean lockMinecraftScreen = false;

    @Override
    public void onInitialize() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> e.printStackTrace());
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
        LOGGER.info("The mod is loaded, Please wait for the game...");
        LOGGER.info(String.format("Version: %s, Build: %d", UFEInfo.MOD_VERSION, UFEInfo.PNB_VERSION));

        TCMRegistry.ITEM.register(new ResourceLocation(UFEInfo.MOD_ID, "editor"), EDITOR);
        TCMRegistry.ITEM.register(new ResourceLocation(UFEInfo.MOD_ID, "pound_1"), POUND_1);
        TCMRegistry.ITEM.register(new ResourceLocation(UFEInfo.MOD_ID, "pound_5"), POUND_5);
        TCMRegistry.ITEM.register(new ResourceLocation(UFEInfo.MOD_ID, "pound_10"), POUND_10);
        TCMRegistry.ITEM.register(new ResourceLocation(UFEInfo.MOD_ID, "pound_50"), POUND_50);
        TCMRegistry.ITEM.register(new ResourceLocation(UFEInfo.MOD_ID, "pound_100"), POUND_100);
        TCMRegistry.ITEM.register(new ResourceLocation(UFEInfo.MOD_ID, "credit_card_tbb"), CREDIT_CARD_TIANCHENG_BUSINESS_BANK);

        Blocks.register();
        BlockEntityTypes.register();
        TCMGameRules.register();

        ItemGroupEvents.modifyEntriesEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, CreativeModeTabs.CORE.identifier)).register((entries) -> {
            entries.addAfter(Items.BRUSH.get().data, EDITOR);
        });

        ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.TOOLS_AND_UTILITIES).register((entries) -> {
            entries.addAfter(net.minecraft.world.item.Items.SPYGLASS, POUND_100);
            entries.addAfter(net.minecraft.world.item.Items.SPYGLASS, POUND_50);
            entries.addAfter(net.minecraft.world.item.Items.SPYGLASS, POUND_10);
            entries.addAfter(net.minecraft.world.item.Items.SPYGLASS, POUND_5);
            entries.addAfter(net.minecraft.world.item.Items.SPYGLASS, POUND_1);
            entries.addAfter(net.minecraft.world.item.Items.SPYGLASS, CREDIT_CARD_TIANCHENG_BUSINESS_BANK);
        });

        TCMRegistry.registerNetworkReceiver(TCM.PACKET_SERVER_VERSION_QUERY, PacketServerVersionQuery::receiveVersionQueryC2S);
        TCMRegistry.registerNetworkReceiver(TCM.PACKET_MODIFY_BLOCK_ENTITY, PacketModifyBlockEntityServer::receiveModifyBlockEntityC2S);
        TCMRegistry.registerNetworkReceiver(TCM.PACKET_MODIFY_BLOCK_STATE, PacketModifyBlockStateServer::receiveModifyPSDTopBlockStateC2S);
        TCMRegistry.registerNetworkReceiver(TCM.PACKET_TELEPORT, PacketTeleportServer::receiveTeleportC2S);
        TCMRegistry.registerNetworkReceiver(TCM.PACKET_GET_ITEM, PacketGetItemServer::receiveGetItemC2S);
        TCMRegistry.registerNetworkReceiver(TCM.PACKET_CLEAR_ITEM, PacketClearItemServer::receiveClearItemC2S);
        TCMRegistry.registerNetworkReceiver(TCM.PACKET_PAY, PacketPayServer::receivePayC2S);

        //Init.REGISTRY.setupPackets(new Identifier(new ResourceLocation(UFEInfo.MOD_ID, "packet")));
        Init.REGISTRY.registerPacket(MTRDataS2CPacket.class, MTRDataS2CPacket::new);
        Init.REGISTRY.registerPacket(RequestStopsDataC2SPacket.class, RequestStopsDataC2SPacket::new);
        Init.REGISTRY.registerPacket(RequestMTRDataC2SPacket.class, RequestMTRDataC2SPacket::new);
        Init.REGISTRY.registerPacket(StopsDataS2CPacket.class, StopsDataS2CPacket::new);
    }

    public static String getVersion() {
        return "27.0.0";
    }

    public static String getFullVersion() { return UFEInfo.MOD_VERSION + " build-" + UFEInfo.PNB_VERSION; }
}
