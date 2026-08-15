package berries.servermod.tcm.client.vehicle.processing.vehicle.trm02;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.client.render.GraphicsTexture;
import berries.servermod.tcm.client.vehicle.processing.screens.ScreenDisplayDrawer;
import berries.servermod.tcm.util.VehicleWrapper;
import org.mtr.mapping.holder.Identifier;
import org.mtr.mapping.mapper.ResourceManagerHelper;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

public interface TRM02TrainNumberDrawer {
    Font ARIAL_FONT = getFont("mtr:font/noto-sans-semibold.ttf");

    static void draw(VehicleWrapper wrapper, GraphicsTexture tex, Map<Byte, Object> states, int car) {
        if (!states.containsKey((byte) 0x7a) || states.get((byte) 0x7a) == null) {
            return;
        }
        var trainNumber = String.format("01%03d", Integer.parseInt((String) states.get((byte) 0x7a)));
        var trainNumberWithCar = trainNumber + (car + 1);
        var g = tex.graphics;
        /*g.setColor(Color.BLACK);
        g.fillRect(0, 0, 640, 96);*/
        g.setColor(Color.WHITE);
        g.setFont(ARIAL_FONT.deriveFont(Font.PLAIN, 120));
        var x0 = (640 - g.getFontMetrics().stringWidth(trainNumberWithCar)) / 2;
        var a0 = ScreenDisplayDrawer.createFallbackString(trainNumberWithCar, ARIAL_FONT.deriveFont(Font.PLAIN, 120), ARIAL_FONT.deriveFont(Font.PLAIN, 120));
        g.drawString(a0.getIterator(), x0, 94);
        g.setColor(new Color(218, 52, 11));
        var fs0 = 142;
        var uy = 263;
        g.setFont(ARIAL_FONT.deriveFont(Font.PLAIN, fs0));
        var a1 = ScreenDisplayDrawer.createFallbackString(trainNumberWithCar, ARIAL_FONT.deriveFont(Font.PLAIN, fs0), ARIAL_FONT.deriveFont(Font.PLAIN, fs0));
        g.drawString(a1.getIterator(), 16, uy);

        var fs1 = 82;
        g.setColor(new Color(218, 52, 11));
        g.setFont(ARIAL_FONT.deriveFont(Font.PLAIN, fs1));
        var y1 = 267 + 108;
        var a2x = ScreenDisplayDrawer.createFallbackString(trainNumber,
                ARIAL_FONT.deriveFont(Font.PLAIN, fs1), ARIAL_FONT.deriveFont(Font.PLAIN, fs1));
        g.drawString(a2x.getIterator(), (344 - g.getFontMetrics().stringWidth(trainNumber)) / 2, y1);

        //dwy 382
        var dmy = 382;
        var rw = 100;
        var bw = 4;
        g.setColor(new Color(20, 48, 113));
        g.fillRect(0, dmy, rw * 5, rw * 2);
        g.setColor(Color.WHITE);
        g.fillRect(0, dmy, rw * 5, bw);
        g.fillRect(0, dmy, bw, rw * 2);
        for (int i = 0; i < 2; i ++) {
            g.fillRect(0, dmy + (rw * (i + 1) - bw), rw * 5, bw * 2);
            for (int j = 0; j < 5; j ++) {
                g.fillRect(rw * (j + 1) - bw, dmy + rw * j, bw * 2, rw);
                var fs2 = 32;
                var fs3 = 42;
                g.setFont(ARIAL_FONT.deriveFont(Font.PLAIN, fs2));
                var tl0 = g.getFontMetrics().stringWidth(trainNumber);
                g.drawString(trainNumber, rw * j + (rw - tl0) / 2, dmy + rw * i + 8 + fs2);
                var text1 = String.format("%d%d", car + 1,  i == 0 ? 5 - j : j + 1);
                var tl1 = g.getFontMetrics().stringWidth(text1);
                g.drawString(text1, rw * j + (rw - tl1) / 2 + 14, dmy + rw * i + 55 + fs2);
                g.setFont(ARIAL_FONT.deriveFont(Font.PLAIN, fs3));
                g.drawString(i == 0 ? "A" : "B", rw * j + (rw - tl1) / 2 - 14, dmy + rw * i + 55 + fs2);
            }
        }

        g.setColor(new Color(44, 39, 35));
        g.fillRect(0, 582, 239, 58);
        g.setColor(Color.WHITE);
        g.setFont(ARIAL_FONT.deriveFont(Font.PLAIN, 56));
        var tlp = g.getFontMetrics().stringWidth(trainNumber);
        g.drawString(trainNumber, 239 - tlp, 639);
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
}
