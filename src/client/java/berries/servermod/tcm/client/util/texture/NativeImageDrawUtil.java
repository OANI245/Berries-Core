package berries.servermod.tcm.client.util.texture;

import net.minecraft.util.FastColor;
import org.mtr.core.tool.Utilities;
import org.mtr.mapping.holder.NativeImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.util.function.BiConsumer;

import static org.mtr.mod.data.IGui.ARGB_BLACK;

public interface NativeImageDrawUtil {
    static void createDrawAndApply(NativeImage nativeImage, BiConsumer<BufferedImage, Graphics2D> drawer) {
        var bufImage = new BufferedImage(nativeImage.getWidth(), nativeImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        var g = bufImage.createGraphics();
        drawer.accept(bufImage, g);
        applyAWTImage(nativeImage, bufImage);
        g.dispose();
        bufImage.flush();
    }

    static void applyAWTImage(NativeImage nativeImage, BufferedImage include) {
        int[] colors = ((DataBufferInt)include.getRaster().getDataBuffer()).getData();

        int x = 0;
        int y = 0;
        int width = nativeImage.getWidth();
        int height = nativeImage.getHeight();
        for (int i = 0; i < width * height; i ++) {
            blendPixel(nativeImage, x, y, colors[i]);
            x++;
            if (x == width) {
                x = 0;
                y++;
            }
        }
    }

    static void drawPixelSafe(NativeImage nativeImage, int x, int y, int color) {
        if (Utilities.isBetween(x, 0, nativeImage.getWidth() - 1) && Utilities.isBetween(y, 0, nativeImage.getHeight() - 1)) {
            nativeImage.setPixelColor(x, y, invertColor(color));
        }
    }

    static int invertColor(int color) {
        return ((color & ARGB_BLACK) != 0 ? ARGB_BLACK : 0) + ((color & 0xFF) << 16) + (color & 0xFF00) + ((color & 0xFF0000) >> 16);
    }

    static void blendPixel(NativeImage nativeImage, int x, int y, int argb) {
        int a = FastColor.ARGB32.alpha(argb);
        int r = FastColor.ARGB32.red(argb);
        int g = FastColor.ARGB32.green(argb);
        int b = FastColor.ARGB32.blue(argb);

        if (Utilities.isBetween(x, 0, nativeImage.getWidth() - 1) && Utilities.isBetween(y, 0, nativeImage.getHeight() - 1)) {
            final float percent = (float) (a & 0xFF) / 0xFF;
            if (percent > 0) {
                final int existingPixel = nativeImage.getColor(x, y);
                final boolean existingTransparent = ((existingPixel >> 24) & 0xFF) == 0;
                final int r1 = existingTransparent ? 0xFF : (existingPixel & 0xFF);
                final int g1 = existingTransparent ? 0xFF : ((existingPixel >> 8) & 0xFF);
                final int b1 = existingTransparent ? 0xFF : ((existingPixel >> 16) & 0xFF);
                final int r2 = r & 0xFF;
                final int g2 = g & 0xFF;
                final int b2 = b & 0xFF;
                final float inversePercent = 1 - percent;
                final int finalColor = ARGB_BLACK | (((int) (r1 * inversePercent + r2 * percent) << 16) + ((int) (g1 * inversePercent + g2 * percent) << 8) + (int) (b1 * inversePercent + b2 * percent));
                drawPixelSafe(nativeImage, x, y, finalColor);
            }
        }
    }
}
