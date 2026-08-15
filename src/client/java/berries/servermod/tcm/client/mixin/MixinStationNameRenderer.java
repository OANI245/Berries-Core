package berries.servermod.tcm.client.mixin;

import berries.servermod.tcm.block.MixinStates;
import berries.servermod.tcm.block.blockentity.MixinBlockEntityHelper;
import berries.servermod.tcm.client.data.TCMDynamicResourceCacheV2;
import berries.servermod.tcm.client.util.TextUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.mtr.core.data.Station;
import org.mtr.core.data.StationExit;
import org.mtr.libraries.it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import org.mtr.libraries.it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArraySet;
import org.mtr.mapping.holder.BlockPos;
import org.mtr.mapping.holder.BlockState;
import org.mtr.mapping.holder.Direction;
import org.mtr.mapping.holder.World;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mod.InitClient;
import org.mtr.mod.block.BlockStationNameBase;
import org.mtr.mod.block.BlockStationNameEntrance;
import org.mtr.mod.block.IBlock;
import org.mtr.mod.client.DynamicTextureCache;
import org.mtr.mod.client.IDrawing;
import org.mtr.mod.client.MinecraftClientData;
import org.mtr.mod.data.IGui;
import org.mtr.mod.generated.lang.TranslationProvider;
import org.mtr.mod.render.*;
import org.mtr.mod.screen.DashboardListItem;
import org.mtr.mod.screen.EditStationScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.*;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
@Mixin(RenderStationNameTiled.class)
public abstract class MixinStationNameRenderer<T extends BlockStationNameBase.BlockEntityBase> extends RenderStationNameBase<T> {
    @Final
    @Shadow(remap = false)
    private boolean showLogo;

    @Unique
    private long hash1 = 0L;

    public MixinStationNameRenderer(Argument dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(T entity, float tickDelta, GraphicsHolder graphicsHolder, int light, int overlay) {
        final World world = entity.getWorld2();
        if (world == null) {
            return;
        }

        final BlockPos pos = entity.getPos2();
        final BlockState state = world.getBlockState(pos);
        final Direction facing = IBlock.getStatePropertySafe(state, BlockStationNameBase.FACING);
        final int color = RenderRouteBase.getShadingColor(facing, entity.getColor(state));

        final StoredMatrixTransformations storedMatrixTransformations = new StoredMatrixTransformations(0.5 + pos.getX(), 0.5 + entity.yOffset + pos.getY(), 0.5 + pos.getZ());
        storedMatrixTransformations.add(graphicsHolderNew -> {
            graphicsHolderNew.rotateYDegrees(-facing.asRotation());
            graphicsHolderNew.rotateZDegrees(180);
        });

        final Station station = InitClient.findStation(pos);
        long selectedExit = 0;
        if (station != null) {
            try {
                selectedExit =
                        Optional.ofNullable(MixinBlockEntityHelper.invokeGetMethodInBlockEntity(entity, "getSelectedExitZone", Long.TYPE))
                                .orElse(0L);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        /*String left = exitZoneName.replaceAll("\\d+$", ""),
                right = exitZoneName.replaceAll("^" + left, "");
        String[] exitZone = !exitZoneName.isEmpty() ? new String[]{left, right} : new String[]{null, null};*/

        for (int i = 0; i < (entity.isDoubleSided ? 2 : 1); i++) {
            final StoredMatrixTransformations storedMatrixTransformations2 = storedMatrixTransformations.copy();
            final boolean shouldFlip = i == 1;
            storedMatrixTransformations2.add(graphicsHolderNew -> {
                if (shouldFlip) {
                    graphicsHolderNew.rotateYDegrees(180);
                }
                graphicsHolderNew.translate(0, 0, 0.5 - entity.zOffset - SMALL_OFFSET);
            });
            drawStationName(world, pos, state, facing, storedMatrixTransformations2, station == null ? TranslationProvider.GUI_MTR_UNTITLED.getString() : station.getName(), station == null ? 0 : station.getColor(), color, light, station == null ? 0 : station.getId(), selectedExit);
        }
    }

    @Unique
    protected void drawStationName(World world, BlockPos pos, BlockState state, Direction facing, StoredMatrixTransformations storedMatrixTransformations, String stationName, int stationColor, int color, int light, long stationId, long selectedExit) {
        final int lengthLeft = getLength(world, pos, false);
        final int lengthRight = getLength(world, pos, true);

        final int totalLength = lengthLeft + lengthRight - 1;
        if (showLogo) {
            final int propagateProperty = IBlock.getStatePropertySafe(world, pos, BlockStationNameEntrance.STYLE);
            final float logoSize = propagateProperty % 2 == 0 ? 0.5F : 1;
            MainRenderer.scheduleRender(
                    state.data.hasProperty(MixinStates.SHOW_LINES_AND_EXIT_ZONE) && !state.data.getValue(MixinStates.SHOW_LINES_AND_EXIT_ZONE) ?
                            DynamicTextureCache.instance.getStationNameEntrance(
                                    propagateProperty < 2 || propagateProperty >= 4 ? ARGB_WHITE : ARGB_BLACK,
                                    IGui.insertTranslation(TranslationProvider.GUI_MTR_STATION_CJK,
                                            TranslationProvider.GUI_MTR_STATION, 1, stationName),
                                    totalLength / logoSize)
                                    .identifier :
                            TCMDynamicResourceCacheV2.instance.getStationNameEntrance(
                                    propagateProperty < 2 || propagateProperty >= 4 ? ARGB_WHITE : ARGB_BLACK,
                                    TextUtil.getCjkParts(stationName).endsWith("站") || TextUtil.getNonCjkParts(stationName).endsWith("Station") ? stationName : IGui.insertTranslation(TranslationProvider.GUI_MTR_STATION_CJK,
                                            TranslationProvider.GUI_MTR_STATION, 1, stationName),
                                    totalLength / logoSize, stationId, selectedExit).identifier
                    , false, QueuedRenderLayer.INTERIOR, (graphicsHolder, offset) -> {
                        storedMatrixTransformations.transform(graphicsHolder, offset);
                        IDrawing.drawTexture(graphicsHolder, -0.5F, -logoSize / 2, 1, logoSize, (float) (lengthLeft - 1) / totalLength, 0, (float) lengthLeft / totalLength, 1, facing, color, light);
                        graphicsHolder.pop();
                    });
        } else {
            MainRenderer.scheduleRender(DynamicTextureCache.instance.getStationName(stationName, totalLength).identifier, false, QueuedRenderLayer.EXTERIOR, (graphicsHolder, offset) -> {
                storedMatrixTransformations.transform(graphicsHolder, offset);
                IDrawing.drawTexture(graphicsHolder, -0.5F, -0.5F, 1, 1, (float) (lengthLeft - 1) / totalLength, 0, (float) lengthLeft / totalLength, 1, facing, color, light);
                graphicsHolder.pop();
            });
        }
    }

    @Unique
    private static long serializeExit(String exitName) {
        final char[] characters = exitName.toCharArray();
        long code = 0;
        for (final char character : characters) {
            code = code << 8;
            code += character;
        }
        return code;
    }

    @Shadow(remap = false)
    protected abstract int getLength(@Nullable World world, BlockPos pos, boolean lookRight);

    @Override
    protected void drawStationName(World world, BlockPos pos, BlockState state, Direction facing, StoredMatrixTransformations storedMatrixTransformations, String stationName, int stationColor, int color, int light) {
        //Do nothing
    }
}
