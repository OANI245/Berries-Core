package berries.servermod.tcm.client.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FocusableTextWidget extends AbstractWidget {
    private static final int DEFAULT_PADDING = 4;
    private final boolean alwaysShowBorder;
    private final int padding;
    private final Screen screen;

    public FocusableTextWidget(int i, Component component, Font font, Screen screen) {
        this(i, component, font, 4, screen);
    }

    public FocusableTextWidget(int i, Component component, Font font, int j, Screen screen) {
        this(i, component, font, true, j, screen);
    }

    public FocusableTextWidget(int i, Component component, Font font, boolean bl, int j, Screen screen) {
        super(0, 0, Math.min(i, font.width(component) + j * 2), font.lineHeight + (j * 2),  component);
        Minecraft mc = Minecraft.getInstance();
        this.setWidth(i);
        this.active = true;
        this.alwaysShowBorder = bl;
        this.padding = j;
        this.screen = screen;
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        return false;
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        this.setX(screen.width / 2 - this.width / 2);
        this.setY(screen.height / 2 - this.height / 2);

        if (this.isFocused() || this.alwaysShowBorder) {
            int k = this.getX() - this.padding;
            int l = this.getY() - this.padding;
            int m = this.getWidth() + this.padding * 2;
            int n = this.getHeight() + this.padding * 2;
            int o = this.alwaysShowBorder ? (this.isFocused() ? -1 : -6250336) : -1;
            guiGraphics.fill(k + 1, l, k + m, l + n, -16777216);
            renderOutline(guiGraphics, k, l, m, n, o);
        }

        /*super.render(guiGraphics, i, j, f);*/
    }

    public void renderOutline(GuiGraphics guiGraphics, int i, int j, int k, int l, int m) {
        guiGraphics.fill(i, j, i + k, j + 1, m);
        guiGraphics.fill(i, j + l - 1, i + k, j + l, m);
        guiGraphics.fill(i, j + 1, i + 1, j + l - 1, m);
        guiGraphics.fill(i + k - 1, j + 1, i + k, j + l - 1, m);
    }
}
