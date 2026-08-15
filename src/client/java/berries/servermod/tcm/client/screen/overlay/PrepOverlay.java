package berries.servermod.tcm.client.screen.overlay;

import berries.servermod.tcm.client.Config;
import berries.servermod.tcm.client.flueroui.TextDrawer;
import berries.servermod.tcm.client.screen.GUILocations;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class PrepOverlay extends Overlay implements GuiEventListener {
    private static final float SMOOTHING = 0.95F;
    public static final long FADE_OUT_TIME = 1000L;
    public static final long FADE_IN_TIME = 500L;
    private final Minecraft minecraft;
    private final AtomicBoolean shouldClose;
    public final Consumer<Optional<Throwable>> onFinish;
    private final boolean fadeIn;
    private float currentProgress;
    private long fadeInStart = -1L;
    private long fadeOutStart = -1L;
    private boolean finished = false;
    private float cycle = 0.0F;
    private final long createdMillis;
    private long animationStartTime = System.currentTimeMillis();

    private final boolean debugMode;

    public Component messageText = Component.empty();

    public PrepOverlay(Minecraft minecraft, AtomicBoolean shouldClose, Consumer<Optional<Throwable>> consumer, boolean bl, boolean bl2) {
        this.minecraft = minecraft;
        this.shouldClose = shouldClose;
        this.onFinish = consumer;
        this.fadeIn = bl;
        this.debugMode = bl2;
        this.createdMillis = Util.getMillis();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        int k = this.minecraft.getWindow().getGuiScaledWidth();
        int l = this.minecraft.getWindow().getGuiScaledHeight();
        long m = Util.getMillis();

        if (m - createdMillis < 200) {
            guiGraphics.fill(0, 0, k, l, FastColor.ARGB32.color(255, 0, 0, 0));
            return;
        }

        if (this.fadeIn && this.fadeInStart == -1L) {
            this.fadeInStart = m;
        }

        float g = this.fadeOutStart > -1L ? (float) (m - this.fadeOutStart) / 1800.0F : -1.0F;
        float h = this.fadeInStart > -1L ? (float) (m - this.fadeInStart) / 1800.0F : -1.0F;
        float o;

        guiGraphics.fill(0, 0, k, l, FastColor.ARGB32.color(255, 0, 0, 0));

        int n = (int) ((double) this.minecraft.getWindow().getGuiScaledWidth() * 0.5);
        int s = (int) ((double) this.minecraft.getWindow().getGuiScaledHeight() * 0.5);
        double d = Math.min((double) this.minecraft.getWindow().getGuiScaledWidth() * 0.75, (double) this.minecraft.getWindow().getGuiScaledHeight()) * 0.25;
        int t = (int) (d * 0.5);
        double e = d * 4.0;
        int u = (int) (e * 0.5);
        int v = (int) ((double) this.minecraft.getWindow().getGuiScaledHeight() * 0.8325);

        if (g >= 1.0F) {
            /*if (this.minecraft.screen != null) {
                this.minecraft.screen.render(poseStack, 0, 0, f);
            }*/

            /*int p = Mth.ceil((1.0F - Mth.clamp(g - 1.0F, 0.0F, 1.0F)) * 255.0F);
            fill(poseStack, 0, 0, k, l, replaceAlpha(FastColor.ARGB32.color(255, 0, 0, 0), p));*/
            o = 1.0F - Mth.clamp(g - 1.0F, 0.0F, 1.0F);
        } else if (this.fadeIn) {
            /*if (this.minecraft.screen != null && h < 1.0F) {
                this.minecraft.screen.render(poseStack, i, j, f);
            }*/

            /*int p = Mth.ceil(Mth.clamp((double)h, 0.15, 1.0) * 255.0);
            fill(poseStack, 0, 0, k, l, replaceAlpha(FastColor.ARGB32.color(255, 0, 0, 0), p));*/
            o = Mth.clamp(h, 0.0F, 1.0F);
        } else {
            int w = FastColor.ARGB32.color(255, 0, 0, 0);
            float p = (float) (w >> 16 & 0xFF) / 255.0F;
            float q = (float) (w >> 8 & 0xFF) / 255.0F;
            float r = (float) (w & 0xFF) / 255.0F;
            GlStateManager._clearColor(p, q, r, 1.0F);
            GlStateManager._clear(16384, Minecraft.ON_OSX);
            o = 1.0F;
        }

        if (!finished && this.shouldClose.get()) {
            try {
                onFinish.accept(Optional.empty());
            } catch (Throwable th) {
                onFinish.accept(Optional.of(th));
            }

            this.finished = true;
            this.minecraft.setOverlay(null);
            return;
        }

        /*RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, o);*/

        this.renderProgressRing(guiGraphics, k / 2 - u, v - 5, k / 2 + u, v + 5, o);

        if (o < 1.0F) {
            int alpha = 255 - (int) Math.floor(o * 255.0F);
            guiGraphics.fill(0, 0, k, l, FastColor.ARGB32.color(alpha, 0, 0, 0));
        }
    }

    public void renderProgressRing(GuiGraphics guiGraphics, int i, int j, int k, int l, float f) {
        int frames = Objects.equals(Config.INSTANCE.ringLoadingAnimation, "style1") ? 119 : 123;
        int rate = 35;
        int imageHeight = fadeIn ? 20 : 16;
        int imageWidth = imageHeight * frames;
        int x = i + (k - i) / 2 - imageHeight / 2;
        int y = j - 8;
        var ps0 = guiGraphics.pose();
        ResourceLocation animationImageId = Objects.equals(Config.INSTANCE.ringLoadingAnimation, "style1") ? GUILocations.LOADING_ANIMATION_STYLE_1 : GUILocations.LOADING_ANIMATION_STYLE_2;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        guiGraphics.blit(animationImageId, x, y, ((int) Math.floor(cycle)) * imageHeight, 0, imageHeight, imageHeight, imageWidth, imageHeight);
        if (cycle < frames) {
            cycle = 1.0F + (float) (System.currentTimeMillis() - animationStartTime) / ((float) 1000 / rate);
        } else {
            cycle = 1.0F;
            animationStartTime = System.currentTimeMillis();
        }

        if (messageText != null) {
            ps0.pushPose();
            var scale = 1.25f;
            ps0.scale(scale, scale, scale);
            try {
                TextDrawer.drawText(guiGraphics,
                        minecraft.font, messageText, TextDrawer.Alignment.LEFT,
                        (int) (TextDrawer.Alignment.CENTER.calculateX((int) (minecraft.getWindow().getGuiScaledWidth() / 2.0f), minecraft.font.width(messageText)) / scale),
                        (int) ((y + (float) imageHeight / 2 + 14) / scale),
                        0xFFFFFF, false
                );
            } catch (Throwable ignored) {
            }
            ps0.popPose();
        }
    }

    private static int replaceAlpha(int i, int j) {
        return i & 16777215 | j << 24;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (debugMode && i == GLFW.GLFW_KEY_R) {
            this.shouldClose.set(true);
            return true;
        }

        return false;
    }

    @Override
    public void setFocused(boolean bl) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return super.isPauseScreen();
    }
}
