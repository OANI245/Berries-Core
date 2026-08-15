package berries.servermod.tcm.block;

import berries.servermod.tcm.data.PSDTopRenderStyles;
import berries.servermod.tcm.packet.PacketScreenServer;
import berries.servermod.tcm.util.TCMComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;
import org.mtr.mapping.holder.DirectionProperty;
import org.mtr.mapping.holder.Property;
import org.mtr.mapping.holder.World;
import org.mtr.mod.block.BlockPSDTop;
import org.mtr.mod.block.BlockStationNameEntrance;
import org.mtr.mod.block.IBlock;

public class MixinStates {
    public static final IntegerProperty PSD_TOP_CONTENT_STYLE = IntegerProperty.create("psd_top_content_style", 0, PSDTopRenderStyles.values().length - 1);
    public static final EnumProperty<BeijingStylePSDTopType> PSD_TOP_DISPLAY_TYPE = EnumProperty.create("psd_top_display_type", BeijingStylePSDTopType.class);

    public static final BooleanProperty SHOW_LINES_AND_EXIT_ZONE = BooleanProperty.create("show_lines_and_exit");

    public static boolean sneakAndUse01(BlockState state, Level world, BlockPos pos, Player player) {
        if (!(state.getBlock() instanceof BlockPSDTop block) || !player.isSecondaryUseActive()) return false;
        if (!world.isClientSide) {
            PacketScreenServer.sendScreenS2C((ServerPlayer) player, world.getBlockEntity(pos), pos, "EDIT_PSD_TOP");
        }
        /*world.setBlockAndUpdate(pos, state.cycle(PSD_TOP_CONTENT_STYLE));
        if (!world.isClientSide()) {
            block.propagate(new World(world), new org.mtr.mapping.holder.BlockPos(pos), IBlock.getStatePropertySafe(new org.mtr.mapping.holder.BlockState(state), new DirectionProperty(HorizontalDirectionalBlock.FACING)).rotateYClockwise(), new Property<Integer>(PSD_TOP_CONTENT_STYLE), 1);
            block.propagate(new World(world), new org.mtr.mapping.holder.BlockPos(pos), IBlock.getStatePropertySafe(new org.mtr.mapping.holder.BlockState(state), new DirectionProperty(HorizontalDirectionalBlock.FACING)).rotateYCounterclockwise(), new Property<Integer>(PSD_TOP_CONTENT_STYLE), 1);
            var styleId = state.getValue(PSD_TOP_CONTENT_STYLE);
            showInActionBar((ServerPlayer) player, PSDTopRenderStyles.getById(styleId + 1).getComponent());
        }*/
        return true;
    }

    public static boolean sneakAndUse02(BlockState state, Level world, BlockPos pos, Player player) {
        if (!(state.getBlock() instanceof BlockStationNameEntrance) || !player.isSecondaryUseActive()) return false;
        if (!world.isClientSide) {
            PacketScreenServer.sendScreenS2C((ServerPlayer) player, world.getBlockEntity(pos), pos, "SELECT_EXIT_ZONES");
        }
        return true;
    }

    private static void showInActionBar(ServerPlayer player, Component cp) {
        player.connection.send(new ClientboundSetActionBarTextPacket(cp));
    }

    public static enum BeijingStylePSDTopType implements StringRepresentable {
        DEFAULT("default"),
        ARROW("arrow"),
        STATION_NAME("station_name");

        public final String name;

        BeijingStylePSDTopType(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}
