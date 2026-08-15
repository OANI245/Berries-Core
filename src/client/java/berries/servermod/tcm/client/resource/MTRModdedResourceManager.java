package berries.servermod.tcm.client.resource;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.client.vehicle.processing.Processor;
import berries.servermod.tcm.client.vehicle.processing.Processors;
import berries.servermod.tcm.data.vehicle.VehicleDataCache;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.mtr.mapping.holder.Identifier;
import org.mtr.mapping.mapper.ResourceManagerHelper;
import org.mtr.mod.Init;
import org.mtr.mod.client.CustomResourceLoader;
import org.mtr.mod.data.VehicleExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MTRModdedResourceManager {
    private static final Map<String, ProcessorInstancedInfo<? extends Processor>> vehicleProcessors = new HashMap<>();
    private static final Map<String, List<String>> vehicleProcessorIds = new HashMap<>();

    public static void reload() {
        vehicleProcessors.clear();
        vehicleProcessorIds.clear();
        VehicleDataCache.clearData();
        CustomResourceLoader.OPTIMIZED_RENDERER_WRAPPER.beginReload();
        readMtrCustomResources(false);
        readMtrCustomResources(true);
        CustomResourceLoader.OPTIMIZED_RENDERER_WRAPPER.finishReload();
    }

    private static void readMtrCustomResources(boolean pendingMigration) {
        ResourceManagerHelper.readAllResources(
                new Identifier(Init.MOD_ID, pendingMigration ? CustomResourceLoader.CUSTOM_RESOURCES_PENDING_MIGRATION_ID + ".json" : CustomResourceLoader.CUSTOM_RESOURCES_ID + ".json"),
                (is) -> {
                    try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        JsonObject o = JsonParser.parseReader(r).getAsJsonObject();
                        final JsonElement vehicleElement = o.get("vehicles");
                        final JsonElement scriptsElement = o.get("vehicleProcessors");

                        if (vehicleElement != null) {
                            final JsonArray va = vehicleElement.getAsJsonArray();

                            for(JsonElement ve : va) {
                                JsonObject vehicleObject = ve.getAsJsonObject();
                                String baseId = vehicleObject.get("id").getAsString();
                                if(vehicleObject.has("procIds")) {
                                    if (!vehicleProcessorIds.containsKey(baseId)) {
                                        vehicleProcessorIds.put(baseId, new ArrayList<>());
                                    }
                                    vehicleProcessorIds.get(baseId).addAll(vehicleObject.get("procIds").getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList());
                                }
                            }
                        }

                        if (scriptsElement != null) {
                            JsonArray scriptArray = scriptsElement.getAsJsonArray();

                            for(JsonElement ee : scriptArray) {
                                JsonObject so = ee.getAsJsonObject();
                                String pei = so.get("id").getAsString();
                                boolean forceLoad = so.has("forceLoad") && so.get("forceLoad").getAsBoolean();
                                boolean entryReferenced = vehicleProcessorIds.values().stream().anyMatch(e0 -> e0.stream().anyMatch(e1 -> e1.equals(pei)));

                                if (entryReferenced || forceLoad) {
                                    String pi = so.get("proc").getAsString().trim();
                                    Map<String, Object> in = new HashMap<>(so.getAsJsonObject("input").asMap());
                                    Class<? extends Processor> p = Processors.get(pi);
                                    if (p != null) {
                                        try {
                                            var pii = p.getConstructor(Map.class).newInstance(in);
                                            vehicleProcessors.put(pei, new ProcessorInstancedInfo<>(pii, in));
                                        } catch (Exception e) {
                                            TCM.LOGGER.error("Cannot register processor: ", e);
                                        }
                                    }
                                } else {
                                    TCM.LOGGER.warn("Skip load vehicle processors \"{}\", which is not referenced by any vehicle!", pei);
                                }
                            }
                        }
                    } catch (IOException e) {
                        TCM.LOGGER.error("Failed to load processor in vehicles: ", e);
                    }
                }
        );
    }

    public static Processor getVehicleProcessor(String eid) {
        return (Processor) ((Object) vehicleProcessors.get(eid));
    }

    public static Map<String, ProcessorInstancedInfo<? extends Processor>> getVehicleProcessors() {
        return new HashMap<>(vehicleProcessors);
    }

    public static List<String> getVehicleProcessorEntryIds(String s) {
        return vehicleProcessorIds.getOrDefault(s, List.of());
    }

    public static class ProcessorInfo<T extends Processor> {
        public Class<T> proc;
        public Map<String, Object> inputs;

        ProcessorInfo(Class<T> proc, Map<String, Object> inputs) {
            this.proc = proc;
            this.inputs = inputs;
        }
    }

    public static class ProcessorInstancedInfo<T extends Processor> {
        public T proc;
        public Map<String, Object> inputs;

        ProcessorInstancedInfo(T proc, Map<String, Object> inputs) {
            this.proc = proc;
            this.inputs = inputs;
        }
    }
}
