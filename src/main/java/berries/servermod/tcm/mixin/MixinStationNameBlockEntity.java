package berries.servermod.tcm.mixin;

import org.mtr.mapping.holder.BlockEntityType;
import org.mtr.mapping.holder.BlockPos;
import org.mtr.mapping.holder.BlockState;
import org.mtr.mapping.holder.CompoundTag;
import org.mtr.mapping.mapper.BlockEntityExtension;
import org.mtr.mod.block.BlockStationNameBase;
import org.mtr.mod.block.BlockStationNameEntrance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockStationNameEntrance.BlockEntity.class)
public abstract class MixinStationNameBlockEntity extends BlockStationNameBase.BlockEntityBase {
    @Unique
    protected long selectedExitZone = 0;

    public MixinStationNameBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, float yOffset, float zOffset, boolean isDoubleSided) {
        super(type, pos, state, yOffset, zOffset, isDoubleSided);
    }

    @Override
    public void readCompoundTag(CompoundTag compoundTag) {
        selectedExitZone = compoundTag.getLong("selected_exit_zone");
        super.readCompoundTag(compoundTag);
    }

    @Override
    public void writeCompoundTag(CompoundTag compoundTag) {
        compoundTag.putLong("selected_exit_zone", selectedExitZone);
        super.writeCompoundTag(compoundTag);
    }

    @Unique
    public void setSelectedExitZone(Long value) {
        selectedExitZone = value;
        markDirty2();
    }

    @Unique
    public Long getSelectedExitZone() {
        return selectedExitZone;
    }
}