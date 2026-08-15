package berries.servermod.tcm.client.vehicle.processing;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.client.render.MTRModdedRenderManager;

import java.util.List;
import java.util.Map;

public abstract class Processor {
    public final Map<String, Object> inputs;

    public Processor(Map<String, Object> inputs) {
        this.inputs = inputs;
    }

    public abstract boolean load(berries.servermod.tcm.util.VehicleWrapper wrapper, Map<Byte, Object> st, List<MTRModdedRenderManager> renderManagers);
    public abstract void submit(berries.servermod.tcm.util.VehicleWrapper wrapper, Map<Byte, Object> st, List<MTRModdedRenderManager> renderManagers, long millisElapsed);
    public abstract void destroy(berries.servermod.tcm.util.VehicleWrapper wrapper, Map<Byte, Object> st);

    public final void doInvokeSubmit(berries.servermod.tcm.util.VehicleWrapper wrapper, ProcessorInstance instance, long me) {
        if (instance.shouldInvalidate()) {
            return;
        }
        try {
            if (!instance.isCreateFunctionInvoked()) {
                boolean res = load(wrapper, instance.getStateObject(), instance.getRenderManagers());
                instance.setCreateFunctionInvoked(res);
            } else {
                submit(wrapper, instance.getStateObject(), instance.getRenderManagers(), me);
            }
        } catch (Exception e) {
            TCM.LOGGER.error("Exception to run processor: ", e);
        }
    }

    public final String getName() {
        return this.getClass().getAnnotation(ProcessorName.class).value();
    }
}
