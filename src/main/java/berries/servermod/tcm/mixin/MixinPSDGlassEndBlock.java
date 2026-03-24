package berries.servermod.tcm.mixin;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.mtr.mapping.holder.Property;
import org.mtr.mapping.tool.HolderBase;
import org.mtr.mod.block.BlockPSDAPGGlassEndBase;
import org.mtr.mod.block.BlockPSDGlassEnd;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(value = BlockPSDGlassEnd.class,remap = false)
public class MixinPSDGlassEndBlock extends BlockPSDAPGGlassEndBase {
    @Unique
    private static final BooleanProperty IS_RIGHT_ANGLE_END = BooleanProperty.create("right_angle_end");

    @Unique
    private static final BooleanProperty END_DOOR = BooleanProperty.create("end_door");

    @Override
    public void addBlockProperties(List<HolderBase<?>> properties) {
        properties.add(FACING);
        properties.add(HALF);
        properties.add(SIDE_EXTENDED);
        properties.add(TOUCHING_LEFT);
        properties.add(TOUCHING_RIGHT);
        properties.add(new Property<>(IS_RIGHT_ANGLE_END));
        properties.add(new Property<>(END_DOOR));
    }
}
