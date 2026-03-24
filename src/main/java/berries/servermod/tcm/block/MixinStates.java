package berries.servermod.tcm.block;

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
import org.mtr.mapping.holder.Direction;
import org.mtr.mapping.holder.DirectionProperty;
import org.mtr.mapping.holder.Property;
import org.mtr.mapping.holder.World;
import org.mtr.mod.block.BlockPSDTop;
import org.mtr.mod.block.BlockStationNameEntrance;
import org.mtr.mod.block.IBlock;

public class MixinStates {
    public static final IntegerProperty BEIJING_STYLE = IntegerProperty.create("beijing_style", 0, 2);
    public static final EnumProperty<BeijingStylePSDTopType> PSD_TOP_DISPLAY_TYPE = EnumProperty.create("psd_top_display_type", BeijingStylePSDTopType.class);

    public static final BooleanProperty SHOW_LINES_AND_EXIT_ZONE = BooleanProperty.create("show_lines_and_exit");

    public static boolean sneakAndUse01(BlockState state, Level world, BlockPos pos, Player player) {
        if (!(state.getBlock() instanceof BlockPSDTop) || !player.isSecondaryUseActive()) return false;
        world.setBlockAndUpdate(pos, state.cycle(BEIJING_STYLE));
        if (!world.isClientSide()) {
            BlockPSDTop block = (BlockPSDTop) state.getBlock();
            block.propagate(new World(world), new org.mtr.mapping.holder.BlockPos(pos), IBlock.getStatePropertySafe(new org.mtr.mapping.holder.BlockState(state), new DirectionProperty(HorizontalDirectionalBlock.FACING)).rotateYClockwise(), new Property<Integer>(BEIJING_STYLE), 1);
            block.propagate(new World(world), new org.mtr.mapping.holder.BlockPos(pos), IBlock.getStatePropertySafe(new org.mtr.mapping.holder.BlockState(state), new DirectionProperty(HorizontalDirectionalBlock.FACING)).rotateYCounterclockwise(), new Property<Integer>(BEIJING_STYLE), 1);
            switch (state.getValue(BEIJING_STYLE)) {
                case 2 -> {
                    showInActionBar((ServerPlayer) player, TCMComponent.translatable("actionbar.tcm.psd_top.default_style"));
                }
                case 0 -> {
                    showInActionBar((ServerPlayer) player, TCMComponent.translatable("actionbar.tcm.psd_top.beijing_1"));
                }
                case 1 -> {
                    showInActionBar((ServerPlayer) player, TCMComponent.translatable("actionbar.tcm.psd_top.beijing_2"));
                }
            }
        }
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
