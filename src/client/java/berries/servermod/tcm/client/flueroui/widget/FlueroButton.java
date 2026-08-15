package berries.servermod.tcm.client.flueroui.widget;

import berries.servermod.tcm.client.flueroui.FlueroUI;
import berries.servermod.tcm.client.flueroui.TextDrawer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.function.Consumer;


public class FlueroButton extends AbstractButton {
    protected final Consumer<FlueroButton> onClick;
    protected boolean isAccentStyle = false;
    protected boolean isLightMode = false;
    protected ResourceLocation icon;

    public FlueroButton(int x, int y, int width, int height, Component message, Consumer<FlueroButton> onClick) {
        super(x, y, width, height, message);
        this.onClick = onClick;
    }

    public FlueroButton(int x, int y, int width, int height, ResourceLocation icon, Component message, Consumer<FlueroButton> onClick) {
        this(x, y, width, height, message, onClick);
        this.icon = icon;
    }

    public FlueroButton(int x, int y, int width, int height, Component message, boolean isAccentStyle, Consumer<FlueroButton> onClick) {
        this(x, y, width, height, message, onClick);
        this.isAccentStyle = isAccentStyle;
    }

    public FlueroButton(int x, int y, int width, int height, ResourceLocation icon, Component message, boolean isAccentStyle, Consumer<FlueroButton> onClick) {
        this(x, y, width, height, message, isAccentStyle, onClick);
        this.icon = icon;
    }

    public FlueroButton(int x, int y, Component message, Consumer<FlueroButton> onClick) {
        this(x, y, 36, 18, message, onClick);
    }

    public FlueroButton(int x, int y, ResourceLocation icon, Component message, Consumer<FlueroButton> onClick) {
        this(x, y, 36, 18, icon, message, onClick);
    }

    public void setIcon(ResourceLocation icon) {
        this.icon = icon;
    }

    @Override
    public void onPress() {
        this.onClick.accept(this);
    }

    public void setAccent(boolean bl) {
        isAccentStyle = bl;
    }

    public boolean isLightMode() {
        return isLightMode;
    }

    public void setLightMode(boolean lightMode) {
        isLightMode = lightMode;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        int buttonBackground = (this.isAccentStyle ?
                (this.isHovered() ? FlueroUI.PRIMARY_COLOR - FlueroUI.argb(0, 24,24,24) : FlueroUI.PRIMARY_COLOR) :
                (isLightMode ? FlueroUI.SECONDARY_LIGHT : FlueroUI.SECONDARY_COLOR) + (this.isHovered() ? isLightMode ? -FlueroUI.argb(0, 24,24,24) : FlueroUI.argb(0, 24,24,24) : 0));
        int buttonBorder = (this.isAccentStyle ?
                (this.isHovered() ? FlueroUI.PRIMARY_LOW - FlueroUI.argb(0, 24,24,24) : FlueroUI.PRIMARY_LOW):
                (isLightMode ? FlueroUI.SECONDARY_LIGHT_LOW : FlueroUI.SECONDARY_COLOR) + (this.isHovered() ? isLightMode ? -FlueroUI.argb(0, 24,24,24) : FlueroUI.argb(0, 24,24,24) : 0));
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, buttonBackground);
        guiGraphics.fill(getX(), getY() + height - 1, getX() + width, getY() + height, buttonBorder);
        guiGraphics.fill(getX(), getY(), getX() + 1, getY() + height, buttonBorder);
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + 1, buttonBorder);
        guiGraphics.fill(getX() + width - 1, getY(), getX() + width, getY() + height, buttonBorder);
        if (this.isFocused()) {
            guiGraphics.fill(getX(), getY() + height, getX() + width, getY() + height + 1, FlueroUI.rgb(255, 255, 255));
            guiGraphics.fill(getX() - 1, getY(), getX(), getY() + height, FlueroUI.rgb(255, 255, 255));
            guiGraphics.fill(getX(), getY() - 1, getX() + width, getY(), FlueroUI.rgb(255, 255, 255));
            guiGraphics.fill(getX() + width, getY(), getX() + width + 1, getY() + height, FlueroUI.rgb(255, 255, 255));
        }
        FormattedCharSequence formattedCharSequence = this.getMessage().getVisualOrderText();
        int fontWidth = minecraft.font.width(formattedCharSequence) / 2;
        if (icon != null) {
            fontWidth -= this.getHeight() / 2;RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            guiGraphics.blit(icon, this.getX() + width / 2 - minecraft.font.width(formattedCharSequence) / 2 - this.getHeight() / 2, this.getY(), 0, 0, height, height, height, height);
        }
        TextDrawer.drawText(guiGraphics, minecraft.font, this.getMessage(), TextDrawer.Alignment.LEFT, (this.getX() + width / 2) - fontWidth, (this.getY() + height / 2 - minecraft.font.lineHeight / 2), this.isAccentStyle || this.isLightMode ? FlueroUI.textColor(0): FlueroUI.textColor(0xFFFFFF), false);
        //this.renderString(guiGraphics, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }
}
