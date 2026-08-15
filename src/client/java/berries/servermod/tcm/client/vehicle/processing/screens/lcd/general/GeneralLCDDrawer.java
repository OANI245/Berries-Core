package berries.servermod.tcm.client.vehicle.processing.screens.lcd.general;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.client.render.GraphicsTexture;
import berries.servermod.tcm.client.util.TextUtil;
import berries.servermod.tcm.client.vehicle.processing.screens.ScreenDisplayDrawer;
import berries.servermod.tcm.util.VehicleWrapper;
import org.mtr.core.data.PathData;
import org.mtr.core.tool.Utilities;
import org.mtr.mapping.holder.Identifier;
import org.mtr.mapping.mapper.ResourceManagerHelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class GeneralLCDDrawer {
    public static final String NAME = "Tiancheng Route Map Display";
    public static final Font FONT_CJK = getFont("mtr:font/noto-serif-cjk-tc-semibold.ttf");
    public static final Font FONT_ASCII = getFont("mtr:font/noto-sans-semibold.ttf");
    public static final Font FONT_DATETIME = getFont("tcm:font/mis.ttf");
    public static final BufferedImage TIANCHENGGUIDAOJIAOTONG_LOGO =
            getImage("tcm:textures/gui/tianchengguidaojiaotong_logo.png");

    public static final float SWITCH_DARK_TEXT_BACKGROUND_BRIGHTNESS = 0.67f;

    private final int width;
    private final int height;

    private final int textureWidth;
    private final int textureHeight;

    private GraphicsTexture texture = null;

    private static final Calendar CALENDER_INSTANCE = Calendar.getInstance();

    public GeneralLCDDrawer(
            int width, int height, int textureWidth, int textureHeight
    ) {
        this.width = width;
        this.height = height;

        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    public void init() {
        if (texture != null) {
            close();
        }
        texture = new GraphicsTexture(textureWidth, textureHeight);
        var g = texture.graphics;
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, this.textureWidth, this.textureHeight);
    }

    public void draw(VehicleWrapper wrapper, Map<Byte, Object> st) {
        if (texture == null) {
            throw new IllegalStateException("Graphics Texture is not loaded!");
        }

        var g = texture.graphics;

        LCDDrawInfos infos = ((LCDDrawInfos) st.getOrDefault((byte)0x7f, null));
        try {
            var thisRouteStations = wrapper.getThisRouteStops();
            var thisStationIndex = wrapper.getNextStopIndex(thisRouteStations);
            var thisStation = thisRouteStations.get(thisStationIndex);
            var thisRouteName = thisStation.route.getName();

            var paths = wrapper.getPathData();
            var path = paths.isEmpty() ? null : paths.get(
                    Utilities.getIndexFromConditionalList(
                            wrapper.getVehicle().vehicleExtraData.immutablePath,
                            wrapper.getRailProgress(0) - 1.0F));

            if (infos != null && infos.thisRouteNameCjk.equals(TextUtil.getCjkParts(thisRouteName))) {
                var rni = infos.thisRouteNumber;
                var trn = infos.thisRouteNameCjk;
                var asm = infos.animStartMillis;
                var psm = infos.pageStartMillis;
                infos = new LCDDrawInfos(
                        thisRouteStations,
                        thisStationIndex,
                        thisStation,
                        trn,
                        TextUtil.getNonCjkParts(thisRouteName),
                        thisStation.route.getColor(),
                        rni, asm, psm, path);
            } else if (thisRouteStations != null && !thisRouteStations.isEmpty() && !thisRouteName.isEmpty()) {
                var rni = -1;
                for (char c : TextUtil.getCjkParts(thisRouteName).toCharArray()) {
                    if (!isNumber(String.valueOf(c))) {
                        break;
                    }
                    if (rni < 0) {
                        rni = 0;
                    }
                    rni = rni * 10 + Integer.parseInt(String.valueOf(c));
                }
                long asm;
                long psm;
                if (infos != null) {
                    asm = infos.animStartMillis;
                    psm = infos.pageStartMillis;
                } else {
                    asm = System.currentTimeMillis();
                    psm = System.currentTimeMillis();
                }
                infos = new LCDDrawInfos(
                        thisRouteStations,
                        thisStationIndex,
                        thisStation,
                        TextUtil.getCjkParts(thisRouteName),
                        TextUtil.getNonCjkParts(thisRouteName),
                        thisStation.route.getColor(),
                        rni, asm, psm, path);
            }
        } catch (Throwable ignored) {}

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, this.width, this.height);

        if (infos == null) {
            drawEmptyPage();
            texture.upload();
            return;
        }

        drawTopBar(infos);
        drawRouteLineAndStations(infos);

        texture.upload();
        st.put((byte)0x7f, infos);
    }

    public void close() {
        if (texture != null) {
            texture.close();
            texture = null;
        }
    }

    @SuppressWarnings("all")
    private void drawTopBar(LCDDrawInfos infos) {
        var g = texture.graphics;
        var r0x = (int)Math.ceil(this.width / 6.0 * 1.8);
        var r0y = Math.max(this.height / 64, 1);
        var r0w = (int)Math.ceil(this.width / 6.0 * 2.4);
        var r0h = (int)Math.ceil(this.height / 4.1);
        var r0r = (int)Math.ceil(((double) r0h / 3.4) * 2);
        var i0x = this.width / 256;
        var i0y = -((int)(this.height / 19.2));
        var i0w = (int)Math.ceil(this.width / 6.0);
        var i0h = (int)Math.ceil(r0h / 8.0 * 12.8);
        var r1x = i0x + i0w + this.width / (1024 / 6);
        var r1y = (int)(r0y + this.height / (256 / 7.0));
        var r1w = (int)Math.ceil(this.width / 6.0 * 0.4);
        var r1h = (int)(r0h - this.height / (256 / 14.4));
        var r1r = (int)Math.ceil(((double) r1h / 3.4) * 2);
        g.drawImage(TIANCHENGGUIDAOJIAOTONG_LOGO, i0x, i0y, i0w, i0h, null);
        g.setColor(new Color(infos.thisRouteColor));
        g.fillRoundRect(r0x, r0y, r0w, r0h, r0r, r0r);
        g.fillRoundRect(r1x, r1y, r1w, r1h, r1r, r1r);

        var textOnRouteColorColor = getColorLightNess(infos.thisRouteColor) >= SWITCH_DARK_TEXT_BACKGROUND_BRIGHTNESS ?
                Color.BLACK : Color.WHITE;
        var hrx = (int)Math.ceil(r0x + r0w / 3.6);
        var hrh = (int)Math.ceil(r0h * 0.9);
        var hry = r0y + (r0h - hrh) / 2;
        var hrw = this.width / 589;

        g.setColor(textOnRouteColorColor.getRGB() == Color.BLACK.getRGB() ? new Color(0, 0, 0, 72) : new Color(255, 255, 255, 72));
        g.fillRect(hrx, hry, hrw, hrh);
        var rni = infos.thisRouteNumber;
        if (rni != -1) {
            var t0y = (int)(r1y * 1.16);
            var t0s = (int)(r1h * 0.75);
            AffineTransform stretch = new AffineTransform();
            g.setColor(textOnRouteColorColor);
            g.setFont(FONT_ASCII.deriveFont(Font.PLAIN, t0s));
            var t0w = g.getFontMetrics().stringWidth(String.valueOf(rni));
            var t0xs = Math.max((r1w / 3.0 - t0w) / 2, 1);
            var t0x = (r1x * 1.013) + t0xs;
            var t0pt = g.getTransform();
            var t0sx = Math.min(t0w, r1w / 3.0) / t0w;
            var t0fx = Math.ceil(t0x / t0sx);
            stretch.concatenate(AffineTransform.getScaleInstance(
                    t0sx, 1.0));
            g.setTransform(stretch);
            g.drawString(String.valueOf(rni), (int)t0fx, t0y + t0s);
            g.setTransform(t0pt);
            var t1y = (int)(r1y * 1.52);
            var t1x = t0x + t0w * t0sx + this.textureWidth / 192;
            var t1s = t0s / 2;
            g.setFont(FONT_CJK.deriveFont(Font.PLAIN, t1s));
            var t1w = g.getFontMetrics().stringWidth("号  线");
            g.drawString("号  线", (int) t1x, t1y + t1s);
            var t2c = infos.thisRouteNameNonCjk;
            var t2s = (int)(t0s / 2.36);
            g.setFont(FONT_ASCII.deriveFont(Font.BOLD, t2s));
            var t2yat = (int)(r1y + r1h / 16.0 * 13.5);
            var t2w = g.getFontMetrics().stringWidth(t2c);
            var t2x = t1x + (t1w - t2w) / 2;
            g.drawString(t2c, (int) t2x, t2yat);
        } else {
            AffineTransform stretch = new AffineTransform();
            var t3pt = g.getTransform();
            var t3y = (int)(r1y * 1.52);
            var t3s = (int)(r1h * 0.75 / 2);
            g.setColor(textOnRouteColorColor);
            g.setFont(FONT_CJK.deriveFont(Font.PLAIN, t3s));
            var t3w = g.getFontMetrics().stringWidth(infos.thisRouteNameCjk);
            var t3fw = (int)Math.min(t3w, r1w * 0.8);
            stretch.concatenate(AffineTransform.getScaleInstance(t3fw * 1.0 / t3w, 1.0));
            var t3x = r1x + (r1w - t3fw) / 2;
            g.setTransform(stretch);
            g.drawString(infos.thisRouteNameCjk, (int)(t3x * (t3w * 1.0 / t3fw)), t3y + t3s);
            AffineTransform stretch1 = new AffineTransform();
            var t4yat = (int)(r1y + r1h / 16.0 * 13.5);
            var t4s = (int)(r1h * 0.75 / 2.36);
            g.setFont(FONT_ASCII.deriveFont(Font.BOLD, t4s));
            var t4w = g.getFontMetrics().stringWidth(infos.thisRouteNameNonCjk);
            var t4fw = (int)Math.min(t4w, r1w * 0.8);
            var t4x = r1x + (r1w - t4fw) / 2;
            stretch1.concatenate(AffineTransform.getScaleInstance(t4fw * 1.0 / t4w, 1.0));
            g.setTransform(stretch1);
            g.drawString(infos.thisRouteNameNonCjk, (int)(t4x * (t4w * 1.0 / t4fw)), t4yat);
            g.setTransform(t3pt);
        }

        g.setColor(textOnRouteColorColor);
        var t5c = infos.pathData.getDwellTime() > 0 ? "本站" : "下一站";
        var t5s = r0h / 2.3;
        var t5y = (r0y * 2.5) + t5s;
        g.setFont(FONT_CJK.deriveFont(Font.PLAIN, (float) t5s));
        var t5w = g.getFontMetrics().stringWidth(t5c);
        var t5x = r0x + (r0w / 3.6 - t5w) / 2;
        g.drawString(t5c, (int) t5x, (int) t5y);
        var t6c = infos.pathData.getDwellTime() > 0 ? "This station" : "Next station";
        var t6s = r0h / 4.8;
        var t6y = r0y + (r0h * 0.82);
        g.setFont(FONT_ASCII.deriveFont(Font.BOLD, (float) t6s));
        var t6w = g.getFontMetrics().stringWidth(t6c);
        var t6x = r0x + (r0w / 3.54 - t6w) / 2;
        g.drawString(t6c, (int) t6x, (int) t6y);

        var thisStation = infos.thisStation;
        var t7c = TextUtil.getCjkParts(thisStation.name) + " · " + TextUtil.getNonCjkParts(thisStation.name);
        var t7s = r0h / 2.2;
        var t7ca = ScreenDisplayDrawer.createFallbackString(
                t7c, FONT_ASCII.deriveFont(Font.PLAIN, (float) t7s),
                FONT_CJK.deriveFont(Font.PLAIN, (float) t7s));
        g.setFont(FONT_CJK.deriveFont(Font.PLAIN, (float) t7s));
        var t7w = g.getFontMetrics().stringWidth(t7c);
        var t7fw = Math.min(t7w, (r0w - r0w / 3.6) * 0.91);
        AffineTransform stretch2 = new AffineTransform();
        stretch2.concatenate(AffineTransform.getScaleInstance(t7fw / t7w, 1.0));
        var t7x = r0x + (r0w / 3.6) + (r0w - r0w / 3.6 - t7fw) / 2;
        var t7y = r0y + (r0h - t7s) / 2 + t7s;
        var t7pt = g.getTransform();
        g.setTransform(stretch2);
        g.drawString(t7ca.getIterator(), (int) (t7x * (t7w / t7fw)), (int) t7y);

        AffineTransform stretch3 = new AffineTransform();
        g.setColor(Color.BLACK);
        var t8c = "终点站：" + TextUtil.getCjkParts(infos.thisRouteStations.getLast().name);
        var t8s = r0h / 2.3;
        var t8y = (r0y * 2.5) + t8s;
        g.setFont(FONT_CJK.deriveFont(Font.PLAIN, (float) t8s));
        var t8w = g.getFontMetrics().stringWidth(t8c);
        var t8fw = Math.min(t8w, this.width / 6.9);
        stretch3.concatenate(AffineTransform.getScaleInstance(t8fw / t8w, 1.0));
        g.setTransform(stretch3);
        var t8x = r0x + r0w + this.width / 72.0;
        g.drawString(t8c, (int) (t8x * ((double)t8w / t8fw)), (int) t8y);
        AffineTransform stretch4 = new AffineTransform();
        var t9c = "Destination: " + TextUtil.getNonCjkParts(infos.thisRouteStations.getLast().name);
        var t9s = r0h / 4.8;
        var t9y = r0y + (r0h * 0.82);
        g.setFont(FONT_ASCII.deriveFont(Font.BOLD, (float) t9s));
        var t9w = g.getFontMetrics().stringWidth(t9c);
        var t9fw = Math.min(t9w, this.width / 6.9);
        stretch4.concatenate(AffineTransform.getScaleInstance(t9fw / t9w, 1.0));
        g.setTransform(stretch4);
        var t9x = t8x;
        g.drawString(t9c, (int) (t9x *((double)t9w / t9fw)), (int) t9y);
        g.setTransform(t7pt);

        CALENDER_INSTANCE.setTimeInMillis(System.currentTimeMillis());
        var t10c = String.format("%d月%d日", CALENDER_INSTANCE.get(Calendar.MONTH), CALENDER_INSTANCE.get(Calendar.DAY_OF_MONTH));
        var t10s = t8s * (2.0 / 3.0);
        g.setFont(FONT_DATETIME.deriveFont(Font.PLAIN, (float) t10s));
        var t10w = g.getFontMetrics().stringWidth(t10c);
        var t10x = r0x + r0w + this.width / 5.96;
        var t10y = t8y * 0.83;
        g.drawString(t10c, (int) t10x, (int) t10y);
        var t11c = String.format("星期%s", switch (CALENDER_INSTANCE.get(Calendar.DAY_OF_WEEK)) {case 2->"一";case 3->"二";case 4->"三";case 5->"四";case 6->"五";case 7->"六";case 0->"曰";default->"?";});
        var t11s = t8s * (5.0 / 6.0);
        g.setFont(FONT_DATETIME.deriveFont(Font.PLAIN, (float) t11s));
        var t11w = g.getFontMetrics().stringWidth(t11c);
        var t11x = t10x + (t10w - t11w) / 2;
        var t11y = t8y * 1.45;
        g.drawString(t11c, (int) t11x, (int) t11y);
        g.setFont(FONT_DATETIME.deriveFont(Font.PLAIN, (float) t8s));
        var t12x = this.width / 14.0 * 13.0;
        var t12y = t8y * 1.23;
        g.drawString(String.format("%d:%02d", CALENDER_INSTANCE.get(Calendar.HOUR_OF_DAY), CALENDER_INSTANCE.get(Calendar.MINUTE)),
                (int) t12x, (int) t12y);
    }

    private void drawRouteLineAndStations(LCDDrawInfos infos) {
        //TODO RouteLine and Stations
        var g = texture.graphics;
        g.setColor(new Color(infos.thisRouteColor));
        var r0y = this.height * 0.6;
        var r0w = this.width * 0.8;
        var r0x = (this.width - r0w) / 2;
        var r0h = this.height / 24.0;
        g.fillRect((int) r0x, (int) r0y, (int) r0w, (int) r0h);
    }

    private void drawEmptyPage() {}

    private static float getColorLightNess(int rgb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = (rgb) & 0xff;
        return (float) Math.pow(Math.pow(r / 255.0f, 2.2f) + Math.pow(g / 170.0f, 2.2f) + Math.pow(b / 425.0f, 2.2f), 1 / 2.2f) * 0.547373141f;
    }

    private static boolean isNumber(String str) {
        return str.matches("^\\d+$");
    }

    static Font getFont(String path) {
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

    static BufferedImage getImage(String path) {
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

    public Identifier getIdentifier() {
        return texture.identifier;
    }

    public record LCDDrawInfos(
            List<VehicleWrapper.Stop> thisRouteStations,
            int thisStationIndex,
            VehicleWrapper.Stop thisStation,
            String thisRouteNameCjk,
            String thisRouteNameNonCjk,
            int thisRouteColor,
            int thisRouteNumber,
            long animStartMillis,
            long pageStartMillis,
            PathData pathData
    ) {
    }
}
