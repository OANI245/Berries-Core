package berries.servermod.tcm.client.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class MetroTileType2 extends AbstractButton {
    public static final int TILE_PADDING = 12;
    public static final int TILE_MIN_HEIGHT = TILE_PADDING * 2 + 26;

    public ImageWidget icon = null;
    public TextWidget title = null;

    public ResourceLocation background = null;

    protected int backgroundColor = 0;
    public int offsetY = 0;

    private boolean styleChanged;

    private final Consumer<MetroTileType2> onPress;

    public MetroTileType2(int i, int j, int k, int l, ResourceLocation icon, Component title, Consumer<MetroTileType2> onPress) {
        super(i, j, k, Math.max(l, TILE_MIN_HEIGHT), title.copy().withStyle(Style.EMPTY.withBold(true).withColor(0xFDFDFD)));
        if (icon != null) {
            this.icon = new ImageWidget(
                    getX() + TILE_PADDING,
                    getY() + TILE_PADDING,
                    Math.min(this.height - TILE_PADDING * 2, this.width - TILE_PADDING * 2),
                    Math.min(this.height - TILE_PADDING * 2, this.width - TILE_PADDING * 2),
                    icon
            );
        }
        this.title = new TextWidget(
                getX() + TILE_PADDING,
                (getY() + getHeight() - Minecraft.getInstance().font.lineHeight - TILE_PADDING),
                width,
                10,
                Minecraft.getInstance().font,
                getMessage(),
                true
        );
        this.onPress = onPress;
        this.styleChanged = true;
    }

    @Override
    public void setWidth(int i) {
        this.title.setWidth(i);
        super.setWidth(i);
        this.styleChanged = true;
    }

    public void setHeight(int height) {
        this.height = Math.max(height, TILE_MIN_HEIGHT);
        this.styleChanged = true;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.setMessage(((Component) this.getMessage()).copy().withStyle(Style.EMPTY.withBold(true).withColor(((backgroundColor != 0) && (getColorLightNess(backgroundColor)) <= 0.67F) ? 0xFDFDFD : 0x0A0A0A)));
        this.title.setText(this.getMessage());
        this.backgroundColor = backgroundColor;
        this.styleChanged = true;
    }

    @Override
    public void onPress() {
        this.onPress.accept(this);
        this.styleChanged = true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        if (styleChanged) {
            if (icon != null) {
                this.icon.setX(getX() + (width / 2 - icon.getWidth() / 2));
                this.icon.setY(getY() + (height / 2 - icon.getHeight() / 2));
            }
            this.title.setX(getX() + TILE_PADDING);
            this.title.setY(getY() + getHeight() - Minecraft.getInstance().font.lineHeight - TILE_PADDING);

            if (!active) {
                this.title.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
            }

            this.styleChanged = false;
        }

        boolean hovered = active && isHoveredOrFocused();
        if (background != null) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.6F);
            RenderSystem.setShaderTexture(0, background);

            guiGraphics.blit(
                    background,
                    getX(), getY(), offsetY, 0,
                    width, height, width, height
            );

            int argb = active ? (hovered ? 0xCFF1F1F1 : 0x9EDADADA) : 0x95AEAEAE;
            guiGraphics.fill(getX(), getY(), getX() + this.width, getY() + 1, -(0xFFFFFFFF - argb));
            guiGraphics.fill(getX(), getY() + 1, getX() + 1, getY() + this.height - 1, -(0xFFFFFFFF - argb));
            guiGraphics.fill(getX(), getY() + this.height - 1, getX() + this.width, getY() + this.height, -(0xFFFFFFFF - argb));
            guiGraphics.fill(getX() + this.width - 1, getY() + 1, getX() + width, getY() + this.height - 1, -(0xFFFFFFFF - argb));

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            int argb = active ?
                    ((backgroundColor != 0 ? (hovered ? backgroundColor + (getColorLightNess(backgroundColor) <= 0.67f ? 0x00111111 : -0x00111111) : backgroundColor) : (hovered ? 0xC5858585 : 0xC5616161))) - 1:
                    ((backgroundColor != 0 ? (hovered ? backgroundColor + (getColorLightNess(backgroundColor) <= 0.67f ? 0x22111111 : -0x22111111) : backgroundColor) : (hovered ? 0x99858585 : 0x99616161))) - 1;
            guiGraphics.fill(getX(), getY(), getX() + this.width, getY() + this.height, -(0xFFFFFFFF - argb));
        }

        if (this.icon != null) {
            this.icon.render(guiGraphics, i, j, f);
        }

        this.title.render(guiGraphics, i, j, f);
    }

    private static float getColorLightNess(int argb) {
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = (argb) & 0xff;
        return (float) Math.pow(Math.pow(r / 255.0f, 2.2f) + Math.pow(g / 170.0f, 2.2f) + Math.pow(b / 425.0f, 2.2f), 1 / 2.2f) * 0.547373141f;
    }

    public void sendStyleChanged() {
        this.styleChanged = true;
    }
}
