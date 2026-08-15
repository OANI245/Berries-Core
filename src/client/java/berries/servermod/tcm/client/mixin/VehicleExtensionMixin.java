package berries.servermod.tcm.client.mixin;

import berries.servermod.tcm.data.vehicle.VehicleDataCache;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.mtr.core.data.Vehicle;
import org.mtr.mod.data.VehicleExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = VehicleExtension.class, remap = false)
public class VehicleExtensionMixin {
    @Inject(method = "dispose", at = @At("HEAD"))
    public void clearScriptedVehicleStopsCache(CallbackInfo ci) {
        VehicleDataCache.clearStopsDataCache(((Vehicle)(Object)this).getId());
    }
}