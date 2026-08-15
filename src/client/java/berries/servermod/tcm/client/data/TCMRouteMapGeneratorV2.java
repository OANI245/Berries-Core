package berries.servermod.tcm.client.data;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.UFEInfo;
import berries.servermod.tcm.client.util.TextUtil;
import berries.servermod.tcm.client.util.texture.AWTDrawUtil;
import berries.servermod.tcm.client.util.texture.NativeImageDrawUtil;
import berries.servermod.tcm.util.TCMComponent;
import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;
import org.jetbrains.annotations.Nullable;
import org.mtr.core.data.*;
import org.mtr.core.tool.Utilities;
import org.mtr.libraries.it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import org.mtr.libraries.it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import org.mtr.libraries.it.unimi.dsi.fastutil.ints.IntArrayList;
import org.mtr.libraries.it.unimi.dsi.fastutil.longs.LongArrayList;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.mtr.mapping.holder.*;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mapping.mapper.ResourceManagerHelper;
import org.mtr.mod.Init;
import org.mtr.mod.client.DynamicTextureCache;
import org.mtr.mod.client.IDrawing;
import org.mtr.mod.client.MinecraftClientData;
import org.mtr.mod.config.Config;
import org.mtr.mod.data.IGui;
import org.mtr.mod.screen.EditStationScreen;
import org.spongepowered.asm.mixin.Unique;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.text.AttributedString;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;

import static org.mtr.mod.data.IGui.*;

public class TCMRouteMapGeneratorV2 {

    public static int scale;
    private static int lineSize;
    private static int lineSpacing;
    private static int fontSizeBig;
    private static int fontSizeSmall;

    public static final int PIXEL_SCALE = 4;
    private static final int MIN_VERTICAL_SIZE = 5;
    private static final Font FONT_CJK = AWTDrawUtil.getResourceFont("mtr:font/noto-serif-cjk-tc-semibold.ttf");
    private static final Font FONT_CJK_LIGHT = AWTDrawUtil.getResourceFont("tcm:font/nscl.ttf");
    private static final Font FONT_CJK_EXTRA_LIGHT = AWTDrawUtil.getResourceFont("tcm:font/nscll.ttf");
    private static final Font FONT_ASCII = AWTDrawUtil.getResourceFont("mtr:font/noto-sans-semibold.ttf");
    private static final Font FONT_ASCII_BOLD = AWTDrawUtil.getResourceFont("tcm:font/sab.ttf");
    private static final String LOGO_RESOURCE = "textures/block/sign/logo.png";
    private static final String EXIT_RESOURCE = "textures/block/sign/exit_letter_blank.png";
    private static final String ARROW_RESOURCE = "textures/block/sign/arrow.png";
    private static final String CIRCLE_RESOURCE = "textures/block/sign/circle.png";
    private static final String LINE_NAME_BORDER_RESOURCE = "textures/pattern/line_name_border.png";
    private static final String TRANSFER_RESOURCE = "textures/pattern/transfer.png";
    private static final String TEMP_CIRCULAR_MARKER_CLOCKWISE = String.format("temp_circular_marker_%s_clockwise", Init.randomString());
    private static final String TEMP_CIRCULAR_MARKER_ANTICLOCKWISE = String.format("temp_circular_marker_%s_anticlockwise", Init.randomString());
    private static final String TEMP_CIRCULAR_MARKER = "temp_circular_marker";
    private static final int PIXEL_RESOLUTION = 24;

    private static BufferedImage blackDirectionArrowPatternImage;
    private static BufferedImage lightGrayTransferPatternImage;
    private static BufferedImage blackTransferPatternImage;

    static {
        loadPatternImages();
    }

    public static void loadPatternImages() {
        var a0tt = AWTDrawUtil.getResourceImage("mtr:" + ARROW_RESOURCE);
        blackDirectionArrowPatternImage = new BufferedImage(a0tt.getWidth(), a0tt.getHeight(), BufferedImage.TYPE_INT_ARGB);
        var g1 = blackDirectionArrowPatternImage.createGraphics();
        g1.drawImage(a0tt, 0, 0, a0tt.getWidth(), a0tt.getHeight(), null);
        g1.setComposite(AlphaComposite.SrcIn);
        g1.setColor(Color.BLACK);
        g1.fillRect(0, 0, blackDirectionArrowPatternImage.getWidth(), blackDirectionArrowPatternImage.getHeight());
        g1.dispose();
        blackDirectionArrowPatternImage.flush();

        blackTransferPatternImage = AWTDrawUtil.getResourceImage(UFEInfo.MOD_ID + ":" + TRANSFER_RESOURCE);

        lightGrayTransferPatternImage = new BufferedImage(blackTransferPatternImage.getWidth(), blackTransferPatternImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        var g2 = lightGrayTransferPatternImage.createGraphics();
        g2.drawImage(blackTransferPatternImage, 0, 0, lightGrayTransferPatternImage.getWidth(), lightGrayTransferPatternImage.getHeight(), null);
        g2.setComposite(AlphaComposite.SrcIn);
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(0, 0, lightGrayTransferPatternImage.getWidth(), lightGrayTransferPatternImage.getHeight());
        g2.dispose();
        lightGrayTransferPatternImage.flush();
    }

    public static void setConstants() {
        scale = (int) Math.pow(2, Config.getClient().getDynamicTextureResolution() + 5);
        lineSize = scale / 8;
        lineSpacing = lineSize * 6 / 5;
        fontSizeBig = lineSize * 16 / 9;
        fontSizeSmall = fontSizeBig * 2 / 3;
    }

    public static NativeImage generatePixelatedText(String text, int textColor, int maxWidth, double cjkSizeRatio, boolean fullPixel) {
        try {
            final int scale = fullPixel ? 1 : PIXEL_SCALE;
            final int newMaxWidth = maxWidth / scale;
            final int[] dimensions = new int[2];
            final byte[] pixels = DynamicTextureCache.instance.getTextPixels(text, dimensions, newMaxWidth, Integer.MAX_VALUE, (int) Math.round(PIXEL_RESOLUTION * (cjkSizeRatio > 0 ? cjkSizeRatio + 1 : 1)), (int) Math.round(PIXEL_RESOLUTION * (cjkSizeRatio < 0 ? 1 - cjkSizeRatio : 1)), 0, IGui.HorizontalAlignment.CENTER);
            final int width = Math.min(newMaxWidth, dimensions[0]) * scale;
            final int height = dimensions[1] * scale;

            final NativeImage nativeImage = new NativeImage(NativeImageFormat.getAbgrMapped(), width, height, false);
            nativeImage.fillRect(0, 0, width, height, 0);
            drawStringPixelated(nativeImage, pixels, dimensions, textColor, fullPixel);
            return nativeImage;
        } catch (Exception e) {
            Init.LOGGER.error("", e);
        }

        return null;
    }

    public static NativeImage generateColorStrip(long platformId) {
        try {
            final IntArrayList colors = getRouteStream(platformId, (simplifiedRoute, currentStationIndex) -> {
            });
            if (colors.isEmpty()) {
                final NativeImage nativeImage = new NativeImage(NativeImageFormat.getAbgrMapped(), 1, 1, false);
                nativeImage.setPixelColor(0, 0, 0);
                return nativeImage;
            } else {
                final NativeImage nativeImage = new NativeImage(NativeImageFormat.getAbgrMapped(), 1, colors.size(), false);
                for (int i = 0; i < colors.size(); i++) {
                    drawPixelSafe(nativeImage, 0, i, ARGB_BLACK | colors.getInt(i));
                }
                return nativeImage;
            }
        } catch (Exception e) {
            Init.LOGGER.error("", e);
        }

        return null;
    }

    public static NativeImage generateStationName(String stationName, float aspectRatio) {
        if (aspectRatio <= 0) {
            return null;
        }

        try {
            final int height = scale * 2;
            final int width = Math.round(height * aspectRatio);
            final int padding = scale / 16;
            final int[] dimensions = new int[2];
            final byte[] pixels = DynamicTextureCache.instance.getTextPixels(stationName, dimensions, width - padding * 2, height - padding * 2, fontSizeBig * 2, fontSizeSmall * 2, padding, IGui.HorizontalAlignment.CENTER);

            final NativeImage nativeImage = new NativeImage(NativeImageFormat.getAbgrMapped(), width, height, false);
            nativeImage.fillRect(0, 0, width, height, 0);
            drawString(nativeImage, pixels, width / 2, height / 2, dimensions, IGui.HorizontalAlignment.CENTER, IGui.VerticalAlignment.CENTER, 0, ARGB_WHITE, false);
            return nativeImage;
        } catch (Exception e) {
            Init.LOGGER.error("", e);
        }

        return null;
    }

    public static NativeImage generateTallStationName(int textColor, String stationName, int stationColor, float aspectRatio) {
        if (aspectRatio <= 0) {
            return null;
        }

        try {
            final int width = Math.round(scale * 1.6F);
            final int height = Math.round(width / aspectRatio);
            final int[] dimensions = new int[2];
            final byte[] pixels = DynamicTextureCache.instance.getTextPixels(IGui.formatVerticalChinese(stationName), dimensions, width, height, fontSizeBig * 2, fontSizeSmall * 2, 0, IGui.HorizontalAlignment.CENTER);

            final NativeImage nativeImage = new NativeImage(NativeImageFormat.getAbgrMapped(), width, height, false);
            nativeImage.fillRect(0, 0, width, height, 0);
            drawString(nativeImage, pixels, width / 2, height / 2, dimensions, IGui.HorizontalAlignment.CENTER, IGui.VerticalAlignment.CENTER, ARGB_BLACK | stationColor, textColor, false);
            clearColor(nativeImage, invertColor(ARGB_BLACK | stationColor));
            return nativeImage;
        } catch (Exception e) {
            Init.LOGGER.error("", e);
        }

        return null;
    }

    public static NativeImage generateStationNameEntrance(int textColor, String stationName, float aspectRatio, long stationId, long selectedExit) {
        if (aspectRatio <= 0) {
            return null;
        }

        try {
            var station = MinecraftClientData.getInstance().stationIdMap.get(stationId);
            List<String> lineNames = new ArrayList<>();
            List<Integer> lineColors = new ArrayList<>();
            ObjectObjectMutablePair<String, String> exitZone = new ObjectObjectMutablePair<>(null, null);

            if (station != null) {
                final ObjectArraySet<Station> connectingStationsIncludingThisOne = new ObjectArraySet<>(station.connectedStations);
                connectingStationsIncludingThisOne.add(station);

                final LongAVLTreeSet platformIds = new LongAVLTreeSet();
                connectingStationsIncludingThisOne.forEach(connectingStation -> connectingStation.savedRails.forEach(platform -> platformIds.add(platform.getId())));
                final IntAVLTreeSet addedColors = new IntAVLTreeSet();
                //long hash = MinecraftClientData.getInstance().simplifiedRoutes.hashCode();
                MinecraftClientData.getInstance().simplifiedRoutes
                        .stream().sorted((r1, r2) -> {
                            String s1 = r1.getName();
                            String s2 = r2.getName();
                            if (s1.matches("\\d+.+") && s2.matches("\\d+.+")) {
                                try {
                                    return Integer.decode(
                                            s1.replaceAll(
                                                    s1.replaceFirst("\\d+", "")
                                                    , "")) - Integer.decode(
                                            s2.replaceAll(
                                                    s2.replaceFirst("\\d+", "")
                                                    , ""));
                                } catch (NumberFormatException e) {
                                    return 0;
                                }
                            } else if (s2.matches("\\d+.+")) {
                                return 0;
                            } else if (s1.matches("\\d+.+")) {
                                return 1;
                            }
                            return 0;
                        }).forEach(simplifiedRoute -> {
                            final int rcolor = simplifiedRoute.getColor();
                            if (!addedColors.contains(rcolor) && simplifiedRoute.getPlatforms().stream().anyMatch(simplifiedRoutePlatform -> platformIds.contains(simplifiedRoutePlatform.getPlatformId()))) {
                                lineNames.add(simplifiedRoute.getName());
                                lineColors.add(simplifiedRoute.getColor());
                                addedColors.add(rcolor);
                            }
                        });

                final ObjectArrayList<StationExit> exits = EditStationScreen.getStationExits(station, true);

                Map<Long, String> exitNamesMap = new HashMap<>();
                exits.forEach(stationExit ->
                        exitNamesMap.put(serializeExit(stationExit.getName()), stationExit.getName()));

                if (exitNamesMap.containsKey(selectedExit)) {
                    String exitZoneName = exitNamesMap.get(selectedExit);

                    String left = exitZoneName.replaceAll("\\d+$", ""),
                            right = exitZoneName.replaceAll("^" + left, "");
                    if (!exitZoneName.isEmpty()) {
                        exitZone.left(left);
                        exitZone.right(right);
                    }
                }
            }
            var lineNamesCount = Math.min(lineNames.size(), lineColors.size());

            final int size = scale * 2;
            final int width = Math.round(size * aspectRatio);
            final int padding = scale / 16;
            final int borderWidth = size * 4 / 6;
            final int borderHeight = borderWidth / 2;
            final int tileSize = size - padding * 2;
            final int tilePadding = size / 8;
            //final int[] dimensions = new int[2];
            //final byte[] pixels = DynamicTextureCache.instance.getTextPixels(stationName, dimensions, width - size - padding, size - padding * 2, fontSizeBig * 13 / 4, fontSizeSmall * 5 / 2, padding, IGui.HorizontalAlignment.LEFT);
            //final int xOffset = (size * 6 < width || width <= size * 4) ? (width - dimensions[0] - size) / 2 : size / 2;
            final int fakeBackgroundColor = textColor == ARGB_BLACK ? textColor + 0x010101 : 0;

            final NativeImage nativeImage = new NativeImage(NativeImageFormat.getAbgrMapped(), width, size, false);
            nativeImage.fillRect(0, 0, width, size, fakeBackgroundColor);
            NativeImageDrawUtil.createDrawAndApply(nativeImage, (i, g) -> {
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                var textColorAWT = new Color(textColor);

                var du = new AWTDrawUtil(g);
                //du.drawRect(0, 0, width, size, new Color(fakeBackgroundColor));

                var a0s = Math.min(size * 0.95, width * 0.2);
                var a0y = (size - a0s) / 2.0;
                var a0t = AWTDrawUtil.getResourceImage("mtr:" + LOGO_RESOURCE);
                if (width <= size) {
                    g.drawImage(a0t, (int) 0, (int) 0, (int) width, (int) width, null);
                    return;
                }

                var t0c = TextUtil.getCjkParts(stationName);
                var t0s = size * 0.428;
                var t0y = size * 0.1;
                var t0f = FONT_CJK.deriveFont(Font.PLAIN, (float) t0s);
                g.setFont(t0f);
                var t0m = g.getFontMetrics();
                var t0w = t0m.stringWidth(t0c);

                var t1c = TextUtil.getNonCjkParts(stationName);
                var t1s = size * 0.22;
                var t1f = FONT_ASCII_BOLD.deriveFont(Font.PLAIN, (float) t1s);
                g.setFont(t1f);
                var t1w = g.getFontMetrics().stringWidth(t1c);

                var atg = width * 0.04;
                var ttg = size * 0.05;

                var tmw = Math.min(t0w * 1.645, Math.min(width - atg * 2 - a0s, size * 6.0));
                var t0mw = Math.min(t0w, tmw);
                var t1mw = Math.min(t1w, tmw);

                var t1xs = 1.0;
                var t1t = g.getTransform();
                if (t1w > t1mw) {
                    t1xs = t1mw / t1w;
                    t1t = new AffineTransform();
                    t1t.concatenate(AffineTransform.getScaleInstance(t1xs, 1.0));
                }
                var t0xs = 1.0;
                var t0t = g.getTransform();
                if (t0w > t0mw) {
                    t0xs = t0mw / t0w;
                    t0t = new AffineTransform();
                    t0t.concatenate(AffineTransform.getScaleInstance(t0xs, 1.0));
                }

                var taw = Math.max(t1w * t1xs, t0w * t0xs);
                var gtw = a0s + atg + taw;
                var gax = (width - gtw) / 2;
                var tax = gax + a0s + atg;

                g.drawImage(a0t, (int) gax, (int) a0y, (int) a0s, (int) a0s, null);
                if (t1w > t1mw) du.setTransform(t1t);
                du.drawText(t1c, (Font) null, (float) ((tax + (taw - t1w * t1xs) / 2.0) / t1xs), (float) (t0y + t0s + ttg), textColorAWT, VerticalAlignment.TOP, HorizontalAlignment.LEFT);
                if (t1w > t1mw) du.resetTransform();

                var t0ww = t1w - t1w / 6.0;
                if (t0ww > t0w && t1xs == 1.0 && t0xs == 1.0) {
                    g.setFont(t0f);
                    var t0xl = tax + (taw - t0ww) / 2;
                    var t0cc = t0c.toCharArray();
                    if (t0cc.length == 0) {
                        return;
                    }
                    var t0cw = t0m.stringWidth("口");
                    var t0bw = t0ww - t0cw;
                    var t0cl = t0bw / (t0cc.length - 1);
                    for (int j = 0; j < t0cc.length; j++) {
                        var tjx = t0xl + t0cl * j;
                        du.drawText(String.valueOf(t0cc[j]), (Font) null, (float) tjx, (float) t0y, textColorAWT,
                                VerticalAlignment.TOP, HorizontalAlignment.LEFT);
                    }
                } else {
                    if (t0w > t0mw) du.setTransform(t0t);
                    du.drawText(t0c, t0f, (float) ((tax + (taw - t0w * t0xs) / 2.0) / t0xs), (float) t0y, textColorAWT,
                            VerticalAlignment.TOP, HorizontalAlignment.LEFT);
                    if (t0w > t0mw) du.resetTransform();
                }

                if (width - gtw >= size * 2) {
                    if (exitZone.left() != null && exitZone.right() != null) {
                        var r0s = size * 0.55;
                        var r0x = (size * 1.25 - r0s) / 2;
                        var r0y = (size - r0s) / 2;
                        du.drawBorderedRadiusRect((int) r0x, (int) r0y, (int) r0s, (int) r0s, new Color(0, 0, 0, 0), textColorAWT, size * 0.14, (int) (size * 0.032));

                        var teaw = 0.0;
                        var t2f = FONT_ASCII.deriveFont(Font.PLAIN, (float) (size * 0.45));
                        var t3f = FONT_ASCII_BOLD.deriveFont(Font.PLAIN, (float) (size * 0.28));
                        var t3x = 0.0;
                        g.setFont(t2f);
                        teaw += g.getFontMetrics().stringWidth(exitZone.left());
                        var hasNumber = exitZone.right() != null && !(exitZone.right().isEmpty());
                        if (hasNumber) {
                            g.setFont(t3f);
                            var t2t3g = size * 0.012;
                            t3x = teaw + t2t3g;
                            teaw += g.getFontMetrics().stringWidth(exitZone.right()) + t2t3g;
                        }

                        var teax = r0x + (r0s - teaw) / 2;
                        var teay = r0y + r0s / 2 - size * 0.07 + t2f.getSize() / 2.0;
                        du.drawText(exitZone.left(), (Font) t2f, (float) teax, (float) teay, textColorAWT, VerticalAlignment.BOTTOM, HorizontalAlignment.LEFT);
                        if (hasNumber)
                            du.drawText(exitZone.right(), t3f, (float) (teax + t3x), (float) teay, textColorAWT, VerticalAlignment.BOTTOM, HorizontalAlignment.LEFT);
                    }

                    if (lineNamesCount > 0 && width - gtw >= size * 3) {
                        if ((width - gtw) / 2 <= size * 1.0 * lineNamesCount && lineNamesCount > 1) {
                            var rhh = size * 1.0 / lineNamesCount;
                            var thf = FONT_CJK.deriveFont(Font.PLAIN, (float) Math.min(size * 0.25, rhh * 0.8));

                            var rhx = (int) (width - size * 1.2);
                            var rhw = (int) (size * 1.2);

                            for (int j = 0; j < lineNamesCount; j++) {
                                var rhjy = (int) (rhh * j);
                                var rhjc = lineColors.get(j);
                                du.drawRect(rhx, rhjy, rhw, (int) rhh, new Color(rhjc));
                                du.drawText(TextUtil.getCjkParts(lineNames.get(j)), thf, (float) (rhx + rhw / 2.0), (float) (rhjy + rhh / 2.0), getColorLightNess(rhjc) <= 0.67f ? Color.WHITE : Color.BLACK, VerticalAlignment.CENTER, HorizontalAlignment.CENTER);
                            }
                        } else {
                            var rrp = size * 0.25;
                            var rhcs = size * 4 / 9;
                            var rhw = rhcs * 2.12;
                            var rhy = size / 2;

                            for (int j = lineNamesCount - 1; j >= 0; j--) {
                                drawRouteName(du, (int) (width - rrp - (rhw * j) - rhw / 2), (int) rhy, rhcs, lineNames.get(j), new Color(lineColors.get(j)), -1, false, VerticalAlignment.CENTER, HorizontalAlignment.CENTER);
                            }
                        }
                    }
                }
            });
            /*nativeImage.fillRect(0, 0, width, size, fakeBackgroundColor);
            drawResource(nativeImage, LOGO_RESOURCE, xOffset, 0, size, size, false, 0, 1, 0, true);
            drawString(nativeImage, pixels, size + xOffset, size / 2, dimensions, IGui.HorizontalAlignment.LEFT, IGui.VerticalAlignment.CENTER, fakeBackgroundColor, textColor, false);

            //TCM 编写部分
            if (width > size * 4) {
                //当存在出口编号时（可显示字母）
                if (exitZone.length >= 2 && exitZone[0] != null && !(exitZone[0].isEmpty())) {
                    int x = width > size * 6 ? size / 2 : width - size / 2 - (borderWidth + size / 8) * (lineNamesCount + 1);
                    int y = size / 2;
                    final int[] dimensionsExitZoneLeft = new int[2];
                    final byte[] pixelsExitZone = DynamicTextureCache.instance.getTextPixels(exitZone[0], dimensionsExitZoneLeft, Integer.MAX_VALUE, (int) (Math.max(0, lineSize * 10) * TCMDynamicResourceCacheV2.LINE_HEIGHT_MULTIPLIER), 0, lineSize * 10, 0, HorizontalAlignment.LEFT);
                    drawString(nativeImage, pixelsExitZone, x, y, dimensionsExitZoneLeft, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, fakeBackgroundColor, textColor, false);
                    //当存在数字时（显示下标小数字）
                    if (exitZone[1] != null && !(exitZone[1].isEmpty())) {
                        int x1 = x + dimensionsExitZoneLeft[0] + size / 20;
                        int y1 = size * 6 / 9;
                        final int[] dimensionsExitZoneRight = new int[2];
                        final byte[] pixelsExitZoneRight = DynamicTextureCache.instance.getTextPixels(exitZone[1], dimensionsExitZoneRight, Integer.MAX_VALUE, (int) (Math.max(0, lineSize * 5) * TCMDynamicResourceCacheV2.LINE_HEIGHT_MULTIPLIER), 0, lineSize * 4, 0, HorizontalAlignment.LEFT);
                        drawString(nativeImage, pixelsExitZoneRight, x1, y1, dimensionsExitZoneRight, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, fakeBackgroundColor, textColor, false);
                    }
                }

                //遍历线路列表
                for (int i = 0; i < lineNamesCount; i++) {
                    int x = width - size / 2 - (borderWidth + size / 8) * (lineNamesCount - i);
                    int y = size / 2;

                    String lineName = lineNames[i];
                    lineName = lineName.replaceAll("\\|\\|.+", "");
                    String[] lineNameSplit = lineName.trim().split("\\|");
                    //使用正则表达式分离中文和数字
                    //仅限CJK文字，并未测试当非CJK时存在的问题，不过天城应该不会有纯英文和数字的线路，懒得修了（）
                    //用于显示下沉数字（X号线中的X更大且下沉）
                    StringBuilder lnb = new StringBuilder();
                    boolean matchesResult = false;
                    for (int j = 0; j < lineNameSplit.length; j++) {
                        String s = lineNameSplit[j];
                        String news;
                        boolean hasDownNumber = Pattern.matches("^\\d+(.线)|号线", s);
                        matchesResult = hasDownNumber || matchesResult;
                        if (IGui.isCjk(s) && hasDownNumber) {
                            String ends = s.replaceAll("^\\d+", "");
                            String lineNumber = s.replaceAll(String.format("%s", ends), "");
                            news = lineNumber + '|' + ends;
                        } else {
                            news = s;
                        }
                        lnb.append(news);
                        if (j < (lineNameSplit.length - 1)) lnb.append('|');
                    }
                    lineName = lnb.toString();

                    final int[] dimensionsLineName = new int[2];
                    final byte[] pixelsLineName = TCMDynamicResourceCacheV2.instance.getTextPixels(lineName, dimensionsLineName, borderWidth, (int) (tileSize * TCMDynamicResourceCacheV2.LINE_HEIGHT_MULTIPLIER), tileSize * 8 / 46, tileSize * 4 / 46, tilePadding, IGui.HorizontalAlignment.CENTER, matchesResult, size);

                    drawResource(nativeImage, UFEInfo.MOD_ID, LINE_NAME_BORDER_RESOURCE, x, y - borderHeight / 2, borderWidth, borderHeight, false, 0, 1, lineColors[i], false);
                    drawString(nativeImage, pixelsLineName, x + borderWidth / 2, y, dimensionsLineName, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, 0, getColorLightNess(lineColors[i]) <= 0.67f ? ARGB_WHITE : ARGB_BLACK, false);
                }
            }*/

            clearColor(nativeImage, invertColor(fakeBackgroundColor));

            return nativeImage;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static NativeImage generateSingleRowStationName(long platformId, float aspectRatio) {
        if (aspectRatio <= 0) {
            return null;
        }

        try {
            final int[] dimensions = new int[2];
            final byte[] pixels = DynamicTextureCache.instance.getTextPixels(getStationName(platformId).replace("|", " | "), dimensions, fontSizeBig, fontSizeSmall);
            final int padding = dimensions[1] / 2;
            final int height = dimensions[1] + padding;
            final int width = Math.max(Math.round(height * aspectRatio), dimensions[0] + padding);

            final NativeImage nativeImage = new NativeImage(NativeImageFormat.getAbgrMapped(), width, height, false);
            nativeImage.fillRect(0, 0, width, height, ARGB_WHITE);
            drawString(nativeImage, pixels, width / 2, height / 2, dimensions, IGui.HorizontalAlignment.CENTER, IGui.VerticalAlignment.CENTER, 0, ARGB_BLACK, false);
            return nativeImage;
        } catch (Exception e) {
            Init.LOGGER.error("", e);
        }

        return null;
    }

    public static NativeImage generateSignText(String text, IGui.HorizontalAlignment horizontalAlignment, float paddingScale, int backgroundColor, int textColor) {
        try {
            final int height = scale;
            final int padding = Math.round(height * paddingScale);
            final int tileSize = height - padding * 2;
            final int tilePadding = tileSize / 4;

            final int[] dimensions = new int[2];
            final byte[] pixels = DynamicTextureCache.instance.getTextPixels(text, dimensions, Integer.MAX_VALUE, (int) (tileSize * DynamicTextureCache.LINE_HEIGHT_MULTIPLIER), tileSize * 3 / 5, tileSize * 3 / 10, tilePadding, horizontalAlignment);
            final int width = dimensions[0] - tilePadding * 2;

            if (width <= 0 || height <= 0) {
                return null;
            }

            final NativeImage nativeImage = new NativeImage(NativeImageFormat.getAbgrMapped(), width, height, false);
            nativeImage.fillRect(0, 0, width, height, 0);
            drawString(nativeImage, pixels, width / 2, height / 2, dimensions, IGui.HorizontalAlignment.CENTER, IGui.VerticalAlignment.CENTER, backgroundColor, textColor, false);
            clearColor(nativeImage, invertColor(backgroundColor));

            return nativeImage;
        } catch (Exception e) {
            Init.LOGGER.error("", e);
        }

        return null;
    }

    public static NativeImage generateLiftPanel(String text, int textColor) {
        try {
            final int width = Math.round(scale * 1.5F);
            final int height = fontSizeSmall * 2 * text.split("\\|").length;
            final int[] dimensions = new int[2];
            final byte[] pixels = DynamicTextureCache.instance.getTextPixels(text.toUpperCase(Locale.ENGLISH), dimensions, width, height, fontSizeSmall * 2, fontSizeSmall * 2, 0, IGui.HorizontalAlignment.CENTER);
            final NativeImage nativeImage = new NativeImage(NativeImageFormat.getAbgrMapped(), width, height, false);
            nativeImage.fillRect(0, 0, width, height, 0);
            drawString(nativeImage, pixels, width / 2, height / 2, dimensions, IGui.HorizontalAlignment.CENTER, IGui.VerticalAlignment.CENTER, ARGB_BLACK, textColor, false);
            clearColor(nativeImage, invertColor(ARGB_BLACK));

            return nativeImage;
        } catch (Exception e) {
            Init.LOGGER.error("", e);
        }

        return null;
    }

    public static NativeImage generateExitSignLetter(String exitLetter, String exitNumber, int backgroundColor) {
        try {
            final int size = scale / 2;
            final boolean noNumber = exitNumber.isEmpty();
            final int textSize = size * 7 / 8;
            final int[] dimensions1 = new int[2];
            final byte[] pixels1 = DynamicTextureCache.instance.getTextPixels(exitLetter, dimensions1, noNumber ? textSize : textSize * 2 / 3, textSize, textSize, size, size, IGui.HorizontalAlignment.CENTER);
            final int[] dimensions2 = new int[2];
            final byte[] pixels2 = noNumber ? null : DynamicTextureCache.instance.getTextPixels(exitNumber, dimensions2, textSize / 3, textSize, textSize / 2, textSize / 2, size, IGui.HorizontalAlignment.CENTER);

            final NativeImage nativeImage = new NativeImage(NativeImageFormat.getAbgrMapped(), size, size, false);
            nativeImage.fillRect(0, 0, size, size, backgroundColor);
            drawResource(nativeImage, EXIT_RESOURCE, 0, 0, size, size, false, 0, 1, 0, true);
            drawString(nativeImage, pixels1, size / 2 - (noNumber ? 0 : textSize / 6 - size / 32), size / 2, dimensions1, IGui.HorizontalAlignment.CENTER, IGui.VerticalAlignment.CENTER, 0, ARGB_WHITE, false);
            if (!noNumber) {
                drawString(nativeImage, pixels2, size / 2 + textSize / 3 - size / 32, size / 2 + textSize / 8, dimensions2, IGui.HorizontalAlignment.CENTER, IGui.VerticalAlignment.CENTER, 0, ARGB_WHITE, false);
            }
            return nativeImage;
        } catch (Exception e) {
            Init.LOGGER.error("", e);
        }

        return null;
    }

    public static NativeImage generateRouteSquare(int color, String routeName, IGui.HorizontalAlignment horizontalAlignment) {
        try {
            final int padding = scale / 32;
            final int[] dimensions = new int[2];
            final byte[] pixels = DynamicTextureCache.instance.getTextPixels(routeName, dimensions, Integer.MAX_VALUE, (int) ((fontSizeBig + fontSizeSmall) * DynamicTextureCache.LINE_HEIGHT_MULTIPLIER), fontSizeBig, fontSizeSmall, padding, horizontalAlignment);

            final int width = dimensions[0] + padding * 2;
            final int height = dimensions[1] + padding * 2;
            final NativeImage nativeImage = new NativeImage(NativeImageFormat.getAbgrMapped(), width, height, false);
            nativeImage.fillRect(0, 0, width, height, invertColor(ARGB_BLACK | color));
            drawString(nativeImage, pixels, width / 2, height / 2, dimensions, IGui.HorizontalAlignment.CENTER, IGui.VerticalAlignment.CENTER, 0, ARGB_WHITE, false);
            return nativeImage;
        } catch (Exception e) {
            Init.LOGGER.error("", e);
        }

        return null;
    }

    public static NativeImage generatePSDTopStationName(long platformId, String stationName, boolean showToString, float paddingScale, float aspectRatio, int backgroundColor, int textColor, int transparentColor, int val) {
        if (aspectRatio <= 0) {
            return null;
        }

        try {
            if (stationName.isEmpty()) {
                stationName = (new String[]{"四惠|Sihui", "四惠东|Sihuidong", "天宫院|Tiangongyuan", "高碑店|Gaobeidian", "金安桥|Jin'anqiao", "七里庄|Qilizhuang", "东坝|Dongba", "蓟门桥|Jimenqiao", "东四十条|Dongsishitiao", "丽泽商务区|Lize Shangwuqu", "······", "······", "······", "······", "······", "······"})[(int) (Math.random() * 10)];
            }

            List<String> destinations = new ArrayList<>();
            List<String> lineNames = new ArrayList<>();
            List<Integer> colors = getRouteStream(platformId, (simplifiedRoute, currentStationIndex) -> {
                destinations.add(simplifiedRoute.getPlatforms().get(currentStationIndex).getDestination());
                lineNames.add(simplifiedRoute.getName());
            });
            boolean isTerminating = destinations.isEmpty();

            int height = scale;
            int width = Math.round(height * aspectRatio);
            int padding = Math.round(height * paddingScale);
            int tileSize = height - padding * 2;

            if (width <= 0 || height <= 0) {
                return null;
            }

            NativeImage nativeImage = new NativeImage(NativeImageFormat.getAbgrMapped(), width, height, false);
            String finalStationName = stationName.replaceAll("(?i)First", "1st")
                    .replaceAll("(?i)Second", "2nd")
                    .replaceAll("(?i)Third", "3rd")
                    .replaceAll("(?i)Forth", "4th")
                    .replaceAll("(?i)Fifth", "5th")
                    .replaceAll("(?i)Sixth", "6th")
                    .replaceAll("(?i)Seventh", "7th")
                    .replaceAll("(?i)Eighth", "8th")
                    .replaceAll("(?i)Ninth", "9th")
                    .replaceAll("(?i)Tenth", "10th")
                    .replaceAll("(?i)University", "Univ.")
                    .replaceAll("(?i)Company", "Co.")
                    .replaceAll("(?i)Department", "Dept.");
            NativeImageDrawUtil.createDrawAndApply(nativeImage, (i, g) -> {
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                var du = new AWTDrawUtil(g);
                du.drawRect(0, 0, width, height, new Color(backgroundColor));
                g.setFont(autoFontSize(FONT_CJK_LIGHT, 33));
                var t0c = TextUtil.getCjkParts(finalStationName);
                var t0w = g.getFontMetrics().stringWidth(t0c);
                var tmw = g.getFontMetrics().stringWidth("口口口口口口");
                var t0sx = (double) Math.min(tmw, t0w) / t0w;
                AffineTransform t0 = new AffineTransform();
                t0.concatenate(AffineTransform.getScaleInstance(t0sx, 1.0));
                du.setTransform(t0);
                du.drawText(t0c, (Font) null, (float) (width / 2.0f / t0sx), height / 2.0f * 1.02f, Color.BLACK, VerticalAlignment.BOTTOM, HorizontalAlignment.CENTER);
                g.setFont(autoFontSize(FONT_ASCII_BOLD, 18));
                var t1c = TextUtil.getNonCjkParts(finalStationName);
                var t1w = g.getFontMetrics().stringWidth(t1c);
                var t1sx = (double) Math.min(tmw, t1w) / t1w;
                AffineTransform t1 = new AffineTransform();
                t1.concatenate(AffineTransform.getScaleInstance(t1sx, 1.0));
                du.setTransform(t1);
                du.drawText(t1c, (Font) null, (float) (width / 2.0f / t1sx), height / 2.0f * 1.16f, Color.BLACK, VerticalAlignment.TOP, HorizontalAlignment.CENTER);
                du.resetTransform();

                int y = height / 2;

                if (!lineNames.isEmpty() && !colors.isEmpty()) {
                    String ln0;
                    int li0;
                    String ln1;
                    int li1;

                    if (lineNames.size() == 1 || colors.size() == 1 || lineNames.get(1).isEmpty()) {
                        ln0 = lineNames.getFirst();
                        li0 = 0;
                        ln1 = ln0;
                        li1 = 0;
                    } else if (lineNames.get(0).isEmpty()) {
                        li0 = 1;
                        ln0 = lineNames.get(li0);
                        ln1 = ln0;
                        li1 = 1;
                    } else {
                        if (lineNames.getFirst().compareTo(lineNames.get(1)) <= 0) {
                            ln0 = lineNames.getFirst();
                            li0 = 0;
                            li1 = 1;
                            ln1 = lineNames.get(li1);
                        } else {
                            li0 = 1;
                            ln0 = lineNames.get(li0);
                            ln1 = lineNames.getFirst();
                            li1 = 0;
                        }
                    }

                    drawRouteName(du, width * 8 / 51, y, scale * 4 / 12, ln0, new Color(colors.get(li0)), val, false, VerticalAlignment.CENTER, HorizontalAlignment.CENTER);
                    drawRouteName(du, width - width * 8 / 51, y, scale * 4 / 12, ln1, new Color(colors.get(li1)), val, false, VerticalAlignment.CENTER, HorizontalAlignment.CENTER);
                }
            });


            if (transparentColor != 0) {
                clearColor(nativeImage, invertColor(transparentColor));
            }

            return nativeImage;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static NativeImage generateStationsNameInfo(long platformId, String stationName, float aspectRatio, int backgroundColor, int textColor) {
        if (aspectRatio <= 0) {
            return null;
        }

        try {
            List<String> nextStations = new ArrayList<>();
            List<Integer> colors = getRouteStream(platformId, (sr, csi) -> {
                if (csi < sr.getPlatforms().size() - 1) {
                    nextStations.add(sr.getPlatforms().get(sr.getPlatformIndex(platformId) + 1).getStationName());
                }
            });
            boolean isTerminating = nextStations.isEmpty();
            int height = scale * 2;
            int width = Math.round(height * aspectRatio);

            if (width <= 0 || height <= 0) {
                return null;
            }

            var finalStationName = stationName.replaceAll("(?i)First", "1st")
                    .replaceAll("(?i)Second", "2nd")
                    .replaceAll("(?i)Third", "3rd")
                    .replaceAll("(?i)Forth", "4th")
                    .replaceAll("(?i)Fifth", "5th")
                    .replaceAll("(?i)Sixth", "6th")
                    .replaceAll("(?i)Seventh", "7th")
                    .replaceAll("(?i)Eighth", "8th")
                    .replaceAll("(?i)Ninth", "9th")
                    .replaceAll("(?i)Tenth", "10th")
                    .replaceAll("(?i)University", "Univ.")
                    .replaceAll("(?i)Company", "Co.")
                    .replaceAll("(?i)Department", "Dept.");

            var tmw = width * 0.87;

            final NativeImage nativeImage = new NativeImage(NativeImageFormat.RGBA, width, height, false);
            NativeImageDrawUtil.createDrawAndApply(nativeImage, (i, g) -> {
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                var du = new AWTDrawUtil(g);
                du.drawRect(0, 0, width, height, new Color(backgroundColor));

                if (stationName.isEmpty()) {
                    return;
                }

                var r0h = height / 12.5;
                var r0y = height / 3.0 * 2.0 - r0h;

                if (platformId == 0 || colors.isEmpty()) {
                    du.drawRect(0, (int) r0y, width, (int) r0h, Color.BLACK);
                } else {
                    var r0sh = r0h / colors.size();
                    for (int j = 0; j < colors.size(); j++) {
                        int color = colors.get(j);
                        var r0sy = r0y + r0sh * j;
                        du.drawRect(0, (int) r0sy, width, (int) r0sh, new Color(color));
                    }
                }

                var textColorAWT = new Color(textColor);

                var t1y = height / 2.738;
                var t1s = height / 8.0;
                var t1c = TextUtil.getNonCjkParts(finalStationName);
                var t1f = FONT_ASCII_BOLD.deriveFont(Font.PLAIN, (float) t1s);
                g.setFont(t1f);
                var t1w = g.getFontMetrics().stringWidth(t1c);
                var t1sw = Math.min(tmw, t1w);
                var t1xs = t1sw / t1w;
                var t1t = g.getTransform();
                var t1hs = t1sw != t1w;
                if (t1hs) {
                    t1t = new AffineTransform();
                    t1t.concatenate(AffineTransform.getScaleInstance(t1xs, 1.0));
                }
                du.setTransform(t1t);
                du.drawText(t1c, null, (float) (((double) width / 2) / t1xs), (float) t1y, textColorAWT, VerticalAlignment.TOP, HorizontalAlignment.CENTER);
                du.resetTransform();

                var t0y = height / 11.75;
                var t0s = height / 4.25;
                var t0c = TextUtil.getCjkParts(finalStationName);
                var t0f = FONT_CJK.deriveFont(Font.PLAIN, (float) t0s);
                g.setFont(t0f);
                var t0w = g.getFontMetrics().stringWidth(t0c);
                var t0sw = Math.min(tmw, t0w);
                var t0g = Math.min(width / 40.0, (width / 10.0 * 7 - t0w) * 0.15);
                var t0hs = t0sw != t0w;
                if (t0g <= 0 || t0hs) {
                    var t0xs = t0sw / t0w;
                    var t0t = g.getTransform();
                    if (t0hs) {
                        t0t = new AffineTransform();
                        t0t.concatenate(AffineTransform.getScaleInstance(t0xs, 1.0));
                    }
                    du.setTransform(t0t);
                    du.drawText(t0c, (Font) null, (float) (((double) width / 2) / t0xs), (float) t0y, textColorAWT, VerticalAlignment.TOP, HorizontalAlignment.CENTER);
                    du.resetTransform();
                } else {
                    var t0ca = new ArrayList<Character>();
                    for (char c : t0c.toCharArray()) {
                        t0ca.add(c);
                    }
                    var t0fm = g.getFontMetrics();
                    var t0cw = t0ca.stream().map((v) -> t0fm.charWidth(v) + t0g).toList();
                    double[] t0aw = new double[]{0.0};
                    t0cw.forEach((v) -> t0aw[0] += v);
                    t0aw[0] -= t0g;
                    var t0ax = width / 2.0 - t0aw[0] / 2;
                    var t0lx = 0.0;
                    for (char t0jc : t0ca) {
                        var t0jx = t0ax + t0lx;
                        du.drawText(String.valueOf(t0jc), (Font) null, (float) t0jx, (float) t0y, textColorAWT, VerticalAlignment.TOP, HorizontalAlignment.LEFT);
                        t0lx += t0fm.charWidth(t0jc) + t0g;
                    }
                }

                if (!isTerminating) {
                    var t3x = width / 7.66;
                    var t3y = height - height / 4.34;
                    var t3s = height / 11.5;
                    du.drawText("下一站", FONT_CJK_LIGHT.deriveFont(Font.PLAIN, (float) t3s), (float) t3x, (float) t3y, textColorAWT, VerticalAlignment.TOP, HorizontalAlignment.LEFT);
                    var t4y = t3y + t3s / 5 * 6;
                    var t4s = t3s * 0.575;
                    du.drawText("Next Station", FONT_ASCII_BOLD.deriveFont(Font.PLAIN, (float) t4s), (float) t3x, (float) t4y, textColorAWT, VerticalAlignment.TOP, HorizontalAlignment.LEFT);

                    var tnsmw = width * 0.5;

                    var t5s = (t0s / 1.95) / Math.max(1, nextStations.size());
                    var t5y = height - height / 3.5;
                    var t5mh = height / 4.0 / nextStations.size();
                    var t5f = FONT_CJK.deriveFont(Font.PLAIN, (float) t5s);
                    var t5fm = g.getFontMetrics(t5f);

                    var t6x = width - width / Math.PI;
                    var t6s = t5s * 0.58;
                    var t6y = t5y + t5s * 1.2;
                    var t6f = FONT_ASCII_BOLD.deriveFont(Font.PLAIN, (float) t6s);
                    var t6fm = g.getFontMetrics(t6f);

                    for (int j = 0; j < nextStations.size(); j++) {
                        var nextStation = nextStations.get(j);
                        var t5jc = TextUtil.getCjkParts(nextStation);
                        var t6jc = TextUtil.getNonCjkParts(nextStation);
                        var t5jw = t5fm.stringWidth(t5jc);
                        var t5jrw = Math.min(tnsmw, t5jw);
                        var t5jxs = t5jrw / t5jw;
                        var t6jw = t6fm.stringWidth(t6jc);
                        var t6jrw = Math.min(tnsmw, t6jw);
                        var t6jxs = t6jrw / t6jw;
                        var t6rx = t6x + Math.max(t6jrw, t5jrw) * 0.47;
                        var t5jx = t6rx - t6jrw / 2.0 - t5jrw / 2.0;
                        var t6jx = t6rx - t6jrw;
                        var t5jy = t5y + t5mh * j;
                        var t6jy = t6y + t5mh * j;

                        var t5t = g.getTransform();
                        var t6t = g.getTransform();

                        if (t5jxs < 1.0) {
                            t5t = new AffineTransform();
                            t5t.concatenate(AffineTransform.getScaleInstance(t5jxs, 1.0));
                        }
                        du.setTransform(t5t);
                        du.drawText(t5jc, t5f, (float) (t5jx / t5jxs), (float) t5jy, textColorAWT, VerticalAlignment.TOP, HorizontalAlignment.LEFT);
                        du.resetTransform();

                        if (t6jxs < 1.0) {
                            t6t = new AffineTransform();
                            t6t.concatenate(AffineTransform.getScaleInstance(t6jxs, 1.0));
                        }
                        du.setTransform(t6t);
                        du.drawText(t6jc, t6f, (float) (t6jx / t6jxs), (float) t6jy, textColorAWT, VerticalAlignment.TOP, HorizontalAlignment.LEFT);
                        du.resetTransform();
                    }
                } else {
                    du.drawText("终点站", FONT_CJK.deriveFont(Font.PLAIN, (float) (t0s / 1.95)), (float) (width / 2), (float) (height - height / 3.5), textColorAWT, VerticalAlignment.TOP, HorizontalAlignment.CENTER);
                    du.drawText("Terminus", FONT_ASCII_BOLD.deriveFont(Font.PLAIN, (float) (t0s / 1.95 * 0.58)), (float) (width / 2), (float) ((height - height / 3.5) + (t0s / 1.95) * 1.2), textColorAWT, VerticalAlignment.TOP, HorizontalAlignment.CENTER);
                }
            });
            return nativeImage;
        } catch (Exception e) {
            TCM.LOGGER.error("Cannot to draw Stations Name Info: ", e);
        }

        return null;
    }

    public static NativeImage generateDirectionArrow(long platformId, boolean hasLeft, boolean hasRight, IGui.HorizontalAlignment horizontalAlignment, boolean showToString, float paddingScale, float aspectRatio, int backgroundColor, int textColor, int transparentColor, int val) {
        if (aspectRatio <= 0) {
            return null;
        }

        try {
            final ObjectArrayList<ObjectIntImmutablePair<SimplifiedRoute>> routeDetails = new ObjectArrayList<>();
            final ObjectArrayList<ObjectIntImmutablePair<String>> destinations = new ObjectArrayList<>();
            final List<Integer> colors = getRouteStream(platformId, (simplifiedRoute, currentStationIndex) -> {
                final String tempMarker = switch (simplifiedRoute.getCircularState()) {
                    case CLOCKWISE -> TEMP_CIRCULAR_MARKER_CLOCKWISE;
                    case ANTICLOCKWISE -> TEMP_CIRCULAR_MARKER_ANTICLOCKWISE;
                    default -> "";
                };
                var dest = simplifiedRoute.getPlatforms().get(currentStationIndex).getDestination();
                if (currentStationIndex < simplifiedRoute.getPlatforms().size() - 1) {
                    routeDetails.add(new ObjectIntImmutablePair<>(simplifiedRoute, currentStationIndex));
                    destinations.add(new ObjectIntImmutablePair<>(tempMarker + dest
                            .replaceAll("(?i)First", "1st")
                            .replaceAll("(?i)Second", "2nd")
                            .replaceAll("(?i)Third", "3rd")
                            .replaceAll("(?i)Forth", "4th")
                            .replaceAll("(?i)Fifth", "5th")
                            .replaceAll("(?i)Sixth", "6th")
                            .replaceAll("(?i)Seventh", "7th")
                            .replaceAll("(?i)Eighth", "8th")
                            .replaceAll("(?i)Ninth", "9th")
                            .replaceAll("(?i)Tenth", "10th")
                            .replaceAll("(?i)University", "Univ.")
                            .replaceAll("(?i)Company", "Co.")
                            .replaceAll("(?i)Department", "Dept."), simplifiedRoute.getPlatforms().size()));
                }
            });
            routeDetails.sort(Comparator.<ObjectIntImmutablePair<SimplifiedRoute>>comparingInt(l -> l.left().getPlatforms().size()).reversed());
            destinations.sort(Comparator.<ObjectIntImmutablePair<String>>comparingInt(ObjectIntImmutablePair::rightInt).reversed());

            int routeSize = Math.min(destinations.size(), routeDetails.size());
            for (int i = 1; i < routeSize; i++) {
                for (int j = 0; j < i; j++) {
                    int finalI = i;
                    if (routeDetails.get(j).left().getPlatforms().stream().anyMatch((p) -> p.getStationName().trim().equals(destinations.get(finalI).left().trim()))) {
                        destinations.set(i, null);
                        routeDetails.set(i, null);
                        break;
                    }
                }
            }

            int ci = 0;
            boolean removeStop = ci >= Math.min(destinations.size(), routeDetails.size());
            while (!removeStop) {
                if (destinations.get(ci) == null || routeDetails.get(ci) == null) {
                    destinations.remove(ci);
                    routeDetails.remove(ci);
                } else {
                    ci++;
                }
                removeStop = ci >= Math.min(destinations.size(), routeDetails.size());
            }

            boolean isTerminating = destinations.isEmpty();

            boolean leftToRight = horizontalAlignment == HorizontalAlignment.CENTER ? hasLeft || !hasRight : horizontalAlignment != HorizontalAlignment.RIGHT;
            int height = scale;
            int width = Math.round(height * aspectRatio);

            if (width <= 0 || height <= 0) {
                return null;
            }

            final TCMDynamicResourceCacheV2 clientCache = TCMDynamicResourceCacheV2.instance;
            final NativeImage nativeImage = new NativeImage(NativeImageFormat.RGBA, width, height, false);
            NativeImageDrawUtil.createDrawAndApply(nativeImage, (i, g) -> {
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                var du = new AWTDrawUtil(g);
                du.drawRect(0, 0, width, height, new Color(backgroundColor));


                if (isTerminating) {
                    du.drawText("终点站", (Font) autoFontSize(FONT_CJK_LIGHT, 33), (float) (width / 2.0f), height / 2.0f * 1.02f, Color.BLACK, VerticalAlignment.BOTTOM, HorizontalAlignment.CENTER);
                    du.drawText("Terminus", (Font) autoFontSize(FONT_ASCII_BOLD, 18), (float) (width / 2.0f), height / 2.0f * 1.16f, Color.BLACK, VerticalAlignment.TOP, HorizontalAlignment.CENTER);
                } else {
                    String destinationString = IGui.mergeStations(destinations.stream().map(ObjectIntImmutablePair::left).toList());
                    boolean noToString = destinationString.startsWith(TEMP_CIRCULAR_MARKER);
                    destinationString = destinationString.replace(TEMP_CIRCULAR_MARKER, "");
                    if (!destinationString.isEmpty() && showToString && !noToString) {
                        destinationString = insertTranslation("gui.tcm.to_cjk", "gui.tcm.to", null, 1, destinationString);
                    }

                    var a0s = height * 0.60;

                    var t0c = TextUtil.getCjkParts(destinationString);
                    var as0 = new AttributedString(t0c);
                    var t0l = t0c.length();
                    as0.addAttribute(TextAttribute.FONT, autoFontSize(FONT_CJK_LIGHT, 33), 0, t0l);
                    var t1c = TextUtil.getNonCjkParts(destinationString);
                    var as1 = new AttributedString(t1c);
                    var t1l = t1c.length();
                    as1.addAttribute(TextAttribute.FONT, autoFontSize(FONT_ASCII_BOLD, 18), 0, t1l);

                    if (val == 3) {
                        as0.addAttribute(TextAttribute.FONT, autoFontSize(FONT_CJK_EXTRA_LIGHT, 33), 0, 2);
                    }
                    var t0a = leftToRight ? (((!hasLeft && !hasRight) || (hasLeft && hasRight)) ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT) : HorizontalAlignment.RIGHT;
                    var t0w = du.width(as0);
                    var t0xs = (double) Math.min(t0w, g.getFontMetrics(autoFontSize(FONT_CJK, 33)).stringWidth("口口口口口口口口口")) / t0w;
                    var gap = scale * 0.20;
                    var ttw = a0s + t0w * t0xs + gap;
                    var asx = (width - ttw) / 2;
                    var t0x = (float) ((hasLeft && hasRight) || (!hasLeft && !hasRight) ? width / 2.0f : leftToRight ? asx + a0s + gap : asx + t0w);
                    var t0t = new AffineTransform();
                    t0t.concatenate(AffineTransform.getScaleInstance(t0xs, 1.0));
                    du.setTransform(t0t);
                    du.drawText(as0, (float) (t0x / t0xs), height / 2.0f * 1.02f, Color.BLACK, VerticalAlignment.BOTTOM, t0a);
                    var t1w = du.width(as1);
                    var t1xs = (double) Math.min(t1w, t0w * 1.06) / t1w;
                    var t1t = new AffineTransform();
                    t1t.concatenate(AffineTransform.getScaleInstance(t1xs, 1.0));
                    du.setTransform(t1t);
                    if (val == 1 || val == 2) {
                        du.drawText(as1, (float) ((float) (t0a == HorizontalAlignment.CENTER ? t0x : t0x + (t0a == HorizontalAlignment.LEFT ? t0w : -t0w) / 2.0f) / t1xs), height / 2.0f * 1.16f, Color.BLACK, VerticalAlignment.TOP, HorizontalAlignment.CENTER);
                    } else {
                        //var t1x = t0a == HorizontalAlignment.CENTER ? t0x : (t0a == HorizontalAlignment.LEFT ? t0x - t0w / 2.0f : t0x + t0w / 2.0f);
                        du.drawText(as1, (float) (t0x / t1xs), height / 2.0f * 1.16f, Color.BLACK, VerticalAlignment.TOP, t0a);
                    }
                    du.resetTransform();

                    if (hasLeft) {
                        var a0x = hasRight ? (width - ttw - a0s - gap) / 2 : asx;
                        var a0y = (height - a0s) / 2;
                        g.drawImage(blackDirectionArrowPatternImage, (int) a0x, (int) a0y, (int) a0s, (int) a0s, null);
                    }
                    if (hasRight) {
                        var a1st = new AffineTransform();
                        var a1x = hasLeft ? width - ((width - ttw - a0s - gap) / 2) - a0s : asx + t0w + gap;
                        var a1y = (height - a0s) / 2;
                        a1st.translate(a1x + a0s, a1y);
                        a1st.scale(-1, 1);
                        du.setTransform(a1st);
                        g.drawImage(blackDirectionArrowPatternImage, 0, 0, (int) a0s, (int) a0s, null);
                        du.resetTransform();
                    }
                }
            });
            /*nativeImage.fillRect(0, 0, width, height, invertColor(backgroundColor));

            final int circleX;
            if (isTerminating) {
            } else {
                String destinationString = IGui.mergeStations(destinations);
                final boolean noToString = destinationString.startsWith(TEMP_CIRCULAR_MARKER);
                destinationString = destinationString.replace(TEMP_CIRCULAR_MARKER, "");
                if (!destinationString.isEmpty() && showToString && !noToString) {
                    destinationString = insertTranslation(beijingStyleValue == 0 ? "gui.mtr.to_cjk" : "gui.tcm.to_cjk", beijingStyleValue == 0 ? "gui.mtr.to" : "gui.tcm.to", null, 1, destinationString);
                }

                final int clearPadding = clearSize / 4;
                final int leftSize;
                final int rightSize;
                if (beijingStyleValue > 0 && ((!hasLeft && !hasRight) || (hasLeft && hasRight))) {
                    leftSize = (clearSize + clearPadding);
                    rightSize = (clearSize + clearPadding);
                } else if (beijingStyleValue > 0) {
                    leftSize = (hasLeft ? 1 : 0) * (clearSize + clearPadding);
                    rightSize = (hasRight ? 1 : 0) * (clearSize + clearPadding);
                } else {
                    leftSize = ((hasLeft ? 1 : 0) + (leftToRight ? 1 : 0)) * (clearSize + clearPadding);
                    rightSize = ((hasRight ? 1 : 0) + (leftToRight ? 0 : 1)) * (clearSize + clearPadding);
                }

                HorizontalAlignment textAlignment = beijingStyleValue > 0 && ((!hasLeft && !hasRight) || (hasLeft && hasRight)) ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT;

                final int[] dimensionsDestination = new int[2];
                final byte[] pixelsDestination = clientCache.getTextPixels(destinationString, dimensionsDestination, width - leftSize - rightSize - padding * (showToString ? 2 : 1), (int) (clearSize * TCMDynamicResourceCacheV2.LINE_HEIGHT_MULTIPLIER), clearSize * 3 / 5, clearSize * 3 / 10, clearPadding, leftToRight ? textAlignment : HorizontalAlignment.RIGHT, false, scale);
                final int leftPadding = (int) horizontalAlignment.getOffset(0, leftSize + rightSize + dimensionsDestination[0] - clearPadding * 2 - width);
                drawString(nativeImage, pixelsDestination, beijingStyleValue > 0 && ((!hasLeft && !hasRight) || (hasLeft && hasRight)) ? width / 2 : leftPadding + leftSize - clearPadding, height / 2, dimensionsDestination, textAlignment, VerticalAlignment.CENTER, backgroundColor, textColor, false);

                if (hasLeft) {
                    drawResource(nativeImage, ARROW_RESOURCE, leftPadding, padding, clearSize, clearSize, false, 0, 1, textColor, false);
                }
                if (hasRight) {
                    drawResource(nativeImage, ARROW_RESOURCE, leftPadding + leftSize + dimensionsDestination[0] - clearPadding * 2 + rightSize - clearSize, padding, clearSize, clearSize, true, 0, 1, textColor, false);
                }

                circleX = leftPadding + leftSize + (leftToRight ? -clearSize - clearPadding : dimensionsDestination[0] - clearPadding);
            }*/

            if (transparentColor != 0) {
                clearColor(nativeImage, invertColor(transparentColor));
            }

            return nativeImage;
        } catch (Throwable e) {
            TCM.LOGGER.error("Cannot to draw PSD Top: ", e);
        }

        return null;
    }

    public static NativeImage generatePSDTopRouteMap(long platformId, boolean flip, float aspectRatio, boolean transparentWhite) {
        if (aspectRatio <= 0) {
            return null;
        }

        try {
            final ObjectArrayList<ObjectIntImmutablePair<SimplifiedRoute>> routeDetails = new ObjectArrayList<>();
            getRouteStream(platformId, (simplifiedRoute, currentStationIndex) -> routeDetails.add(new ObjectIntImmutablePair<>(simplifiedRoute, currentStationIndex)));
            final int routeCount = routeDetails.size();

            if (routeCount > 0) {
                final ObjectArrayList<LongArrayList> stationsIdsBefore = new ObjectArrayList<>();
                final ObjectArrayList<LongArrayList> stationsIdsAfter = new ObjectArrayList<>();
                final ObjectArrayList<Int2ObjectAVLTreeMap<TCMRouteMapGeneratorV2.StationPosition>> stationPositions = new ObjectArrayList<>();
                final IntAVLTreeSet colors = new IntAVLTreeSet();
                final int[] colorIndices = new int[routeCount];
                int colorIndex = -1;
                int previousColor = -1;
                for (int routeIndex = 0; routeIndex < routeCount; routeIndex++) {
                    stationsIdsBefore.add(new LongArrayList());
                    stationsIdsAfter.add(new LongArrayList());
                    stationPositions.add(new Int2ObjectAVLTreeMap<>());

                    final ObjectIntImmutablePair<SimplifiedRoute> routeDetail = routeDetails.get(routeIndex);
                    final ObjectArrayList<SimplifiedRoutePlatform> simplifiedRoutePlatforms = routeDetail.left().getPlatforms();
                    final int currentIndex = routeDetail.rightInt();
                    for (int stationIndex = 0; stationIndex < simplifiedRoutePlatforms.size(); stationIndex++) {
                        if (stationIndex != currentIndex) {
                            final long stationId = simplifiedRoutePlatforms.get(stationIndex).getStationId();
                            if (stationIndex < currentIndex) {
                                stationsIdsBefore.get(stationsIdsBefore.size() - 1).add(0, stationId);
                            } else {
                                stationsIdsAfter.get(stationsIdsAfter.size() - 1).add(stationId);
                            }
                        }
                    }

                    final int color = routeDetail.left().getColor();
                    colors.add(color);
                    if (color != previousColor) {
                        colorIndex++;
                        previousColor = color;
                    }
                    colorIndices[routeIndex] = colorIndex;
                }

                for (int routeIndex = 0; routeIndex < routeCount; routeIndex++) {
                    stationPositions.get(routeIndex).put(0, new TCMRouteMapGeneratorV2.StationPosition(0, getLineOffset(routeIndex, colorIndices), true));
                }

                final float[] bounds = new float[3];
                setup(stationPositions, flip ? stationsIdsBefore : stationsIdsAfter, colorIndices, bounds, flip, true);
                final float xOffset = bounds[0] + 0.5F;
                setup(stationPositions, flip ? stationsIdsAfter : stationsIdsBefore, colorIndices, bounds, !flip, false);
                final float rawHeightPart = Math.abs(bounds[1]) + 1;
                final float rawWidth = xOffset + bounds[0] + 0.5F;
                final float rawHeightTotal = rawHeightPart + bounds[2] + 1;
                final float rawHeight;
                final float yOffset;
                final float extraPadding;
                rawHeight = rawHeightTotal;
                extraPadding = 0;
                yOffset = (float) (rawHeightPart + 0.08);

                final int height;
                final int width;
                final float widthScale;
                final float heightScale;
                if (rawWidth / rawHeight > aspectRatio) {
                    width = Math.round(rawWidth * scale);
                    height = Math.round(width / aspectRatio);
                    widthScale = 1;
                    heightScale = height / rawHeight / scale;
                } else {
                    height = Math.round(rawHeight * scale);
                    width = Math.round(height * aspectRatio);
                    heightScale = 1;
                    widthScale = width / rawWidth / scale;
                }

                if (width <= 0 || height <= 0) {
                    return null;
                }

                final NativeImage nativeImage = new NativeImage(NativeImageFormat.getAbgrMapped(), width, height, false);
                final Object2ObjectOpenHashMap<String, ObjectOpenHashSet<TCMRouteMapGeneratorV2.StationPositionGrouped>> stationPositionsGrouped = new Object2ObjectOpenHashMap<>();

                for (int routeIndex = 0; routeIndex < routeCount; routeIndex++) {
                    final SimplifiedRoute simplifiedRoute = routeDetails.get(routeIndex).left();
                    final int currentIndex = routeDetails.get(routeIndex).rightInt();
                    final Int2ObjectAVLTreeMap<TCMRouteMapGeneratorV2.StationPosition> routeStationPositions = stationPositions.get(routeIndex);

                    for (int stationIndex = 0; stationIndex < simplifiedRoute.getPlatforms().size(); stationIndex++) {
                        final TCMRouteMapGeneratorV2.StationPosition stationPosition = routeStationPositions.get(stationIndex - currentIndex);

                        final SimplifiedRoutePlatform simplifiedRoutePlatform = simplifiedRoute.getPlatforms().get(stationIndex);
                        final String key = String.format("%s||%s", simplifiedRoutePlatform.getStationName(), simplifiedRoutePlatform.getStationId());

                        if (!stationPosition.isCommon || stationPositionsGrouped.getOrDefault(key, new ObjectOpenHashSet<>()).stream().noneMatch(stationPosition2 -> stationPosition2.stationPosition.x == stationPosition.x)) {
                            final IntArrayList interchangeColors = new IntArrayList();
                            final ObjectArrayList<String> interchangeNames = new ObjectArrayList<>();
                            simplifiedRoutePlatform.forEach((color, interchangeRouteNamesForColor) -> {
                                if (!colors.contains(color)) {
                                    interchangeColors.add(color);
                                    interchangeRouteNamesForColor.forEach(interchangeNames::add);
                                }
                            });
                            Data.put(stationPositionsGrouped, key, new TCMRouteMapGeneratorV2.StationPositionGrouped(stationPosition, stationIndex, currentIndex, interchangeColors, interchangeNames), ObjectOpenHashSet::new);
                        }
                    }
                }

                NativeImageDrawUtil.createDrawAndApply(nativeImage, (i, g) -> {
                    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    var du = new AWTDrawUtil(g);
                    du.drawRect(0, 0, width, height, Color.WHITE);

                    for (int routeIndex = 0; routeIndex < routeCount; routeIndex++) {
                        final SimplifiedRoute simplifiedRoute = routeDetails.get(routeIndex).left();
                        final int currentIndex = routeDetails.get(routeIndex).rightInt();
                        final Int2ObjectAVLTreeMap<TCMRouteMapGeneratorV2.StationPosition> routeStationPositions = stationPositions.get(routeIndex);

                        for (int stationIndex = 0; stationIndex < simplifiedRoute.getPlatforms().size(); stationIndex++) {
                            final TCMRouteMapGeneratorV2.StationPosition stationPosition = routeStationPositions.get(stationIndex - currentIndex);
                            if (stationIndex < simplifiedRoute.getPlatforms().size() - 1) {
                                drawLineAWT(g, stationPosition, routeStationPositions.get(stationIndex + 1 - currentIndex), widthScale, heightScale, xOffset, yOffset, stationIndex < currentIndex ? ARGB_LIGHT_GRAY : ARGB_BLACK | simplifiedRoute.getColor());
                            }
                        }
                    }

                    final int maxStringWidth = (int) (scale * 0.9 * (widthScale / 2 + extraPadding / routeCount));
                    stationPositionsGrouped.forEach((key, stationPositionGroupedSet) -> stationPositionGroupedSet.forEach(stationPositionGrouped -> {
                        final int x = Math.round((stationPositionGrouped.stationPosition.x + xOffset) * scale * widthScale);
                        final int y = Math.round((stationPositionGrouped.stationPosition.y * 1.1f + yOffset) * scale * heightScale);
                        final int lines = stationPositionGrouped.stationPosition.isCommon ? colorIndices[colorIndices.length - 1] : 0;
                        final IntArrayList interchangeColors = stationPositionGrouped.interchangeColors;
                        boolean isBranchLine = !stationPositionGrouped.stationPosition.isCommon && stationPositionGrouped.stationPosition.y != 0;
                        final boolean routeDrawMode2 = stationPositionsGrouped.size() >= 15;
                        final boolean textBelow = (routeDrawMode2 || interchangeColors.isEmpty()) && ((!isBranchLine ? Math.abs(stationPositionGrouped.stationOffset) % 2 == 0 : y >= yOffset * scale)) || stationPositionGrouped.stationPosition.y > 0;
                        final boolean currentStation = stationPositionGrouped.stationOffset == stationPositionGrouped.stationCurrentAtOffset;
                        final boolean passed = stationPositionGrouped.stationOffset < stationPositionGrouped.stationCurrentAtOffset;

                        if (!interchangeColors.isEmpty()) {
                            final int lineHeight = !isBranchLine ? (routeDrawMode2 ? lineSize * 48 / 14 : lineSize * 12 / 5) : (int) (lineSize * 12.0 / 5 * (0.8));
                            final int lineHeightText = !isBranchLine ? (routeDrawMode2 ? lineSize * 48 / 10 : lineSize * 12 / 3) : (int) (lineSize * 48.0 / 13 * (0.85));
                            final int lineWidth = (int) Math.ceil((float) (lineSize + 6) / interchangeColors.size());
                            for (int j = 0; j < interchangeColors.size(); j++) {
                                g.setColor(new Color(passed ? ARGB_LIGHT_GRAY : ARGB_BLACK | interchangeColors.getInt(j), true));
                                int ix = x + lineWidth * j - lineWidth * interchangeColors.size() / 2;
                                int iy = y + (textBelow ? -lineHeight : lines * lineSpacing);
                                g.fillRect(ix, iy, lineWidth, lineHeight);
                            }

                            String transferText = insertTranslation("gui.tcm.transfer_cjk", "gui.tcm.transfer", null, 1, IGui.mergeStations(stationPositionGrouped.interchangeNames));
                            var transferTextColor = new Color(passed ? ARGB_LIGHT_GRAY : ARGB_BLACK, true);
                            int tx = x;
                            int ty = y + (textBelow ? -lines * lineSpacing : lines * lineSpacing) + (textBelow ? -8 - lineHeightText : 8 + lineHeightText);

                            String transferCjkPart = TextUtil.getCjkParts(transferText);
                            String transferNonCjkPart = TextUtil.getNonCjkParts(transferText);

                            if (!transferCjkPart.isEmpty() && !transferNonCjkPart.isEmpty()) {
                                g.setFont(FONT_CJK_LIGHT.deriveFont(Font.PLAIN, (float) (routeDrawMode2 ? fontSizeBig * 4 / 9 : fontSizeBig * 2 / 4)));
                                var transferCjkFm = g.getFontMetrics();
                                int transferCjkWidth = transferCjkFm.stringWidth(transferCjkPart);
                                int transferCjkHeight = transferCjkFm.getFont().getSize();
                                g.setFont(FONT_ASCII_BOLD.deriveFont(Font.PLAIN, (float) (routeDrawMode2 ? fontSizeSmall * 4 / 9 : fontSizeSmall * 2 / 4)));
                                var transferNonCjkFm = g.getFontMetrics();
                                int transferNonCjkWidth = transferNonCjkFm.stringWidth(transferNonCjkPart);
                                boolean transformCjk = transferCjkWidth > maxStringWidth;
                                boolean transformNonCjk = transferNonCjkWidth > maxStringWidth;
                                int transferNonCjkHeight = (int) (transferNonCjkFm.getFont().getSize() * 1.2);

                                double transferCjkXScale = Math.min(1.0, (double) maxStringWidth / transferCjkWidth);
                                if (transformCjk) {
                                    var transform = new AffineTransform();
                                    transform.concatenate(AffineTransform.getScaleInstance(transferCjkXScale, 1.0));
                                    du.setTransform(transform);
                                }
                                g.setFont(FONT_CJK_LIGHT.deriveFont(Font.PLAIN, (float) (routeDrawMode2 ? fontSizeBig * 4 / 9 : fontSizeBig * 2 / 4)));
                                du.drawText(transferCjkPart, null, (float) (tx / transferCjkXScale), (float) (textBelow ? ty : ty - transferCjkHeight - transferNonCjkHeight), transferTextColor, VerticalAlignment.TOP, HorizontalAlignment.CENTER);
                                if (transformCjk) {
                                    du.resetTransform();
                                }
                                double transferNonCjkXScale = Math.min(1.0, (double) maxStringWidth / transferNonCjkWidth);
                                if (transformNonCjk) {
                                    var transform = new AffineTransform();
                                    transform.concatenate(AffineTransform.getScaleInstance(transferNonCjkXScale, 1.0));
                                    du.setTransform(transform);
                                }
                                g.setFont(FONT_ASCII_BOLD.deriveFont(Font.PLAIN, (float) (routeDrawMode2 ? fontSizeSmall * 4 / 9 : fontSizeSmall * 2 / 4)));
                                du.drawText(transferNonCjkPart, null, (float) (tx / transferNonCjkXScale), (float) (textBelow ? ty + transferCjkHeight : ty - transferNonCjkHeight), transferTextColor, VerticalAlignment.TOP, HorizontalAlignment.CENTER);
                                if (transformNonCjk) {
                                    du.resetTransform();
                                }
                            } else {
                                g.setFont(FONT_CJK_LIGHT.deriveFont(Font.PLAIN, (float) (fontSizeBig * 2 / 3)));
                                du.drawText(transferText, null, (float) tx, (float) ty, transferTextColor, textBelow ? VerticalAlignment.TOP : VerticalAlignment.BOTTOM, HorizontalAlignment.CENTER);
                            }
                        }

                        drawStationAWT(g, x, y, heightScale, lines, passed, !interchangeColors.isEmpty());

                        String stationName = key.split("\\|\\|")[0].replaceAll("(?i)First", "1st")
                                .replaceAll("(?i)Second", "2nd")
                                .replaceAll("(?i)Third", "3rd")
                                .replaceAll("(?i)Forth", "4th")
                                .replaceAll("(?i)Fifth", "5th")
                                .replaceAll("(?i)Sixth", "6th")
                                .replaceAll("(?i)Seventh", "7th")
                                .replaceAll("(?i)Eighth", "8th")
                                .replaceAll("(?i)Ninth", "9th")
                                .replaceAll("(?i)Tenth", "10th")
                                .replaceAll("(?i)University", "Univ.")
                                .replaceAll("(?i)Company", "Co.")
                                .replaceAll("(?i)Department", "Dept.");
                        int textX = x;
                        Color textColor = new Color(passed ? ARGB_LIGHT_GRAY : (currentStation && getColorLightNess(routeDetails.get(0).left().getColor()) <= 0.67f ? ARGB_WHITE : ARGB_BLACK), true);

                        String cjkPart = TextUtil.getCjkParts(stationName);
                        String nonCjkPart = TextUtil.getNonCjkParts(stationName);

                        int cjkHeight = 0;
                        int nonCjkHeight = 0;

                        if (!cjkPart.isEmpty() && !nonCjkPart.isEmpty()) {
                            g.setFont(FONT_CJK.deriveFont(Font.PLAIN, (float) (fontSizeBig * 0.85)));
                            cjkHeight = (int) (fontSizeBig * 0.85 + (scale * 0.05));
                            g.setFont(FONT_ASCII_BOLD.deriveFont(Font.PLAIN, (float) (fontSizeSmall * 0.76)));
                            nonCjkHeight = (int) (fontSizeSmall * 0.8);
                        } else {
                            g.setFont(FONT_CJK.deriveFont(Font.PLAIN, (float) fontSizeBig));
                            cjkHeight = (int) (fontSizeBig + (scale * 0.05));
                        }

                        int totalHeight = cjkHeight + nonCjkHeight;
                        int textY = (int) ((y + (textBelow ? lines * lineSpacing : -lines * lineSpacing) + (textBelow ? 1 : -1) * (lineSize * 10.0 / 7)) - scale * 0.025);

                        if (!cjkPart.isEmpty() && !nonCjkPart.isEmpty()) {
                            g.setFont(FONT_CJK.deriveFont(Font.PLAIN, (float) (fontSizeBig * 0.85)));
                            var cjkFm = g.getFontMetrics();
                            int cjkWidth = cjkFm.stringWidth(cjkPart);
                            g.setFont(FONT_ASCII_BOLD.deriveFont(Font.PLAIN, (float) (fontSizeSmall * 0.76)));
                            var nonCjkFm = g.getFontMetrics();
                            int nonCjkWidth = nonCjkFm.stringWidth(nonCjkPart);

                            int totalWidth = Math.max(cjkWidth, nonCjkWidth);

                            double cjkXScale = Math.min(1.0, (double) maxStringWidth / cjkWidth);
                            double nonCjkXScale = Math.min(1.0, (double) maxStringWidth / nonCjkWidth);

                            if (currentStation) {
                                int bgColor = ARGB_BLACK | routeDetails.getFirst().left().getColor();
                                g.setColor(new Color(bgColor, true));
                                int bgY = textBelow ? (int) (textY - ((totalHeight * 1.2) - totalHeight) / 4) : (int) (textY - totalHeight - ((totalHeight + scale * 0.075) - totalHeight) / 4);
                                du.drawRadiusRect((int) HorizontalAlignment.CENTER.getOffset((float) (textX), (float) Math.min(maxStringWidth * 1.15, totalWidth + scale * 0.1)), bgY, (int) Math.min(maxStringWidth * 1.15, totalWidth + scale * 0.1), (int) (totalHeight + scale * 0.085), null, lineSize * 0.8);
                                int arrowScaleX = (int) (scale * 0.25);
                                int arrowScaleY = (int) (scale * 0.18);
                                int arrowY = (int) ((textBelow ? textY + totalHeight + scale * 0.04 : textY - totalHeight - arrowScaleY) + (textBelow ? scale : -scale) * 0.05);
                                var transform = new AffineTransform();
                                transform.translate(textX + (flip ? arrowScaleX / 2.0 : -arrowScaleX / 2.0), arrowY);
                                transform.scale(flip ? -1 : 1, 1);
                                du.setTransform(transform);
                                g.drawImage(blackDirectionArrowPatternImage, 0, 0, arrowScaleX, arrowScaleY, null);
                                du.resetTransform();
                            }

                            boolean transformCjk = cjkWidth > maxStringWidth;
                            boolean transformNonCjk = nonCjkWidth > maxStringWidth;

                            g.setFont(FONT_CJK.deriveFont(Font.PLAIN, (float) (fontSizeBig * 0.85)));
                            if (transformCjk) {
                                var transform = new AffineTransform();
                                transform.concatenate(AffineTransform.getScaleInstance(cjkXScale, 1.0));
                                du.setTransform(transform);
                            }
                            du.drawText(cjkPart, null, (float) (textX / cjkXScale), (float) (textBelow ? textY : textY - cjkHeight - nonCjkHeight), textColor, VerticalAlignment.TOP, HorizontalAlignment.CENTER);
                            if (transformCjk) {
                                du.resetTransform();
                            }
                            g.setFont(FONT_ASCII_BOLD.deriveFont(Font.PLAIN, (float) (fontSizeSmall * 0.76)));
                            if (transformNonCjk) {
                                var transform = new AffineTransform();
                                transform.concatenate(AffineTransform.getScaleInstance(nonCjkXScale, 1.0));
                                du.setTransform(transform);
                            }
                            du.drawText(nonCjkPart, null, (float) (textX / nonCjkXScale), (float) (textBelow ? textY + cjkHeight + nonCjkHeight : textY), textColor, VerticalAlignment.BOTTOM, HorizontalAlignment.CENTER);
                            if (transformNonCjk) {
                                du.resetTransform();
                            }

                        } else {
                            g.setFont(FONT_CJK.deriveFont(Font.PLAIN, (float) fontSizeBig));
                            var fm = g.getFontMetrics();
                            int textWidth = fm.stringWidth(stationName);

                            if (currentStation) {
                                int bgColor = ARGB_BLACK | routeDetails.get(0).left().getColor();
                                g.setColor(new Color(bgColor, true));
                                int bgY = textBelow ? textY : textY - cjkHeight;
                                g.fillRect((int) HorizontalAlignment.CENTER.getOffset(textX, textWidth), bgY, textWidth, cjkHeight);
                            }


                            du.drawText(stationName, null, (float) textX, (float) textY, textColor, textBelow ? VerticalAlignment.TOP : VerticalAlignment.BOTTOM, HorizontalAlignment.CENTER);

                        }
                    }));
                });

                if (transparentWhite) {
                    clearColor(nativeImage, ARGB_WHITE);
                }

                return nativeImage;
            } else {
                final NativeImage nativeImage = new NativeImage(NativeImageFormat.getAbgrMapped(), 1, 1, false);
                nativeImage.setPixelColor(0, 0, transparentWhite ? 0 : ARGB_WHITE);
            }
        } catch (Exception e) {
            Init.LOGGER.error("", e);
        }

        return null;
    }

    static String insertTranslation(String keyCJK, String key, @Nullable String overrideFirst, int expectedArguments, String... arguments) {
        if (arguments.length < expectedArguments) {
            return "";
        }

        final List<String[]> dataCJK = new ArrayList<>();
        final List<String[]> data = new ArrayList<>();
        for (int i = 0; i < arguments.length; i++) {
            final String[] argumentSplit = arguments[i].split("\\|");

            int indexCJK = 0;
            int index = 0;
            for (final String text : argumentSplit) {
                if (isCjk(text)) {
                    if (indexCJK == dataCJK.size()) {
                        dataCJK.add(new String[expectedArguments]);
                    }
                    dataCJK.get(indexCJK)[i] = text;
                    indexCJK++;
                } else {
                    if (index == data.size()) {
                        data.add(new String[expectedArguments]);
                    }
                    data.get(index)[i] = text;
                    index++;
                }
            }
        }

        final StringBuilder result = new StringBuilder();
        dataCJK.forEach(combinedArguments -> {
            if (Arrays.stream(combinedArguments).allMatch(Objects::nonNull)) {
                result.append("|");
                if (overrideFirst == null) {
                    result.append(TCMComponent.translatable(keyCJK, (Object[]) combinedArguments).getString());
                } else {
                    final String[] newCombinedArguments = new String[expectedArguments + 1];
                    System.arraycopy(combinedArguments, 0, newCombinedArguments, 1, expectedArguments);
                    newCombinedArguments[0] = overrideFirst;
                    result.append(TCMComponent.translatable(keyCJK, (Object[]) newCombinedArguments).getString());
                }
            }
        });
        data.forEach(combinedArguments -> {
            if (Arrays.stream(combinedArguments).allMatch(Objects::nonNull)) {
                result.append("|");
                if (overrideFirst == null) {
                    result.append(TCMComponent.translatable(key, (Object[]) combinedArguments).getString());
                } else {
                    final String[] newCombinedArguments = new String[expectedArguments + 1];
                    System.arraycopy(combinedArguments, 0, newCombinedArguments, 1, expectedArguments);
                    newCombinedArguments[0] = overrideFirst;
                    result.append(TCMComponent.translatable(key, (Object[]) newCombinedArguments).getString());
                }
            }
        });

        if (!result.isEmpty()) {
            return result.substring(1);
        } else {
            return "";
        }
    }

    private static void drawRouteName(AWTDrawUtil drawer, int x, int y, int size, String routeName, Color routeColor, int styleId, boolean forceDrawPlainText, VerticalAlignment verticalAlignment, HorizontalAlignment horizontalAlignment) {
        Color textColor = routeColor;
        var g = drawer.getGraphics();
        if (styleId == 2 || styleId == -1) {
            var bw = (int) (size * 1.9);
            var bh = size;
            drawer.drawRadiusRect((int) horizontalAlignment.getOffset(x, bw), (int) verticalAlignment.getOffset(y, bh), bw, bh, routeColor, size / 6.0);
            textColor = getColorLightNess(routeColor.getRGB()) <= 0.67f ? Color.WHITE : new Color(0, 54, 112);
            if (styleId == -1) {
                var border = size * 0.08;
                drawer.drawBorderedRadiusRect((int) horizontalAlignment.getOffset((float) (x), bw), (int) verticalAlignment.getOffset((float) (y), bh), (int) (bw), (int) (bh), new Color(0, 0, 0, 0), Color.WHITE, size / 6.0, (int) border);
            }
        }

        int rni = -1;
        for (char c : routeName.toCharArray()) {
            if (!isNumber(String.valueOf(c))) {
                break;
            }
            if (rni < 0) {
                rni = 0;
            }
            rni = rni * 10 + Integer.parseInt(String.valueOf(c));
        }

        if (rni != -1 && !forceDrawPlainText) {
            AffineTransform stretch = new AffineTransform();
            g.setFont(FONT_ASCII.deriveFont(Font.PLAIN, (float) (size * 0.81)));
            var t0w = g.getFontMetrics().stringWidth(String.valueOf(rni));
            var t0sx = Math.min(t0w, size * 10.0 / 13.0) / t0w;
            var t0fx = Math.ceil((x - size * 12.0 / 4.0 * 0.172 + (t0sx * t0w) * 0.08) / t0sx);
            stretch.concatenate(AffineTransform.getScaleInstance(t0sx, 1.0));
            drawer.setTransform(stretch);
            drawer.drawText(String.valueOf(rni), null, (int) t0fx, y + size * 0.28f, textColor, VerticalAlignment.BOTTOM, HorizontalAlignment.CENTER);
            drawer.resetTransform();
            var t1x = (float) Math.ceil((x + size * 1.9 / 2) - (t0fx * t0sx - (double) t0w / 2) + (x - size * 1.9 / 2) - (size * 0.08));//(float) (x - (scale * 0.05));
            var t1w = drawer.drawText(TextUtil.getCjkParts(routeName).replaceFirst(String.valueOf(rni), ""), FONT_CJK.deriveFont(Font.PLAIN, (float) (size * 0.39)), t1x, y - size * 0.365f, textColor, VerticalAlignment.TOP, HorizontalAlignment.RIGHT);
            drawer.drawText(TextUtil.getNonCjkParts(routeName), FONT_ASCII_BOLD.deriveFont(Font.PLAIN, (float) (size * 0.22)), t1x - (float) t1w / 2.0f, y + size * 0.081f, textColor, VerticalAlignment.TOP, HorizontalAlignment.CENTER);
        } else {
            drawer.drawText(TextUtil.getCjkParts(routeName), FONT_CJK.deriveFont(Font.PLAIN, (float) (size * 0.39)), x, y - size * 0.365f, textColor, VerticalAlignment.TOP, HorizontalAlignment.CENTER);
            drawer.drawText(TextUtil.getNonCjkParts(routeName), FONT_ASCII_BOLD.deriveFont(Font.PLAIN, (float) (size * 0.22)), x, y + size * 0.081f, textColor, VerticalAlignment.TOP, HorizontalAlignment.CENTER);
        }
    }

    private static float getColorLightNess(int rgb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = (rgb) & 0xff;
        return (float) Math.pow(Math.pow(r / 255.0f, 2.2f) + Math.pow(g / 170.0f, 2.2f) + Math.pow(b / 425.0f, 2.2f), 1 / 2.2f) * 0.547373141f;
    }

    private static boolean isNumber(String str) {
        return str.matches("^\\d+$");
    }

    private static Font autoFontSize(Font font, float size) {
        return font.deriveFont(Font.PLAIN, size * (scale / 25.0f) / 4);
    }

    public static void scrollTextLightRail(GraphicsHolder graphicsHolder, int rows, float availableWidth, float availableHeight, int imageWidth, int imageHeight) {
        final float scale = availableHeight / imageHeight * rows;
        final int delayTime = 3000;
        final int slideTime = 8;
        final int totalTime = delayTime + (int) Math.floor(availableWidth / scale) * slideTime;
        final int totalStep = (int) (System.currentTimeMillis() % (totalTime * rows));
        final int step = totalStep % totalTime;
        final int row = totalStep / totalTime;
        final float xOffset = (availableWidth - imageWidth * scale) / 2;
        final float x = xOffset - Math.max(0, step - delayTime) * scale / slideTime;
        IDrawing.drawTexture(graphicsHolder, Math.max(x, 0), 0, imageWidth * scale + Math.min(x, 0), availableHeight, Math.max(-x, 0) / imageWidth / scale, (float) row / rows, 1, (float) (row + 1) / rows, Direction.UP, ARGB_WHITE, GraphicsHolder.getDefaultLight());
    }

    private static void setup(ObjectArrayList<Int2ObjectAVLTreeMap<TCMRouteMapGeneratorV2.StationPosition>> stationPositions, ObjectArrayList<LongArrayList> stationsIdLists, int[] colorIndices, float[] bounds, boolean passed, boolean reverse) {
        final int passedMultiplier = passed ? -1 : 1;
        final int reverseMultiplier = reverse ? -1 : 1;
        bounds[0] = 0;

        final LongArrayList commonStationIds = new LongArrayList();
        stationsIdLists.get(0).forEach(stationId -> {
            if (stationId != 0 && !commonStationIds.contains(stationId) && stationsIdLists.stream().allMatch(stationsIds -> stationsIds.contains(stationId))) {
                commonStationIds.add(stationId);
            }
        });

        int positionXOffset = 0;
        final int routeCount = stationsIdLists.size();
        final int[] traverseIndex = new int[routeCount];
        for (int commonStationIndex = 0; commonStationIndex <= commonStationIds.size(); commonStationIndex++) {
            final boolean lastStation = commonStationIndex == commonStationIds.size();
            final long commonStationId = lastStation ? -1 : commonStationIds.getLong(commonStationIndex);

            int intermediateSegmentsMaxCount = 0;
            final int[] intermediateSegmentsCounts = new int[routeCount];
            for (int routeIndex = 0; routeIndex < routeCount; routeIndex++) {
                intermediateSegmentsCounts[routeIndex] = (lastStation ? stationsIdLists.get(routeIndex).size() : stationsIdLists.get(routeIndex).indexOf(commonStationId) + 1) - traverseIndex[routeIndex];
                intermediateSegmentsMaxCount = Math.max(intermediateSegmentsMaxCount, intermediateSegmentsCounts[routeIndex]);
            }

            final IntArrayList routesIndicesInSection = new IntArrayList();
            for (int routeIndex = 0; routeIndex < routeCount; routeIndex++) {
                if (!lastStation || intermediateSegmentsCounts[routeIndex] > 0) {
                    routesIndicesInSection.add(routeIndex);
                }
            }

            for (int routeIndex = 0; routeIndex < routeCount; routeIndex++) {
                if (intermediateSegmentsCounts[routeIndex] > 0) {
                    final float increment = (float) intermediateSegmentsMaxCount / intermediateSegmentsCounts[routeIndex];
                    for (int j = 0; j < intermediateSegmentsCounts[routeIndex] - (lastStation ? 0 : 1); j++) {
                        final float stationX = positionXOffset + increment * (j + 1);
                        bounds[0] = Math.max(bounds[0], stationX / 2);
                        final float stationY = routesIndicesInSection.indexOf(routeIndex) - (routesIndicesInSection.size() - 1) / 2F + getLineOffset(routeIndex, colorIndices);
                        bounds[1] = Math.min(bounds[1], stationY);
                        bounds[2] = Math.max(bounds[2], stationY);
                        stationPositions.get(routeIndex).put(passedMultiplier * (j + traverseIndex[routeIndex] + 1), new TCMRouteMapGeneratorV2.StationPosition(reverseMultiplier * stationX / 2, stationY, false));
                    }
                    traverseIndex[routeIndex] += intermediateSegmentsCounts[routeIndex];
                }
            }

            if (!lastStation) {
                positionXOffset += intermediateSegmentsMaxCount;
                for (int routeIndex = 0; routeIndex < routeCount; routeIndex++) {
                    final float stationY = getLineOffset(routeIndex, colorIndices);
                    bounds[1] = Math.min(bounds[1], stationY);
                    bounds[2] = Math.max(bounds[2], stationY);
                    stationPositions.get(routeIndex).put(passedMultiplier * traverseIndex[routeIndex], new TCMRouteMapGeneratorV2.StationPosition(reverseMultiplier * positionXOffset / 2F, stationY, true));
                }
                bounds[0] = positionXOffset / 2F;
            }
        }
    }

    private static float getLineOffset(int routeIndex, int[] colorIndices) {
        return (float) lineSpacing / scale * (colorIndices[routeIndex] - colorIndices[colorIndices.length - 1] / 2F);
    }

    private static IntArrayList getRouteStream(long platformId, BiConsumer<SimplifiedRoute, Integer> nonTerminatingCallback) {
        final IntArrayList colors = new IntArrayList();
        final IntArrayList terminatingColors = new IntArrayList();
        var coll = MinecraftClientData.getInstance().simplifiedRoutes.stream().filter(simplifiedRoute -> simplifiedRoute.getPlatformIndex(platformId) >= 0 && !simplifiedRoute.getName().isEmpty()).sorted().toList();
        boolean allTerminating = coll.stream().allMatch((r) -> r.getPlatformIndex(platformId) >= r.getPlatforms().size() - 1);
        //var colla = coll.toList();
        coll.forEach(simplifiedRoute -> {
            final int currentStationIndex = simplifiedRoute.getPlatformIndex(platformId);
            if (currentStationIndex >= simplifiedRoute.getPlatforms().size() - 1 && !(currentStationIndex < simplifiedRoute.getPlatforms().size() && allTerminating)) {
                if (!terminatingColors.contains(simplifiedRoute.getColor())) {
                    terminatingColors.add(simplifiedRoute.getColor());
                }
            } else {
                nonTerminatingCallback.accept(simplifiedRoute, currentStationIndex);
                if (!colors.contains(simplifiedRoute.getColor())) {
                    colors.add(simplifiedRoute.getColor());
                }
            }
        });
        if (colors.isEmpty()) {
            colors.addAll(terminatingColors);
        }
        return colors;
    }

    private static String getStationName(long platformId) {
        final Platform platform = MinecraftClientData.getInstance().platformIdMap.get(platformId);
        final Station station = platform == null ? null : platform.area;
        return station == null ? "" : station.getName();
    }

    private static void drawLineAWT(Graphics2D g, TCMRouteMapGeneratorV2.StationPosition stationPosition1, TCMRouteMapGeneratorV2.StationPosition stationPosition2, float widthScale, float heightScale, float xOffset, float yOffset, int color) {
        final int x1 = Math.round((stationPosition1.x + xOffset) * scale * widthScale);
        final int x2 = Math.round((stationPosition2.x + xOffset) * scale * widthScale);
        final int y1 = Math.round((stationPosition1.y + yOffset) * scale * heightScale);
        final int y2 = Math.round((stationPosition2.y + yOffset) * scale * heightScale);

        g.setColor(new Color(color, true));
        g.setStroke(new BasicStroke(lineSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(x1, y1, x2, y2);
        g.setStroke(new BasicStroke(1));
    }

    private static void drawLine(NativeImage nativeImage, TCMRouteMapGeneratorV2.StationPosition stationPosition1, TCMRouteMapGeneratorV2.StationPosition stationPosition2, float widthScale, float heightScale, float xOffset, float yOffset, int color) {
        final int x1 = Math.round((stationPosition1.x + xOffset) * scale * widthScale);
        final int x2 = Math.round((stationPosition2.x + xOffset) * scale * widthScale);
        final int y1 = Math.round((stationPosition1.y + yOffset) * scale * heightScale);
        final int y2 = Math.round((stationPosition2.y + yOffset) * scale * heightScale);
        final int xChange = x2 - x1;
        final int yChange = y2 - y1;
        final int xChangeAbs = Math.abs(xChange);
        final int yChangeAbs = Math.abs(yChange);
        final int changeDifference = Math.abs(yChangeAbs - xChangeAbs);

        if (xChangeAbs > yChangeAbs) {
            final boolean y1OffsetGreater = Math.abs(y1 - yOffset * scale) > Math.abs(y2 - yOffset * scale);
            drawLine(nativeImage, x1, y1, x2 - x1, y1OffsetGreater ? 0 : y2 - y1, y1OffsetGreater ? changeDifference : yChangeAbs, color);
            drawLine(nativeImage, x2, y2, x1 - x2, y1OffsetGreater ? y1 - y2 : 0, y1OffsetGreater ? yChangeAbs : changeDifference, color);
        } else {
            final int halfXChangeAbs = xChangeAbs / 2;
            drawLine(nativeImage, x1, y1, x2 - x1, y2 - y1, halfXChangeAbs, color);
            drawLine(nativeImage, x2, y2, x1 - x2, y1 - y2, halfXChangeAbs, color);
            drawLine(nativeImage, (x1 + x2) / 2, y1 + (int) Math.copySign(halfXChangeAbs, y2 - y1), 0, y2 - y1, changeDifference, color);
        }
    }

    private static void drawLine(NativeImage nativeImage, int x, int y, int directionX, int directionY, int length, int color) {
        final int halfLineHeight = lineSize / 2;
        final int xWidth = directionX == 0 ? halfLineHeight : 0;
        final int yWidth = directionX == 0 ? 0 : directionY == 0 ? halfLineHeight : Math.round(lineSize * MathHelper.getSquareRootOfTwoMapped() / 2);
        final int yMin = y - halfLineHeight - (directionY < 0 ? length : 0) + 1;
        final int yMax = y + halfLineHeight + (directionY > 0 ? length : 0) - 1;
        final int drawOffset = directionX != 0 && directionY != 0 ? halfLineHeight : 0;

        for (int i = -drawOffset; i < Math.abs(length) + drawOffset; i++) {
            final int drawX = x + (directionX == 0 ? 0 : (int) Math.copySign(i, directionX)) + (directionX < 0 ? -1 : 0);
            final int drawY = y + (directionY == 0 ? 0 : (int) Math.copySign(i, directionY)) + (directionY < 0 ? -1 : 0);

            for (int xOffset = 0; xOffset < xWidth; xOffset++) {
                drawPixelSafe(nativeImage, drawX - xOffset - 1, drawY, color);
                drawPixelSafe(nativeImage, drawX + xOffset, drawY, color);
            }

            for (int yOffset = 0; yOffset < yWidth; yOffset++) {
                drawPixelSafe(nativeImage, drawX, Math.max(drawY - yOffset, yMin) - 1, color);
                drawPixelSafe(nativeImage, drawX, Math.min(drawY + yOffset, yMax), color);
            }
        }
    }

    private static void drawStationAWT(Graphics2D g, int x, int y, float heightScale, int lines, boolean passed, boolean isTransferStation) {
        if (!isTransferStation) {
            int circleWidth = scale * 2 / 29;
            g.setColor(Color.WHITE);
            g.fillOval(x - circleWidth, y - circleWidth, 2 * circleWidth, 2 * circleWidth);
            g.setColor(passed ? Color.LIGHT_GRAY : Color.BLACK);
            var stroke = new BasicStroke((float) (scale * 0.021));
            var previousStroke = g.getStroke();
            g.setStroke(stroke);
            g.drawOval(x - circleWidth, y - circleWidth, 2 * circleWidth, 2 * circleWidth);
            g.setStroke(previousStroke);
        } else {
            try {
                int circleWidth = scale * 2 / 17;
                g.setColor(Color.WHITE);
                g.fillOval(x - circleWidth, y - circleWidth, 2 * circleWidth, 2 * circleWidth);
                g.drawImage(passed ? lightGrayTransferPatternImage : blackTransferPatternImage, x - circleWidth, y - circleWidth, 2 * circleWidth, 2 * circleWidth, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void drawStation(NativeImage nativeImage, int x, int y, float heightScale, int lines, boolean passed, boolean isTransferStation) {
        if (!isTransferStation) {
            for (int offsetX = -lineSize; offsetX < lineSize; offsetX++) {
                for (int offsetY = -lineSize; offsetY < lineSize; offsetY++) {
                    final int extraOffsetY = offsetY > 0 ? (int) (lines * lineSpacing * heightScale) : 0;
                    final int repeatDraw = offsetY == 0 ? (int) (lines * lineSpacing * heightScale) : 0;
                    final double squareSum = (offsetX + 0.5) * (offsetX + 0.5) + (offsetY + 0.5) * (offsetY + 0.5);

                    if (squareSum <= 0.3 * lineSize * lineSize) {
                        for (int i = 0; i <= repeatDraw; i++) {
                            drawPixelSafe(nativeImage, x + offsetX, y + offsetY + extraOffsetY + i, ARGB_WHITE);
                        }
                    } else if (squareSum <= 0.6 * lineSize * lineSize) {
                        for (int i = 0; i <= repeatDraw; i++) {
                            drawPixelSafe(nativeImage, x + offsetX, y + offsetY + extraOffsetY + i, passed ? ARGB_LIGHT_GRAY : ARGB_BLACK);
                        }
                    }
                }
            }
        } else {
            try {
                for (int offsetX = -lineSize; offsetX < lineSize; offsetX++) {
                    for (int offsetY = -lineSize; offsetY < lineSize; offsetY++) {
                        final int extraOffsetY = offsetY > 0 ? (int) (lines * lineSpacing * heightScale) : 0;
                        final int repeatDraw = offsetY == 0 ? (int) (lines * lineSpacing * heightScale) : 0;
                        final double squareSum = (offsetX + 0.5) * (offsetX + 0.5) + (offsetY + 0.5) * (offsetY + 0.5);

                        if (squareSum <= 0.8 * lineSize * lineSize) {
                            for (int i = 0; i <= repeatDraw; i++) {
                                drawPixelSafe(nativeImage, x + offsetX, y + offsetY + extraOffsetY + i, ARGB_WHITE);
                            }
                        }
                    }
                }
                int dtr = Config.getClient().getDynamicTextureResolution();
                int circleWidth = scale * 2 / 17;
                drawResource(nativeImage, UFEInfo.MOD_ID, TRANSFER_RESOURCE, x - circleWidth, y - circleWidth, 2 * circleWidth, 2 * circleWidth, false, 0, 1, passed ? ARGB_LIGHT_GRAY : ARGB_BLACK, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void drawString(NativeImage nativeImage, byte[] pixels, int x, int y, int[] textDimensions, IGui.HorizontalAlignment horizontalAlignment, IGui.VerticalAlignment verticalAlignment, int backgroundColor, int textColor, boolean rotate90) {
        if (((backgroundColor >> 24) & 0xFF) > 0) {
            for (int drawX = 0; drawX < textDimensions[rotate90 ? 1 : 0]; drawX++) {
                for (int drawY = 0; drawY < textDimensions[rotate90 ? 0 : 1]; drawY++) {
                    drawPixelSafe(nativeImage, (int) horizontalAlignment.getOffset(drawX + x, textDimensions[rotate90 ? 1 : 0]), (int) verticalAlignment.getOffset(drawY + y, textDimensions[rotate90 ? 0 : 1]), backgroundColor);
                }
            }
        }
        int drawX = 0;
        int drawY = rotate90 ? textDimensions[0] - 1 : 0;
        for (int i = 0; i < textDimensions[0] * textDimensions[1]; i++) {
            blendPixel(nativeImage, (int) horizontalAlignment.getOffset(x + drawX, textDimensions[rotate90 ? 1 : 0]), (int) verticalAlignment.getOffset(y + drawY, textDimensions[rotate90 ? 0 : 1]), ((pixels[i] & 0xFF) << 24) + (textColor & RGB_WHITE));
            if (rotate90) {
                drawY--;
                if (drawY < 0) {
                    drawY = textDimensions[0] - 1;
                    drawX++;
                }
            } else {
                drawX++;
                if (drawX == textDimensions[0]) {
                    drawX = 0;
                    drawY++;
                }
            }
        }
    }

    private static void drawStringPixelated(NativeImage nativeImage, byte[] pixels, int[] textDimensions, int textColor, boolean fullPixel) {
        final int yOffset = (textDimensions[1] * (fullPixel ? 1 : PIXEL_SCALE) - nativeImage.getHeight()) / 2;
        int drawX = 0;
        int drawY = 0;
        for (int i = 0; i < textDimensions[0] * textDimensions[1]; i++) {
            if ((pixels[i] & 0xFF) > 0x7F) {
                if (fullPixel) {
                    drawPixelSafe(nativeImage, drawX, drawY - yOffset, textColor);
                } else {
                    for (int j = 0; j < 3; j++) {
                        for (int k = 0; k < 3; k++) {
                            drawPixelSafe(nativeImage, drawX * PIXEL_SCALE + j, drawY * PIXEL_SCALE + k - yOffset, textColor);
                        }
                    }
                }
            }
            drawX++;
            if (drawX == textDimensions[0]) {
                drawX = 0;
                drawY++;
            }
        }
    }

    private static void drawResource(NativeImage nativeImage, String resource, int x, int y, int width, int height, boolean flipX, float v1, float v2, int color, boolean useActualColor) {
        ResourceManagerHelper.readResource(new Identifier(Init.MOD_ID, resource), inputStream -> {
            try {
                final NativeImage nativeImageResource = NativeImage.read(NativeImageFormat.getAbgrMapped(), inputStream);
                final int resourceWidth = nativeImageResource.getWidth();
                final int resourceHeight = nativeImageResource.getHeight();
                for (int drawX = 0; drawX < width; drawX++) {
                    for (int drawY = Math.round(v1 * height); drawY < Math.round(v2 * height); drawY++) {
                        final float pixelX = (float) drawX / width * resourceWidth;
                        final float pixelY = (float) drawY / height * resourceHeight;
                        final int floorX = (int) pixelX;
                        final int floorY = (int) pixelY;
                        final int ceilX = floorX + 1;
                        final int ceilY = floorY + 1;
                        final float percentX1 = ceilX - pixelX;
                        final float percentY1 = ceilY - pixelY;
                        final float percentX2 = pixelX - floorX;
                        final float percentY2 = pixelY - floorY;
                        final int pixel1 = nativeImageResource.getColor(MathHelper.clamp(floorX, 0, resourceWidth - 1), MathHelper.clamp(floorY, 0, resourceHeight - 1));
                        final int pixel2 = nativeImageResource.getColor(MathHelper.clamp(ceilX, 0, resourceWidth - 1), MathHelper.clamp(floorY, 0, resourceHeight - 1));
                        final int pixel3 = nativeImageResource.getColor(MathHelper.clamp(floorX, 0, resourceWidth - 1), MathHelper.clamp(ceilY, 0, resourceHeight - 1));
                        final int pixel4 = nativeImageResource.getColor(MathHelper.clamp(ceilX, 0, resourceWidth - 1), MathHelper.clamp(ceilY, 0, resourceHeight - 1));
                        final int newColor;
                        if (useActualColor) {
                            newColor = invertColor(pixel1);
                        } else {
                            final float luminance1 = ((pixel1 >> 24) & 0xFF) * percentX1 * percentY1;
                            final float luminance2 = ((pixel2 >> 24) & 0xFF) * percentX2 * percentY1;
                            final float luminance3 = ((pixel3 >> 24) & 0xFF) * percentX1 * percentY2;
                            final float luminance4 = ((pixel4 >> 24) & 0xFF) * percentX2 * percentY2;
                            newColor = (color & RGB_WHITE) + ((int) (luminance1 + luminance2 + luminance3 + luminance4) << 24);
                        }
                        blendPixel(nativeImage, (flipX ? width - drawX - 1 : drawX) + x, drawY + y, newColor);
                    }
                }
            } catch (Exception e) {
                Init.LOGGER.error("", e);
            }
        });
    }

    private static void drawResource(NativeImage nativeImage, String modID, String resource, int x, int y, int width, int height, boolean flipX, float v1, float v2, int color, boolean useActualColor) {
        ResourceManagerHelper.readResource(new Identifier(modID, resource), inputStream -> {
            try {
                final NativeImage nativeImageResource = NativeImage.read(NativeImageFormat.getAbgrMapped(), inputStream);
                final int resourceWidth = nativeImageResource.getWidth();
                final int resourceHeight = nativeImageResource.getHeight();
                for (int drawX = 0; drawX < width; drawX++) {
                    for (int drawY = Math.round(v1 * height); drawY < Math.round(v2 * height); drawY++) {
                        final float pixelX = (float) drawX / width * resourceWidth;
                        final float pixelY = (float) drawY / height * resourceHeight;
                        final int floorX = (int) pixelX;
                        final int floorY = (int) pixelY;
                        final int ceilX = floorX + 1;
                        final int ceilY = floorY + 1;
                        final float percentX1 = ceilX - pixelX;
                        final float percentY1 = ceilY - pixelY;
                        final float percentX2 = pixelX - floorX;
                        final float percentY2 = pixelY - floorY;
                        final int pixel1 = nativeImageResource.getColor(MathHelper.clamp(floorX, 0, resourceWidth - 1), MathHelper.clamp(floorY, 0, resourceHeight - 1));
                        final int pixel2 = nativeImageResource.getColor(MathHelper.clamp(ceilX, 0, resourceWidth - 1), MathHelper.clamp(floorY, 0, resourceHeight - 1));
                        final int pixel3 = nativeImageResource.getColor(MathHelper.clamp(floorX, 0, resourceWidth - 1), MathHelper.clamp(ceilY, 0, resourceHeight - 1));
                        final int pixel4 = nativeImageResource.getColor(MathHelper.clamp(ceilX, 0, resourceWidth - 1), MathHelper.clamp(ceilY, 0, resourceHeight - 1));
                        final int newColor;
                        if (useActualColor) {
                            newColor = invertColor(pixel1);
                        } else {
                            final float luminance1 = ((pixel1 >> 24) & 0xFF) * percentX1 * percentY1;
                            final float luminance2 = ((pixel2 >> 24) & 0xFF) * percentX2 * percentY1;
                            final float luminance3 = ((pixel3 >> 24) & 0xFF) * percentX1 * percentY2;
                            final float luminance4 = ((pixel4 >> 24) & 0xFF) * percentX2 * percentY2;
                            newColor = (color & RGB_WHITE) + ((int) (luminance1 + luminance2 + luminance3 + luminance4) << 24);
                        }
                        blendPixel(nativeImage, (flipX ? width - drawX - 1 : drawX) + x, drawY + y, newColor);
                    }
                }
            } catch (Exception e) {
                Init.LOGGER.error("", e);
            }
        });
    }

    private static void blendPixel(NativeImage nativeImage, int x, int y, int color) {
        if (Utilities.isBetween(x, 0, nativeImage.getWidth() - 1) && Utilities.isBetween(y, 0, nativeImage.getHeight() - 1)) {
            final float percent = (float) ((color >> 24) & 0xFF) / 0xFF;
            if (percent > 0) {
                final int existingPixel = nativeImage.getColor(x, y);
                final boolean existingTransparent = ((existingPixel >> 24) & 0xFF) == 0;
                final int r1 = existingTransparent ? 0xFF : (existingPixel & 0xFF);
                final int g1 = existingTransparent ? 0xFF : ((existingPixel >> 8) & 0xFF);
                final int b1 = existingTransparent ? 0xFF : ((existingPixel >> 16) & 0xFF);
                final int r2 = (color >> 16) & 0xFF;
                final int g2 = (color >> 8) & 0xFF;
                final int b2 = color & 0xFF;
                final float inversePercent = 1 - percent;
                final int finalColor = ARGB_BLACK | (((int) (r1 * inversePercent + r2 * percent) << 16) + ((int) (g1 * inversePercent + g2 * percent) << 8) + (int) (b1 * inversePercent + b2 * percent));
                drawPixelSafe(nativeImage, x, y, finalColor);
            }
        }
    }

    @Unique
    private static long serializeExit(String exitName) {
        final char[] characters = exitName.toCharArray();
        long code = 0;
        for (final char character : characters) {
            code = code << 8;
            code += character;
        }
        return code;
    }

    private static void drawPixelSafe(NativeImage nativeImage, int x, int y, int color) {
        if (Utilities.isBetween(x, 0, nativeImage.getWidth() - 1) && Utilities.isBetween(y, 0, nativeImage.getHeight() - 1)) {
            nativeImage.setPixelColor(x, y, invertColor(color));
        }
    }

    private static int invertColor(int color) {
        return ((color & ARGB_BLACK) != 0 ? ARGB_BLACK : 0) + ((color & 0xFF) << 16) + (color & 0xFF00) + ((color & 0xFF0000) >> 16);
    }

    private static void clearColor(NativeImage nativeImage, int color) {
        for (int x = 0; x < nativeImage.getWidth(); x++) {
            for (int y = 0; y < nativeImage.getHeight(); y++) {
                if (nativeImage.getColor(x, y) == color) {
                    nativeImage.setPixelColor(x, y, 0);
                }
            }
        }
    }

    private static class StationPosition {

        private final float x;
        private final float y;
        private final boolean isCommon;

        private StationPosition(float x, float y, boolean isCommon) {
            this.x = x;
            this.y = y;
            this.isCommon = isCommon;
        }
    }

    private static class StationPositionGrouped {

        private final TCMRouteMapGeneratorV2.StationPosition stationPosition;
        private final int stationOffset;
        private final int stationCurrentAtOffset;
        private final IntArrayList interchangeColors;
        private final ObjectArrayList<String> interchangeNames;

        private StationPositionGrouped(TCMRouteMapGeneratorV2.StationPosition stationPosition, int stationOffset, int stationCurrentAtOffset, IntArrayList interchangeColors, ObjectArrayList<String> interchangeNames) {
            this.stationPosition = stationPosition;
            this.stationOffset = stationOffset;
            this.stationCurrentAtOffset = stationCurrentAtOffset;
            this.interchangeColors = interchangeColors;
            this.interchangeNames = interchangeNames;
        }
    }
}
