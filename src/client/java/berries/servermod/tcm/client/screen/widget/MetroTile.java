package berries.servermod.tcm.client.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class MetroTile extends AbstractButton {
    public static final int TILE_PADDING = 12;
    public static final int TILE_ICON_SIZE = 50;
    public static final int TILE_MIN_HEIGHT = TILE_PADDING * 2 + 26;

    public ImageWidget icon = null;
    public TextWidget title = null;
    public TextWidget description = null;

    public ResourceLocation background = null;

    protected int backgroundColor = 0;
    public int offsetY = 0;

    private final Consumer<MetroTile> onPress;

    public MetroTile(int i, int j, int k, int l, ResourceLocation icon, Component title, Component description, Consumer<MetroTile> onPress) {
        super(i, j, k, Math.max(l, TILE_MIN_HEIGHT), title.copy().withStyle(Style.EMPTY.withBold(true).withColor(0xFDFDFD)));
        if (icon != null) {
            this.icon = new ImageWidget(
                    getX() + TILE_PADDING,
                    getY() + (height / 2 - TILE_ICON_SIZE / 2),
                    TILE_ICON_SIZE,
                    TILE_ICON_SIZE,
                    icon
            );
        }
        this.title = new TextWidget(
                getX() + TILE_PADDING + (icon != null ? TILE_PADDING + TILE_ICON_SIZE : 0),
                getY() + TILE_PADDING,
                width - (TILE_PADDING * 2 + (icon != null ? TILE_ICON_SIZE : 0)),
                10,
                Minecraft.getInstance().font,
                getMessage(),
                true
        );
        this.description = new TextWidget(
                getX() + TILE_PADDING + (icon != null ? TILE_PADDING + TILE_ICON_SIZE : 0),
                getY() + TILE_PADDING + 16,
                width - (TILE_PADDING * 2 + (icon != null ? TILE_ICON_SIZE : 0)),
                this.height - 10 - (getY() + (icon != null ? TILE_ICON_SIZE * 2 : 0) + TILE_PADDING * 3), Minecraft.getInstance().font,
                description.copy().withStyle(Style.EMPTY.withColor(0xDEDEDE)),
                true
        );
        this.onPress = onPress;
    }

    @Override
    public void setWidth(int i) {
        this.title.setWidth(i - (TILE_PADDING * 3 + TILE_ICON_SIZE));
        this.description.setWidth(width - (TILE_PADDING * 3 + TILE_ICON_SIZE));
        super.setWidth(i);
    }

    public void setHeight(int height) {
        this.height = Math.max(height, TILE_MIN_HEIGHT);
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.setMessage(((Component) this.getMessage()).copy().withStyle(Style.EMPTY.withBold(true).withColor(((backgroundColor != 0) && (getColorLightNess(backgroundColor)) <= 0.67F) ? 0xFDFDFD : 0x0A0A0A)));
        this.title.setText(this.getMessage());
        this.description.setText(((Component) this.description.getMessage()).copy().withStyle(Style.EMPTY.withBold(false).withColor(((backgroundColor != 0) && (getColorLightNess(backgroundColor)) <= 0.67F) ? 0xDEDEDE : 0x505050)));
        this.backgroundColor = backgroundColor;
    }

    @Override
    public void onPress() {
        this.onPress.accept(this);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        if (icon != null) {
            this.icon.setX(getX() + TILE_PADDING);
            this.icon.setY(getY() + (height / 2 - TILE_ICON_SIZE / 2));
        }
        this.title.setX(getX() + TILE_PADDING + (icon != null ? TILE_PADDING + TILE_ICON_SIZE : 0));
        this.title.setY(getY() + TILE_PADDING);
        this.description.setX(getX() + TILE_PADDING + (icon != null ? TILE_PADDING + TILE_ICON_SIZE : 0));
        this.description.setY(getY() + TILE_PADDING + 16);

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
            int argb = (backgroundColor != 0 ? (hovered ? backgroundColor + (getColorLightNess(backgroundColor) <= 0.67f ? 0x00111111 : -0x00111111) : backgroundColor) : (hovered ? 0xC5858585 : 0xC5616161)) - 1;
            guiGraphics.fill(getX(), getY(), getX() + this.width, getY() + this.height, -(0xFFFFFFFF - argb));
        }

        if (this.icon != null) {
            this.icon.render(guiGraphics, i, j, f);
        }

        this.title.render(guiGraphics, i, j, f);
        this.description.render(guiGraphics, i, j, f);
    }

    private static float getColorLightNess(int argb) {
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = (argb) & 0xff;
        return (float) Math.pow(Math.pow(r / 255.0f, 2.2f) + Math.pow(g / 170.0f, 2.2f) + Math.pow(b / 425.0f, 2.2f), 1 / 2.2f) * 0.547373141f;
    }
}
