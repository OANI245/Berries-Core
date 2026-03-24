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
import org.mtr.mod.block.BlockPSDTop;
import org.mtr.mod.block.IBlock;

import java.util.ArrayList;

import static berries.servermod.tcm.block.MixinStates.PSD_TOP_DISPLAY_TYPE;
import static org.mtr.mod.block.BlockPSDTop.PERSISTENT;
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

        if (state.getValue(PERSISTENT.data) == BlockPSDTop.EnumPersistent.ARROW || state.getValue(PERSISTENT.data) == BlockPSDTop.EnumPersistent.NONE) {
            world.setBlock(pos, state.cycle(PSD_TOP_DISPLAY_TYPE), 3);
            BlockPos pos1 = (getStatePropertySafe(state, SIDE_EXTENDED.data) == IBlock.EnumSide.LEFT) ? pos.relative(getStatePropertySafe(state, HorizontalDirectionalBlock.FACING).getClockWise()) : pos.relative(getStatePropertySafe(state, HorizontalDirectionalBlock.FACING).getCounterClockWise());
            world.setBlock(pos1, world.getBlockState(pos1).cycle(PSD_TOP_DISPLAY_TYPE), 3);
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
