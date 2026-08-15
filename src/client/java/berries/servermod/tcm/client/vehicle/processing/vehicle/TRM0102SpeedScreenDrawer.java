package berries.servermod.tcm.client.vehicle.processing.vehicle;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.client.render.GraphicsTexture;
import berries.servermod.tcm.client.util.texture.AWTDrawUtil;
import berries.servermod.tcm.util.VehicleWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.mtr.core.data.PathData;
import org.mtr.mapping.holder.Identifier;
import org.mtr.mapping.mapper.ResourceManagerHelper;
import org.mtr.mod.client.MinecraftClientData;
import org.mtr.mod.data.IGui;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface TRM0102SpeedScreenDrawer {
    Font APH_FONT = AWTDrawUtil.getResourceFont("modernui:font/aph.ttf");
    Font ARIAL_FONT = AWTDrawUtil.getResourceFont("mtr:font/noto-sans-semibold.ttf");

    BufferedImage TIANCHENGGUIDAOJIAOTONG_LOGO = AWTDrawUtil.getResourceImage("mtr:textures/block/sign/logo.png");

    static void draw(VehicleWrapper wrapper, GraphicsTexture tex, int lineNumber, Map<Byte, Object> states, PathData path) {
        var g = tex.graphics;
        var du = new AWTDrawUtil(g);
        var v = wrapper.getVehicle();
        Color c;
        var stations = wrapper.getThisRouteStops();
        var route = !stations.isEmpty() ? stations.get(0).route : null;
        if (route == null) {
            c = Color.WHITE;
        } else {
            c = new Color(route.getColor());
        }
        g.setColor(Color.BLACK);
        g.fillRect(0, 120, 640, 427);
        g.setColor(c);
        g.fillRect(0, 500, 640, 47);
        double leftPadding0 = 0.0;
        try {
            if (TIANCHENGGUIDAOJIAOTONG_LOGO != null) {
                g.drawImage(TIANCHENGGUIDAOJIAOTONG_LOGO, 23, 138, 74, 74, null);
                leftPadding0 = 96.0;
            }
        } catch (Exception ignored) {
        }
        g.setColor(Color.WHITE);
        try {
            var trainNumber = String.format("0%d%03d", lineNumber, Integer.parseInt((String) states.get((byte) 0x7a)));
            du.drawText("Train " + trainNumber,
                    ARIAL_FONT.deriveFont(Font.PLAIN, 48), (float) leftPadding0 + 24, 120 + 72, null, IGui.VerticalAlignment.BOTTOM, IGui.HorizontalAlignment.LEFT);
        } catch (NumberFormatException ignored) {
        }
        var speedText = String.format("%.1f", v.getSpeed() * 1000 * 3.6);
        g.setFont(APH_FONT.deriveFont(Font.PLAIN, 130));
        var speedTextLength = g.getFontMetrics().stringWidth(speedText);
        var leftPadding1 = (640 - speedTextLength) / 2 - 78;
        du.drawText(speedText, null, leftPadding1, 390, null, IGui.VerticalAlignment.BOTTOM, IGui.HorizontalAlignment.LEFT);
        du.drawText("km/h", APH_FONT.deriveFont(Font.PLAIN, 64)
                , leftPadding1 + speedTextLength + 24, 390, Color.LIGHT_GRAY, IGui.VerticalAlignment.BOTTOM, IGui.HorizontalAlignment.LEFT);
        if (v.vehicleExtraData.getIsCurrentlyManual()) {
            var bestSpeedText = (path != null && path.getDwellTime() > 0) ? "↘ 进站" : (v.getIsOnRoute() ? "↗ " + v.getSpeedLimitKilometersPerHour() + "km/h" : "车辆段停放");
            du.drawText(bestSpeedText, APH_FONT.deriveFont(Font.BOLD, 60),
                    (float) 640 / 2, 485, Color.GRAY, IGui.VerticalAlignment.BOTTOM, IGui.HorizontalAlignment.CENTER);
        } else {
            du.drawText("Automatic Mode", ARIAL_FONT.deriveFont(Font.BOLD, 60),
                    (float) 640 / 2, 485, new Color(147, 112, 219), IGui.VerticalAlignment.BOTTOM, IGui.HorizontalAlignment.CENTER);
        }
    }
}
