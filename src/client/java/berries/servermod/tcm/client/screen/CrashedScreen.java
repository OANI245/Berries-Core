package berries.servermod.tcm.client.screen;

import berries.servermod.tcm.util.TCMComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class CrashedScreen extends Screen {
    private final Runnable onClose;

    public CrashedScreen(Runnable onClose) {
        super(TCMComponent.translatable("gui.tcm.crashed"));
        this.onClose = onClose;
    }

    @Override
    protected void init() {
        addRenderableWidget(
                Button.builder(
                        TCMComponent.translatable("gui.tcm.crashed.copy"),
                        (button) -> this.onClose.run()
                ).size(120, 20).pos(
                        this.width / 2 - 60,
                        this.height - 30
                ).build()
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUILocations.XIBAO_BACKGROUND_LOCATION);
        guiGraphics.blit(GUILocations.XIBAO_BACKGROUND_LOCATION, 0, 0, 0, 0, width, height, width, height);
        if (this.minecraft == null) {
            return;
        }
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(3.0F, 3.0F, 3.0F);
        guiGraphics.drawCenteredString(this.minecraft.font, this.title.copy().withStyle(Style.EMPTY.withBold(true)), this.width / 6, this.height / 6, 0xFF0000);
        guiGraphics.drawCenteredString(this.minecraft.font, this.title.copy(), this.width / 6, this.height / 6, 0xFEF909);
        guiGraphics.pose().popPose();
        super.render(guiGraphics, i, j, f);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
        this.onClose.run();
    }
}
