package berries.servermod.tcm.client.screen;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.UFEInfo;
import berries.servermod.tcm.client.TCMClient;
import berries.servermod.tcm.client.util.ModUpdate;
import berries.servermod.tcm.util.TCMComponent;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class VersionLowScreen extends Screen {
    //Widgets
    public MultiLineLabel titleLabel;
    public MultiLineLabel[] descriptionLabels;
    public Button okButton;
    public Button updateButton;
    public Button dnvButton;

    private final String vo;
    private final String vn;
    private final int bo;
    private final int bn;

    private final PanoramaRenderer pr;

    public VersionLowScreen(String vo, int bo, String vn, int bn) {
        super(TCMComponent.text(UFEInfo.MOD_NAME).copy().withStyle(Style.EMPTY.withBold(true)));
        this.vo = vo;
        this.bo = bo;
        this.vn = vn;
        this.bn = bn;

        this.pr = new PanoramaRenderer(new CubeMap(new ResourceLocation("textures/gui/title/background/panorama")));
    }

    @Override
    protected void init() {
        if (minecraft == null) { return; }

        titleLabel = MultiLineLabel.create(minecraft.font, getTitle());

        String[] texts = TCMComponent.translatable("gui.tcm.version_check_disconnect.description", vo, bo, vn, bn).getString().split("\\|");
        descriptionLabels = new MultiLineLabel[texts.length];
        for (int i = 0; i < texts.length; i++) {
            descriptionLabels[i] = MultiLineLabel.create(
                    minecraft.font,
                    TCMComponent.text(texts[i])
            );
        }

        okButton = Button.builder(
                TCMComponent.translatable("gui.tcm.version_check_disconnect.okButton.text"),
                (btn) -> minecraft.setScreen(new JoinMultiplayerScreen(null)))
                .pos((width / 2) - 75,
                        height - (height / 6))
                .size(150, 20).build();
        okButton.active = false;

        addRenderableWidget(okButton);

        dnvButton = Button.builder(
                TCMComponent.translatable("gui.tcm.version_check_disconnect.dnvButton.text"),
                (btn) -> Util.getPlatform().openUri("http://" + TCMClient.syncDirectionIp))
                .pos((width / 2) - 75,
                        height - (height / 6) - 22)
                .size(150, 20).build();

        addRenderableWidget(dnvButton);

        updateButton = Button.builder(
                TCMComponent.translatable("gui.tcm.version_check_disconnect.updateButton.text"),
                (btn) -> ModUpdate.screens(false))
                .pos((width / 2) - 75,
                        height - (height / 6) - 44)
                .size(150, 20).build();

        addRenderableWidget(updateButton);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(new JoinMultiplayerScreen(null));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        if (minecraft == null) { return; }
        renderBackground(guiGraphics);
        this.pr.render(f, Mth.clamp(1.0F, 0.0F, 1.0F));
        titleLabel.renderCentered(guiGraphics, width / 2, height / 8);
        //okButton.render(poseStack, width / 2, height - (height / 6) , 10.0f);
        if (okButton.isHoveredOrFocused()) {
            final Component hoverText = TCMComponent.translatable("gui.tcm.version_check_disconnect.okButton.tooltip");
            guiGraphics.renderTooltip(minecraft.font, hoverText, i, j);
        } else if (updateButton.isHoveredOrFocused()) {
            final Component hoverText = TCMComponent.translatable("gui.tcm.version_check_disconnect.updateButton.tooltip");
            guiGraphics.renderTooltip(minecraft.font, hoverText, i, j);
        }
        for (int k = 0; k < descriptionLabels.length; k++) {
            MultiLineLabel label = descriptionLabels[k];
            label.renderCentered(guiGraphics, width / 2, height / 16 * (k + 5));
        }
        super.render(guiGraphics, i, j, f);
        okButton.active = !TCM.lockMinecraftScreen;
    }
}
