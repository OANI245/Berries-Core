package berries.servermod.tcm.client.screen.widget;

import berries.servermod.tcm.util.TCMComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.gui.widget.DynamicEntryListWidget;
import me.shedaniel.clothconfig2.impl.EasingMethod;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;

public class ScrollBar extends AbstractWidget {
    public double scrollAmount;
    public double scrollTarget;
    public long start;
    public long duration;
    private float maxScroll = 0.0F;

    public ScrollBar(int i, int j, int l, float maxScroll) {
        super(i - 2, j, 2, l, TCMComponent.text(""));
        this.maxScroll = maxScroll;
    }

    public float getMaxScroll() {
        return Math.max(0, maxScroll - this.height);
    }

    public void setMaxScroll(float maxScroll) {
        this.maxScroll = maxScroll;
    }

    public void resetScrolling() {
        this.scrollAmount = 0.0;
        this.scrollTarget = 0.0;
        this.start = 0;
        this.duration = 0;
    }

    public boolean showScrollBar() {
        return this.maxScroll > this.height;
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        if (f != 0d) {
            this.offset(16.0 * -f * 2, true);
            return true;
        }
        return super.mouseScrolled(d, e, f);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    public final void offset(double value, boolean animated) {
        scrollTo(scrollTarget + value, animated);
    }

    public final void scrollTo(double value, boolean animated) {
        scrollTo(value, animated, 200);
    }

    public final void scrollTo(double value, boolean animated, long duration) {
        scrollTarget = clamp(value);

        if (animated) {
            start = System.currentTimeMillis();
            this.duration = duration;
        } else
            scrollAmount = scrollTarget;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        double[] target = new double[]{this.scrollTarget};
        this.scrollAmount = handleScrollingPosition(target, this.scrollAmount, this.getMaxScroll(), f, (double) this.start / 100000, (double) this.duration / 100000);
        this.scrollTarget = target[0];
        this.renderScrollBar(guiGraphics, i, j, f);
    }

    private void renderScrollBar(GuiGraphics guiGraphics, int i, int j, float f) {
        if (showScrollBar()) {
            int maxScroll = (int) getMaxScroll();
            int height = (int) (this.height * this.height / this.maxScroll);
            height = Mth.clamp(height, 32, this.height);
            height -= (int) Math.min((scrollAmount < 0 ? (int) -scrollAmount : scrollAmount > maxScroll ? (int) scrollAmount - maxScroll : 0), height * 0.95);
            height = Math.max(10, height);
            int minY = (int) Math.min(Math.max((int) scrollAmount * (this.height - height) / maxScroll + getY(), getY()), (getY() + this.height) - height);

            int scrollbarPositionMinX = getX();
            int scrollbarPositionMaxX = scrollbarPositionMinX + 6;
            boolean hovered = (new Rectangle(scrollbarPositionMinX, minY, scrollbarPositionMaxX - scrollbarPositionMinX, height)).contains(PointHelper.ofMouse());
            float bottomC = (hovered ? .67f : .5f);
            float topC = (hovered ? .87f : .67f);

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            {
                float a = 0;
                float r = 0;
                float g = 0;
                float b = 0;
                buffer.vertex(scrollbarPositionMinX, (getY() + this.height), 0.0D).color(r, g, b, a).endVertex();
                buffer.vertex(scrollbarPositionMaxX, (getY() + this.height), 0.0D).color(r, g, b, a).endVertex();
                buffer.vertex(scrollbarPositionMaxX, getY(), 0.0D).color(r, g, b, a).endVertex();
                buffer.vertex(scrollbarPositionMinX, getY(), 0.0D).color(r, g, b, a).endVertex();
            }
            buffer.vertex(scrollbarPositionMinX, minY + height, 0.0D).color(bottomC, bottomC, bottomC, alpha).endVertex();
            buffer.vertex(scrollbarPositionMaxX, minY + height, 0.0D).color(bottomC, bottomC, bottomC, alpha).endVertex();
            buffer.vertex(scrollbarPositionMaxX, minY, 0.0D).color(bottomC, bottomC, bottomC, alpha).endVertex();
            buffer.vertex(scrollbarPositionMinX, minY, 0.0D).color(bottomC, bottomC, bottomC, alpha).endVertex();
            buffer.vertex(scrollbarPositionMinX, (minY + height - 1), 0.0D).color(topC, topC, topC, alpha).endVertex();
            buffer.vertex((scrollbarPositionMaxX - 1), (minY + height - 1), 0.0D).color(topC, topC, topC, alpha).endVertex();
            buffer.vertex((scrollbarPositionMaxX - 1), minY, 0.0D).color(topC, topC, topC, alpha).endVertex();
            buffer.vertex(scrollbarPositionMinX, minY, 0.0D).color(topC, topC, topC, alpha).endVertex();
            tesselator.end();
            RenderSystem.disableBlend();
        }
    }

    public static double handleScrollingPosition(double[] target, double scroll, double maxScroll, float delta, double start, double duration) {
        return handleScrollingPosition(target, scroll, maxScroll, delta, start, duration, -10);
    }

    public static double handleScrollingPosition(double[] target, double scroll, double maxScroll, float delta, double start, double duration, double bounceBackMultiplier) {
        if (bounceBackMultiplier >= 0) {
            target[0] = clampExtension(target[0], maxScroll);
            if (target[0] < 0) {
                target[0] -= target[0] * (1 - bounceBackMultiplier) * delta / 3;
            } else if (target[0] > maxScroll) {
                target[0] = (target[0] - maxScroll) * (1 - (1 - bounceBackMultiplier) * delta / 3) + maxScroll;
            }
        } else
            target[0] = clampExtension(target[0], maxScroll, 0);
        return smoothEnds(scroll, target[0], start, start + duration, (double) System.currentTimeMillis() / 100000);
    }

    private static double smoothEnds(double startValue, double endValue, double startTime, double endTime, double time) {
        if (time < startTime) {
            return startValue;
        } else if (time > endTime) {
            return endValue;
        } else {
            double timeChange = endTime - startTime;
            double valueChange = endValue - startValue;
            return valueChange * ((double) 1.0F - Math.cos(Math.PI * (double) (time - startTime) / (double) timeChange)) / 2.0F + startValue;
        }
    }

    public final double clamp(double v) {
        return this.clamp(v, 200.0D);
    }

    public final double clamp(double v, double clampExtension) {
        return Mth.clamp(v, -clampExtension, (double) this.getMaxScroll() + clampExtension);
    }

    public static double clampExtension(double value, double maxScroll) {
        return clampExtension(value, maxScroll, DynamicEntryListWidget.SmoothScrollingSettings.CLAMP_EXTENSION);
    }

    public static double clampExtension(double v, double maxScroll, double clampExtension) {
        return Mth.clamp(v, -clampExtension, maxScroll + clampExtension);
    }
}
