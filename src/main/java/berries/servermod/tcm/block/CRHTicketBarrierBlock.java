package berries.servermod.tcm.block;

import berries.servermod.tcm.block.blockentity.BlockEntityTypes;
import berries.servermod.tcm.data.TCMGameRules;
import berries.servermod.tcm.packet.PacketScreenServer;
import berries.servermod.tcm.util.TCMComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtr.libraries.it.unimi.dsi.fastutil.longs.LongArrayList;
import org.mtr.mapping.holder.*;
import org.mtr.mapping.tool.HolderBase;
import org.mtr.mod.SoundEvents;
import org.mtr.mod.block.BlockTicketBarrier;
import org.mtr.mod.block.IBlock;
import org.mtr.mod.data.ArrivalsCacheClient;
import org.mtr.mod.data.TicketSystem;

import java.util.List;

public class CRHTicketBarrierBlock extends BlockTicketBarrier implements EntityBlock {
    public static final BooleanProperty LOCKED = BooleanProperty.create("locked");

    public final boolean entranceType;

    public CRHTicketBarrierBlock(boolean isEntrance) {
        super(isEntrance);
        this.entranceType = isEntrance;
    }

    @Override
    public @NotNull ActionResult onUse2(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return IBlock.checkHoldingBrush(world, player, () -> {
            if (!world.isClient()) {
                if (entranceType) {
                    var entity = world.getBlockEntity(pos);
                    if (entity != null && entity.data instanceof CRHTicketBarrierBlockEntity entityVanilla) {
                        PacketScreenServer.sendScreenS2C((ServerPlayer) player.data, entityVanilla, pos.data, "SELECT_PLATFORM_NUMBERS");
                    }
                } else {
                    world.setBlockState(pos, state.cycle(new Property<>(LOCKED)));
                }
            }
        });
    }

    @Override
    public void onEntityCollision2(BlockState state, World world, BlockPos blockPos, Entity entity) {
        if (!world.isClient() && PlayerEntity.isInstance(entity)) {
            if (state.get(new Property<>(LOCKED))) {
                ((Player) entity.data).displayClientMessage(TCMComponent.translatable(entranceType ? "gui.tcm.cr_ticket_barrier.platform_not_open" : "gui.tcm.cr_ticket_barrier.locked"), true);
                return;
            }

            final Direction facing = IBlock.getStatePropertySafe(state, FACING);
            final Vector3d playerPosRotated = entity.getPos().subtract(blockPos.getX() + 0.5, 0, blockPos.getZ() + 0.5).rotateY((float) Math.toRadians(facing.asRotation()));
            final TicketSystem.EnumTicketBarrierOpen open = IBlock.getStatePropertySafe(state, new Property<>(OPEN.data));

            if ((open == TicketSystem.EnumTicketBarrierOpen.OPEN || open == TicketSystem.EnumTicketBarrierOpen.OPEN_CONCESSIONARY) && playerPosRotated.getZMapped() > 0) {
                world.setBlockState(blockPos, state.with(new Property<>(OPEN.data), TicketSystem.EnumTicketBarrierOpen.CLOSED));
            } else if (open == TicketSystem.EnumTicketBarrierOpen.CLOSED && playerPosRotated.getZMapped() < 0) {
                final BlockPos blockPosCopy = new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                world.setBlockState(blockPosCopy, state.with(new Property<>(OPEN.data), TicketSystem.EnumTicketBarrierOpen.PENDING));
                TicketSystem.passThrough(
                        world, blockPosCopy, PlayerEntity.cast(entity),
                        entranceType, !entranceType,
                        new SoundEvent(net.minecraft.sounds.SoundEvents.EMPTY), org.mtr.mod.SoundEvents.TICKET_BARRIER_CONCESSIONARY.get(),
                        new SoundEvent(net.minecraft.sounds.SoundEvents.EMPTY), SoundEvents.TICKET_BARRIER_CONCESSIONARY.get(),
                        null,
                        false,
                        newOpen -> {
                            world.setBlockState(blockPosCopy, state.with(new Property<>(OPEN.data), newOpen));
                            if (newOpen != TicketSystem.EnumTicketBarrierOpen.CLOSED && !hasScheduledBlockTick(world, blockPosCopy, new Block(this))) {
                                scheduleBlockTick(world, blockPosCopy, new Block(this), 40);
                            }
                        }
                );
            }
        }
    }

    @Override
    public BlockState getPlacementState2(@NotNull ItemPlacementContext ctx) {
        var t = super.getPlacementState2(ctx);
        return t == null ? null : t.with(new Property<>(LOCKED), entranceType);
    }

    @Override
    public void addBlockProperties(@NotNull List<HolderBase<?>> properties) {
        super.addBlockProperties(properties);
        properties.add(new Property<>(LOCKED));
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, net.minecraft.world.level.block.state.BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide || !entranceType || blockEntityType != BlockEntityTypes.CR_TICKET_BARRIER_ENTRANCE_BLOCK_ENTITY ? null :
                CRHTicketBarrierBlockEntity::tick;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(net.minecraft.core.BlockPos blockPos, net.minecraft.world.level.block.state.BlockState blockState) {
        return new CRHTicketBarrierBlockEntity(blockPos, blockState);
    }

    @BlockEntityId("cr_ticket_barrier_entrance_tile_entity")
    public static class CRHTicketBarrierBlockEntity extends BlockEntity {
        public LongArrayList selectedPlatforms = new LongArrayList();

        public CRHTicketBarrierBlockEntity(net.minecraft.core.BlockPos blockPos, net.minecraft.world.level.block.state.BlockState blockState) {
            super(BlockEntityTypes.CR_TICKET_BARRIER_ENTRANCE_BLOCK_ENTITY, blockPos, blockState);
        }

        @Override
        public void load(@NotNull net.minecraft.nbt.CompoundTag tag) {
            super.load(tag);
            ListTag platforms = tag.getList("platforms", 10);
            selectedPlatforms.clear();
            platforms.forEach((platform) -> {
                selectedPlatforms.add(((CompoundTag) platform).getLong("id"));
            });
        }

        @Override
        protected void saveAdditional(@NotNull net.minecraft.nbt.CompoundTag tag) {
            super.saveAdditional(tag);
            ListTag platforms = new ListTag();
            selectedPlatforms.forEach((platformId) -> {
                CompoundTag platformTag = new CompoundTag();
                platformTag.putLong("id", platformId);
                platforms.add(platformTag);
            });
            tag.put("platforms", platforms);
        }

        @Override
        public @NotNull CompoundTag getUpdateTag() {
            return saveWithoutMetadata();
        }

        @Override
        public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
            return ClientboundBlockEntityDataPacket.create(this);
        }

        public static void tick(Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state, BlockEntity blockEntity) {
            // 每 tick 都会调用，实现你的功能

            if (blockEntity instanceof  CRHTicketBarrierBlockEntity blockEntity1) {
                try {
                    var selectedPlatforms = blockEntity1.selectedPlatforms;
                    if (ArrivalsCacheClient.INSTANCE.requestArrivals(selectedPlatforms).stream().anyMatch(
                            (platform) -> {
                                var at = ((platform.getArrival() - ArrivalsCacheClient.INSTANCE.getMillisOffset() - System.currentTimeMillis()) / 1000) / 60;
                                return at >= level.getGameRules().getInt(TCMGameRules.TICKET_CHECK_END_ARRIVING_TIME) &&
                                        at <= level.getGameRules().getInt(TCMGameRules.TICKET_CHECK_START_ARRIVING_TIME);
                            })) {
                        level.setBlockAndUpdate(pos, state.setValue(LOCKED, false));
                    } else {
                        level.setBlockAndUpdate(pos, state.setValue(LOCKED, true));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
