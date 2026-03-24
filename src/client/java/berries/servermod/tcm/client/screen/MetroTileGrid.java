package berries.servermod.tcm.client.screen;

import berries.servermod.tcm.UFEInfo;
import berries.servermod.tcm.client.Config;
import berries.servermod.tcm.client.packet.PacketTeleportClient;
import berries.servermod.tcm.client.screen.widget.MetroTile;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.Consumer;

public class MetroTileGrid {
    public static final MetroTileGrid INSTANCE = new MetroTileGrid();
    public static final int MAX_BACKGROUND_INDEX = 3;

    public int halfPadding = 2;

    private MetroTileGrid() {}

    public MetroTile[] newTiles(int screenX, int screenY, int screenWidth, int screenPadding, MetroTileData[] data, boolean active) {
        MetroTile[] items = new MetroTile[data.length];
        for (int i = 0; i < data.length; i++) {
            MetroTileData itemData = data[i];
            items[i] = new MetroTile(
                    this.getChildX(screenX + screenPadding, screenWidth - screenX - screenPadding * 2, i, this.halfPadding),
                    this.getChildY(screenY, screenWidth - screenX - screenPadding * 2, this.getChildWidth(screenWidth - screenX - screenPadding * 2, halfPadding * 2) / 16 * 7, i, halfPadding),
                    this.getChildWidth(screenWidth - screenX - screenPadding * 2, halfPadding * 2),
                    this.getChildWidth(screenWidth - screenX - screenPadding * 2, halfPadding * 2) / 16 * 7,
                    itemData.icon, itemData.title, itemData.description, itemData.onPress
            );
            items[i].active = active;
            ResourceLocation changedLocation = null;
            try {
                ResourceLocation tileLocation = new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/images/t_" + (i + 1) + ".png");
                Minecraft.getInstance().getResourceManager().getResourceOrThrow(tileLocation);
                changedLocation = tileLocation;
            } catch (IOException ignored) {
            }
            items[i].background = changedLocation;
        }
        return items;
    }

    private int getChildX(int screenX, int screenWidth, int index, int offset) {
        if (screenWidth < 380) {
            return screenX + offset;
        } else if (screenWidth < 520) {
            return screenX + (((screenWidth - offset) / 2 + offset * 2) * (index % 2)) + offset;
        } else {
            return screenX + (((screenWidth - offset) / 3 + offset * 2) * (index % 3)) + offset;
        }
    }

    private int getChildY(int screenY, int screenWidth, int widgetHeight, int index, int offset) {
        if (screenWidth < 380) {
            return screenY + (widgetHeight + offset) * index + offset;
        } else if (screenWidth < 520) {
            return screenY + (int) ((widgetHeight + offset) * Math.floor((float) index / 2) + offset);
        } else {
            return screenY + (int) ((widgetHeight + offset) * Math.floor((float) index / 3) + offset);
        }
    }

    private int getChildWidth(int screenWidth, int padding) {
        if (screenWidth < 380) {
            return screenWidth - padding * 2;
        } else if (screenWidth < 520) {
            return screenWidth / 2 - padding * 2;
        } else {
            return screenWidth / 3 - padding * 2;
        }
    }

    public MetroTileData dataOf(Component title, Component description, Consumer<MetroTile> onPress) {
        return new MetroTileData(title, description, onPress);
    }

    public MetroTileData dataOfTeleport(Component title, Component description, Vec3 pos) {
        return new MetroTileData(title, description, (btn) -> {
            if (Minecraft.getInstance().player != null) {
                PacketTeleportClient.sendTeleportC2S(pos);
                Config.INSTANCE.saveConfig();
                Minecraft.getInstance().setScreen(null);
            }
        });
    }

    public static class MetroTileData {
        Component title;
        Component description;
        Consumer<MetroTile> onPress;
        ResourceLocation icon = null;

        public MetroTileData(Component title, Component description, Consumer<MetroTile> onPress) {
            this.title = title;
            this.description = description;
            this.onPress = onPress;
        }

        public MetroTileData(Component title, Component description, ResourceLocation icon, Consumer<MetroTile> onPress) {
            this(title, description, onPress);
            this.icon = icon;
        }
    }
}
