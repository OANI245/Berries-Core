package berries.servermod.tcm.client.screen.widget;

import berries.servermod.tcm.client.screen.GUILocations;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public final class SlideShowWidget extends ImageWidget {
    public ResourceLocation[] srcList;
    public boolean enableTimer;
    private long startTime = System.currentTimeMillis();
    private int showing = 0;

    private SlideShowWidget(int i, int j, int k, int l, boolean enableTimer, ResourceLocation[] srcList) {
        super(i, j, k, l, srcList[0]);
        this.srcList = srcList;
        this.enableTimer = enableTimer;
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (d > getX() + 24 && d < width + getX() - 24 && e > getY() + height - 12 && e < getY() + height - 7) {
            double clickedDoubleIndex = (d - (getX() + 24)) / 12;
            int clickedIndex = (int) ((int) clickedDoubleIndex + (clickedDoubleIndex - ((int) clickedDoubleIndex)));
            if (clickedDoubleIndex > (clickedIndex + (5.0 / 12.0))) {
                return false;
            } else if (clickedIndex >= 0 && clickedIndex < srcList.length) {
                this.startTime = System.currentTimeMillis();
                setShowing(clickedIndex);
            }
        }
        return false;
    }

    public static SlideShowWidget create(int x, int y, int width, int height, boolean enableTimer, ResourceLocation[] srcList) {
        if (srcList.length < 1) {
            return null;
        }
        return new SlideShowWidget(x, y, width, height, enableTimer, srcList);
    }

    public int cycle() {
        this.showing += this.showing >= (this.srcList.length - 1) ? -this.showing : 1;
        this.src = this.srcList[this.showing];
        return showing;
    }

    public void setShowing(int i) {
        if (i < srcList.length) {
            this.showing = i;
            this.src = this.srcList[showing];
        }
    }

    public int showing() {
        return showing;
    }

    public long tick(long startTime, long duration) {
        long currentTime = System.currentTimeMillis();
        if (startTime + duration <= currentTime) {
            this.cycle();
            return currentTime;
        }
        return startTime;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        startTime = this.tick(startTime, 6000);
        super.renderWidget(guiGraphics, i, j, f);
        int psx = getX() + 24;
        int psy = getY() + height - 12;
        for (int k = 0; k < srcList.length; k++) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            //RenderSystem.setShaderTexture(0, GUILocations.MAIN_SCREEN_ICON_PAGE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            guiGraphics.blit(GUILocations.MAIN_SCREEN_ICON_PAGE, psx + k * 12, psy, k == showing ? 0 : 5, 0, 5, 5, 10, 5);
        }
    }
}
