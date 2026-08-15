package berries.servermod.tcm.item;

import berries.servermod.tcm.block.MixinStates;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.mtr.mapping.holder.Block;
import org.mtr.mapping.holder.Direction;
import org.mtr.mapping.holder.World;
import org.mtr.mod.block.BlockPSDAPGDoorBase;
import org.mtr.mod.block.BlockPSDAPGGlassEndBase;
import org.mtr.mod.block.BlockPSDTop;
import org.mtr.mod.block.IBlock;
import org.mtr.mod.render.RenderRouteBase;

import static berries.servermod.tcm.block.MixinStates.BeijingStylePSDTopType.*;

import java.util.ArrayList;

import static berries.servermod.tcm.block.MixinStates.PSD_TOP_DISPLAY_TYPE;
import static org.mtr.mapping.mapper.DirectionHelper.FACING;
import static org.mtr.mod.block.BlockPSDTop.*;
import static org.mtr.mod.block.IBlock.SIDE_EXTENDED;

public class Editor extends Item {
    public Editor(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);

        boolean bl = MixinStates.sneakAndUse01(state, world, pos, context.getPlayer()) || MixinStates.sneakAndUse02(state, world, pos, context.getPlayer());
        if (bl) {
            return InteractionResult.SUCCESS;
        }

        if (!(state.getBlock() instanceof BlockPSDTop) || context.isSecondaryUseActive()) return InteractionResult.PASS;

        if (state.getValue(PERSISTENT.data) == BlockPSDTop.EnumPersistent.ARROW || state.getValue(PERSISTENT.data) == BlockPSDTop.EnumPersistent.ROUTE || state.getValue(PERSISTENT.data) == BlockPSDTop.EnumPersistent.NONE) {
            var cycleValue = switch (state.getValue(PSD_TOP_DISPLAY_TYPE)) {
                case DEFAULT -> {
                    BlockPSDTop.EnumPersistent persistent = state.getValue(PERSISTENT.data);
                    if (persistent == EnumPersistent.NONE) {
                        var blockBelow = world.getBlockState(pos.below()).getBlock();
                        if (blockBelow instanceof BlockPSDAPGDoorBase) {
                            yield STATION_NAME;
                        } else {
                            yield !(blockBelow instanceof BlockPSDAPGGlassEndBase) ? ARROW : STATION_NAME;
                        }
                    } else {
                        yield persistent == EnumPersistent.ARROW ? STATION_NAME : (persistent == EnumPersistent.ROUTE ? ARROW : STATION_NAME);
                    }
                }
                case ARROW -> STATION_NAME;
                case STATION_NAME -> DEFAULT;
            };
            world.setBlock(pos, state.setValue(PSD_TOP_DISPLAY_TYPE, cycleValue), 3);
            boolean[] sign = new boolean[] {true};
            ((IBlock)state.getBlock()).propagate(new World(world), new org.mtr.mapping.holder.BlockPos(pos),
                    Direction.convert(state.getValue(HorizontalDirectionalBlock.FACING)).rotateYClockwise(),
                    ((offsetPos) -> {
                        var s0 = world.getBlockState(offsetPos.data);
                        if (!sign[0] ||
                                s0.getValue(SIDE_EXTENDED.data) == EnumSide.LEFT || s0.getValue(SIDE_EXTENDED.data) == EnumSide.SINGLE || s0.getValue(PERSISTENT.data) != state.getValue(PERSISTENT.data)) {
                            sign[0] = false;
                            return;
                        }
                        world.setBlockAndUpdate(offsetPos.data,
                                s0.setValue(PSD_TOP_DISPLAY_TYPE, cycleValue));
                    }),
                    20);
            sign[0] = true;
            ((IBlock)state.getBlock()).propagate(new World(world), new org.mtr.mapping.holder.BlockPos(pos),
                    Direction.convert(state.getValue(HorizontalDirectionalBlock.FACING)).rotateYCounterclockwise(),
                    ((offsetPos) -> {
                        var s0 = world.getBlockState(offsetPos.data);
                        if (!sign[0] ||
                                s0.getValue(SIDE_EXTENDED.data) == EnumSide.RIGHT || s0.getValue(SIDE_EXTENDED.data) == EnumSide.SINGLE || s0.getValue(PERSISTENT.data) != state.getValue(PERSISTENT.data)) {
                            sign[0] = false;
                            return;
                        }
                        world.setBlockAndUpdate(offsetPos.data,
                                s0.setValue(PSD_TOP_DISPLAY_TYPE, cycleValue));
                    }),
                    20);
            /*BlockPos pos1 = (getStatePropertySafe(state, SIDE_EXTENDED.data) == IBlock.EnumSide.LEFT) ? pos.relative(getStatePropertySafe(state, HorizontalDirectionalBlock.FACING).getClockWise()) : pos.relative(getStatePropertySafe(state, HorizontalDirectionalBlock.FACING).getCounterClockWise());
            world.setBlock(pos1, world.getBlockState(pos1).setValue(PSD_TOP_DISPLAY_TYPE, cycleValue), 3);*/
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private <T extends Comparable<T>> T getStatePropertySafe(BlockState state, Property<T> property) {
        try {
            return state.hasProperty(property) ? state.getValue(property) : new ArrayList<>(property.getAllValues().toList()).get(0).value();
        } catch (Exception ignored) {
        }
        return new ArrayList<>(property.getAllValues().toList()).get(0).value();
    }
}
