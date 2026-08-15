package berries.servermod.tcm.block;

import berries.servermod.tcm.block.blockentity.BlockEntityTypes;
import berries.servermod.tcm.packet.PacketScreenServer;
import berries.servermod.tcm.util.BlockFacingUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtr.mod.Items;

@BlockId(value = "stations_name_info", itemGroup = BlockId.ItemGroupFor.RAILWAY_FACILITIES)
public class StationsNameInfoBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final double MODEL_X = 0.8;
    public static final double MODEL_Y = 3.1;
    public static final double MODEL_Z = 15.5;
    public static final double MODEL_WIDTH = 14.4;
    public static final double MODEL_HEIGHT = 9.8;
    public static final double MODEL_DEPTH = 0.52;

    public StationsNameInfoBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        try {
            if (player.isHolding(Items.BRUSH.get().data)) {
                if (!level.isClientSide) {
                    PacketScreenServer.sendScreenS2C((ServerPlayer) player, level.getBlockEntity(blockPos), blockPos, "SELECT_PLATFORMS");
                }
                return InteractionResult.SUCCESS;
            }
        } catch (Throwable ignored) {}
        return InteractionResult.PASS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState st) {
        return new StationsNameInfoBlockEntity(pos, st);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    private boolean canAttachTo(@NotNull BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        BlockState blockState = blockGetter.getBlockState(blockPos);
        return blockState.isFaceSturdy(blockGetter, blockPos, direction);
    }

    @Override
    public boolean canSurvive(@NotNull BlockState blockState, LevelReader levelReader, @NotNull BlockPos blockPos) {
        Direction direction = (Direction)blockState.getValue(FACING);
        return this.canAttachTo(levelReader, blockPos.relative(direction.getOpposite()), direction);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Level level = blockPlaceContext.getLevel();
        if (!blockPlaceContext.replacingClickedOnBlock()) {
            BlockState blockState = level.getBlockState(blockPlaceContext.getClickedPos().relative(blockPlaceContext.getClickedFace().getOpposite()));
            if (blockState.is(this) && blockState.getValue(FACING) == blockPlaceContext.getClickedFace()) {
                return null;
            }
        }
        return this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return BlockFacingUtils.getCenterRotatedBox(blockState.getValue(FACING), MODEL_X, MODEL_Y, MODEL_Z, MODEL_WIDTH, MODEL_HEIGHT, MODEL_DEPTH);/*switch (blockState.getValue(FACING)) {
            case SOUTH -> box(MODEL_X,
                    MODEL_Y,
                    (16 - MODEL_Z - MODEL_DEPTH),
                    (MODEL_X + MODEL_WIDTH),
                    (MODEL_Y + MODEL_HEIGHT),
                    (16 - MODEL_Z));
            case EAST -> box((16 - MODEL_Z - MODEL_DEPTH),
                    MODEL_Y,
                    MODEL_X,
                    (16 - MODEL_Z),
                    (MODEL_Y + MODEL_HEIGHT),
                    (MODEL_X + MODEL_WIDTH));
            case WEST -> box(MODEL_Z,
                    MODEL_Y,
                    MODEL_X,
                    (MODEL_Z + MODEL_DEPTH),
                    (MODEL_Y + MODEL_HEIGHT),
                    (MODEL_X + MODEL_WIDTH));
            default -> box(MODEL_X,
                    MODEL_Y,
                    MODEL_Z,
                    (MODEL_X + MODEL_WIDTH),
                    (MODEL_Y + MODEL_HEIGHT),
                    (MODEL_Z + MODEL_DEPTH));
        };*/
    }

    @BlockEntityId(value = "stations_name_info_tile_entity")
    public static class StationsNameInfoBlockEntity extends BlockEntity {
        public long selectedPlatformId = 0;

        public StationsNameInfoBlockEntity(BlockPos blockPos, BlockState blockState) {
            super(BlockEntityTypes.STATIONS_NAME_INFO_BLOCK_ENTITY, blockPos, blockState);
        }

        @Override
        public void load(@NotNull CompoundTag tag) {
            super.load(tag);
            selectedPlatformId = tag.getLong("selected_platform_id");
        }

        @Override
        protected void saveAdditional(@NotNull CompoundTag tag) {
            super.saveAdditional(tag);
            tag.putLong("selected_platform_id", selectedPlatformId);
        }

        @Override
        public @NotNull CompoundTag getUpdateTag() {
            return saveWithoutMetadata();
        }

        @Override
        public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
            return ClientboundBlockEntityDataPacket.create(this);
        }
    }
}
