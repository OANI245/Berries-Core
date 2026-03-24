package berries.servermod.tcm.client.mixin;

import berries.servermod.tcm.client.data.TCMDynamicResourceCacheV2;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(KeyboardHandler.class)
public abstract class MixinKeyboardHandler {
    @Shadow
    private long debugCrashKeyTime;

    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(method = "handleDebugKeys", at = @At(value = "HEAD"), cancellable = true)
    private void inject01(int i, CallbackInfoReturnable<Boolean> cir) {
        if (!(this.debugCrashKeyTime > 0L && this.debugCrashKeyTime < Util.getMillis() - 100L) && i == 65) {
            this.minecraft.levelRenderer.allChanged();
            TCMDynamicResourceCacheV2.instance.reload();
            this.debugFeedbackTranslated("debug.reload_chunks.message");
            cir.setReturnValue(true);
        }
    }

    @Shadow
    protected abstract void debugFeedbackTranslated(String s, Object... objects);
}
