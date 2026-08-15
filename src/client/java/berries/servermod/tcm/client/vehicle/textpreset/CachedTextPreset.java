package berries.servermod.tcm.client.vehicle.textpreset;

import berries.servermod.tcm.util.VehicleWrapper;
import it.unimi.dsi.fastutil.booleans.BooleanBooleanImmutablePair;
import it.unimi.dsi.fastutil.bytes.ByteObjectImmutablePair;
import it.unimi.dsi.fastutil.longs.LongLongImmutablePair;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import org.mtr.libraries.de.javagl.obj.Obj;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CachedTextPreset<T extends TextPreset> implements TextPreset {
    private final T parent;
    private final Map<ByteObjectImmutablePair<ObjectObjectImmutablePair<String, ?>>, Cache> cachedMessages = new HashMap<>();

    private ScheduledExecutorService tickExecutor;

    public CachedTextPreset(T parent) {
        this.parent = parent;
    }

    public void startTick() {
        if (tickExecutor != null) {
            endTick();
        }

        tickExecutor = Executors.newScheduledThreadPool(1);
        tickExecutor.scheduleAtFixedRate(() -> {
            var nowTime = System.currentTimeMillis();

            synchronized (cachedMessages) {
                for (Map.Entry<ByteObjectImmutablePair<ObjectObjectImmutablePair<String, ?>>, Cache> entry : cachedMessages.entrySet()) {
                    if ((nowTime - entry.getValue().startTime) >= entry.getValue().expire) {
                        cachedMessages.remove(entry.getKey());
                    }
                }
            }
        }, 0, 2500, TimeUnit.MILLISECONDS);
    }

    public void endTick() {
        if (tickExecutor != null) {
            tickExecutor.shutdown();
            tickExecutor.close();
            tickExecutor = null;
        }
    }

    @Override
    public String[] getIdleMessage(VehicleWrapper wrapper) {
        var routeId = wrapper.getVehicle().vehicleExtraData.getThisRouteId();
        var cache = getCache(IDLE, wrapper, routeId, null);
        if (cache != null) {
            return cache;
        }
        return putCache(IDLE, wrapper, routeId,
                null, parent.getIdleMessage(wrapper));
    }

    @Override
    public String[] getNextStationMessage(VehicleWrapper wrapper) {
        try {
            var stops = wrapper.getThisRouteStops();
            var stationIndex = wrapper.getNextStopIndex(stops);
            var station = stops.get(wrapper.getNextStopIndex(stops));
            var transferRouteNames = station.routeInterchanges.stream().sorted(Comparator.comparing(v0 -> v0.name));
            var cache = getCache(NEXT_STATION, wrapper, stationIndex, transferRouteNames);
            if (cache != null) {
                return cache;
            }
            return putCache(NEXT_STATION, wrapper, stationIndex,
                    transferRouteNames, parent.getNextStationMessage(wrapper));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String[] getArrivingMessage(VehicleWrapper wrapper) {
        try {
            var stops = wrapper.getThisRouteStops();
            var stationIndex = wrapper.getNextStopIndex(stops);
            var station = stops.get(wrapper.getNextStopIndex(stops));
            var transferRouteNames = station.routeInterchanges.stream().sorted(Comparator.comparing(v0 -> v0.name));
            var cache = getCache(ARRIVING, wrapper, stationIndex, transferRouteNames);
            if (cache != null) {
                return cache;
            }
            return putCache(ARRIVING, wrapper, stationIndex,
                    transferRouteNames, parent.getArrivingMessage(wrapper));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String[] getArrivedMessage(VehicleWrapper wrapper) {
        try {
            var stops = wrapper.getThisRouteStops();
            var stationIndex = wrapper.getNextStopIndex(stops);
            var station = stops.get(wrapper.getNextStopIndex(stops) - 1);
            var transferRouteNames = station.routeInterchanges.stream().sorted(Comparator.comparing(v0 -> v0.name));
            var cache = getCache(ARRIVED, wrapper, stationIndex, transferRouteNames);
            if (cache != null) {
                return cache;
            }
            return putCache(ARRIVED, wrapper, stationIndex,
                    transferRouteNames, parent.getArrivedMessage(wrapper));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String[] getHeadDisplayMessage(VehicleWrapper wrapper, String[] def) {
        try {
            var stops = wrapper.getThisRouteStops();
            var stationIndex = wrapper.getNextStopIndex(stops);
            var station = stops.getLast().station;
            var stationName = station.getName();
            var cache = getCache(HEAD_DISPLAY, wrapper, stationIndex, stationName);
            if (cache != null) {
                return cache;
            }
            var result = parent.getHeadDisplayMessage(wrapper, def);
            if (result == null || result == def) {
                return def;
            }
            return putCache(HEAD_DISPLAY, wrapper, stationIndex,
                    stationName, result);
        } catch (Exception e) {
            return def;
        }
    }

    @Override
    public String[] getSideDisplayMessage(VehicleWrapper wrapper, String[] def) {
        try {
            var stops = wrapper.getThisRouteStops();
            var stationIndex = wrapper.getNextStopIndex(stops);
            var station = stops.getLast().station;
            var stationName = station.getName();
            var cache = getCache(SIDE_DISPLAY, wrapper, stationIndex, stationName);
            if (cache != null) {
                return cache;
            }
            var result = parent.getSideDisplayMessage(wrapper, def);
            if (result == null || result == def) {
                return def;
            }
            return putCache(SIDE_DISPLAY, wrapper, stationIndex,
                    stationName, result);
        } catch (Exception e) {
            return def;
        }
    }

    private String[] putCache(byte mode, VehicleWrapper wrapper, long index, Object val1, String[] value) {
        if (value == null || value.length == 0) {
            return new String[0];
        }

        var key = new ByteObjectImmutablePair<>(
                mode, (mode == 0x00 || !wrapper.getVehicle().getIsOnRoute()) ?
                new ObjectObjectImmutablePair<>(index + "_WITHOUT_STOPS", List.<String>of()) :
                new ObjectObjectImmutablePair<>(wrapper.getThisRouteStops().get((int) Math.min(index, Integer.MAX_VALUE)).name,
                        val1));
        synchronized (cachedMessages) {
            cachedMessages.put(key, new Cache(value, 5000));
        }
        return value;
    }

    private String[] getCache(byte mode, VehicleWrapper wrapper, long index, Object val1) {
        try {
            synchronized (cachedMessages) {
                var cachedMessage = cachedMessages.getOrDefault(
                        new ByteObjectImmutablePair<>(
                                mode, (mode == 0x00 || !wrapper.getVehicle().getIsOnRoute()) ?
                                new ObjectObjectImmutablePair<>(index + "_WITHOUT_STOPS", List.<String>of()) :
                                new ObjectObjectImmutablePair<>(wrapper.getThisRouteStops().get((int) Math.min(index, Integer.MAX_VALUE)).name,
                                        val1)), null);
                if (cachedMessage != null && cachedMessage.value.length > 0
                ) {
                    cachedMessage.setNow();
                    return cachedMessage.value;
                }
            }
        } catch (Exception ignored) {}

        return null;
    }

    public static class Cache {
        public final String[] value;
        public final long expire;
        private long startTime;

        private Cache(String[] value, long expire) {
            this.value = value;
            this.expire = expire;
        }

        public void setNow() {
            startTime = System.currentTimeMillis();
        }
    }
}
