package berries.servermod.tcm.client.data;

import berries.servermod.tcm.TCM;
import org.jetbrains.annotations.Nullable;
import org.mtr.core.servlet.MessageQueue;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.mtr.mapping.holder.*;
import org.mtr.mapping.mapper.ResourceManagerHelper;
import org.mtr.mod.Init;
import org.mtr.mod.client.RouteMapGenerator;
import org.mtr.mod.config.Config;
import org.mtr.mod.config.LanguageDisplay;
import org.mtr.mod.data.IGui;
import org.mtr.mod.render.MainRenderer;
import org.mtr.mod.render.MoreRenderLayers;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.text.AttributedString;
import java.util.Arrays;
import java.util.function.Supplier;

import static org.mtr.mod.data.IGui.ARGB_WHITE;
import static berries.servermod.tcm.client.data.TCMDynamicResourceCacheV2.DynamicResourceFormats.*;

public class TCMDynamicResourceCacheV2 {
    protected interface DynamicResourceFormats {
        String F_PT = "tcm_pixelated_text_%s_%s_%s_%s_%s";
        String F_C = "tcm_color_%s";
        String F_SN = "tcm_station_name_%s_%s";
        String F_TSN = "tcm_tall_station_name_%s_%s_%s_%s";
        String F_SNE = "tcm_station_name_entrance_%s_%s_%s_%s_%s";
        String F_SRSN = "tcm_single_row_station_name_%s_%s";
        String F_ST = "tcm_sign_text_%s_%s_%s_%s_%s";
        String F_LPD = "tcm_lift_panel_display_%s";
        String F_ESL = "tcm_exit_sign_letter_%s_%s";
        String F_RS = "tcm_route_square_%s_%s_%s";
        String F_DA = "tcm_direction_arrow_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%d";
        String F_PSN = "tcm_psdtop_station_name_%s_%s_%s_%s_%s_%s_%s_%s_%s_%d";
        String F_RM = "tcm_route_map_%s_%s_%s_%s_%s";
    }

    private Font font;
    private Font fontCjk;

    private final Object2ObjectLinkedOpenHashMap<String, TCMDynamicResourceCacheV2.DynamicResource> dynamicResources = new Object2ObjectLinkedOpenHashMap<>();
    private final ObjectOpenHashSet<String> generatingResources = new ObjectOpenHashSet<>();
    private final MessageQueue<Runnable> resourceRegistryQueue = new MessageQueue<>();
    private final Object2LongArrayMap<Identifier> deletedResources = new Object2LongArrayMap<>();

    public static TCMDynamicResourceCacheV2 instance = new TCMDynamicResourceCacheV2();

    public static final float LINE_HEIGHT_MULTIPLIER = 1.25F;
    private static final int COOLDOWN_TIME = 10000; // Images not requested within the last 10 seconds will be unregistered
    private static final Identifier DEFAULT_BLACK_RESOURCE = new Identifier(Init.MOD_ID, "textures/block/black.png");
    private static final Identifier DEFAULT_WHITE_RESOURCE = new Identifier(Init.MOD_ID, "textures/block/white.png");
    private static final Identifier DEFAULT_TRANSPARENT_RESOURCE = new Identifier(Init.MOD_ID, "textures/block/transparent.png");
    private static final int MAX_IMAGE_SIZE = 2048;

    public void reload() {
        font = null;
        fontCjk = null;
        TCM.LOGGER.debug("Refreshing dynamic resources; {} textures in memory; {} textures queued to be destroyed", dynamicResources.size(), deletedResources.size());
        dynamicResources.values().forEach(dynamicResource -> dynamicResource.needsRefresh = true);
        generatingResources.clear();
    }

    public void tick() {
        final ObjectArrayList<String> keysToRemove = new ObjectArrayList<>();
        dynamicResources.forEach((checkKey, checkDynamicResource) -> {
            if (checkDynamicResource.expiryTime < System.currentTimeMillis()) {
                checkDynamicResource.remove();
                deletedResources.put(checkDynamicResource.identifier, System.currentTimeMillis() + COOLDOWN_TIME);
                keysToRemove.add(checkKey);
            }
        });
        keysToRemove.forEach(dynamicResources::remove);

        final ObjectArrayList<Identifier> deletedResourcesToRemove = new ObjectArrayList<>();
        deletedResources.forEach((identifier, expiryTime) -> {
            if (expiryTime < System.currentTimeMillis()) {
                MinecraftClient.getInstance().getTextureManager().destroyTexture(identifier);
                deletedResourcesToRemove.add(identifier);
            }
        });
        deletedResourcesToRemove.forEach(deletedResources::removeLong);
    }

    public TCMDynamicResourceCacheV2.DynamicResource getPixelatedText(String text, int textColor, int maxWidth, double cjkSizeRatio, boolean fullPixel) {
        return getResource(String.format(F_PT, text, textColor, maxWidth, cjkSizeRatio, fullPixel), () -> TCMRouteMapGeneratorV2.generatePixelatedText(text, textColor, maxWidth, cjkSizeRatio, fullPixel), TCMDynamicResourceCacheV2.DefaultRenderingColor.TRANSPARENT);
    }

    public TCMDynamicResourceCacheV2.DynamicResource getColorStrip(long platformId) {
        return getResource(String.format(F_C, platformId), () -> TCMRouteMapGeneratorV2.generateColorStrip(platformId), TCMDynamicResourceCacheV2.DefaultRenderingColor.TRANSPARENT);
    }

    public TCMDynamicResourceCacheV2.DynamicResource getStationName(String stationName, float aspectRatio) {
        return getResource(String.format(F_SN, stationName, aspectRatio), () -> TCMRouteMapGeneratorV2.generateStationName(stationName, aspectRatio), TCMDynamicResourceCacheV2.DefaultRenderingColor.TRANSPARENT);
    }

    public TCMDynamicResourceCacheV2.DynamicResource getTallStationName(int textColor, String stationName, int stationColor, float aspectRatio) {
        return getResource(String.format(F_TSN, textColor, stationName, stationColor, aspectRatio), () -> TCMRouteMapGeneratorV2.generateTallStationName(textColor, stationName, stationColor, aspectRatio), TCMDynamicResourceCacheV2.DefaultRenderingColor.TRANSPARENT);
    }

    public TCMDynamicResourceCacheV2.DynamicResource getStationNameEntrance(int textColor, String stationName, float aspectRatio, String[] lineNames, Integer[] lineColors, int lineNamesLength, String[] exitZone) {
        return getResource(String.format(F_SNE, textColor, stationName, aspectRatio, Arrays.toString(lineNames), Arrays.toString(exitZone)), () -> TCMRouteMapGeneratorV2.generateStationNameEntrance(textColor, stationName, aspectRatio, lineNames, lineColors, lineNamesLength, exitZone), TCMDynamicResourceCacheV2.DefaultRenderingColor.TRANSPARENT);
    }

    public TCMDynamicResourceCacheV2.DynamicResource getSingleRowStationName(long platformId, float aspectRatio) {
        return getResource(String.format(F_SRSN, platformId, aspectRatio), () -> TCMRouteMapGeneratorV2.generateSingleRowStationName(platformId, aspectRatio), TCMDynamicResourceCacheV2.DefaultRenderingColor.WHITE);
    }

    public TCMDynamicResourceCacheV2.DynamicResource getSignText(String text, IGui.HorizontalAlignment horizontalAlignment, float paddingScale, int backgroundColor, int textColor) {
        return getResource(String.format(F_ST, text, horizontalAlignment, paddingScale, backgroundColor, textColor), () -> TCMRouteMapGeneratorV2.generateSignText(text, horizontalAlignment, paddingScale, backgroundColor, textColor), TCMDynamicResourceCacheV2.DefaultRenderingColor.TRANSPARENT);
    }

    public TCMDynamicResourceCacheV2.DynamicResource getLiftPanelDisplay(String originalText, int textColor) {
        return getResource(String.format(F_LPD, originalText), () -> TCMRouteMapGeneratorV2.generateLiftPanel(originalText, textColor), TCMDynamicResourceCacheV2.DefaultRenderingColor.BLACK);
    }

    public TCMDynamicResourceCacheV2.DynamicResource getExitSignLetter(String exitLetter, String exitNumber, int backgroundColor) {
        return getResource(String.format(F_ESL, exitLetter, exitNumber), () -> TCMRouteMapGeneratorV2.generateExitSignLetter(exitLetter, exitNumber, backgroundColor), TCMDynamicResourceCacheV2.DefaultRenderingColor.TRANSPARENT);
    }

    public TCMDynamicResourceCacheV2.DynamicResource getRouteSquare(int color, String routeName, IGui.HorizontalAlignment horizontalAlignment) {
        return getResource(String.format(F_RS, color, routeName, horizontalAlignment), () -> TCMRouteMapGeneratorV2.generateRouteSquare(color, routeName, horizontalAlignment), TCMDynamicResourceCacheV2.DefaultRenderingColor.TRANSPARENT);
    }

    public TCMDynamicResourceCacheV2.DynamicResource getDirectionArrow(long platformId, boolean hasLeft, boolean hasRight, IGui.HorizontalAlignment horizontalAlignment, boolean showToString, float paddingScale, float aspectRatio, int backgroundColor, int textColor, int transparentColor, int style) {
        return getResource(String.format(F_DA, platformId, hasLeft, hasRight, horizontalAlignment, showToString, paddingScale, aspectRatio, backgroundColor, textColor, transparentColor, style), () -> TCMRouteMapGeneratorV2.generateDirectionArrow(platformId, hasLeft, hasRight, horizontalAlignment, showToString, paddingScale, aspectRatio, backgroundColor, textColor, transparentColor, style), transparentColor == 0 && backgroundColor == ARGB_WHITE ? TCMDynamicResourceCacheV2.DefaultRenderingColor.WHITE : TCMDynamicResourceCacheV2.DefaultRenderingColor.TRANSPARENT);
    }

    public TCMDynamicResourceCacheV2.DynamicResource getPSDTopStationName(long platformId, String stationName, IGui.HorizontalAlignment horizontalAlignment, boolean showToString, float paddingScale, float aspectRatio, int backgroundColor, int textColor, int transparentColor, int val) {
        return getResource(String.format(F_PSN, platformId, stationName, horizontalAlignment, showToString, paddingScale, aspectRatio, backgroundColor, textColor, transparentColor, val), () -> TCMRouteMapGeneratorV2.generatePSDTopStationName(platformId, stationName, horizontalAlignment, showToString, paddingScale, aspectRatio, backgroundColor, textColor, transparentColor, val), transparentColor == 0 && backgroundColor == ARGB_WHITE ? TCMDynamicResourceCacheV2.DefaultRenderingColor.WHITE : TCMDynamicResourceCacheV2.DefaultRenderingColor.TRANSPARENT);
    }

    public TCMDynamicResourceCacheV2.DynamicResource getRouteMap(long platformId, boolean vertical, boolean flip, float aspectRatio, boolean transparentWhite) {
        return getResource(String.format(F_RM, platformId, vertical, flip, aspectRatio, transparentWhite), () -> TCMRouteMapGeneratorV2.generateRouteMap(platformId, vertical, flip, aspectRatio, transparentWhite), transparentWhite ? TCMDynamicResourceCacheV2.DefaultRenderingColor.TRANSPARENT : TCMDynamicResourceCacheV2.DefaultRenderingColor.WHITE);
    }

    public byte[] getTextPixels(String text, int[] dimensions, int fontSizeCjk, int fontSize) {
        return getTextPixels(text, dimensions, Integer.MAX_VALUE, (int) (Math.max(fontSizeCjk, fontSize) * LINE_HEIGHT_MULTIPLIER), fontSizeCjk, fontSize, 0, null);
    }

    public byte[] getTextPixels(String text, int[] dimensions, int maxWidth, int maxHeight, int fontSizeCjk, int fontSize, int padding, @Nullable IGui.HorizontalAlignment horizontalAlignment) {
        return getTextPixels(text, dimensions, maxWidth, maxHeight, fontSizeCjk, fontSize, padding, horizontalAlignment, false, TCMRouteMapGeneratorV2.scale);
    }

    public byte[] getTextPixels(String text, int[] dimensions, int maxWidth, int maxHeight, int fontSizeCjk, int fontSize, int padding, @Nullable IGui.HorizontalAlignment horizontalAlignment, boolean hasPrefixNumber, float scale) {
        if (maxWidth <= 0) {
            dimensions[0] = 0;
            dimensions[1] = 0;
            return new byte[0];
        }

        final boolean oneRow = horizontalAlignment == null;
        final String[] defaultTextSplit = IGui.textOrUntitled(text).split("\\|");
        final String[] textSplit;
        if (Config.getClient().getLanguageDisplay() == LanguageDisplay.NORMAL) {
            textSplit = defaultTextSplit;
        } else {
            final String[] tempTextSplit = Arrays.stream(IGui.textOrUntitled(text).split("\\|")).filter(textPart -> IGui.isCjk(textPart) == (Config.getClient().getLanguageDisplay() == LanguageDisplay.CJK_ONLY)).toArray(String[]::new);
            textSplit = tempTextSplit.length == 0 ? defaultTextSplit : tempTextSplit;
        }
        final AttributedString[] attributedStrings = new AttributedString[textSplit.length];
        final int[] textWidths = new int[textSplit.length];
        final int[] fontSizes = new int[textSplit.length];
        final FontRenderContext context = new FontRenderContext(new AffineTransform(), false, false);
        int width = 0;
        int height = 0;

        for (int index = 0; index < textSplit.length; index++) {
            final int newFontSize = IGui.isCjk(textSplit[index]) || isNumber(textSplit[index], !hasPrefixNumber) || font.canDisplayUpTo(textSplit[index]) >= 0 ? (isNumber(textSplit[index], !hasPrefixNumber) ? (fontSizeCjk + fontSize) * 5 / 4 : fontSizeCjk) : fontSize;
            attributedStrings[index] = new AttributedString(textSplit[index]);
            fontSizes[index] = newFontSize;

            final Font fontSized = font.deriveFont(Font.PLAIN, newFontSize);
            final Font fontCjkSized = fontCjk.deriveFont(Font.PLAIN, newFontSize);

            for (int characterIndex = 0; characterIndex < textSplit[index].length(); characterIndex++) {
                final char character = textSplit[index].charAt(characterIndex);
                final Font newFont;
                if (fontSized.canDisplay(character)) {
                    newFont = fontSized;
                } else if (fontCjkSized.canDisplay(character)) {
                    newFont = fontCjkSized;
                } else {
                    Font defaultFont = null;
                    for (final Font testFont : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()) {
                        if (testFont.canDisplay(character)) {
                            defaultFont = testFont;
                            break;
                        }
                    }
                    newFont = (defaultFont == null ? new Font(null) : defaultFont).deriveFont(Font.PLAIN, newFontSize);
                }
                textWidths[index] += newFont.getStringBounds(textSplit[index].substring(characterIndex, characterIndex + 1), context).getBounds().width;
                attributedStrings[index].addAttribute(TextAttribute.FONT, newFont, characterIndex, characterIndex + 1);
            }

            if (oneRow) {
                if (index > 0) {
                    width += padding;
                }
                width += textWidths[index];
                height = Math.max(height, (int) (fontSizes[index] * LINE_HEIGHT_MULTIPLIER));
            } else {
                width = Math.max(width, Math.min(maxWidth, textWidths[index]));
                height += (int) (fontSizes[index] * LINE_HEIGHT_MULTIPLIER);
            }
        }

        int textOffset = 0;
        final int imageHeight = Math.min(height, maxHeight);
        final BufferedImage image = new BufferedImage(width + (oneRow ? 0 : padding * 2), imageHeight + (oneRow ? 0 : padding * 2), BufferedImage.TYPE_BYTE_GRAY);
        final Graphics2D graphics2D = image.createGraphics();
        graphics2D.setColor(Color.WHITE);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        float numberWidth = 0;
        for (int index = 0; index < textSplit.length; index++) {
            if (oneRow) {
                graphics2D.drawString(attributedStrings[index].getIterator(), textOffset, height / LINE_HEIGHT_MULTIPLIER);
                textOffset += textWidths[index] + padding;
            } else {
                final float scaleY = (float) imageHeight / height;
                final float textWidth = Math.min((isNumber(textSplit[index], !hasPrefixNumber) ? (float) scale * 4 / 15 : maxWidth), textWidths[index] * scaleY);
                final float scaleX = textWidth / textWidths[index];
                if (isNumber(textSplit[index], !hasPrefixNumber)) numberWidth = textWidth;
                final AffineTransform stretch = new AffineTransform();
                stretch.concatenate(AffineTransform.getScaleInstance(scaleX, scaleY));
                graphics2D.setTransform(stretch);
                float x =
                        isNumber(textSplit[index], !hasPrefixNumber) ?
                                (((float) scale / 26) +
                                        (float) (textWidths[index] * 16) / 38) / 2 - (float) textWidths[index] / 2 + ((float) scale / 16) :
                                horizontalAlignment.getOffset(0, textWidth - width) / scaleY + padding / scaleX +
                                        (hasPrefixNumber ?
                                                (((float) scale / 26) + (float) numberWidth * 16 / 38) : 0);
                graphics2D.drawString(attributedStrings[index].getIterator(), x, textOffset + fontSizes[index] + padding / scaleY + (isNumber(textSplit[index], !hasPrefixNumber) ? ((fontSizes[0]) * LINE_HEIGHT_MULTIPLIER) - ((float) ((fontSizes[0]) * LINE_HEIGHT_MULTIPLIER) * 16 / 27) : 0));
                textOffset += isNumber(textSplit[index], !hasPrefixNumber) ? (int) (fontSizes[index] * LINE_HEIGHT_MULTIPLIER / ((float) 23 / 12)) : (int) (fontSizes[index] * LINE_HEIGHT_MULTIPLIER * (hasPrefixNumber ? ((float) 11 / 12) : 1));
            }
        }

        dimensions[0] = width + (oneRow ? 0 : padding * 2);
        dimensions[1] = imageHeight + (oneRow ? 0 : padding * 2);
        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        graphics2D.dispose();
        image.flush();
        return pixels;
    }

    private boolean isNumber(String str, boolean ban) {
        return !ban && str.matches("^\\d+$");
    }

    private TCMDynamicResourceCacheV2.DynamicResource getResource(String key, Supplier<NativeImage> supplier, TCMDynamicResourceCacheV2.DefaultRenderingColor defaultRenderingColor) {
        resourceRegistryQueue.process(Runnable::run);
        final TCMDynamicResourceCacheV2.DynamicResource dynamicResource = dynamicResources.get(key);

        if (dynamicResource != null && !dynamicResource.needsRefresh) {
            dynamicResource.expiryTime = System.currentTimeMillis() + COOLDOWN_TIME;
            return dynamicResource;
        }

        if (generatingResources.contains(key)) {
            return defaultRenderingColor.dynamicResource;
        }

        MainRenderer.WORKER_THREAD.scheduleDynamicTextures(() -> {
            while (font == null) {
                ResourceManagerHelper.readResource(new Identifier(Init.MOD_ID, "font/noto-sans-semibold.ttf"), inputStream -> {
                    try {
                        font = Font.createFont(Font.TRUETYPE_FONT, inputStream);
                    } catch (Exception e) {
                        TCM.LOGGER.error("", e);
                    }
                });
            }

            while (fontCjk == null) {
                ResourceManagerHelper.readResource(new Identifier(Init.MOD_ID, "font/noto-serif-cjk-tc-semibold.ttf"), inputStream -> {
                    try {
                        fontCjk = Font.createFont(Font.TRUETYPE_FONT, inputStream);
                    } catch (Exception e) {
                        TCM.LOGGER.error("", e);
                    }
                });
            }

            final NativeImage nativeImage = supplier.get();

            resourceRegistryQueue.put(() -> {
                final TCMDynamicResourceCacheV2.DynamicResource staticTextureProviderOld = dynamicResources.get(key);
                if (staticTextureProviderOld != null) {
                    staticTextureProviderOld.remove();
                    deletedResources.put(staticTextureProviderOld.identifier, System.currentTimeMillis() + COOLDOWN_TIME);
                }

                final TCMDynamicResourceCacheV2.DynamicResource dynamicResourceNew;
                if (nativeImage != null) {
                    final NativeImage newNativeImage;
                    final int newMaxImageSize = MAX_IMAGE_SIZE * (int) Math.pow(2, Config.getClient().getDynamicTextureResolution());
                    if (nativeImage.getWidth() > newMaxImageSize || nativeImage.getHeight() > newMaxImageSize) {
                        newNativeImage = new NativeImage(NativeImageFormat.getAbgrMapped(), Math.min(newMaxImageSize, nativeImage.getWidth()), Math.min(newMaxImageSize, nativeImage.getHeight()), false);
                        for (int x = 0; x < Math.min(newMaxImageSize, nativeImage.getWidth()); x++) {
                            for (int y = 0; y < Math.min(newMaxImageSize, nativeImage.getHeight()); y++) {
                                newNativeImage.setPixelColor(x, y, nativeImage.getColor(x, y));
                            }
                        }
                    } else {
                        newNativeImage = nativeImage;
                    }

                    final NativeImageBackedTexture nativeImageBackedTexture = new NativeImageBackedTexture(newNativeImage);
                    final Identifier identifier = new Identifier(Init.MOD_ID, "id_" + Init.randomString());
                    MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, new AbstractTexture(nativeImageBackedTexture.data));
                    dynamicResourceNew = new TCMDynamicResourceCacheV2.DynamicResource(identifier, nativeImageBackedTexture);
                    dynamicResources.put(key, dynamicResourceNew);
                }

                generatingResources.remove(key);
            });
        });
        TCMRouteMapGeneratorV2.setConstants();
        generatingResources.add(key);

        if (dynamicResource == null) {
            return defaultRenderingColor.dynamicResource;
        } else {
            dynamicResource.expiryTime = System.currentTimeMillis() + COOLDOWN_TIME;
            dynamicResource.needsRefresh = false;
            return dynamicResource;
        }
    }

    public static class DynamicResource {

        private long expiryTime;
        private boolean needsRefresh;
        public final int width;
        public final int height;
        public final Identifier identifier;

        private DynamicResource(Identifier identifier, @Nullable NativeImageBackedTexture nativeImageBackedTexture) {
            this.identifier = identifier;
            if (nativeImageBackedTexture != null) {
                final NativeImage nativeImage = nativeImageBackedTexture.getImage();
                if (nativeImage != null) {
                    width = nativeImage.getWidth();
                    height = nativeImage.getHeight();
                } else {
                    width = 16;
                    height = 16;
                }
            } else {
                width = 16;
                height = 16;
            }
        }

        private void remove() {
            MainRenderer.cancelRender(identifier);
            MoreRenderLayers.removeFromCache(identifier);
        }
    }

    private enum DefaultRenderingColor {
        BLACK(DEFAULT_BLACK_RESOURCE),
        WHITE(DEFAULT_WHITE_RESOURCE),
        TRANSPARENT(DEFAULT_TRANSPARENT_RESOURCE);

        private final TCMDynamicResourceCacheV2.DynamicResource dynamicResource;

        DefaultRenderingColor(Identifier identifier) {
            dynamicResource = new TCMDynamicResourceCacheV2.DynamicResource(identifier, null);
        }
    }
}
