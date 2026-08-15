package berries.servermod.tcm.client.vehicle.processing;

import berries.servermod.tcm.client.render.MTRModdedRenderManager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ProcessorInstances {
    private final Map<VehicleProcessorInstanceKey, ProcessorInstance> instances = new HashMap<>();

    public ProcessorInstance getInstance(VehicleProcessorInstanceKey id, Supplier<ProcessorInstance> getInstance) {
        ProcessorInstance existingInstance = getInstance(id);
        if(existingInstance != null) {
            return existingInstance;
        }

        ProcessorInstance newInstance = getInstance.get();
        instances.put(id, newInstance);
        return newInstance;
    }

    public ProcessorInstance getInstance(VehicleProcessorInstanceKey id) {
        return instances.get(id);
    }

    public Map<VehicleProcessorInstanceKey, ProcessorInstance> getInstances() {
        return new HashMap<>(this.instances);
    }



    public int clearDeadInstance() {
        int count = 0;
        for(Map.Entry<VehicleProcessorInstanceKey, ProcessorInstance> entry : new HashMap<>(instances).entrySet()) {
            if(entry.getValue().shouldInvalidate()) {
                ProcessorInstance instance = entry.getValue();
                count++;
                instances.remove(entry.getKey());
                if(instance.isCreateFunctionInvoked()) {
                    instance.getProcessor().destroy(new berries.servermod.tcm.util.VehicleWrapper((byte) 0x00, instance.getVehicle(), new int[0]), instance.getStateObject());
                }
            }
        }
        return count;
    }

    public void reset() {
        for(ProcessorInstance instance : new HashMap<>(instances).values()) {
            instance.getRenderManagers().forEach(MTRModdedRenderManager::clear);
            instance.getRenderManagers().clear();
            instance.getProcessor().destroy(new berries.servermod.tcm.util.VehicleWrapper((byte) 0x00, instance.getVehicle(), new int[0]), instance.getStateObject());
        }
        instances.clear();
    }
}
