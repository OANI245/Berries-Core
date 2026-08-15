package berries.servermod.tcm.client.flueroui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;

public class FlueroUI {
    public static final int PRIMARY_COLOR = rgb(143,236,30);
    public static final int PRIMARY_LOW = rgb(132,212,30);
    public static final int BACKGROUND_COLOR = rgb(32, 32, 32);
    public static final int BACKGROUND_HIGH = rgb(49, 49, 49);
    public static final int SECONDARY_COLOR = FlueroUI.rgb(50,50,50);
    public static final int SECONDARY_HIGH = FlueroUI.rgb(72,72,72);
    public static final int SECONDARY_LIGHT = FlueroUI.rgb(0xE0,0xE0,0xE0);
    public static final int SECONDARY_LIGHT_LOW = FlueroUI.rgb(0xC8,0xC8,0xC8);

    public static void renderCenteredDialog(GuiGraphics graphics, int screenWidth, int screenHeight, int width, int height) {
        int minX = screenWidth / 2 - width / 2;
        int minY = screenHeight / 2 - height / 2;
        int maxX = screenWidth / 2 + width / 2;
        int maxY = screenHeight / 2 + height / 2;
        graphics.fill(minX, minY, maxX, maxY, BACKGROUND_COLOR);
        graphics.fill(minX, minY, maxX, minY + 1, BACKGROUND_HIGH);
        graphics.fill(minX, minY, minX + 1, maxY, BACKGROUND_HIGH);
        graphics.fill(minX, maxY - 1, maxX, maxY, BACKGROUND_HIGH);
        graphics.fill(maxX - 1, minY, maxX, maxY, BACKGROUND_HIGH);
    }

    public static void renderDialog(GuiGraphics graphics, int x, int y, int width, int height) {
        int maxX = x + width;
        int maxY = y + height;
        graphics.fill(x, y, maxX, maxY, BACKGROUND_COLOR);
        graphics.fill(x, y, maxX, y + 1, BACKGROUND_HIGH);
        graphics.fill(x, y, x + 1, maxY, BACKGROUND_HIGH);
        graphics.fill(x, maxY - 1, maxX, maxY, BACKGROUND_HIGH);
        graphics.fill(maxX - 1, y, maxX, maxY, BACKGROUND_HIGH);
    }

    public static int rgb(int r, int g, int b) {
        return FastColor.ARGB32.color(255, r, g, b);
    }

    public static int argb(int a, int r, int g, int b) {
        return FastColor.ARGB32.color(a, r, g, b);
    }

    public static int textColor(int hex) {
        return hex;
    }
}
