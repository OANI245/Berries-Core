package berries.servermod.tcm.client.vehicle.processing;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.client.vehicle.processing.door.KDoorProcessor;
import berries.servermod.tcm.client.vehicle.processing.screens.lcd.general.GeneralLCDProcessor;
import berries.servermod.tcm.client.vehicle.processing.vehicle.trm01.TRM01MainProcessor;
import berries.servermod.tcm.client.vehicle.processing.vehicle.trm02.TRM02MainProcessor;
import org.mtr.libraries.de.javagl.obj.Obj;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class Processors {
    private static boolean initialized = false;

    private static final Map<String, Class<? extends Processor>> PROCESSORS = new HashMap<>();

    public static void init() {
        //register(GeneralLCDProcessor.class);

        register(KDoorProcessor.class);
        register(TRM01MainProcessor.class);
        register(TRM02MainProcessor.class);
        initialized = true;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static <T extends Processor> void register(Class<T> in) {
        try {
            PROCESSORS.put(in.getAnnotation(ProcessorName.class).value(), in);
        } catch (Exception e) {
            TCM.LOGGER.error("Failed load processor: ", e);
        }
    }

    public static Class<? extends Processor> get(String name) {
        return PROCESSORS.get(name);
    }

    public static Map<String, Class<? extends Processor>> getAll() {
        return new HashMap<>(PROCESSORS);
    }
}
