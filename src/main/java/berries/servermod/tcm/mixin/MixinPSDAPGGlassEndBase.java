package berries.servermod.tcm.mixin;

import org.mtr.mapping.holder.*;
import org.mtr.mod.block.BlockPSDAPGGlassBase;
import org.mtr.mod.block.BlockPSDAPGGlassEndBase;
import org.mtr.mod.block.IBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockPSDAPGGlassEndBase.class, remap = false)
public class MixinPSDAPGGlassEndBase extends BlockPSDAPGGlassBase {
    @Inject(
            method = "getEndOutlineShape",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void getEndOutlineShape(VoxelShape baseShape, BlockState state, int height, int thickness, boolean leftAir, boolean rightAir, CallbackInfoReturnable<VoxelShape> cir) {
        Direction facing = IBlock.getStatePropertySafe(state, FACING);
        if (facing == Direction.NORTH && leftAir || facing == Direction.SOUTH && rightAir) {
            baseShape = VoxelShapes.union(baseShape, Block.createCuboidShape((double)0.0F, (double)0.0F, (double)0.0F, (double)thickness, (double)height, (double)16.0F));
        }

        if (facing == Direction.EAST && leftAir || facing == Direction.WEST && rightAir) {
            baseShape = VoxelShapes.union(baseShape, Block.createCuboidShape((double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)height, (double)thickness));
        }

        if (facing == Direction.SOUTH && leftAir || facing == Direction.NORTH && rightAir) {
            baseShape = VoxelShapes.union(baseShape, Block.createCuboidShape((double)(16 - thickness), (double)0.0F, (double)0.0F, (double)16.0F, (double)height, (double)16.0F));
        }

        if (facing == Direction.WEST && leftAir || facing == Direction.EAST && rightAir) {
            baseShape = VoxelShapes.union(baseShape, Block.createCuboidShape((double)0.0F, (double)0.0F, (double)(16 - thickness), (double)16.0F, (double)height, (double)16.0F));
        }

        cir.setReturnValue(baseShape);
    }
}
