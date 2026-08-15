package berries.servermod.tcm.client.mixin;

import berries.servermod.tcm.client.data.TCMDynamicResourceCacheV2;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;
import org.mtr.mapping.holder.*;
import org.mtr.mod.block.BlockPSDTop;
import org.mtr.mod.block.IBlock;
import org.mtr.mod.client.IDrawing;
import org.mtr.mod.render.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import static berries.servermod.tcm.block.MixinStates.PSD_TOP_CONTENT_STYLE;

@Environment(EnvType.CLIENT)
@Mixin(RenderPSDTop.class)
public abstract class MixinPSDTopRenderV2 extends RenderRouteBase<BlockPSDTop.BlockEntity> {
    @Shadow(remap = false)
    private static final float END_FRONT_OFFSET = 1 / (MathHelper.getSquareRootOfTwoMapped() * 16);
    @Shadow(remap = false)
    private static final float BOTTOM_DIAGONAL_OFFSET = ((float) Math.sqrt(3) - 1) / 32;
    @Shadow(remap = false)
    private static final float ROOT_TWO_SCALED = MathHelper.getSquareRootOfTwoMapped() / 16;
    @Shadow(remap = false)
    private static final float BOTTOM_END_DIAGONAL_OFFSET = END_FRONT_OFFSET - BOTTOM_DIAGONAL_OFFSET / MathHelper.getSquareRootOfTwoMapped();
    @Shadow(remap = false)
    private static final float COLOR_STRIP_START = 14.5F / 16;
    @Shadow(remap = false)
    private static final float COLOR_STRIP_END = 15 / 16F;
    @Unique
    private static final float F_1 = 1 / 64.0F;
    @Unique
    private static final float F_2 = 0.02F;
    @Unique
    private static final float F_3 = 1 / 64.0F;
    @Unique
    private static final float F_4 = 0.035F;
    
    public MixinPSDTopRenderV2(Argument dispatcher, float z, float topPadding, float bottomPadding, float sidePadding, boolean transparentWhite, int platformSearchYOffset, IntegerProperty arrowDirectionProperty) {
        super(dispatcher, z, topPadding, bottomPadding, sidePadding, transparentWhite, platformSearchYOffset, arrowDirectionProperty);
    }

    @Shadow(remap = false)
    protected abstract void renderAdditionalUnmodified(@NotNull StoredMatrixTransformations storedMatrixTransformations, @NotNull BlockState state, @NotNull Direction facing, int light);

    @Override
    protected void renderAdditional(@NotNull StoredMatrixTransformations storedMatrixTransformations, long platformId, @NotNull BlockState state, int leftBlocks, int rightBlocks, Direction facing, int color, int light) {
        final boolean airLeft = IBlock.getStatePropertySafe(state, BlockPSDTop.AIR_LEFT);
        final boolean airRight = IBlock.getStatePropertySafe(state, BlockPSDTop.AIR_RIGHT);
        final boolean persistent = IBlock.getStatePropertySafe(state, BlockPSDTop.PERSISTENT) != BlockPSDTop.EnumPersistent.NONE;
        /*if (!airLeft && !airRight || persistent) {
            return;
        }*/
        MainRenderer.scheduleRender(TCMDynamicResourceCacheV2.instance.getColorStrip(platformId).identifier, false, QueuedRenderLayer.EXTERIOR, (graphicsHolder, offset) -> {
            storedMatrixTransformations.transform(graphicsHolder, offset);
            if (state.data.hasProperty(PSD_TOP_CONTENT_STYLE) && state.data.getValue(PSD_TOP_CONTENT_STYLE) > 0) {
                IDrawing.drawTexture(graphicsHolder, airLeft ? 0.625F : 0, COLOR_STRIP_START + 1 / 96.0F, 0, airRight ? 0.375F : 1, COLOR_STRIP_END, 0, 0, 0, 1, 1, facing, color, light);
                IDrawing.drawTexture(
                        graphicsHolder,
                        airLeft ? 0.625F : 0, COLOR_STRIP_END + (COLOR_STRIP_END - COLOR_STRIP_START + F_1), F_2,
                        airRight ? 0.375F : 1, COLOR_STRIP_END + (COLOR_STRIP_END - COLOR_STRIP_START + F_1), F_2,
                        airRight ? 0.375F : 1, COLOR_STRIP_END, 0,
                        airLeft ? 0.625F : 0, COLOR_STRIP_END, 0,
                        0, 0, 1, 1,
                        facing, color, light
                );
                IDrawing.drawTexture(
                        graphicsHolder,
                        airLeft ? 0.625F : 0, COLOR_STRIP_END + (COLOR_STRIP_END - COLOR_STRIP_START + F_1) + F_3, F_4 + F_2,
                        airRight ? 0.375F : 1, COLOR_STRIP_END + (COLOR_STRIP_END - COLOR_STRIP_START + F_1) + F_3, F_4 + F_2,
                        airRight ? 0.375F : 1, COLOR_STRIP_END + (COLOR_STRIP_END - COLOR_STRIP_START + F_1), F_2,
                        airLeft ? 0.625F : 0, COLOR_STRIP_END + (COLOR_STRIP_END - COLOR_STRIP_START + F_1), F_2,
                        0, 0, 1, 1,
                        facing, color, light
                );
                if (airLeft) {
                    IDrawing.drawTexture(graphicsHolder, END_FRONT_OFFSET, COLOR_STRIP_START + 1 / 96.0F, -0.625F - END_FRONT_OFFSET, 0.75F + END_FRONT_OFFSET, COLOR_STRIP_END, 0.125F - END_FRONT_OFFSET, facing, -1, light);
                    IDrawing.drawTexture(
                            graphicsHolder,
                            END_FRONT_OFFSET, COLOR_STRIP_END + (COLOR_STRIP_END - COLOR_STRIP_START + F_1), -0.625F - END_FRONT_OFFSET + F_2,
                            0.75F + END_FRONT_OFFSET, COLOR_STRIP_END + (COLOR_STRIP_END - COLOR_STRIP_START + F_1), 0.125F - END_FRONT_OFFSET + F_2,
                            0.75F + END_FRONT_OFFSET, COLOR_STRIP_END, 0.125F - END_FRONT_OFFSET,
                            END_FRONT_OFFSET, COLOR_STRIP_END, -0.625F - END_FRONT_OFFSET,
                            0, 0, 1, 1,
                            facing, color, light
                    );
                    IDrawing.drawTexture(
                            graphicsHolder,
                            END_FRONT_OFFSET, COLOR_STRIP_END + (COLOR_STRIP_END - COLOR_STRIP_START + F_1) + F_3, -0.625F - END_FRONT_OFFSET + F_2 + F_4,
                            0.75F + END_FRONT_OFFSET, COLOR_STRIP_END + (COLOR_STRIP_END - COLOR_STRIP_START + F_1) + F_3, 0.125F - END_FRONT_OFFSET + F_2 + F_4,
                            0.75F + END_FRONT_OFFSET, COLOR_STRIP_END + (COLOR_STRIP_END - COLOR_STRIP_START + F_1), 0.125F - END_FRONT_OFFSET + F_2,
                            END_FRONT_OFFSET, COLOR_STRIP_END + (COLOR_STRIP_END - COLOR_STRIP_START + F_1), -0.625F - END_FRONT_OFFSET + F_2,
                            0, 0, 1, 1,
                            facing, color, light
                    );
                }
                if (airRight) {
                    IDrawing.drawTexture(graphicsHolder, 0.25F - END_FRONT_OFFSET, COLOR_STRIP_START + 1 / 96.0F, 0.125F - END_FRONT_OFFSET, 1 - END_FRONT_OFFSET, COLOR_STRIP_END, -0.625F - END_FRONT_OFFSET, facing, -1, light);
                    IDrawing.drawTexture(
                            graphicsHolder,
                            0.25F - END_FRONT_OFFSET, COLOR_STRIP_END + (COLOR_STRIP_END - COLOR_STRIP_START + F_1), 0.125F - END_FRONT_OFFSET + F_2,
                            1 - END_FRONT_OFFSET, COLOR_STRIP_END + (COLOR_STRIP_END - COLOR_STRIP_START + F_1), -0.625F - END_FRONT_OFFSET + F_2,
                            1 - END_FRONT_OFFSET, COLOR_STRIP_END, -0.625F - END_FRONT_OFFSET,
                            0.25F - END_FRONT_OFFSET, COLOR_STRIP_END, 0.125F - END_FRONT_OFFSET,
                            0, 0, 1, 1,
                            facing, color, light
                    );
                    IDrawing.drawTexture(
                            graphicsHolder,
                            0.25F - END_FRONT_OFFSET, COLOR_STRIP_END + (COLOR_STRIP_END - COLOR_STRIP_START + F_1) + F_3, 0.125F - END_FRONT_OFFSET + F_2 + F_4,
                            1 - END_FRONT_OFFSET, COLOR_STRIP_END + (COLOR_STRIP_END - COLOR_STRIP_START + F_1) + F_3, -0.625F - END_FRONT_OFFSET + F_2 + F_4,
                            1 - END_FRONT_OFFSET, COLOR_STRIP_END + (COLOR_STRIP_END - COLOR_STRIP_START + F_1), -0.625F - END_FRONT_OFFSET + F_2,
                            0.25F - END_FRONT_OFFSET, COLOR_STRIP_END + (COLOR_STRIP_END - COLOR_STRIP_START + F_1), 0.125F - END_FRONT_OFFSET + F_2,
                            0, 0, 1, 1,
                            facing, color, light
                    );
                }
            } else {
                IDrawing.drawTexture(graphicsHolder, airLeft ? 0.625F : 0, COLOR_STRIP_START, 0, airRight ? 0.375F : 1, COLOR_STRIP_END, 0, facing, color, light);
                if (airLeft) {
                    IDrawing.drawTexture(graphicsHolder, END_FRONT_OFFSET, COLOR_STRIP_START, -0.625F - END_FRONT_OFFSET, 0.75F + END_FRONT_OFFSET, COLOR_STRIP_END, 0.125F - END_FRONT_OFFSET, facing, -1, light);
                }
                if (airRight) {
                    IDrawing.drawTexture(graphicsHolder, 0.25F - END_FRONT_OFFSET, COLOR_STRIP_START, 0.125F - END_FRONT_OFFSET, 1 - END_FRONT_OFFSET, COLOR_STRIP_END, -0.625F - END_FRONT_OFFSET, facing, -1, light);
                }
            }
            graphicsHolder.pop();
        });
    }
}
