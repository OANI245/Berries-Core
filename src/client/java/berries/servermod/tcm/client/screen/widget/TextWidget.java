package berries.servermod.tcm.client.screen.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class TextWidget extends AbstractWidget {
    public Font font;
    public Alignment alignment = Alignment.LEFT;
    public int color = 0xffffff;
    public float scale = 1;
    public boolean disabledShadow = false;
    private MultiLineLabel label;
    private final MutableComponent text;

    public TextWidget(int x, int y, int width, int height, Font font, Component text, float scale) {
        super(x, y, (int)Math.floor(width / scale), height, text);
        this.font = font;
        this.label = MultiLineLabel.create(font, text, (int)Math.floor(width / scale));
        this.active = false;
        this.text = text.copy();
        this.scale = scale;
    }

    public TextWidget(int x, int y, int width, int height, Font font, Component text, Alignment alignment, float scale) {
        this(x, y, width, height, font, text, scale);
        this.alignment = alignment;
        this.init();
    }

    public TextWidget(int x, int y, int width, int height, Font font, Component text, boolean disabledShadow, float scale) {
        this(x, y, width, height, font, text, scale);
        this.alignment = Alignment.LEFT;
        this.disabledShadow = disabledShadow;
        this.init();
    }

    public TextWidget(int x, int y, int width, int height, Font font, Component text) {
        this(x, y, width, height, font, text, 1);
    }

    public TextWidget(int x, int y, int width, int height, Font font, Component text, Alignment alignment) {
        this(x, y, width, height, font, text, 1);
        this.alignment = alignment;
        this.init();
    }

    public TextWidget(int x, int y, int width, int height, Font font, Component text, boolean disabledShadow) {
        this(x, y, width, height, font, text, 1);
        this.alignment = Alignment.LEFT;
        this.disabledShadow = disabledShadow;
        this.init();
    }

    public void init() {
    }

    public void setText(Component text) {
        this.label = MultiLineLabel.create(font, text, width);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, scale);
        switch (alignment) {
            case LEFT -> {
                if (!disabledShadow) {
                    label.renderLeftAligned(guiGraphics, (int)Math.floor(getX() / scale), (int)Math.floor(getY() / scale), (int) Math.floor(10 / scale), color);
                } else {
                    label.renderLeftAlignedNoShadow(guiGraphics, (int)Math.floor(getX() / scale), (int)Math.floor(getY() / scale), (int) Math.floor(10 / scale), color);
                }
            }
            case CENTER -> {
                label.renderCentered(guiGraphics, (int)Math.floor((float) width / 2 / scale) + (int)Math.floor(getX() / scale), (int)Math.floor(getY() / scale), (int) Math.floor(10 / scale), color);
            }
            case RIGHT -> {
                label.renderLeftAligned(guiGraphics, (int)Math.floor(getX() / scale) + (font.width(getMessage())), (int)Math.floor(getY() / scale), (int) Math.floor(10 / scale), color);
            }
            default -> {}
        }
        guiGraphics.pose().popPose();
        guiGraphics.pose().pushPose();
    }

    public void setStyle(Style style) {
        this.label = MultiLineLabel.create(font, text.withStyle(style), width);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    public enum Alignment {LEFT, CENTER, RIGHT}
}
