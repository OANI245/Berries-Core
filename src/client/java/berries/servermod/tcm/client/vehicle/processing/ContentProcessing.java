package berries.servermod.tcm.client.vehicle.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContentProcessing {
    private static final int EXECUTOR_AMOUNT = 4;
    private static final List<ExecutorService> executors = new ArrayList<>();

    static {
        for(int i = 0; i < EXECUTOR_AMOUNT; i++) {
            executors.add(Executors.newFixedThreadPool(1));
        }
    }

    private static ProcessorManager processorManager;

    public static void register() {
        processorManager = new ProcessorManager(executors);
    }

    public static ProcessorManager getProcessorManager() {
        return processorManager;
    }

    public static void tick() {
        processorManager.getInstanceManager().clearDeadInstance();
    }

    public static List<ExecutorService> getExecutors() {
        return executors;
    }

    public static void reset() {
        processorManager.reset();
    }
}
