package berries.servermod.tcm.client.flueroui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class TextDrawer {
    public static void drawText(GuiGraphics guiGraphics, Font font, Component text, Alignment alignment, int x, int y, int color) {
        guiGraphics.drawString(font, text, alignment.calculateX(x, font.width(text.getVisualOrderText())), y, color);
    }

    public static void drawText(GuiGraphics guiGraphics, Font font, FormattedCharSequence text, Alignment alignment, int x, int y, int color) {
        guiGraphics.drawString(font, text, alignment.calculateX(x, font.width(text)), y, color);
    }

    public static void drawText(GuiGraphics guiGraphics, Font font, Component text, Alignment alignment, int x, int y, int color, boolean shadow) {
        guiGraphics.drawString(font, text, alignment.calculateX(x, font.width(text.getVisualOrderText())), y, color, shadow);
    }

    public static void drawText(GuiGraphics guiGraphics, Font font, FormattedCharSequence text, Alignment alignment, int x, int y, int color, boolean shadow) {
        guiGraphics.drawString(font, text, alignment.calculateX(x, font.width(text)), y, color, shadow);
    }

    public enum Alignment {
        LEFT(0), CENTER(1), RIGHT(2);

        private final int mode;

        private Alignment(int mode) {
            this.mode = mode;
        }

        public int calculateX(int x, int length) {
            switch (mode) {
                case 1 -> {
                    return x - (length / 2);
                }
                case 2 -> {
                    return x - length;
                }
                default -> {
                    return x;
                }
            }
        }
    }
}
