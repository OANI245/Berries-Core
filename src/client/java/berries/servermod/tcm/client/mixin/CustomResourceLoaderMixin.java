package berries.servermod.tcm.client.mixin;

import berries.servermod.tcm.client.resource.MTRModdedResourceManager;
import berries.servermod.tcm.client.util.model.ModelManager;
import berries.servermod.tcm.client.vehicle.processing.ContentProcessing;
import berries.servermod.tcm.client.vehicle.processing.ProcessorInstances;
import org.mtr.mod.client.CustomResourceLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CustomResourceLoader.class, remap = false)
public class CustomResourceLoaderMixin {
    @Inject(method = "reload", at = @At("TAIL"))
    private static void notifyCustomReload(CallbackInfo ci) {
        ContentProcessing.reset();
        ModelManager.reset();
        MTRModdedResourceManager.reload();
    }
}
