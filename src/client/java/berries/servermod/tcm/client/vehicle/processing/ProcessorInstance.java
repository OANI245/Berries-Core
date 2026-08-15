package berries.servermod.tcm.client.vehicle.processing;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.client.render.MTRModdedRenderManager;
import berries.servermod.tcm.client.resource.MTRModdedResourceManager;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectAVLTreeMap;
import org.mtr.libraries.de.javagl.obj.Obj;
import org.mtr.mapping.holder.MinecraftClient;
import org.mtr.mod.client.MinecraftClientData;
import org.mtr.mod.data.VehicleExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessorInstance {
    private VehicleExtension vehicleExtension;
    private Processor instance;
    private boolean createFunctionInvoked = false;
    private final Map<Byte, Object> stateObject = new Byte2ObjectAVLTreeMap<>();
    private final Map<String, Object> inputs;
    private final List<MTRModdedRenderManager> renderManagers = new ArrayList<>();

    private final Class<? extends Processor> clazz;

    public ProcessorInstance(VehicleExtension ve, Map<String, Object> inputs, Class<? extends Processor> c) {
        this.vehicleExtension = ve;
        this.inputs = inputs;
        this.clazz = c;

        for (int i = 0; i < vehicleExtension.vehicleExtraData.immutableVehicleCars.size(); i++) {
            renderManagers.add(i, new MTRModdedRenderManager());
        }
    }

    public ProcessorInstance(VehicleExtension ve, Map<String, Object> inputs, Processor p) {
        this.vehicleExtension = ve;
        this.inputs = inputs;
        this.instance = p;
        this.clazz = p.getClass();

        for (int i = 0; i < vehicleExtension.vehicleExtraData.immutableVehicleCars.size(); i++) {
            renderManagers.add(i, new MTRModdedRenderManager());
        }
    }

    public Map<Byte, Object> getStateObject() {
        return stateObject;
    }

    public void setCreateFunctionInvoked(boolean value) {
        this.createFunctionInvoked = value;
    }

    public boolean isCreateFunctionInvoked() {
        return this.createFunctionInvoked;
    }

    public void setCreateFunctionInvokedTrue() {
        this.createFunctionInvoked = true;
    }

    public Processor getProcessor() {
        return instance;
    }

    public VehicleExtension getVehicle() {
        return vehicleExtension;
    }

    public Map<String, Object> getInputs() {
        return inputs;
    }

    public List<MTRModdedRenderManager> getRenderManagers() {
        return renderManagers;
    }

    public Processor initProcessor() {
        try {
            this.instance = clazz.getConstructor().newInstance();
        } catch (Exception e) {
            TCM.LOGGER.error("Failed init processor: ", e);
        }
        return this.instance;
    }

    public void updateVehicle(VehicleExtension latest) {
        this.vehicleExtension = latest;
    }

    public boolean shouldInvalidate() {
        boolean notInGame = MinecraftClient.getInstance().getWorldMapped() == null;
        return notInGame || MinecraftClientData.getInstance().vehicles.stream().noneMatch(v -> v.getId() == vehicleExtension.getId());
    }
}
