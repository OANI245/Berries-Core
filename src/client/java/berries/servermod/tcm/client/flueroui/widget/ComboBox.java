package berries.servermod.tcm.client.flueroui.widget;

import berries.servermod.tcm.client.flueroui.FlueroUI;
import berries.servermod.tcm.client.flueroui.Icons;
import berries.servermod.tcm.client.flueroui.TextDrawer;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphics;

public class ComboBox extends FlueroButton {
    protected List<Component> items;
    protected int value;
    protected boolean opened = false;
    protected Consumer<Integer> onValueChanged;
    protected final Consumer<BiConsumer<ObjectObjectImmutablePair<GuiGraphics, Float>, IntIntImmutablePair>> latestRendererApplier;

    public ComboBox(int x, int y, int width, int height, List<Component> items, int value, Consumer<BiConsumer<ObjectObjectImmutablePair<GuiGraphics, Float>, IntIntImmutablePair>> latestRendererApplier) {
        super(x, y, width, height, Component.empty(), ((cb) -> {}));
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
        this.items = new ArrayList<>(items);
        this.value = Math.min(value, items.size() - 1);
        this.setMessage(items.get(this.value));
        this.latestRendererApplier = latestRendererApplier;
    }

    public ComboBox(int x, int y, int width, int height, List<Component> items, Consumer<BiConsumer<ObjectObjectImmutablePair<GuiGraphics, Float>, IntIntImmutablePair>> latestRendererApplier) {
        this(x, y, width, height, items, 0, latestRendererApplier);
    }

    public boolean isOpened() {
        return opened;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = 0; i < items.size(); i++) {
            if (opened && mouseY > (this.getY() + 2 + this.getHeight()) && mouseX >= (this.getX()) && mouseX < (this.getX() + this.getWidth()) && mouseY < getY() + 3 + this.height + (16 * (i + 1))) {
                this.value = i;
                this.setMessage(items.get(i));
                onValueChanged.accept(i);
                closeList();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onPress() {
        if (opened) {
            closeList();
        } else {
            openList();
        }
    }

    public void openList() {
        this.opened = true;
    }

    public void closeList() {
        this.opened = false;
    }

    public Component getCurrentValue() {
        return this.items.get(this.value);
    }

    public int getValue() {
        return value;
    }

    public void onValueChanged(Consumer<Integer> listener) {
        this.onValueChanged = listener;
    }

    @Override
    @Deprecated(forRemoval = true)
    public void setAccent(boolean bl) {
        throw new RuntimeException("Not supported");
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        int buttonBackground = (this.isAccentStyle ?
                (this.isHovered() ? FlueroUI.PRIMARY_COLOR - FlueroUI.argb(0, 24,24,24) : FlueroUI.PRIMARY_COLOR) :
                (this.isHovered() ? FlueroUI.SECONDARY_COLOR + FlueroUI.argb(0, 24,24,24) : FlueroUI.SECONDARY_COLOR));
        int buttonBorder = (this.isAccentStyle ?
                (this.isHovered() ? FlueroUI.PRIMARY_LOW - FlueroUI.argb(0, 24,24,24) : FlueroUI.PRIMARY_LOW):
                (this.isHovered() ? FlueroUI.SECONDARY_HIGH + FlueroUI.argb(0, 24,24,24) : FlueroUI.SECONDARY_HIGH));
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, buttonBackground);
        guiGraphics.fill(getX(), getY() + height - 1, getX() + width, getY() + height, buttonBorder);
        guiGraphics.fill(getX(), getY(), getX() + 1, getY() + height, buttonBorder);
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + 1, buttonBorder);
        guiGraphics.fill(getX() + width - 1, getY(), getX() + width, getY() + height, buttonBorder);
        if (this.isFocused()) {
            guiGraphics.fill(getX(), getY() + height, getX() + width, getY() + height + 1, FlueroUI.rgb(255, 255, 255));
            guiGraphics.fill(getX() - 1, getY(), getX(), getY() + height, FlueroUI.rgb(255, 255, 255));
            guiGraphics.fill(getX(), getY() - 1, getX() + width, getY(), FlueroUI.rgb(255, 255, 255));
            guiGraphics.fill(getX() + width, getY(), getX() + width + 1, getY() + height, FlueroUI.rgb(255, 255, 255));
        }
        TextDrawer.drawText(guiGraphics, minecraft.font, this.getMessage(), TextDrawer.Alignment.LEFT, (this.getX() + 5), (this.getY() + height / 2 - minecraft.font.lineHeight / 2), FlueroUI.textColor(0xFFFFFF), false);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                guiGraphics.blit(opened ? Icons.ARROW_UP : Icons.ARROW_DOWN,
                getX() + width - 12, getY() + 5, 0, 0, 9, 9, 9, 9);

        if (opened) {
            if (latestRendererApplier != null) {
                latestRendererApplier.accept((p0, p1) -> {
                    renderComboBoxList(p0.left(), p1.leftInt(), p1.rightInt());
                });
            } else {
                renderComboBoxList(guiGraphics, mouseX, mouseY);
            }
        }
    }

    protected void renderComboBoxList(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.fill(getX(), getY() + 2 + this.height, getX() + this.width, getY() + 3 + this.height + items.size() * 16, FlueroUI.rgb(60, 60, 60));
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            if (mouseX > getX() && mouseY > getY() + 2 + this.height + (16 * i) && mouseX < getX() + this.width && mouseY < getY() + 3 + this.height + (16 * (i + 1))) {
                guiGraphics.fill(getX(), getY() + 2 + this.height + (16 * i), getX() + this.width, getY() + 2 + this.height + (16 * (i + 1)), FlueroUI.rgb(72, 72, 72));
            }
            TextDrawer.drawText(guiGraphics, Minecraft.getInstance().font, item, TextDrawer.Alignment.LEFT, getX() + 5, getY() + 2 + this.height + (8 - Minecraft.getInstance().font.lineHeight / 2) + 16 * i, FlueroUI.textColor(0xFFFFFF), false);
        }
    }
}
