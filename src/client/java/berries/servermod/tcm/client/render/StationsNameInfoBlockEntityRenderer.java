package berries.servermod.tcm.client.render;

import berries.servermod.tcm.block.StationsNameInfoBlock;
import berries.servermod.tcm.client.data.TCMDynamicResourceCacheV2;
import berries.servermod.tcm.client.flueroui.FlueroUI;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import org.jetbrains.annotations.NotNull;
import org.mtr.mapping.holder.BlockPos;
import org.mtr.mapping.holder.Direction;
import org.mtr.mapping.holder.Identifier;
import org.mtr.mod.InitClient;
import org.mtr.mod.client.IDrawing;
import org.mtr.mod.data.IGui;
import org.mtr.mod.render.MainRenderer;
import org.mtr.mod.render.QueuedRenderLayer;
import org.mtr.mod.render.StoredMatrixTransformations;

public class StationsNameInfoBlockEntityRenderer implements BlockEntityRenderer<StationsNameInfoBlock.StationsNameInfoBlockEntity> {
    private final BlockEntityRendererProvider.Context renderContext;

    public StationsNameInfoBlockEntityRenderer(BlockEntityRendererProvider.Context renderContext) {
        this.renderContext = renderContext;
    }

    @Override
    public void render(StationsNameInfoBlock.@NotNull StationsNameInfoBlockEntity entity,
                       float f, @NotNull PoseStack poseStack, @NotNull MultiBufferSource mbs, int i, int j) {
        var level = entity.getLevel();
        if (level == null) {
            return;
        }

        var pos = entity.getBlockPos();

        var station = InitClient.findStation(new BlockPos(pos));

        var platformId = entity.selectedPlatformId;

        var state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof StationsNameInfoBlock)) {
            return;
        }

        var facing = state.getValue(HorizontalDirectionalBlock.FACING);

        var translateX = 14.2 / 16.0;
        var translateY = 11.9 / 16.0;
        var translateZ = 15.0 / 16.0;

        var width = 14.4;
        var height = 9.8;

        var ti = (Identifier) TCMDynamicResourceCacheV2.instance.getStationsNameInfo(station == null ? 0 : platformId, station == null ? "" : station.getName(), (float) (width / height), IGui.ARGB_WHITE, FlueroUI.rgb(0, 41, 84)).identifier;

        var smt = new StoredMatrixTransformations(0.5 + pos.getX(), 0.5 + pos.getY(), 0.5 + pos.getZ());
        smt.add((ghn) -> {
            ghn.rotateYDegrees(180 - facing.toYRot());
            ghn.rotateZDegrees(180);
        });

        MainRenderer.scheduleRender(ti, false, QueuedRenderLayer.EXTERIOR, (ghn, ofs) -> {
            smt.transform(ghn, ofs);
            IDrawing.drawTexture(ghn, -((float) (width / 16) / 2), -((float)(height / 16) / 2), (float) (translateZ / 2), (float) (width / 16) / 2, (float)(height / 16) / 2, (float) (translateZ / 2), 0, 0, 1, 1, Direction.convert(facing.getOpposite()), -1, i);
            ghn.pop();
        });
    }
}
