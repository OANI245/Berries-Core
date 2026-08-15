package berries.servermod.tcm.client.vehicle.processing;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class ProcessorManager {
    private final ProcessorInstances instanceManager;
    private final List<ExecutorService> processorExecutors;
    private int scriptExecutorCounter = 0;

    public ProcessorManager(List<ExecutorService> executors) {
        if(executors == null || executors.isEmpty()) throw new IllegalArgumentException("At least 1 script executors must be passed to ProcessorManager!");
        this.processorExecutors = executors;
        this.instanceManager = new ProcessorInstances();
    }

    public ProcessorInstances getInstanceManager() {
        return instanceManager;
    }

    public ExecutorService getDesignatedExecutor() {
        ExecutorService executor = processorExecutors.get(scriptExecutorCounter);
        scriptExecutorCounter = (scriptExecutorCounter + 1) % processorExecutors.size();
        return executor;
    }

    public void reset() {
        instanceManager.reset();
    }
}
