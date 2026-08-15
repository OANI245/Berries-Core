package berries.servermod.tcm.block;

import berries.servermod.tcm.UFEInfo;
import berries.servermod.tcm.util.SingleTimeSetField;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.mtr.mod.CreativeModeTabs;

public interface Blocks {
    Block STATIONS_NAME_INFO_BLOCK = register(new StationsNameInfoBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.TERRACOTTA)));
    Block TRAIN_STOP_POSITION_BLOCK = register(new TrainStopPositionBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.IRON_BLOCK)));
    SingleTimeSetField<Block> CR_TICKET_BARRIER_ENTRANCE_BLOCK = new SingleTimeSetField<>();
    SingleTimeSetField<Block> CR_TICKET_BARRIER_EXIT_BLOCK = new SingleTimeSetField<>();
    SingleTimeSetField<Block> CR_TICKET_BARRIER_BARE_BLOCK = new SingleTimeSetField<>();

    static void register() {
        CR_TICKET_BARRIER_ENTRANCE_BLOCK.set(
                Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(UFEInfo.MOD_ID, "cr_ticket_barrier_entrance"),
                        new CRHTicketBarrierBlock(true)));
        var ctbebi = Registry.register(BuiltInRegistries.ITEM,
                new ResourceLocation(UFEInfo.MOD_ID, "cr_ticket_barrier_entrance"),
                new BlockItem(CR_TICKET_BARRIER_ENTRANCE_BLOCK.get(), new Item.Properties())
        );
        CR_TICKET_BARRIER_EXIT_BLOCK.set(
                Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(UFEInfo.MOD_ID, "cr_ticket_barrier_exit"),
                        new CRHTicketBarrierBlock(false)));
        var ctbfbi = Registry.register(BuiltInRegistries.ITEM,
                new ResourceLocation(UFEInfo.MOD_ID, "cr_ticket_barrier_exit"),
                new BlockItem(CR_TICKET_BARRIER_EXIT_BLOCK.get(), new Item.Properties())
        );
        ItemGroupEvents.modifyEntriesEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, CreativeModeTabs.RAILWAY_FACILITIES.identifier))
                .register((ItemGroupEvents.ModifyEntries)(content) -> {
                    content.accept(ctbebi);
                    content.accept(ctbfbi);
                });

        CR_TICKET_BARRIER_BARE_BLOCK.set(register(new CRHTicketBarrierBareBlock(BlockBehaviour.Properties.copy(CR_TICKET_BARRIER_ENTRANCE_BLOCK.get()).noOcclusion())));
    }

    static <T extends Block> T register(T v) {
        try {
            var anno = v.getClass().getAnnotation(BlockId.class);
            var block = Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(anno.modid(), anno.value()), v);
            var blockItem = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(anno.modid(), anno.value()), new BlockItem(block, new Item.Properties()));
            ItemGroupEvents.modifyEntriesEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, anno.itemGroup().getTab().identifier)).register((ItemGroupEvents.ModifyEntries)(content) -> content.accept((ItemLike)blockItem));
            return block;
        } catch (Exception e) {
            e.printStackTrace();
            return v;
        }
    }
}
