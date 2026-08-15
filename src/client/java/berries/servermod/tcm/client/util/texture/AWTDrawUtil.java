package berries.servermod.tcm.client.util.texture;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.client.render.GraphicsTexture;
import org.mtr.mapping.holder.Identifier;
import org.mtr.mapping.mapper.ResourceManagerHelper;
import org.mtr.mod.data.IGui;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.AttributedString;

public class AWTDrawUtil implements IGui {
    private final Graphics2D graphics;
    private Stroke previousStroke;
    private AffineTransform previousTransform;

    public AWTDrawUtil(Graphics2D graphics) {
        this.graphics = graphics;
    }

    public int drawText(String context, Font font, float x0, float y0, Color color,
                         VerticalAlignment verticalAlignment,
                         HorizontalAlignment horizontalAlignment) {
        if (color != null) {
            graphics.setColor(color);
        }
        if (font != null) {
            graphics.setFont(font);
        }
        var w = graphics.getFontMetrics().stringWidth(context);
        var h = graphics.getFont().getSize();//graphics.getFontMetrics().getHeight();
        var x = horizontalAlignment.getOffset(x0, w);
        var y = verticalAlignment.getOffset(y0 + h, h);
        graphics.drawString(context, x, y);
        return w;
    }

    public int drawText(AttributedString context, float x0, float y0, Color color,
                        VerticalAlignment verticalAlignment,
                        HorizontalAlignment horizontalAlignment) {
        if (color != null) {
            graphics.setColor(color);
        }
        var w = 0;
        var h = 0;
        var it = context.getIterator();
        for (int i = 0; i <= it.getEndIndex(); i++) {
            it.setIndex(i);
            char c = it.current();
            var f = (Font) it.getAttribute(TextAttribute.FONT);
            if (f == null) {
                if (i >= 100) {
                    break;
                }
                continue;
            }
            w += graphics.getFontMetrics(f).stringWidth(String.valueOf(c));
            h = Math.max(h, f.getSize());
        }
        it.setIndex(0);
        var x = horizontalAlignment.getOffset(x0, w);
        var y = verticalAlignment.getOffset(y0 + h, h);
        graphics.drawString(it, x, y);
        return w;
    }

    public int width(AttributedString context) {
        var it = context.getIterator();
        var w = 0;
        for (int i = 0; i <= it.getEndIndex(); i++) {
            it.setIndex(i);
            char c = it.current();
            var f = (Font) it.getAttribute(TextAttribute.FONT);
            if (f == null) {
                if (i >= 100) {
                    break;
                }
                continue;
            }
            w += graphics.getFontMetrics(f).stringWidth(String.valueOf(c));
        }
        it.setIndex(0);
        return w;
    }

    public void setTransform(AffineTransform value) {
        if (previousTransform == null) {
            previousTransform = graphics.getTransform();
        }
        graphics.setTransform(value);
    }

    public void resetTransform() {
        graphics.setTransform(previousTransform);
        previousTransform = null;
    }

    public void setStroke(Stroke value) {
        if (previousStroke == null) {
            previousStroke = graphics.getStroke();
        }
        graphics.setStroke(value);
    }

    public void resetStroke() {
        graphics.setStroke(previousStroke);
        previousStroke = null;
    }

    public int drawText(String context, Font font, float x, float y, Color color, HorizontalAlignment horizontalAlignment) {
        return drawText(context, font, x, y, color, VerticalAlignment.TOP, horizontalAlignment);
    }

    public int drawText(String context, float x, float y, HorizontalAlignment horizontalAlignment) {
        return drawText(context, null, x, y, null, horizontalAlignment);
    }

    public int drawText(String context, float x, float y, VerticalAlignment verticalAlignment, HorizontalAlignment horizontalAlignment) {
        return drawText(context, null, x, y, null, verticalAlignment, horizontalAlignment);
    }

    public int drawText(String context, Font font, float x, float y, Color color) {
        return drawText(context, font, x, y, color, HorizontalAlignment.LEFT);
    }

    public int drawText(String context, Font font, float x, float y) {
        return drawText(context, font, x, y, null);
    }

    public int drawText(String context, float x, float y, Color color) {
        return drawText(context, null, x, y, color);
    }

    public int drawText(String context, float x, float y) {
        return drawText(context, null, x, y);
    }

    public void drawRect(int x, int y, int w, int h, Color color) {
        if (color != null) {
            graphics.setColor(color);
        }
        graphics.fillRect(x, y, w, h);
    }

    public void drawBorderedRect(int x, int y, int w, int h, Color fillColor, Color borderColor, int bh) {
        if (borderColor != null) {
            setStroke(new BasicStroke(bh, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL));
            graphics.setColor(borderColor);
            graphics.drawRect(x, y, w, h);
            resetStroke();
        }
        drawRect(x, y, w, h, fillColor);
    }

    public void drawRadiusRect(int x, int y, int w, int h, Color color, double radius) {
        if (color != null) {
            graphics.setColor(color);
        }
        graphics.fillRoundRect(x, y, w, h, (int) (radius * 2), (int) (radius * 2));
    }

    public void drawBorderedRadiusRect(int x, int y, int w, int h, Color fillColor, Color borderColor, double radius, int bh) {
        if (borderColor != null) {
            setStroke(new BasicStroke(bh, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL));
            graphics.setColor(borderColor);
            graphics.drawRoundRect(x, y, w, h, (int) (radius * 2), (int) (radius * 2));
            resetStroke();
        }
        drawRadiusRect(x, y, w, h, fillColor, radius);
    }

    public static Font getResourceFont(String path) {
        Font result = null;
        try {
            byte[][] fileBytes = new byte[][]{null};
            ResourceManagerHelper.readResource(new Identifier(path), is -> {
                try {
                    fileBytes[0] = is.readAllBytes();
                } catch (IOException e) {
                    TCM.LOGGER.error("Error while reading data {}", path);
                }
            });

            if (fileBytes[0] != null) {
                result = Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(fileBytes[0]));
            }
        } catch (Exception e) {
            TCM.LOGGER.error("Error load font {}", result);
        }
        return result;
    }

    public static BufferedImage getResourceImage(String path) {
        BufferedImage result = null;
        try {
            byte[][] fileBytes = new byte[][]{null};
            ResourceManagerHelper.readResource(new Identifier(path), is -> {
                try {
                    fileBytes[0] = is.readAllBytes();
                } catch (IOException e) {
                    TCM.LOGGER.error("Error while reading data {}", path);
                }
            });

            if (fileBytes[0] != null) {
                result = GraphicsTexture.createArgbBufferedImage(ImageIO.read(new ByteArrayInputStream(fileBytes[0])));
            }
        } catch (Exception e) {
            TCM.LOGGER.error("Error load image {}", result);
        }
        return result;
    }

    public Graphics2D getGraphics() {
        return graphics;
    }
}
