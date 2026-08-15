package berries.servermod.tcm.block.blockentity;

import berries.servermod.tcm.block.BlockEntityId;
import berries.servermod.tcm.block.Blocks;
import berries.servermod.tcm.block.CRHTicketBarrierBlock;
import berries.servermod.tcm.block.StationsNameInfoBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface BlockEntityTypes {
    BlockEntityType<StationsNameInfoBlock.StationsNameInfoBlockEntity> STATIONS_NAME_INFO_BLOCK_ENTITY =
            register(BlockEntityType.Builder.of(StationsNameInfoBlock.StationsNameInfoBlockEntity::new, Blocks.STATIONS_NAME_INFO_BLOCK).build(null), StationsNameInfoBlock.StationsNameInfoBlockEntity.class);
    BlockEntityType<CRHTicketBarrierBlock.CRHTicketBarrierBlockEntity> CR_TICKET_BARRIER_ENTRANCE_BLOCK_ENTITY =
            register(BlockEntityType.Builder.of(CRHTicketBarrierBlock.CRHTicketBarrierBlockEntity::new, Blocks.CR_TICKET_BARRIER_ENTRANCE_BLOCK.get()).build(null), CRHTicketBarrierBlock.CRHTicketBarrierBlockEntity.class);

    static void register() {
    }

    static <T extends BlockEntity> BlockEntityType<T> register(BlockEntityType<T> v, Class<T> c) {
        try {
            var anno = c.getAnnotation(BlockEntityId.class);
            return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation(anno.modid(), anno.value()), v);
        } catch (Exception e) {
            e.printStackTrace();
            return v;
        }
    }
}
