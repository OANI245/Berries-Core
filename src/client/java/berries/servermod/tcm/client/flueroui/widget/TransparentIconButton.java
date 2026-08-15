package berries.servermod.tcm.client.flueroui.widget;

import berries.servermod.tcm.client.flueroui.FlueroUI;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;

public class TransparentIconButton extends AbstractButton {
    public ResourceLocation icon;
    protected Consumer<TransparentIconButton> onPress;

    public TransparentIconButton(int x, int y, int scale, @NotNull ResourceLocation icon, Consumer<TransparentIconButton> onPress) {
        super(x, y, scale, scale, Component.empty());
        this.icon = icon;
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        this.onPress.accept(this);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(icon, getX(), getY(), 0, 0, width, height, width, height);
        if (isHoveredOrFocused()) {
            guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, FlueroUI.argb(isFocused() ? 6 : 18, 255, 255, 255));
        }
    }
}
