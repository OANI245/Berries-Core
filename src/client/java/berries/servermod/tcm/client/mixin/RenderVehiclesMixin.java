package berries.servermod.tcm.client.mixin;

import berries.servermod.tcm.client.resource.MTRModdedResourceManager;
import berries.servermod.tcm.client.vehicle.processing.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.mtr.core.data.VehicleCar;
import org.mtr.mapping.holder.Vector3d;
import org.mtr.mod.client.MinecraftClientData;
import org.mtr.mod.client.VehicleRidingMovement;
import org.mtr.mod.data.VehicleExtension;
import org.mtr.mod.render.RenderVehicles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
@Mixin(RenderVehicles.class)
public class RenderVehiclesMixin {
    @Inject(
            method = "render",
            at = @At(value = "INVOKE", target = "Lorg/mtr/libraries/it/unimi/dsi/fastutil/objects/ObjectArraySet;forEach(Ljava/util/function/Consumer;)V")
    )
    private static void render$invoke$forEach(long millisElapsed, Vector3d cameraShakeOffset, CallbackInfo ci) {
        for (VehicleExtension vehicle : MinecraftClientData.getInstance().vehicles) {
            List<VehicleCar> cl = vehicle.vehicleExtraData.immutableVehicleCars;
            Map<String, MTRModdedResourceManager.ProcessorInstancedInfo<? extends Processor>> piv = new HashMap<>();

            for (VehicleCar c : cl) {
                List<String> sei = MTRModdedResourceManager.getVehicleProcessorEntryIds(c.getVehicleId());
                Map<String, MTRModdedResourceManager.ProcessorInstancedInfo<? extends Processor>> pi = MTRModdedResourceManager.getVehicleProcessors();
                for (String s : sei) {
                    if (piv.containsKey(s)) continue;
                    MTRModdedResourceManager.ProcessorInstancedInfo<? extends Processor> pia = pi.getOrDefault(s, null);
                    if (pia != null) {
                        piv.put(s, pia);
                    }
                }
            }

            for (Map.Entry<String, MTRModdedResourceManager.ProcessorInstancedInfo<? extends Processor>> me : piv.entrySet()) {
                String ei = me.getKey();
                List<Integer> cfp = new ArrayList<>();
                for (int i = 0; i < cl.size(); ++i) {
                    if (MTRModdedResourceManager.getVehicleProcessorEntryIds(cl.get(i).getVehicleId()).stream().anyMatch((v) -> v.equals(ei))) {
                        cfp.add(i);
                    }
                }
                int[] ca = cfp.stream().mapToInt(i -> i).toArray();
                if (ca.length == 0) continue;
                Processor p = me.getValue().proc;
                if (p == null) continue;
                ProcessorInstance pi = ContentProcessing.getProcessorManager().getInstanceManager().getInstance(new VehicleProcessorInstanceKey(vehicle.getHexId(), ei), () -> new ProcessorInstance(vehicle, me.getValue().inputs, p));
                pi.updateVehicle(vehicle);

                if (pi.getProcessor() != null) {
                    berries.servermod.tcm.util.VehicleWrapper wrapper = new berries.servermod.tcm.util.VehicleWrapper((byte) 0x00, vehicle, ca);
                    pi.getProcessor().doInvokeSubmit(wrapper, pi, millisElapsed);
                }
            }
        }
    }
}
