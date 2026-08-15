package berries.servermod.tcm.client.mixin;

import berries.servermod.tcm.block.MixinStates;
import berries.servermod.tcm.client.data.TCMDynamicResourceCacheV2;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;
import org.mtr.core.data.Station;
import org.mtr.mapping.holder.*;
import org.mtr.mapping.mapper.BlockEntityRenderer;
import org.mtr.mapping.mapper.DirectionHelper;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mod.InitClient;
import org.mtr.mod.block.BlockAPGGlass;
import org.mtr.mod.block.BlockPSDTop;
import org.mtr.mod.block.IBlock;
import org.mtr.mod.client.DynamicTextureCache;
import org.mtr.mod.client.IDrawing;
import org.mtr.mod.data.IGui;
import org.mtr.mod.render.MainRenderer;
import org.mtr.mod.render.QueuedRenderLayer;
import org.mtr.mod.render.RenderRouteBase;
import org.mtr.mod.render.StoredMatrixTransformations;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static berries.servermod.tcm.block.MixinStates.PSD_TOP_CONTENT_STYLE;
import static berries.servermod.tcm.block.MixinStates.PSD_TOP_DISPLAY_TYPE;
import static org.mtr.mod.data.IGui.ARGB_BLACK;
import static org.mtr.mod.data.IGui.ARGB_WHITE;
import static org.mtr.mod.block.IBlock.*;

@Environment(EnvType.CLIENT)
@Mixin(RenderRouteBase.class)
public abstract class MixinRenderRouteBase<T extends BlockPSDTop.BlockEntityBase> extends BlockEntityRenderer<T> {
    @Final @Shadow(remap = false)
    protected float topPadding;
    @Final @Shadow(remap = false)
    protected float bottomPadding;
    @Final @Shadow(remap = false)
    protected float sidePadding;
    @Final @Shadow(remap = false)
    private float z;
    @Final @Shadow(remap = false)
    private boolean transparentWhite;
    @Final @Shadow(remap = false)
    private int platformSearchYOffset;
    @Final @Shadow(remap = false)
    private IntegerProperty arrowDirectionProperty;

    public MixinRenderRouteBase(Argument argument) {
        super(argument);
    }

    @Override
    public final void render(@NotNull T entity, float tickDelta, @NotNull GraphicsHolder graphicsHolder, int light, int overlay) {
        if (entity instanceof BlockPSDTop.BlockEntity) {
            final World world = entity.getWorld2();
            if (world == null) {
                return;
            }

            final BlockPos blockPos = entity.getPos2();
            final BlockState state = world.getBlockState(blockPos);
            final Direction facing = IBlock.getStatePropertySafe(state, DirectionHelper.FACING);

            final StoredMatrixTransformations storedMatrixTransformations = new StoredMatrixTransformations(0.5 + entity.getPos2().getX(), entity.getPos2().getY(), 0.5 + entity.getPos2().getZ());
            storedMatrixTransformations.add(graphicsHolderNew -> graphicsHolderNew.rotateYDegrees(-facing.asRotation()));

            renderAdditionalUnmodified(storedMatrixTransformations.copy(), state, facing, light);

            InitClient.findClosePlatform(blockPos.down(platformSearchYOffset), 5, platform -> {
                final long platformId = platform.getId();

                storedMatrixTransformations.add(graphicsHolderNew -> {
                    graphicsHolderNew.translate(0, 1, 0);
                    graphicsHolderNew.rotateZDegrees(180);
                    graphicsHolderNew.translate(-0.5, -getAdditionalOffset(state), z);
                });
                try {
                    final int leftBlocks = getTextureNumber(world, blockPos, facing, true);
                    final int rightBlocks = getTextureNumber(world, blockPos, facing, false);
                    final int color = RenderRouteBase.getShadingColor(facing, ARGB_WHITE);
                    final Object renderType = RenderRouteBase.class.getDeclaredMethod("getRenderType", World.class, BlockPos.class, BlockState.class).invoke(this, world, blockPos.offset(facing.rotateYCounterclockwise(), leftBlocks), state);//getRenderType(world, blockPos.offset(facing.rotateYCounterclockwise(), leftBlocks), state);
                    if ((renderType == Class.forName("org.mtr.mod.render.RenderRouteBase$RenderType").getField("ARROW").get(null) || renderType == Class.forName("org.mtr.mod.render.RenderRouteBase$RenderType").getField("ROUTE").get(null)) && IBlock.getStatePropertySafe(state, SIDE_EXTENDED) != IBlock.EnumSide.SINGLE) {
                        final float width = leftBlocks + rightBlocks + 1 - sidePadding * 2;
                        final float height = 1 - topPadding - bottomPadding;
                        final int arrowDirection = IBlock.getStatePropertySafe(state, arrowDirectionProperty);

                        MixinStates.BeijingStylePSDTopType type = state.data.getValue(PSD_TOP_DISPLAY_TYPE);

                        final Identifier identifier;
                        if (renderType == Class.forName("org.mtr.mod.render.RenderRouteBase$RenderType").getField("ARROW").get(null)) {
                            if (state.data.getValue(PSD_TOP_CONTENT_STYLE) != 0 && type == MixinStates.BeijingStylePSDTopType.DEFAULT) {
                                identifier = TCMDynamicResourceCacheV2.instance.getDirectionArrow(platformId, (arrowDirection & 0b01) > 0, (arrowDirection & 0b10) > 0, IGui.HorizontalAlignment.CENTER, true, 0.25F, width / height, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE, state.data.getValue(PSD_TOP_CONTENT_STYLE)).identifier;
                            } else if (state.data.getValue(PSD_TOP_CONTENT_STYLE) != 0) {
                                Station station = InitClient.findStation(blockPos);
                                String stationName;
                                if (station == null) {
                                    stationName = "";
                                } else {
                                    stationName = station.getName();
                                }
                                identifier = TCMDynamicResourceCacheV2.instance.getPSDTopStationName(platformId, stationName, true, 0.25F, width / height, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE, state.data.getValue(PSD_TOP_CONTENT_STYLE)).identifier;
                            } else {
                                identifier = DynamicTextureCache.instance.getDirectionArrow(platformId, (arrowDirection & 0b01) > 0, (arrowDirection & 0b10) > 0, IGui.HorizontalAlignment.CENTER, true, 0.25F, width / height, ARGB_WHITE, ARGB_BLACK, transparentWhite ? ARGB_WHITE : 0).identifier;
                            }
                            //identifier = DynamicTextureCache.instance.getDirectionArrow(platformId, (arrowDirection & 0b01) > 0, (arrowDirection & 0b10) > 0, IGui.HorizontalAlignment.CENTER, true, 0.25F, width / height, ARGB_WHITE, ARGB_BLACK, transparentWhite ? ARGB_WHITE : 0).identifier;
                        } else {
                            if (state.data.getValue(PSD_TOP_CONTENT_STYLE) != 0 && type == MixinStates.BeijingStylePSDTopType.DEFAULT) {
                                identifier = TCMDynamicResourceCacheV2.instance.getPSDTopRouteMap(platformId, arrowDirection == 2, width / height, transparentWhite).identifier;
                            } else if (state.data.getValue(PSD_TOP_CONTENT_STYLE) != 0 && type == MixinStates.BeijingStylePSDTopType.ARROW) {
                                identifier = TCMDynamicResourceCacheV2.instance.getDirectionArrow(platformId, (arrowDirection & 0b01) > 0, (arrowDirection & 0b10) > 0, IGui.HorizontalAlignment.CENTER, true, 0.25F, width / height, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE, state.data.getValue(PSD_TOP_CONTENT_STYLE)).identifier;
                            } else if (state.data.getValue(PSD_TOP_CONTENT_STYLE) != 0 && type == MixinStates.BeijingStylePSDTopType.STATION_NAME) {
                                Station station = InitClient.findStation(blockPos);
                                String stationName;
                                if (station == null) {
                                    stationName = "";
                                } else {
                                    stationName = station.getName();
                                }
                                identifier = TCMDynamicResourceCacheV2.instance.getPSDTopStationName(platformId, stationName, true, 0.25F, width / height, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE, state.data.getValue(PSD_TOP_CONTENT_STYLE)).identifier;
                            } else {
                                identifier = DynamicTextureCache.instance.getRouteMap(platformId, false, arrowDirection == 2, width / height, transparentWhite).identifier;
                            }
                        }

                        MainRenderer.scheduleRender(identifier, false, QueuedRenderLayer.EXTERIOR, (graphicsHolderNew, offset) -> {
                            storedMatrixTransformations.transform(graphicsHolderNew, offset);
                            IDrawing.drawTexture(graphicsHolderNew, leftBlocks == 0 ? sidePadding : 0, topPadding, 0, 1 - (rightBlocks == 0 ? sidePadding : 0), 1 - bottomPadding, 0, (leftBlocks - (leftBlocks == 0 ? 0 : sidePadding)) / width, 0, (width - rightBlocks + (rightBlocks == 0 ? 0 : sidePadding)) / width, 1, facing.getOpposite(), color, light);
                            graphicsHolderNew.pop();
                        });
                    }

                    renderAdditional(storedMatrixTransformations, platformId, state, leftBlocks, rightBlocks, facing.getOpposite(), color, light);
                } catch (Throwable e) {
                }
            });
        } else if (entity instanceof BlockAPGGlass.BlockEntity) {
            final World world = entity.getWorld2();
            if (world == null) {
                return;
            }

            final BlockPos blockPos = entity.getPos2();
            final BlockState state = world.getBlockState(blockPos);
            final Direction facing = IBlock.getStatePropertySafe(state, DirectionHelper.FACING);

            final StoredMatrixTransformations storedMatrixTransformations = new StoredMatrixTransformations(0.5 + entity.getPos2().getX(), entity.getPos2().getY(), 0.5 + entity.getPos2().getZ());
            storedMatrixTransformations.add(graphicsHolderNew -> graphicsHolderNew.rotateYDegrees(-facing.asRotation()));

            renderAdditionalUnmodified(storedMatrixTransformations.copy(), state, facing, light);

            InitClient.findClosePlatform(blockPos.down(platformSearchYOffset), 5, platform -> {
                final long platformId = platform.getId();

                storedMatrixTransformations.add(graphicsHolderNew -> {
                    graphicsHolderNew.translate(0, 1, 0);
                    graphicsHolderNew.rotateZDegrees(180);
                    graphicsHolderNew.translate(-0.5, -getAdditionalOffset(state), z);
                });
                final int leftBlocks = getTextureNumber(world, blockPos, facing, true);
                final int rightBlocks = getTextureNumber(world, blockPos, facing, false);
                final int color = RenderRouteBase.getShadingColor(facing, ARGB_WHITE);

                try {
                    final Object renderType = RenderRouteBase.class.getDeclaredMethod("getRenderType", World.class, BlockPos.class, BlockState.class).invoke(this, world, blockPos.offset(facing.rotateYCounterclockwise(), leftBlocks), state);

                    if ((renderType == Class.forName("org.mtr.mod.render.RenderRouteBase$RenderType").getField("ARROW").get(null) || renderType == Class.forName("org.mtr.mod.render.RenderRouteBase$RenderType").getField("ROUTE").get(null)) && IBlock.getStatePropertySafe(state, SIDE_EXTENDED) != EnumSide.SINGLE) {
                        final float width = leftBlocks + rightBlocks + 1 - sidePadding * 2;
                        final float height = 1 - topPadding - bottomPadding;
                        final int arrowDirection = IBlock.getStatePropertySafe(state, arrowDirectionProperty);

                        final Identifier identifier;
                        if (renderType == Class.forName("org.mtr.mod.render.RenderRouteBase$RenderType").getField("ARROW").get(null)) {
                            identifier = DynamicTextureCache.instance.getDirectionArrow(platformId, (arrowDirection & 0b01) > 0, (arrowDirection & 0b10) > 0, IGui.HorizontalAlignment.CENTER, true, 0.25F, width / height, ARGB_WHITE, ARGB_BLACK, transparentWhite ? ARGB_WHITE : 0).identifier;
                        } else {
                            identifier = DynamicTextureCache.instance.getRouteMap(platformId, false, arrowDirection == 2, width / height, transparentWhite).identifier;
                        }

                        MainRenderer.scheduleRender(identifier, false, QueuedRenderLayer.EXTERIOR, (graphicsHolderNew, offset) -> {
                            storedMatrixTransformations.transform(graphicsHolderNew, offset);
                            IDrawing.drawTexture(graphicsHolderNew, leftBlocks == 0 ? sidePadding : 0, topPadding, 0, 1 - (rightBlocks == 0 ? sidePadding : 0), 1 - bottomPadding, 0, (leftBlocks - (leftBlocks == 0 ? 0 : sidePadding)) / width, 0, (width - rightBlocks + (rightBlocks == 0 ? 0 : sidePadding)) / width, 1, facing.getOpposite(), color, light);
                            graphicsHolderNew.pop();
                        });
                    }
                } catch (Throwable e) {
                }

                renderAdditional(storedMatrixTransformations, platformId, state, leftBlocks, rightBlocks, facing.getOpposite(), color, light);
            });
        }
    }

    @Shadow(remap = false)
    protected abstract void renderAdditional(StoredMatrixTransformations storedMatrixTransformations, long platformId, BlockState state, int leftBlocks, int rightBlocks, Direction facing, int color, int light);

    @Shadow(remap = false)
    protected abstract int getTextureNumber(World world, BlockPos blockPos, Direction facing, boolean b);

    @Shadow(remap = false)
    protected abstract float getAdditionalOffset(BlockState state);

    @Shadow(remap = false)
    protected abstract void renderAdditionalUnmodified(StoredMatrixTransformations copy, BlockState state, Direction facing, int light);
}
