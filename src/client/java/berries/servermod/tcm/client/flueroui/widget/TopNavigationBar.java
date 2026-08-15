package berries.servermod.tcm.client.flueroui.widget;

import berries.servermod.tcm.client.flueroui.FlueroUI;
import berries.servermod.tcm.client.flueroui.TextDrawer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TopNavigationBar extends AbstractWidget {
    private final List<Item> items;

    public TopNavigationBar(int i, int j, int k, int l, List<Item> items, int selected) {
        super(i, j, k, l, Component.empty());
        this.items = new ArrayList<>();
        items.forEach((item) -> this.items.add(item.id, item));
        if (selected >= 0 && selected < items.size()) {
            this.items.get(selected).selected = true;
        }
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        return super.mouseClicked(d, e, i);
    }

    @Override
    public void onClick(double i, double j) {
        int itemWidth = width / items.size();
        if (i > getX() && i < getX() + width && j > getY() && j < getY() + height) {
            int selected = (int) Math.floor(i / itemWidth);
            if (selected < items.size()) {
                setSelected(selected);
            }
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, FlueroUI.BACKGROUND_HIGH);
        int itemWidth = width / items.size();
        int bottomLineWidth = 12;
        int bottomLineHeight = 2;
        boolean bl = false;
        for (Item item : items) {
            int index = item.id;
            int startX = getX() + (itemWidth * index);
            int endX = startX + itemWidth;
            if (i > startX && i < endX && j > getY() && j < getY() + height) {
                guiGraphics.fill(startX, getY(), startX + itemWidth, getY() + height, FlueroUI.BACKGROUND_HIGH + FlueroUI.argb(0, 22, 22, 22));
            }
            if (item.selected && !bl) {
                bl = true;
                int bottomLineSidePadding = (int)((endX - startX - bottomLineWidth) / 2.0);
                int bottomLineX = startX + bottomLineSidePadding;
                guiGraphics.fill(bottomLineX, getY() + height - bottomLineHeight, endX - bottomLineSidePadding, getY() + height, FlueroUI.PRIMARY_COLOR);
            }
            TextDrawer.drawText(guiGraphics, Minecraft.getInstance().font, item.name, TextDrawer.Alignment.CENTER, startX + (endX - startX) / 2, getY() + getHeight() / 2 - 4, FlueroUI.textColor(0xFFFFFF));
        }
    }

    public void setSelected(int index) {
        for (Item item : items) {
            item.selected = item.id == index;
        }
    }

    @Nullable
    public Item getSelected() {
        for (Item item : items) {
            if (item.selected) {
                return item;
            }
        }
        return null;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    public List<Item> getItems() {
        return items;
    }

    public static final class Item {
        private final int id;
        private final Component name;
        boolean selected;

        public Item(int id, Component name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public Component getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Item) obj;
            return this.id == that.id &&
                    Objects.equals(this.name, that.name) &&
                    this.selected == that.selected;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, selected);
        }
    }
}
