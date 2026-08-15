package berries.servermod.tcm.client.screen;

import berries.servermod.tcm.block.MixinStates;
import berries.servermod.tcm.client.data.TCMDynamicResourceCacheV2;
import berries.servermod.tcm.client.flueroui.FlueroUI;
import berries.servermod.tcm.client.flueroui.TextDrawer;
import berries.servermod.tcm.client.flueroui.widget.ComboBox;
import berries.servermod.tcm.client.flueroui.widget.FlueroButton;
import berries.servermod.tcm.client.flueroui.widget.TopNavigationBar;
import berries.servermod.tcm.client.packet.PacketModifyBlockStateClient;
import berries.servermod.tcm.util.TCMComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.mtr.mod.InitClient;
import org.mtr.mod.data.IGui;

import java.util.List;

import static org.mtr.mod.data.IGui.ARGB_BLACK;
import static org.mtr.mod.data.IGui.ARGB_WHITE;

public class EditPSDTopScreen extends Screen {
    public static final int SIDE_BAR_WIDTH = 160;

    public BlockPos blockPos;
    public long platformId = 0;

    private TopNavigationBar navigationBar = null;
    private ComboBox comboBox = null;

    public EditPSDTopScreen(BlockPos blockPos) {
        super(Component.empty());
        this.blockPos = blockPos;
    }

    @Override
    protected void init() {
        super.init();
        InitClient.findClosePlatform(new org.mtr.mapping.holder.BlockPos(blockPos.below(3)), 5, platform -> {
            this.platformId = platform.getId();
        });

        if (minecraft == null || minecraft.level == null) return;

        var bs = minecraft.level.getBlockState(blockPos);
        var ssv = bs.getValue(MixinStates.PSD_TOP_CONTENT_STYLE);

        navigationBar = addRenderableWidget(new TopNavigationBar(0, 0, width - SIDE_BAR_WIDTH, 25, List.of(
                new TopNavigationBar.Item(0, TCMComponent.translatable("gui.tcm.edit_psd_top.nav.0")),
                new TopNavigationBar.Item(1, TCMComponent.translatable("gui.tcm.edit_psd_top.nav.1")),
                new TopNavigationBar.Item(2, TCMComponent.translatable("gui.tcm.edit_psd_top.nav.2")),
                new TopNavigationBar.Item(3, TCMComponent.translatable("gui.tcm.edit_psd_top.nav.3"))
        ), 0));

        comboBox = addRenderableWidget(new ComboBox(width - SIDE_BAR_WIDTH + 10, 68, (int) (SIDE_BAR_WIDTH * 0.845), 20, List.of(
                TCMComponent.translatable("gui.tcm.psd_top.style_0"),
                TCMComponent.translatable("gui.tcm.psd_top.style_1"),
                TCMComponent.translatable("gui.tcm.psd_top.style_2"),
                TCMComponent.translatable("gui.tcm.psd_top.style_3")
        ), ssv, null));
        comboBox.onValueChanged((i) -> {});

        var submitButton = addRenderableWidget(new FlueroButton((int) (width - SIDE_BAR_WIDTH + SIDE_BAR_WIDTH * 0.1), height - 40, (int) (SIDE_BAR_WIDTH * 0.8), 20, TCMComponent.translatable("gui.tcm.edit_psd_top.submitButton.text"), true, (b) -> {
            PacketModifyBlockStateClient.sendModifyPSDTopBlockStateC2S(blockPos, 0, comboBox.getValue());
            if (minecraft != null) {
                minecraft.setScreen(null);
            }
        }));
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
        if (navigationBar == null) {
            return;
        }
        guiGraphics.fill(0, 0, width, height, FlueroUI.BACKGROUND_COLOR);
        guiGraphics.fill(width - SIDE_BAR_WIDTH, 0, width, height, FlueroUI.BACKGROUND_HIGH);
        guiGraphics.fill(width - SIDE_BAR_WIDTH, 0, width - SIDE_BAR_WIDTH + 1, height, FlueroUI.BACKGROUND_HIGH + FlueroUI.argb(0, 16, 16, 16));
        var av = navigationBar.getSelected();
        var st = comboBox.getValue();
        if (av != null && st > 0) {
            final float defaultAspectRatio = 4.535714f;
            final int contentAreaWidth = this.width - SIDE_BAR_WIDTH;
            final boolean isNarrowScreen = contentAreaWidth < SIDE_BAR_WIDTH * 3;
            final int startY = 48;
            final int gap = 8;

            // Step 1: 获取三张动态图片（先使用默认 aspectRatio 生成）
            var dt0 = TCMDynamicResourceCacheV2.instance.getDirectionArrow(
                    platformId,
                    av.getId() == 0 || av.getId() == 2,
                    av.getId() == 1 || av.getId() == 2,
                    IGui.HorizontalAlignment.CENTER, true, 0.25f,
                    defaultAspectRatio, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE, st
            );
            var dt1 = TCMDynamicResourceCacheV2.instance.getPSDTopStationName(
                    platformId,
                    "调试|Debug", true, 0.25f,
                    defaultAspectRatio, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE, st
            );
            var dt2 = TCMDynamicResourceCacheV2.instance.getPSDTopRouteMap(
                    platformId, av.getId() == 1 || av.getId() == 2,
                    defaultAspectRatio, true
            );

            // Step 2: 从动态资源中读取每张图片的实际 aspectRatio（宽高比）
            final float ar0 = (float) dt0.width / (float) dt0.height;
            final float ar1 = (float) dt1.width / (float) dt1.height;
            final float ar2 = (float) dt2.width / (float) dt2.height;

            // Step 3: 响应式布局计算
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);

            if (isNarrowScreen) {
                // ============ 窄屏幕布局 ============
                // 第一行：dt0
                // 第二行：dt1
                // 第三行：dt2（宽度是其它图片的 2 倍）
                final float contentWidthRatio = 0.9f;
                final int maxSingleWidth = Math.round(contentAreaWidth * contentWidthRatio);

                // dt0 和 dt1 采用相同宽度（窄屏基准宽度）
                final int baseWidth = maxSingleWidth / 2; // dt2 是它的 2 倍
                final int dt0DisplayWidth = baseWidth;
                final int dt0DisplayHeight = Math.round((float) dt0DisplayWidth / ar0);
                final int dt1DisplayWidth = baseWidth;
                final int dt1DisplayHeight = Math.round((float) dt1DisplayWidth / ar1);

                // dt2 宽度是其它图片的 2 倍
                final int dt2DisplayWidth = baseWidth * 2;
                final int dt2DisplayHeight = Math.round((float) dt2DisplayWidth / ar2);

                // 计算居中 X 坐标
                final int dt0X = (contentAreaWidth - dt0DisplayWidth) / 2;
                final int dt1X = (contentAreaWidth - dt1DisplayWidth) / 2;
                final int dt2X = (contentAreaWidth - dt2DisplayWidth) / 2;

                // 计算 Y 坐标
                final int dt0Y = startY;
                final int dt1Y = dt0Y + dt0DisplayHeight + gap;
                final int dt2Y = dt1Y + dt1DisplayHeight + gap;

                // 使用实际显示的 aspectRatio 重新获取动态资源，保证生成内容与显示精确匹配
                final float displayAr0 = (float) dt0DisplayWidth / (float) dt0DisplayHeight;
                final float displayAr1 = (float) dt1DisplayWidth / (float) dt1DisplayHeight;
                final float displayAr2 = (float) dt2DisplayWidth / (float) dt2DisplayHeight;
                dt0 = TCMDynamicResourceCacheV2.instance.getDirectionArrow(
                        platformId,
                        av.getId() == 0 || av.getId() == 2,
                        av.getId() == 1 || av.getId() == 2,
                        IGui.HorizontalAlignment.CENTER, true, 0.25f,
                        displayAr0, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE, st
                );
                dt1 = TCMDynamicResourceCacheV2.instance.getPSDTopStationName(
                        platformId,
                        "调试|Debug", true, 0.25f,
                        displayAr1, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE, st
                );
                dt2 = TCMDynamicResourceCacheV2.instance.getPSDTopRouteMap(
                        platformId, av.getId() == 1 || av.getId() == 2,
                        displayAr2, true
                );

                // 绘制背景和 dt0
                guiGraphics.fill(dt0X, dt0Y, dt0X + dt0DisplayWidth, dt0Y + dt0DisplayHeight, FlueroUI.rgb(255, 255, 255));
                guiGraphics.blit(dt0.identifier.data, dt0X, dt0Y, 0, 0,
                        dt0DisplayWidth, dt0DisplayHeight, dt0DisplayWidth, dt0DisplayHeight);

                // 绘制 dt1
                guiGraphics.fill(dt1X, dt1Y, dt1X + dt1DisplayWidth, dt1Y + dt1DisplayHeight, FlueroUI.rgb(255, 255, 255));
                guiGraphics.blit(dt1.identifier.data, dt1X, dt1Y, 0, 0,
                        dt1DisplayWidth, dt1DisplayHeight, dt1DisplayWidth, dt1DisplayHeight);

                // 绘制 dt2
                guiGraphics.fill(dt2X, dt2Y, dt2X + dt2DisplayWidth, dt2Y + dt2DisplayHeight, FlueroUI.rgb(255, 255, 255));
                guiGraphics.blit(dt2.identifier.data, dt2X, dt2Y, 0, 0,
                        dt2DisplayWidth, dt2DisplayHeight, dt2DisplayWidth,dt2DisplayHeight);

            } else {
                // ============ 宽屏幕布局 ============
                // 第一行：dt0、dt1（并排，高度一致）
                // 第二行：dt2（占满整行宽度）
                final float contentWidthRatio = 0.9f;
                final int rowMaxWidth = Math.round(contentAreaWidth * contentWidthRatio);

                // --- 第一行布局：dt0 + dt1 并排，高度一致 ---
                // 设第一行统一高度为 h，则 dt0 宽度 = ar0 * h，dt1 宽度 = ar1 * h
                // 总宽度 = (ar0 + ar1) * h + gap = rowMaxWidth
                final float row1TotalAspect = ar0 + ar1;
                final int row1Height = Math.round(((float) rowMaxWidth - gap) / row1TotalAspect);
                final int dt0DisplayWidth = Math.round((float) row1Height * ar0);
                final int dt0DisplayHeight = row1Height;
                final int dt1DisplayWidth = Math.round((float) row1Height * ar1);
                final int dt1DisplayHeight = row1Height;

                // 第一行整体居中
                final int row1TotalWidth = dt0DisplayWidth + gap + dt1DisplayWidth;
                final int row1StartX = (contentAreaWidth - row1TotalWidth) / 2;
                final int dt0X = row1StartX;
                final int dt1X = row1StartX + dt0DisplayWidth + gap;
                final int row1Y = startY;

                // --- 第二行布局：dt2 占满整行 ---
                final int dt2DisplayWidth = rowMaxWidth;
                final int dt2DisplayHeight = Math.round((float) dt2DisplayWidth / ar2) / 2;
                final int dt2X = (contentAreaWidth - dt2DisplayWidth) / 2;
                final int dt2Y = row1Y + row1Height + gap;

                // 使用实际显示的 aspectRatio 重新获取动态资源，保证生成内容与显示精确匹配
                final float displayAr0 = (float) dt0DisplayWidth / (float) dt0DisplayHeight;
                final float displayAr1 = (float) dt1DisplayWidth / (float) dt1DisplayHeight;
                final float displayAr2 = (float) dt2DisplayWidth / (float) dt2DisplayHeight;
                dt0 = TCMDynamicResourceCacheV2.instance.getDirectionArrow(
                        platformId,
                        av.getId() == 0 || av.getId() == 2,
                        av.getId() == 1 || av.getId() == 2,
                        IGui.HorizontalAlignment.CENTER, true, 0.25f,
                        displayAr0, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE, st
                );
                dt1 = TCMDynamicResourceCacheV2.instance.getPSDTopStationName(
                        platformId,
                        "调试|Debug", true, 0.25f,
                        displayAr1, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE, st
                );
                dt2 = TCMDynamicResourceCacheV2.instance.getPSDTopRouteMap(
                        platformId, av.getId() == 1 || av.getId() == 2,
                        displayAr2, true
                );

                // 绘制背景和 dt0
                guiGraphics.fill(dt0X, row1Y, dt0X + dt0DisplayWidth, row1Y + dt0DisplayHeight, FlueroUI.rgb(255, 255, 255));
                guiGraphics.blit(dt0.identifier.data, dt0X, row1Y, 0, 0,
                        dt0DisplayWidth, dt0DisplayHeight, dt0DisplayWidth, dt0DisplayHeight);

                // 绘制 dt1
                guiGraphics.fill(dt1X, row1Y, dt1X + dt1DisplayWidth, row1Y + dt1DisplayHeight, FlueroUI.rgb(255, 255, 255));
                guiGraphics.blit(dt1.identifier.data, dt1X, row1Y, 0, 0,
                        dt1DisplayWidth, dt1DisplayHeight, dt1DisplayWidth, dt1DisplayHeight);

                // 绘制 dt2
                guiGraphics.fill(dt2X, dt2Y, dt2X + dt2DisplayWidth, dt2Y + dt2DisplayHeight, FlueroUI.rgb(255, 255, 255));
                guiGraphics.blit(dt2.identifier.data, dt2X, dt2Y, 0, 0,
                        dt2DisplayWidth, dt2DisplayHeight, dt2DisplayWidth, dt2DisplayHeight);
            }
        }

        if (minecraft != null) {
            var tus = 1.8;
            var poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.scale((float) tus, (float) tus, (float) tus);
            TextDrawer.drawText(guiGraphics, minecraft.font, TCMComponent.translatable("gui.tcm.edit_psd_top.rightbar.title"), TextDrawer.Alignment.LEFT,
                    (int) ((width - SIDE_BAR_WIDTH + 10) / tus), (int) (29 / tus), ARGB_WHITE, false);
            poseStack.popPose();
            TextDrawer.drawText(guiGraphics, minecraft.font, TCMComponent.translatable("gui.tcm.edit_psd_top.rightbar.properties.1"), TextDrawer.Alignment.LEFT,
                    width - SIDE_BAR_WIDTH + 10, 55, ARGB_WHITE, false);
        }
        super.render(guiGraphics, i, j, f);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
