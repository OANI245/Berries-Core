package berries.servermod.tcm.mixin;

import net.minecraft.world.level.block.state.BlockState;
import org.mtr.mapping.holder.BlockSettings;
import org.mtr.mapping.holder.Property;
import org.mtr.mapping.holder.WorldAccess;
import org.mtr.mapping.mapper.BlockExtension;
import org.mtr.mapping.tool.HolderBase;
import org.mtr.mod.block.IBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static berries.servermod.tcm.block.MixinStates.*;

@Mixin(value = org.mtr.mod.block.BlockPSDTop.class,remap = false)
public abstract class MixinPSDTopBlock extends BlockExtension implements IBlock {
    private MixinPSDTopBlock(BlockSettings properties) {
        super(properties);
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL"),
            remap = false
    )
    private void injected01(CallbackInfo ci) {
        registerDefaultState(this.getStateDefinition().any().setValue(PSD_TOP_CONTENT_STYLE, 1).setValue(PSD_TOP_DISPLAY_TYPE, BeijingStylePSDTopType.DEFAULT));
    }

    @Inject(
            method = "addBlockProperties",
            at = @At("TAIL"), remap = false
    )
    private void injected02(List<HolderBase<?>> properties, CallbackInfo ci) {
        properties.add(new Property<>(PSD_TOP_CONTENT_STYLE));
        properties.add(new Property<>(PSD_TOP_DISPLAY_TYPE));
    }

    @Inject(
            method = "getActualState",
            at = @At("TAIL"), remap = false,
            cancellable = true)
    private static void injected04(WorldAccess world, org.mtr.mapping.holder.BlockPos pos, CallbackInfoReturnable<org.mtr.mapping.holder.BlockState> cir) {
        BlockState oldState = world.data.getBlockState(pos.data);
        if (!oldState.getValues().containsKey(PSD_TOP_CONTENT_STYLE)) {
            cir.setReturnValue(new org.mtr.mapping.holder.BlockState(cir.getReturnValue().data.setValue(PSD_TOP_CONTENT_STYLE, 1)));
        }
    }
}
