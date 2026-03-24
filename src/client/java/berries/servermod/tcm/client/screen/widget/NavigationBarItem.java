package berries.servermod.tcm.client.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NavigationBarItem extends AbstractButton {
    public static final int TEXTURE_COUNT = 5;

    public boolean checked;
    public ResourceLocation icon;
    private int progress = 0;
    private final Consumer<NavigationBarItem> onPress;
    private final long createdTime = System.currentTimeMillis();

    public NavigationBarItem(int i, int j, int k, int l, Component component, ResourceLocation icon, Consumer<NavigationBarItem> onPress) {
        super(i, j, k, l, component);
        this.onPress = onPress;
        this.icon = icon;
    }

    @Override
    public void onPress() {
        onPress.accept(this);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (!checked) {
            return super.mouseClicked(d, e, i);
        } else {
            return false;
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        //RenderSystem.setShaderTexture(0, icon);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);

        if (progress < 100) {
            long currentTime = System.currentTimeMillis();
            progress = (int) smoothEnds(0.0F, 100.0F, 0.0F, 0.52F, (currentTime - createdTime) / 1000.0F);
        }

        int ax = getX() - 25 * 5 / 4 + (progress * 5 / 16);

        int textureX = 0;
        if (!active) {
            textureX = width * 4;
        } else if (checked && isHoveredOrFocused()) {
            textureX = width * 2;
        } else if (isHoveredOrFocused()) {
            textureX = width * 3;
        } else if (checked) {
            textureX = width;
        }

        guiGraphics.blit(icon, ax, getY(), textureX, 0, width, height, width * 5, height);
    }

    private static float smoothEnds(float startValue, float endValue, float startTime, float endTime, float time) {
        if (time < startTime) {
            return startValue;
        } else if (time > endTime) {
            return endValue;
        } else {
            float timeChange = endTime - startTime;
            float valueChange = endValue - startValue;
            return valueChange * (float)((double)1.0F - Math.cos(Math.PI * (double)(time - startTime) / (double)timeChange)) / 2.0F + startValue;
        }
    }
}
