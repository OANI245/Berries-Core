package berries.servermod.tcm.util;

import berries.servermod.tcm.data.vehicle.VehicleDataCache;
import berries.servermod.tcm.mixin.VehicleSchemaAccessor;
import org.mtr.core.data.*;
import org.mtr.libraries.it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.mtr.libraries.it.unimi.dsi.fastutil.longs.LongArrayList;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.mod.client.MinecraftClientData;
import org.mtr.mod.data.VehicleExtension;
import org.mtr.mod.render.PositionAndRotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VehicleWrapper {
    protected final VehicleExtension vehicleExtension;
    protected final StopsData stopsData;
    protected final List<PositionAndRotation> posAndRotations;

    protected final int[] availableCars;

    private final boolean anyVehicleCarVisible;

    public VehicleWrapper(byte dataFetchMode, VehicleExtension vehicleExtension, int[] processorCars) {
        this.vehicleExtension = vehicleExtension;
        this.posAndRotations = vehicleExtension.getSmoothedVehicleCarsAndPositions(0).stream()
                .map(vehicleCarAndPosition -> {
                    final ObjectArrayList<PositionAndRotation> bogiePositions = vehicleCarAndPosition.right()
                            .stream()
                            .map(bogiePositionPair -> new PositionAndRotation(bogiePositionPair.left(), bogiePositionPair.right(), true))
                            .collect(Collectors.toCollection(ObjectArrayList::new));
                    return new PositionAndRotation(bogiePositions, vehicleCarAndPosition.left(), vehicleExtension.getTransportMode().hasPitchAscending || vehicleExtension.getTransportMode().hasPitchDescending);
                })
                .collect(Collectors.toCollection(ObjectArrayList::new));
        boolean anyVehicleCarVisible = false;
        for(boolean rayTracingVisible : vehicleExtension.persistentVehicleData.rayTracing) {
            if(rayTracingVisible) {
                anyVehicleCarVisible = true;
                break;
            }
        }
        this.anyVehicleCarVisible = anyVehicleCarVisible;
        this.stopsData = StopsData.constructData(dataFetchMode, vehicleExtension);
        this.availableCars = processorCars;
    }

    public List<Stop> getStops() {
        return stopsData.allStops;
    }

    public Siding getSiding() {
        return stopsData.siding;
    }

    public List<Stop> getThisRouteStops() {
        long routeId = getThisRouteId();
        return getRouteStops(routeId);
    }

    public List<Stop> getNextRouteStops() {
        long thisRouteId = getThisRouteId();
        int routeIndex = stopsData.routeToRun.indexOf(thisRouteId);
        int nextRouteIndex = routeIndex+1;
        if(nextRouteIndex >= stopsData.routeToRun.size()) return List.of();
        return getRouteStops(stopsData.routeToRun.getLong(nextRouteIndex));
    }

    public int getNextStopIndex(List<Stop> stops) {
        return getNextStopIndex(stops, 0.5);
    }

    public int getNextStopIndex(List<Stop> stops, double overrunTolerance) {
        return findNextStopIndex(overrunTolerance, getRailProgress(), stops);
    }

    public boolean isStopsDataFetched() {
        return stopsData.isFullData;
    }

    /** Whether stops data (Including MTR data) are fully fetched.
     HACK for existing MTR 3 scripts, not for use by scripts. */
    public boolean isStopsDataFullyFetched() {
        if(!isStopsDataFetched()) return false;

        for(Stop stop : stopsData.allStops) {
            if(stop.platform == null) return false; // Use platform as a measure, since that will never be null if fetched.
        }
        return true;
    }

    protected long getThisRouteId() {
        long routeId = vehicleExtension.vehicleExtraData.getThisRouteId();
        if(isStopsDataFetched()) {
            int nextStopIndex = getNextStopIndex(getStops(), 0);
            if(nextStopIndex < getStops().size()) {
                SimplifiedRoute route = getStops().get(nextStopIndex).route;
                if(route != null) {
                    routeId = route.getId();
                }
            }
        }
        return routeId;
    }

    protected List<Stop> getRouteStops(long routeId) {
        List<Stop> stops = stopsData.routeStops.get(routeId);
        return stops == null ? List.of() : stops;
    }

    protected int findNextStopIndex(double overrunTolerance, double currentRailProgress, List<Stop> stops) {
        boolean distanceAvailable = !stops.isEmpty() && stops.get(0).distance >= 0;

        if(distanceAvailable) {
            int stopIdx = 0;

            for(Stop stop : stops) {
                if(currentRailProgress > stop.distance + overrunTolerance) stopIdx++;
                else break;
            }
            return stopIdx;
        } else {
            long nextPlatId = vehicleExtension.vehicleExtraData.getThisPlatformId();
            Stop nextStop = stops.stream().filter(e -> e.platform != null && e.platform.getId() == nextPlatId).findFirst().orElse(null);
            if(nextStop != null) {
                int idx = stops.indexOf(nextStop);
                if(idx != -1) return idx;
            }
            return stops.size();
        }
    }

    public VehicleExtension getVehicle() {
        return vehicleExtension;
    }

    public List<PositionAndRotation> getPosAndRotations() {
        return posAndRotations;
    }

    public int[] getAvailableCars() {
        return availableCars;
    }

    public boolean isAnyVehicleCarVisible() {
        return anyVehicleCarVisible;
    }

    public List<PathData> getPathData() {
        return vehicleExtension.vehicleExtraData.immutablePath;
    }

    public static class StopsData {
        public final ObjectArrayList<Stop> allStops;
        public final ObjectArrayList<Stop> allStopsNextRoute;
        public final Long2ObjectOpenHashMap<List<Stop>> routeStops;
        public final LongArrayList routeToRun;
        public final Siding siding;
        public final boolean isFullData;

        public StopsData(Siding siding, boolean isFullData) {
            this.isFullData = isFullData;
            this.allStops = new ObjectArrayList<>();
            this.allStopsNextRoute = new ObjectArrayList<>();
            this.routeToRun = new LongArrayList();
            this.routeStops = new Long2ObjectOpenHashMap<>();
            this.siding = siding;
        }

        public static StopsData constructData(byte dataFetchMode, VehicleExtension vehicle) {
            if(dataFetchMode != 0x01) {
                VehicleDataCache.requestVehicleStopsData(vehicle.getId(), vehicle.vehicleExtraData.getSidingId());
            }

            StopsData fullStopsData = VehicleDataCache.buildVehicleStopsData(vehicle);
            if(dataFetchMode != 0x01 && fullStopsData != null) {
                return fullStopsData;
            }

            long sidingId = vehicle.vehicleExtraData.getSidingId();
            Siding siding = MinecraftClientData.getInstance().sidingIdMap.get(sidingId);
            StopsData limitedStopsData = new StopsData(siding, false);
            return buildLimitedStopsData(limitedStopsData, vehicle);
        }

        private static StopsData buildLimitedStopsData(StopsData stopsData, VehicleExtension vehicleExtension) {
            List<SimplifiedRoute> allRoutes = new ArrayList<>();
            SimplifiedRoute lastRoute = MinecraftClientData.getInstance().simplifiedRouteIdMap.get(vehicleExtension.vehicleExtraData.getPreviousRouteId());
            if (lastRoute != null) allRoutes.add(lastRoute);

            SimplifiedRoute thisRoute = MinecraftClientData.getInstance().simplifiedRouteIdMap.get(vehicleExtension.vehicleExtraData.getThisRouteId());
            if (thisRoute != null) allRoutes.add(thisRoute);

            SimplifiedRoute nextRoute = MinecraftClientData.getInstance().simplifiedRouteIdMap.get(vehicleExtension.vehicleExtraData.getNextRouteId());
            if (nextRoute != null) allRoutes.add(nextRoute);

            long lastPlatformId = 0;
            for(SimplifiedRoute route : allRoutes) {
                stopsData.routeToRun.add(route.getId());

                List<SimplifiedRoutePlatform> routePlatforms = route.getPlatforms();
                Station destinationStation = MinecraftClientData.getInstance().stationIdMap.get(routePlatforms.get(routePlatforms.size()-1).getStationId());

                for(SimplifiedRoutePlatform routePlatform : routePlatforms) {
                    String destinationName = routePlatform.getDestination();
                    Station station = MinecraftClientData.getInstance().stationIdMap.get(routePlatform.getStationId());
                    Platform platform = MinecraftClientData.getInstance().platformIdMap.get(routePlatform.getPlatformId());

                    Stop thisStop = new Stop(route, station, platform, routePlatform.getStationName(), destinationName, "", -1, false);
                    thisStop.destinationStation = destinationStation;
                    // In-station interchange
                    routePlatform.forEach((color, routes) -> {
                        routes.forEach(routeName -> {
                            thisStop.routeInterchanges.add(new Stop.RouteInterchange(color, routeName));
                        });
                    });
                    // Connecting station interchange
                    if(station != null) {
                        station.getInterchangeStationNameToColorToRouteNamesMap(true).forEach((stationName, interchanges) -> {
                            if(!stationName.equals(station.getName())) {
                                interchanges.forEach((color, routeNames) -> {
                                    routeNames.forEach(routeName -> {
                                        thisStop.connectingInterchanges.computeIfAbsent(stationName, k -> new ObjectArrayList<>()).add(new Stop.RouteInterchange(color, routeName));
                                    });
                                });
                            }
                        });
                    }

                    if(routePlatform.getPlatformId() == lastPlatformId) { // Duplicated platform, likely double-added stop from route changeover.
                        Stop prevStop = stopsData.allStops.get(stopsData.allStops.size()-1);
                        prevStop.roundUpRoute = thisStop;
                        prevStop.reverseAtPlatform = true;
                        prevStop.isRouteSwitchoverStop = true;
                    } else {
                        stopsData.allStops.add(thisStop);
                    }
                    stopsData.routeStops.computeIfAbsent(route.getId(), (k) -> new ArrayList<>()).add(thisStop);
                    lastPlatformId = routePlatform.getPlatformId();
                }
            }
            return stopsData;
        }
    }

    public static class Stop {
        public SimplifiedRoute route;
        public Station station;
        public String name;
        public Platform platform;
        public String destinationName;
        public String customDestination;
        public List<RouteInterchange> routeInterchanges;
        public Map<String, List<RouteInterchange>> connectingInterchanges;
        public long dwellTimeMillis;
        public double distance;
        public Stop roundUpRoute;
        public boolean isRouteSwitchoverStop;
        @Deprecated
        public Station destinationStation; // Manually obtain the Station of the last stop instead
        @Deprecated
        public long dwellTime; // Use dwellTimeMs instead
        @Deprecated
        public boolean reverseAtPlatform; // Identical to routeSwitchover
        public boolean turnbackPlatform;

        public Stop(SimplifiedRoute route, Station station, Platform platform,
                    String name, String destinationName, String customDestination, double distance, boolean turnbackPlatform) {
            this.route = route;
            this.station = station;
            this.platform = platform;
            this.dwellTime = platform == null ? -1 : platform.getDwellTime() / 500;
            this.dwellTimeMillis = platform == null ? -1 : platform.getDwellTime();
            this.routeInterchanges = new ArrayList<>();
            this.connectingInterchanges = new HashMap<>();
            this.name = name;
            this.destinationName = destinationName;
            this.customDestination = customDestination.isEmpty() ? null : customDestination;
            this.distance = distance;
            this.roundUpRoute = this;
            this.turnbackPlatform = turnbackPlatform;
        }

        public static class RouteInterchange {
            public final int color;
            public final String name;

            public RouteInterchange(int color, String name) {
                this.color = color;
                this.name = name;
            }
        }
    }

    public double getRailProgress() {
        return ((VehicleSchemaAccessor)vehicleExtension).getRailProgress();
    }
    public double getRailProgress(int car) {
        double progress = getRailProgress();
        for(int i = 0; i < Math.min(vehicleExtension.vehicleExtraData.immutableVehicleCars.size(), car); i++) {
            progress -= vehicleExtension.vehicleExtraData.immutableVehicleCars.get(i).getLength();
        }
        return progress;
    }
}
