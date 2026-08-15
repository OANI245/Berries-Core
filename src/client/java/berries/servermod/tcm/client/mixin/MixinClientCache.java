package berries.servermod.tcm.client.mixin;

import berries.servermod.tcm.client.Config;
import berries.servermod.tcm.client.data.TCMDynamicResourceCacheV2;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.mtr.mod.client.DynamicTextureCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(DynamicTextureCache.class)
public class MixinClientCache {
    @Inject(at = @At("TAIL"), method = "reload", remap = false)
    private void injected02(CallbackInfo ci) {
        TCMDynamicResourceCacheV2.instance.reload();
    }

    @Inject(at = @At("TAIL"), method = "refresh", remap = false)
    private void injected03(CallbackInfo ci) {
        TCMDynamicResourceCacheV2.instance.refresh();
    }
}
