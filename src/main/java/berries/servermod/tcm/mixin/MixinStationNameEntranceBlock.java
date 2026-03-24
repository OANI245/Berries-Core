package berries.servermod.tcm.mixin;

import berries.servermod.tcm.block.blockentity.MixinBlockEntityHelper;
import berries.servermod.tcm.item.Items;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtr.mapping.holder.*;
import org.mtr.mapping.mapper.BlockEntityExtension;
import org.mtr.mapping.mapper.BlockExtension;
import org.mtr.mapping.mapper.DirectionHelper;
import org.mtr.mapping.tool.HolderBase;
import org.mtr.mod.block.BlockStationNameEntrance;
import org.mtr.mod.block.IBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static berries.servermod.tcm.block.MixinStates.*;

@Mixin(BlockStationNameEntrance.class)
public class MixinStationNameEntranceBlock extends BlockExtension {
    /*@Unique
    private final MapCodec<Block> codec = simpleCodec((p) -> new BlockStationNameEntrance(new BlockSettings(p)));*/

    public MixinStationNameEntranceBlock(BlockSettings properties) {
        super(properties);
    }

    /*@Override
    protected @NotNull MapCodec<? extends Block> codec() {
        return codec;
    }*/

    @Inject(
            method = "<init>",
            at = @At("TAIL"),
            remap = false
    )
    private void injected01(CallbackInfo ci) {
        this.registerDefaultState(this.getStateDefinition().any().setValue(SHOW_LINES_AND_EXIT_ZONE, true));
    }

    @Inject(
            method = "getPlacementState2",
            at = @At("RETURN"),
            remap = false,
            cancellable = true
    )
    private void injected03(ItemPlacementContext ctx, CallbackInfoReturnable<org.mtr.mapping.holder.BlockState> cir) {
        if (cir.getReturnValue() != null) {
            cir.setReturnValue(cir.getReturnValue().with(new org.mtr.mapping.holder.Property<>(SHOW_LINES_AND_EXIT_ZONE), true));
        }
    }


    @Inject(
            method = "onUse2",
            at = @At("HEAD"),
            remap = false,
            cancellable = true
    )
    private void injected04(org.mtr.mapping.holder.BlockState state, World world, org.mtr.mapping.holder.BlockPos pos, PlayerEntity player, Hand interactionHand, org.mtr.mapping.holder.BlockHitResult blockHitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (player.data.isHolding(Items.StaticFields.EDITOR)) {
            world.data.setBlockAndUpdate(pos.data, state.data.cycle(SHOW_LINES_AND_EXIT_ZONE));
            if (!world.isClient()) {
                BlockStationNameEntrance block = (BlockStationNameEntrance) state.data.getBlock();
                block.propagate(world, pos, org.mtr.mapping.holder.Direction.convert(IBlock.getStatePropertySafe(state, DirectionHelper.FACING).data.getClockWise()),
                        new org.mtr.mapping.holder.Property<Boolean>(SHOW_LINES_AND_EXIT_ZONE), 1);
                block.propagate(world, pos, org.mtr.mapping.holder.Direction.convert(IBlock.getStatePropertySafe(state, DirectionHelper.FACING).data.getCounterClockWise()),
                        new org.mtr.mapping.holder.Property<Boolean>(SHOW_LINES_AND_EXIT_ZONE), 1);
            }
            cir.setReturnValue(ActionResult.convert(InteractionResult.SUCCESS));
        }
    }

    @Unique
    private <T extends Comparable<T>> T getStatePropertySafe(BlockState state, Property<T> property) {
        try {
            return state.hasProperty(property) ? state.getValue(property) : new ArrayList<>(property.getAllValues().toList()).get(0).value();
        } catch (Exception ignored) {
        }
        return new ArrayList<>(property.getAllValues().toList()).get(0).value();
    }

    @Inject(
            method = "addBlockProperties",
            at = @At("TAIL"),
            remap = false
    )
    private void injected05(List<HolderBase<?>> properties, CallbackInfo ci) {
        properties.add(new org.mtr.mapping.holder.Property<>(SHOW_LINES_AND_EXIT_ZONE));
    }

    @Override
    public void onPlaced2(World world, org.mtr.mapping.holder.BlockPos posv, org.mtr.mapping.holder.BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        var blockState = state.data;
        var level = world.data;
        var pos = posv.data;
        if (blockState != null && !level.isClientSide) {
            Direction direction = blockState.getValue(HorizontalDirectionalBlock.FACING);
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof BlockStationNameEntrance.BlockEntity) {
                BlockPos leftBlockPos = pos.relative(direction.getCounterClockWise(), 1);
                BlockPos rightBlockPos = pos.relative(direction.getClockWise(), 1);

                BlockEntity leftBlockEntity = level.getBlockEntity(leftBlockPos);
                BlockEntity rightBlockEntity = level.getBlockEntity(rightBlockPos);

                if (leftBlockEntity != null &&
                        Optional.ofNullable(MixinBlockEntityHelper.invokeGetMethodInBlockEntity(leftBlockEntity, "getSelectedExitZone", Long.TYPE))
                                .orElse(0L)
                                != 0L) {
                    MixinBlockEntityHelper.invokeSetMethodInBlockEntity(
                            entity,
                            "setSelectedExitZone",
                            MixinBlockEntityHelper.invokeGetMethodInBlockEntity(leftBlockEntity, "getSelectedExitZone", Long.TYPE)
                    );
                } else if (rightBlockEntity != null &&
                        Optional.ofNullable(MixinBlockEntityHelper.invokeGetMethodInBlockEntity(rightBlockEntity, "getSelectedExitZone", Long.TYPE))
                                .orElse(0L)
                                != 0L) {
                    MixinBlockEntityHelper.invokeSetMethodInBlockEntity(
                            entity,
                            "setSelectedExitZone",
                            MixinBlockEntityHelper.invokeGetMethodInBlockEntity(rightBlockEntity, "getSelectedExitZone", Long.TYPE)
                    );
                }

                ((BlockStationNameEntrance.BlockEntity) entity).writeCompoundTag(new CompoundTag());
            }
        }
    }
}
