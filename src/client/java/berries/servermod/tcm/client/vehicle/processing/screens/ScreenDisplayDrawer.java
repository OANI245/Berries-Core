package berries.servermod.tcm.client.vehicle.processing.screens;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.client.render.GraphicsTexture;
import berries.servermod.tcm.client.util.texture.AWTDrawUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.mtr.mapping.holder.Identifier;
import org.mtr.mapping.mapper.ResourceManagerHelper;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.AttributedString;
import java.util.function.Consumer;

public class ScreenDisplayDrawer {
    private final int textureWidth;
    private final int textureHeight;

    private final Font[] defaultFonts;
    private final Font asciiOnlyFont;

    private GraphicsTexture texture;

    private static final Font CONSTANT_FALLBACK_FONT = new Font("Serif", Font.PLAIN, 1);

    public ScreenDisplayDrawer(int texWidth, int texHeight, ResourceLocation[] defaultFonts, String... defaultSystemFonts) {
        this.textureWidth = texWidth;
        this.textureHeight = texHeight;

        this.asciiOnlyFont = null;
        this.defaultFonts = new Font[defaultFonts.length + defaultSystemFonts.length];
        for (int i = 0; i < defaultSystemFonts.length; i++) {
            this.defaultFonts[i] = new Font(defaultSystemFonts[i], Font.PLAIN, 1);
        }
        for (int i = defaultSystemFonts.length; i < this.defaultFonts.length; i++) {
            ResourceLocation font = defaultFonts[i - defaultSystemFonts.length];
            try {
                byte[][] fileBytes = new byte[][]{null};
                ResourceManagerHelper.readResource(new Identifier(font), is -> {
                    try {
                        fileBytes[0] = is.readAllBytes();
                    } catch (IOException e) {
                        TCM.LOGGER.error("Error while reading data {}", font);
                    }
                });

                if (fileBytes[0] != null) {
                    this.defaultFonts[i] = Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(fileBytes[0]));
                }
            } catch (Exception e) {
                TCM.LOGGER.error("Error load font {}", font);
            }
        }
    }

    public ScreenDisplayDrawer(int texWidth, int texHeight, ResourceLocation[] defaultFonts, ResourceLocation asciiOnlyFont, String... defaultSystemFonts) {
        this.textureWidth = texWidth;
        this.textureHeight = texHeight;

        this.asciiOnlyFont = AWTDrawUtil.getResourceFont(asciiOnlyFont.toString());
        this.defaultFonts = new Font[defaultFonts.length + defaultSystemFonts.length];
        for (int i = 0; i < defaultSystemFonts.length; i++) {
            this.defaultFonts[i] = new Font(defaultSystemFonts[i], Font.PLAIN, 1);
        }
        for (int i = defaultSystemFonts.length; i < this.defaultFonts.length; i++) {
            ResourceLocation font = defaultFonts[i - defaultSystemFonts.length];
            try {
                byte[][] fileBytes = new byte[][]{null};
                ResourceManagerHelper.readResource(new Identifier(font), is -> {
                    try {
                        fileBytes[0] = is.readAllBytes();
                    } catch (IOException e) {
                        TCM.LOGGER.error("Error while reading data {}", font);
                    }
                });

                if (fileBytes[0] != null) {
                    this.defaultFonts[i] = Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(fileBytes[0]));
                }
            } catch (Exception e) {
                TCM.LOGGER.error("Error load font {}", font);
            }
        }
    }

    public void createGraphicsTexture() {
        if (texture != null) closeGraphicsTexture();
        texture = new GraphicsTexture(this.textureWidth, this.textureHeight);
        var graphics = texture.graphics;
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, this.textureWidth, this.textureHeight);
    }

    public boolean shouldResetStartTime(int startX, int startY, int endX, int endY,
                                        String message, Color background,
                                        Color textColor, int fontSize, long timeElapse, double slideSpeed) {
        if (texture == null) return true;
        var g = texture.graphics;
        var du = new AWTDrawUtil(g);
        drawRect(startX, startY, endX, endY, background);
        if (message == null) return true;

        Font[] fonts;
        if (defaultFonts == null || defaultFonts.length == 0 || defaultFonts[0] == null) {
            fonts = new Font[] { CONSTANT_FALLBACK_FONT };
        } else {
            fonts = defaultFonts;
        }

        g.setColor(textColor);
        g.setFont(fonts[0].deriveFont(Font.PLAIN, fontSize));
        AttributedString as = createFallbackString(message,
                fonts[0].deriveFont(Font.PLAIN, fontSize), asciiOnlyFont == null ? null : asciiOnlyFont.deriveFont(Font.PLAIN, fontSize),
                fonts.length > 1 ? fonts[1].deriveFont(Font.PLAIN, fontSize) : CONSTANT_FALLBACK_FONT);
        var length = (double) du.width(as) + endX - startX;
        var position = (double) timeElapse / 1000 * 60 * slideSpeed;
        g.drawString(as.getIterator(), (int) (endX + 20 - position), startY + fontSize - 1);
        return position > length + 20;
    }

    public void drawConstantMessage(int startX, int startY, int endX, int endY,
                                        String message, Color background,
                                        Color textColor, int fontSize) {
        if (texture == null) return;
        var g = texture.graphics;
        var du = new AWTDrawUtil(g);
        drawRect(startX, startY, endX, endY, background);
        if (message == null) return;

        Font[] fonts;
        if (defaultFonts == null || defaultFonts.length == 0 || defaultFonts[0] == null) {
            fonts = new Font[] { CONSTANT_FALLBACK_FONT };
        } else {
            fonts = defaultFonts;
        }

        AttributedString as = createFallbackString(message,
                fonts[0].deriveFont(Font.PLAIN, fontSize), asciiOnlyFont == null ? null : asciiOnlyFont.deriveFont(Font.PLAIN, fontSize),
                fonts.length > 1 ? fonts[1].deriveFont(Font.PLAIN, fontSize) : CONSTANT_FALLBACK_FONT);
        var length = (double) du.width(as);
        var position = (endX - length) / 2;
        g.setColor(textColor);
        g.setFont(fonts[0].deriveFont(Font.PLAIN, fontSize));
        g.drawString(as.getIterator(), (int) position, startY + fontSize - 1);
    }

    public void drawRect(int startX, int startY, int endX, int endY,
                         Color background) {
        if (texture == null) return;
        var g = texture.graphics;
        g.setColor(background);
        g.fillRect(startX, startY, endX - startX, endY - startY);
    }

    public GraphicsTexture getTexture() {
        return texture;
    }
    public static AttributedString createFallbackString(String text, Font mainFont, Font fallbackFont) {
        return createFallbackString(text, mainFont, null, fallbackFont);
    }

    public static AttributedString createFallbackString(String text, Font mainFont, @Nullable Font asciiOnlyFont, Font fallbackFont) {
        AttributedString result = new AttributedString(text);

        int textLength = text.length();
        result.addAttribute(TextAttribute.FONT, mainFont, 0, textLength);

        if (fallbackFont == null) {
            return result;
        }

        boolean fallback0 = false;
        boolean fallback1 = false;
        int fallbackBegin0 = 0;
        int fallbackBegin1 = 0;
        for (int i = 0; i < text.length(); i++) {
            boolean curFallback = !mainFont.canDisplay(text.charAt(i));
            boolean curAsciiFallback = asciiOnlyFont == null || !asciiOnlyFont.canDisplay(text.charAt(i));
            boolean curFallbackToFallbackFont = (curFallback && curAsciiFallback);
            boolean curFallbackToAsciiFont = (!curFallback && String.valueOf(text.charAt(i)).matches("[a-zA-Z0-9]") && (asciiOnlyFont != null && asciiOnlyFont.canDisplay(text.charAt(i))));
            if ((curFallbackToAsciiFont && i < text.length() - 1) != fallback1) {
                fallback1 = curFallbackToAsciiFont;
                if (fallback1 && i < text.length() - 1) {
                    fallbackBegin1 = i;
                } else {
                    result.addAttribute(TextAttribute.FONT, asciiOnlyFont, fallbackBegin1, i);
                }
            } else if ((curFallbackToFallbackFont && i < text.length() - 1) != fallback0) {
                fallback0 = curFallbackToFallbackFont;
                if (fallback0 && i < text.length() - 1) {
                    fallbackBegin0 = i;
                } else {
                    result.addAttribute(TextAttribute.FONT, fallbackFont, fallbackBegin0, i);
                }
            }
        }
        return result;
    }

    public void executeCustomDraw(Consumer<GraphicsTexture> event) {
        event.accept(texture);
    }

    public void closeGraphicsTexture() {
        if (texture != null) {
            texture.close();
        }
        texture = null;
    }

    public Font getDefaultFont(int i) {
        return defaultFonts[i];
    }

    public void setDefaultFont(int index, Font defaultFont) {
        this.defaultFonts[index] = defaultFont;
    }

    public int getDefaultFontCount() {
        return defaultFonts.length;
    }
}
