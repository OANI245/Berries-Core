package berries.servermod.tcm.client.packet;

import berries.servermod.tcm.block.blockentity.MixinBlockEntityHelper;
import berries.servermod.tcm.client.screen.TCMDialogScreen;
import berries.servermod.tcm.util.TCMComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.mtr.core.data.NameColorDataBase;
import org.mtr.core.data.Station;
import org.mtr.core.data.StationExit;
import org.mtr.libraries.it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import org.mtr.libraries.it.unimi.dsi.fastutil.longs.LongCollection;
import org.mtr.libraries.it.unimi.dsi.fastutil.longs.LongCollections;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import org.mtr.mapping.holder.World;
import org.mtr.mapping.mapper.ScreenExtension;
import org.mtr.mod.InitClient;
import org.mtr.mod.block.BlockStationNameEntrance;
import org.mtr.mod.screen.DashboardListItem;
import org.mtr.mod.screen.DashboardListSelectorScreen;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class PacketScreenClient {
    @SuppressWarnings("all")
    public static void receiveScreenS2C(FriendlyByteBuf packet) {
        String screenId = packet.readUtf();
        BlockPos pos = packet.readBlockPos();
        long selectedId = packet.readLong();
        Minecraft mc = Minecraft.getInstance();

        mc.tell(() -> {
            switch (screenId) {
                case "SELECT_EXIT_ZONES" -> {
                    final Station station = InitClient.findStation(new org.mtr.mapping.holder.BlockPos(pos));
                    final ObjectArrayList<StationExit> exits = station.getExits();
                    final List<DashboardListItem> items = new ArrayList<>();
                    final LongCollection selectedIds = new LongAVLTreeSet();
                    selectedIds.add(selectedId);
                    exits.forEach((exit) -> {
                        final ObjectArrayList<String> destinations = exit.getDestinations();
                        final String additional = destinations.size() > 1 ? String.format("|(+%s)", destinations.size() - 1) : "";
                        items.add(new DashboardListItem(serializeExit(exit.getName()), exit.getName(), 0));
                    });
                    mc.setScreen(new DashboardListSelectorScreen(
                            () -> {
                                try {
                                    BlockEntity blockEntity = mc.level.getBlockEntity(pos);
                                    if (selectedIds.size() > 0) {
                                        PacketModifyBlockEntityClient.sendModifyBlockEntityC2S(pos, Set.of(selectedIds.stream().toList().get(0)));
                                    } else {
                                        PacketModifyBlockEntityClient.sendModifyBlockEntityC2S(pos, Set.of(0L));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    mc.tell(() -> {
                                        mc.setScreen(new TCMDialogScreen(
                                                TCMComponent.text("Oh no..."),
                                                TCMComponent.text(e.getMessage()),
                                                List.of(new Tuple<>(
                                                        TCMComponent.text("Close"),
                                                        (mci, btn) -> {
                                                            mci.tell(() -> {
                                                                mci.setScreen(null);
                                                            });
                                                        }
                                                ))
                                        ));
                                    });
                                }
                            },
                            new ObjectImmutableList<>(items),
                            selectedIds,
                            true,
                            false,
                            null
                    ));
                }
                default -> {
                    return;
                }
            }
        });
    }

    private static long serializeExit(String exitName) {
        final char[] characters = exitName.toCharArray();
        long code = 0;
        for (final char character : characters) {
            code = code << 8;
            code += character;
        }
        return code;
    }
}
