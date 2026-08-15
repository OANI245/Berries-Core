package berries.servermod.tcm.client.mixin;

import berries.servermod.tcm.client.Config;
import berries.servermod.tcm.client.data.LogoTexture2;
import berries.servermod.tcm.client.flueroui.TextDrawer;
import berries.servermod.tcm.client.screen.GUILocations;
import berries.servermod.tcm.util.TCMComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Environment(EnvType.CLIENT)
@Mixin(LoadingOverlay.class)
public abstract class MixinLoadingOverlay extends Overlay {
    @Shadow @Final private boolean fadeIn;
    @Unique
    private static final int SHOW_MILLIS = 7000;

    @Unique
    private final long createdMillis = System.currentTimeMillis();
    @Unique
    private boolean canShowRing = fadeIn;
    @Unique
    private float cycle = 0.0F;
    @Unique
    private long animationStartTime = System.currentTimeMillis();

    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(
            method = "drawProgressBar",
            at = @At("HEAD"),
            cancellable = true)
    public void injected01(GuiGraphics guiGraphics, int i, int j, int k, int l, float f, CallbackInfo ci) {
        int n = Math.round(f * 255.0F);
        if (Objects.equals(Config.INSTANCE.ringLoadingAnimation, "style1") || Objects.equals(Config.INSTANCE.ringLoadingAnimation, "style2")) {
            int frames = Objects.equals(Config.INSTANCE.ringLoadingAnimation, "style1") ? 119 : 123;
            int rate = 35;

            int imageHeight = fadeIn ? 20 : /*(int) Math.ceil((32F / 1152F) * (i + k))*/14;
            int imageWidth = imageHeight * frames;
            int x = i + (k - i) / 2 - imageHeight / 2;
            int y = j - 8;

            var ps0 = guiGraphics.pose();

            if (canShowRing || fadeIn) {
                ResourceLocation animationImageId = Objects.equals(Config.INSTANCE.ringLoadingAnimation, "style1") ? GUILocations.LOADING_ANIMATION_STYLE_1 : GUILocations.LOADING_ANIMATION_STYLE_2;

                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, n / 255.0F);

                guiGraphics.blit(animationImageId, x, y, ((int) Math.floor(cycle)) * imageHeight, 0, imageHeight, imageHeight, imageWidth, imageHeight);
                if (cycle < frames) {
                    cycle = 1.0F + (float) (System.currentTimeMillis() - animationStartTime) / ((float) 1000 / rate);
                } else {
                    cycle = 1.0F;
                    animationStartTime = System.currentTimeMillis();
                }
            } else {
                if ((System.currentTimeMillis() - createdMillis) > SHOW_MILLIS) {
                    canShowRing = true;
                }
            }

            if (fadeIn) {
                ps0.pushPose();
                var scale = 1.25f;
                ps0.scale(scale, scale, scale);
                try {
                    Component text = TCMComponent.translatable("gui.tcm.loading.reloading");
                    TextDrawer.drawText(guiGraphics,
                            minecraft.font, text, TextDrawer.Alignment.LEFT,
                            (int) (TextDrawer.Alignment.CENTER.calculateX((int) (minecraft.getWindow().getGuiScaledWidth() / 2.0f), minecraft.font.width(text)) / scale),
                            (int) ((y + (float) imageHeight / 2 + 14) / scale),
                            0xFFFFFF, false
                    );
                } catch (Throwable ignored) {
                }
                ps0.popPose();
            }
            ci.cancel();
        }
    }

    @Inject(
            method = "registerTextures",
            at = @At("HEAD")
    )
    private static void injected02(Minecraft minecraft, CallbackInfo ci) {
        minecraft.getTextureManager().register(GUILocations.LOADING_ANIMATION_STYLE_1, new LogoTexture2(GUILocations.LOADING_ANIMATION_STYLE_1));
        minecraft.getTextureManager().register(GUILocations.LOADING_ANIMATION_STYLE_2, new LogoTexture2(GUILocations.LOADING_ANIMATION_STYLE_2));
    }
}
