package berries.servermod.tcm.client.packet;

import berries.servermod.tcm.block.blockentity.MixinBlockEntityHelper;
import berries.servermod.tcm.client.screen.EditPSDTopScreen;
import berries.servermod.tcm.client.screen.TCMDialogScreen;
import berries.servermod.tcm.util.TCMComponent;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.mtr.core.data.NameColorDataBase;
import org.mtr.core.data.Station;
import org.mtr.core.data.StationExit;
import org.mtr.core.tool.Utilities;
import org.mtr.libraries.it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import org.mtr.libraries.it.unimi.dsi.fastutil.longs.LongCollection;
import org.mtr.libraries.it.unimi.dsi.fastutil.longs.LongCollections;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import org.mtr.mapping.holder.World;
import org.mtr.mapping.mapper.ScreenExtension;
import org.mtr.mod.InitClient;
import org.mtr.mod.block.BlockStationNameEntrance;
import org.mtr.mod.client.MinecraftClientData;
import org.mtr.mod.data.IGui;
import org.mtr.mod.screen.DashboardListItem;
import org.mtr.mod.screen.DashboardListSelectorScreen;
import org.mtr.mod.screen.EditStationScreen;
import org.mtr.mod.screen.PIDSConfigScreen;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class PacketScreenClient {
    @SuppressWarnings("all")
    public static void receiveScreenS2C(FriendlyByteBuf packet) {
        String screenId = packet.readUtf();
        BlockPos pos = packet.readBlockPos();
        Minecraft mc = Minecraft.getInstance();
        int len = packet.readInt();
        var lal = new LongArrayList();
        for (int i = 0; i < len; i++) {
            lal.add(i, packet.readLong());
        }
        var l0 = len <= 0 ? 0L : lal.get(0);
        mc.tell(() -> {
            switch (screenId) {
                case "SELECT_EXIT_ZONES" -> {
                    final Station station = InitClient.findStation(new org.mtr.mapping.holder.BlockPos(pos));
                    var exitsForDashboardList = new ObjectImmutableList<>(EditStationScreen.getExitsForDashboardList(EditStationScreen.getStationExits(station, true)));
                    //final ObjectArrayList<StationExit> exits = station.getExits();
                    final LongCollection selectedIds = new LongAVLTreeSet();
                    selectedIds.add(l0);
                    /*exits.forEach((exit) -> {
                        final ObjectArrayList<String> destinations = exit.getDestinations();
                        final String additional = destinations.size() > 1 ? String.format("|(+%s)", destinations.size() - 1) : "";
                        items.add(new DashboardListItem(serializeExit(exit.getName()), exit.getName(), 0));
                    });*/
                    mc.setScreen(new DashboardListSelectorScreen(
                            () -> {
                                try {
                                    BlockEntity blockEntity = mc.level.getBlockEntity(pos);
                                    if (selectedIds.size() > 0) {
                                        PacketModifyBlockEntityClient.sendModifyBlockEntityC2S(pos, "SNE_EXIT_ZONE", Set.of(selectedIds.stream().toList().get(0)));
                                    } else {
                                        PacketModifyBlockEntityClient.sendModifyBlockEntityC2S(pos, "SNE_EXIT_ZONE", Set.of(0L));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    mc.tell(() -> {
                                        mc.setScreen(new TCMDialogScreen(
                                                TCMComponent.text("Oh no..."),
                                                TCMComponent.text(e.getMessage()),
                                                List.of(new ObjectObjectImmutablePair<>(
                                                        new ObjectObjectImmutablePair<>(TCMComponent.text("Close"), false),
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
                            new ObjectImmutableList<>(exitsForDashboardList),
                            selectedIds,
                            true,
                            false,
                            null
                    ));
                }
                case "SELECT_PLATFORMS" -> {
                    final Station station = InitClient.findStation(new org.mtr.mapping.holder.BlockPos(pos));
                    var platforms = new ObjectArrayList<>(station.savedRails);
                    final ObjectArrayList<DashboardListItem> platformsForList = new ObjectArrayList<>();
                    Collections.sort(platforms);
                    platforms.forEach(platform -> {
                        var sr = MinecraftClientData.getInstance().simplifiedRoutes
                                .stream()
                                .filter(simplifiedRoute -> simplifiedRoute.getPlatformIndex(platform.getId()) >= 0).toList();
                        var fr = sr.size() > 0 ? sr.getFirst() : null;
                        platformsForList.add(new DashboardListItem(platform.getId(), platform.getName() + " " + IGui.mergeStations(
                                sr.stream()
                                .map(simplifiedRoute -> Utilities.getElement(simplifiedRoute.getPlatforms(), -1).getStationName())
                                .collect(Collectors.toList())
                        ), fr != null ? fr.getColor() : 0));
                    });
                    var platformsForDashboardList = new ObjectImmutableList<>(platformsForList);
                    final LongCollection selectedIds = new LongAVLTreeSet();
                    selectedIds.add(l0);
                    mc.setScreen(new DashboardListSelectorScreen(
                            () -> {
                                try {
                                    BlockEntity blockEntity = mc.level.getBlockEntity(pos);
                                    if (selectedIds.size() > 0) {
                                        PacketModifyBlockEntityClient.sendModifyBlockEntityC2S(pos, "SNE_PLATFORM", Set.of(selectedIds.stream().toList().get(0)));
                                    } else {
                                        PacketModifyBlockEntityClient.sendModifyBlockEntityC2S(pos, "SNE_PLATFORM", Set.of(0L));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    mc.tell(() -> {
                                        mc.setScreen(new TCMDialogScreen(
                                                TCMComponent.text("Oh no..."),
                                                TCMComponent.text(e.getMessage()),
                                                List.of(new ObjectObjectImmutablePair<>(
                                                        new ObjectObjectImmutablePair<>(TCMComponent.text("Close"), false),
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
                            new ObjectImmutableList<>(platformsForDashboardList),
                            selectedIds,
                            true,
                            true,
                            null
                    ));
                }
                case "SELECT_PLATFORM_NUMBERS" -> {
                    final Station station = InitClient.findStation(new org.mtr.mapping.holder.BlockPos(pos));
                    var platforms = new ObjectArrayList<>(station.savedRails);
                    final ObjectArrayList<DashboardListItem> platformsForList = new ObjectArrayList<>();
                    Collections.sort(platforms);
                    platforms.forEach(platform -> {
                        platformsForList.add(new DashboardListItem(platform.getId(), "PLATFORM " + platform.getName(), 0));
                    });
                    var platformsForDashboardList = new ObjectImmutableList<>(platformsForList);
                    final LongCollection selectedIds = new LongAVLTreeSet();
                    selectedIds.addAll(lal);
                    mc.setScreen(new DashboardListSelectorScreen(
                            () -> {
                                try {
                                    BlockEntity blockEntity = mc.level.getBlockEntity(pos);
                                    if (selectedIds.size() > 0) {
                                        PacketModifyBlockEntityClient.sendModifyBlockEntityC2S(pos, "SNE_PLATFORM_NUMBERS", selectedIds.stream().toList());
                                    } else {
                                        PacketModifyBlockEntityClient.sendModifyBlockEntityC2S(pos, "SNE_PLATFORM_NUMBERS", Set.of(0L));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    mc.tell(() -> {
                                        mc.setScreen(new TCMDialogScreen(
                                                TCMComponent.text("Oh no..."),
                                                TCMComponent.text(e.getMessage()),
                                                List.of(new ObjectObjectImmutablePair<>(
                                                        new ObjectObjectImmutablePair<>(TCMComponent.text("Close"), false),
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
                            new ObjectImmutableList<>(platformsForDashboardList),
                            selectedIds,
                            false,
                            false,
                            null
                    ));
                }
                case "EDIT_PSD_TOP" -> {
                    mc.setScreen(new EditPSDTopScreen(pos));
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
