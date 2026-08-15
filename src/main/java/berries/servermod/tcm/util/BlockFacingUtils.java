package berries.servermod.tcm.util;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface BlockFacingUtils {
    static VoxelShape getCenterRotatedBox(Direction direction, double x, double y, double z, double w, double h, double d) {
        double x1 = x;
        double y1 = y;
        double z1 = z;
        double x2 = x + w;
        double y2 = y + h;
        double z2 = z + d;
        switch (direction) {
            case SOUTH -> {
                return Block.box(16 - x2, y1, 16 - z2, 16 - x1, y2, 16 - z1);
            }
            case EAST -> {
                return Block.box(16 - z2, y1, x1, 16 - z1, y2, x2);
            }
            case WEST -> {
                return Block.box(z1, y1, 16 - x2, z2, y2, 16 - x1);
            }
            default -> {
                return Block.box(x1, y1, z1, x2, y2, z2);
            }
        }
    }
}
