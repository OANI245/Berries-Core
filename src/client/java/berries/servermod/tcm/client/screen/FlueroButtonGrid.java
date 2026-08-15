package berries.servermod.tcm.client.screen;

import berries.servermod.tcm.client.Config;
import berries.servermod.tcm.client.flueroui.widget.FlueroButton;
import berries.servermod.tcm.client.packet.PacketGetItemClient;
import berries.servermod.tcm.client.screen.widget.MetroButton;
import berries.servermod.tcm.signal.ItemGettingSignal;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class FlueroButtonGrid {
    public static final FlueroButtonGrid INSTANCE = new FlueroButtonGrid();
    public static final int MAX_BACKGROUND_INDEX = 3;

    public int halfPadding = 2;

    private FlueroButtonGrid() {}

    public FlueroButton[] newButtons(AtomicInteger heights, int screenX, int screenY, int screenWidth, int screenPadding, FlueroButtonData[] data, boolean active) {
        FlueroButton[] items = new FlueroButton[data.length];
        for (int i = 0; i < data.length; i++) {
            FlueroButtonData itemData = data[i];
            if (itemData == null) continue;
            items[i] = new FlueroButton(
                    this.getChildX(screenX + screenPadding, screenWidth - screenX - screenPadding * 2, i, this.halfPadding),
                    this.getChildY(screenY, screenWidth - screenX - screenPadding * 2, 24, i, halfPadding),
                    this.getChildWidth(screenWidth - screenX - screenPadding * 2, halfPadding * 2),
                    20,
                    itemData.text, false, itemData.onPress
            );
            items[i].active = active;
        }
        if (items[items.length - 1] != null) {
            heights.addAndGet(items[items.length - 1].getY() + 20 + 8 - screenY);
        }
        return items;
    }

    private int getChildX(int screenX, int screenWidth, int index, int offset) {
        if (screenWidth < 170) {
            return screenX + offset;
        } else if (screenWidth < 340) {
            return screenX + (((screenWidth - offset) / 2 + offset * 2) * (index % 2)) + offset;
        } else if (screenWidth < 500) {
            return screenX + (((screenWidth - offset) / 3 + offset * 2) * (index % 3)) + offset;
        } else if (screenWidth < 660) {
            return screenX + (((screenWidth - offset) / 4 + offset * 2) * (index % 4)) + offset;
        } else {
            return screenX + (((screenWidth - offset) / 5 + offset * 2) * (index % 5)) + offset;
        }
    }

    private int getChildY(int screenY, int screenWidth, int widgetHeight, int index, int offset) {
        if (screenWidth < 170) {
            return screenY + (widgetHeight + offset) * index + offset;
        } else if (screenWidth < 340) {
            return screenY + (int) ((widgetHeight + offset) * Math.floor((float) index / 2) + offset);
        } else if (screenWidth < 500) {
            return screenY + (int) ((widgetHeight + offset) * Math.floor((float) index / 3) + offset);
        } else if (screenWidth < 660) {
            return screenY + (int) ((widgetHeight + offset) * Math.floor((float) index / 4) + offset);
        } else {
            return screenY + (int) ((widgetHeight + offset) * Math.floor((float) index / 5) + offset);
        }
    }

    private int getChildWidth(int screenWidth, int padding) {
        if (screenWidth < 170) {
            return screenWidth - padding * 2;
        } else if (screenWidth < 340) {
            return screenWidth / 2 - padding * 2;
        } else if (screenWidth < 500) {
            return screenWidth / 3 - padding * 2;
        } else if (screenWidth < 660) {
            return screenWidth / 4 - padding * 2;
        } else {
            return screenWidth / 5 - padding * 2;
        }
    }

    public FlueroButtonData dataOfGetItem(Component text, ItemGettingSignal instance, int count) {
        return new FlueroButtonData(text, (btn) -> {
            if (Minecraft.getInstance().player != null) {
                PacketGetItemClient.sendGetItemC2S(instance.name(), count);
                Config.INSTANCE.saveConfig();
                Minecraft.getInstance().setScreen(null);
            }
        });
    }

    public static class FlueroButtonData {
        Component text;
        Consumer<FlueroButton> onPress;

        public FlueroButtonData(Component text, Consumer<FlueroButton> onPress) {
            this.text = text;
            this.onPress = onPress;
        }
    }
}
