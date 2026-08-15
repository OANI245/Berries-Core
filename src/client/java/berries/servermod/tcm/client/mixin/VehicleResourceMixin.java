package berries.servermod.tcm.client.mixin;

import berries.servermod.tcm.client.render.MTRModdedRenderManager;
import berries.servermod.tcm.client.resource.MTRModdedResourceManager;
import berries.servermod.tcm.client.vehicle.processing.ContentProcessing;
import berries.servermod.tcm.client.vehicle.processing.ProcessorInstance;
import berries.servermod.tcm.client.vehicle.processing.ProcessorInstances;
import berries.servermod.tcm.client.vehicle.processing.VehicleProcessorInstanceKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.mtr.core.data.VehicleCar;
import org.mtr.mapping.holder.Direction;
import org.mtr.mapping.holder.MinecraftClient;
import org.mtr.mapping.holder.World;
import org.mtr.mod.client.VehicleRidingMovement;
import org.mtr.mod.data.VehicleExtension;
import org.mtr.mod.render.PositionAndRotation;
import org.mtr.mod.render.StoredMatrixTransformations;
import org.mtr.mod.resource.VehicleResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(value = VehicleResource.class)
public abstract class VehicleResourceMixin {
    @Inject(method = "queue(Lorg/mtr/mod/render/StoredMatrixTransformations;ZLorg/mtr/mod/render/PositionAndRotation;DLorg/mtr/mod/data/VehicleExtension;IIIZ)V", at = @At("HEAD"))
    private void queue$head(StoredMatrixTransformations storedMatrixTransformations, boolean useDefaultOffset, PositionAndRotation positionAndRotation, double oscillationAmount, VehicleExtension vehicle, int carNumber, int totalCars, int light, boolean noOpenDoorways, CallbackInfo ci) {

        VehicleCar vehicleCar = vehicle.vehicleExtraData.immutableVehicleCars.get(carNumber);
        List<String> eids = MTRModdedResourceManager.getVehicleProcessorEntryIds(vehicleCar.getVehicleId());
        if (eids == null || eids.isEmpty()) return;

        for (String s : eids) {
            ProcessorInstance instance = ContentProcessing.getProcessorManager().getInstanceManager().getInstance(new VehicleProcessorInstanceKey(vehicle.getHexId(), s));
            if (instance == null) continue;
            MTRModdedRenderManager carRenderManager = instance.getRenderManagers().get(carNumber);

            StoredMatrixTransformations newTransform = storedMatrixTransformations.copy();
            newTransform.add(gh -> gh.translate(0, -1, 0)); // Replicate behaviour from MTR 3. Not sure if we should do it here tho?

            if (MinecraftClient.getInstance().getWorldMapped() != null) {
                World world = World.cast(MinecraftClient.getInstance().getWorldMapped());

                if(carRenderManager != null) {
                    carRenderManager.render(world, newTransform, Direction.NORTH, light);
                    carRenderManager.clear();
                }
            }
        }
    }
}