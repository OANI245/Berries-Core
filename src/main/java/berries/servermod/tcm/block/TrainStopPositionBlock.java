package berries.servermod.tcm.block;

import berries.servermod.tcm.util.BlockFacingUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@BlockId(value = "train_stop_position", itemGroup = BlockId.ItemGroupFor.RAILWAY_FACILITIES)
public class TrainStopPositionBlock extends HorizontalDirectionalBlock {
    public static final EnumProperty<Half> HALF = EnumProperty.create("half", Half.class);

    protected TrainStopPositionBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, Half.BOTTOM));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, HALF);
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        context.getLevel().scheduleTick(context.getClickedPos(), this, 20);
        return context
                .getLevel()
                .getBlockState(
                        getNeighbourPosition(
                                Half.BOTTOM,
                                context.getClickedPos()
                        )
                )
                .canBeReplaced(context) ?
                this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()) : null;
    }

    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (neighborPos.equals(getNeighbourPosition(state.getValue(HALF), currentPos))) {
            return neighborState.is(this) && neighborState.getValue(HALF) != state.getValue(HALF) ? state : Blocks.AIR.defaultBlockState();
        } else {
            return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            BlockPos blockPos = getNeighbourPosition(Half.BOTTOM, pos);
            level.setBlock(blockPos, (BlockState) state.setValue(HALF, Half.TOP), 3);
            state.updateNeighbourShapes(level, pos, 3);
        }
    }

    public void playerWillDestroy(Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        if (!level.isClientSide() && player.isCreative()) {
            if (state.getValue(HALF) == Half.BOTTOM) {
                BlockPos topPos = getNeighbourPosition(state.getValue(HALF), pos);
                BlockState blockState = level.getBlockState(topPos);
                if (blockState.getBlock() == state.getBlock() && blockState.getValue(HALF) == Half.TOP) {
                    level.setBlock(topPos, Blocks.AIR.defaultBlockState(), 35);
                    level.levelEvent(player, 2001, topPos, Block.getId(blockState));
                }
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        Direction direction = blockState.getValue(FACING);
        Half half = blockState.getValue(HALF);
        double y = half == Half.BOTTOM ? 0 : -16;
        return Shapes.or(
                BlockFacingUtils.getCenterRotatedBox(direction, 7.1, y, 7.9, 1.8, 1, 1.8),
                BlockFacingUtils.getCenterRotatedBox(direction, 7.5, 1 + y, 8.3, 1, 30.7, 1),
                BlockFacingUtils.getCenterRotatedBox(direction, 7.3, 29.2 + y, 8, 1.4, 0.8, 1.5),
                BlockFacingUtils.getCenterRotatedBox(direction, 7.3, 17.2 + y, 8, 1.4, 0.8, 1.5),
                BlockFacingUtils.getCenterRotatedBox(direction, 6.1, 16.1 + y, 7.6, 3.8, 15.9, 0.4)
        );
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        Direction direction = blockState.getValue(FACING);
        Half half = blockState.getValue(HALF);
        if (half == Half.BOTTOM) {
            return Shapes.or(
                    BlockFacingUtils.getCenterRotatedBox(direction, 7.1, 0, 7.9, 1.8, 1, 1.8),
                    BlockFacingUtils.getCenterRotatedBox(direction, 7.5, 1, 8.3, 1, 30.7, 1)
            );
        } else {
            return Shapes.or(
                    BlockFacingUtils.getCenterRotatedBox(direction, 7.3, 13.2, 8, 1.4, 0.8, 1.5),
                    BlockFacingUtils.getCenterRotatedBox(direction, 7.3, 1.2, 8, 1.4, 0.8, 1.5),
                    BlockFacingUtils.getCenterRotatedBox(direction, 6.1, 0.1, 7.6, 3.8, 15.9, 0.4)
            );
        }
    }

    public static BlockPos getNeighbourPosition(Half half, BlockPos pos) {
        return half == Half.BOTTOM ? pos.above() : pos.below();
    }

    public enum Half implements StringRepresentable {
        TOP("top"),BOTTOM("bottom");

        private final String name;

        private Half(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}
