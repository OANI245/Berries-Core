package berries.servermod.tcm.client.vehicle.textpreset.vehicle;

import berries.servermod.tcm.client.util.TextUtil;
import berries.servermod.tcm.client.vehicle.textpreset.TextPreset;
import berries.servermod.tcm.util.VehicleWrapper;
import org.mtr.mod.client.MinecraftClientData;

import java.util.Arrays;
import java.util.List;

public class TRM01TextPreset implements TextPreset {
    public static final TRM01TextPreset INSTANCE = new TRM01TextPreset();

    private TRM01TextPreset() {
    }

    @Override
    public String[] getIdleMessage(VehicleWrapper wrapper) {
        return new String[]{
                "欢迎乘坐天城轨道交通2号线。 Welcome to Tiancheng Rail Transit Line 2."
        };
    }

    @Override
    public String[] getNextStationMessage(VehicleWrapper wrapper) {
        String[] value = new String[1];
        List<VehicleWrapper.Stop> stations = wrapper.getThisRouteStops();
        /*var route = !stations.isEmpty() ? stations.getFirst().route : null;
        if (route == null) {
            return new String[]{"Debug Message 0 Debug Message 0 :" + stations.size() + stations.toString()};
        }*/
        String routeName = wrapper.getVehicle().vehicleExtraData.getThisRouteName();
        var thisStationIndex = wrapper.getNextStopIndex(stations) - 1;
        if (thisStationIndex < 0) {
            return null;
        }
        var thisStation = stations.get(thisStationIndex);
        var nextStation = stations.get(wrapper.getNextStopIndex(stations));
        var terminalStation = stations.getLast();
        boolean isStartStation = thisStation.equals(stations.getFirst());
        boolean isTerminalStation = nextStation.equals(terminalStation);
        StringBuilder sbCjk = new StringBuilder();
        StringBuilder sbNonCjk = new StringBuilder();
        if (isStartStation) {
            sbCjk.append("乘客您好，欢迎乘坐地铁")
                    .append(TextUtil.getCjkParts(routeName))
                    .append("列车。");
            sbNonCjk.append("Welcome to ")
                    .append(TextUtil.getNonCjkParts(routeName))
                    .append(". ");
        }
        if (!isTerminalStation) {
            sbCjk.append("本次列车终点站是").append(TextUtil.getCjkParts(terminalStation.name)).append("。下一站");
            sbNonCjk.append("This train is bound for ").append(TextUtil.getNonCjkParts(terminalStation.name)).append(". ");
        } else {
            sbCjk.append("下一站是终点站");
        }
        if (isTerminalStation) {
            sbNonCjk.append("This train will terminus at the next station");
        } else {
            sbNonCjk.append("The next station is ");
            sbNonCjk.append(TextUtil.getNonCjkParts(nextStation.name));
        }
        sbCjk.append(TextUtil.getCjkParts(nextStation.name)).append("。");
        /*if (isStartStation) {
            sbNonCjk.append(" where train terminus");
        }*/
        sbNonCjk.append(". ");
        List<String> transferRouteNames = nextStation.routeInterchanges.stream().map((r) -> r.name).sorted().toList();
        if (!transferRouteNames.isEmpty()) {
            sbCjk.append("您可以在该站换乘地铁");
            sbNonCjk.append("Change here for ");
            boolean isFirst = true;
            for (String trn : transferRouteNames) {
                if (!isFirst) {
                    sbCjk.append("、");
                    sbNonCjk.append(", ");
                }
                isFirst = false;

                sbCjk.append(TextUtil.getCjkParts(trn));
                sbNonCjk.append(TextUtil.getNonCjkParts(trn));
            }
            sbCjk.append("。");
            sbNonCjk.append(". ");
        }
        value[0] = sbCjk + " " + sbNonCjk;
        return value;
    }

    @Override
    public String[] getArrivingMessage(VehicleWrapper wrapper) {
        List<VehicleWrapper.Stop> stations = wrapper.getThisRouteStops();
        var thisStationIndex = wrapper.getNextStopIndex(stations);
        if (thisStationIndex < 0) {
            return null;
        }
        var thisStation = stations.get(wrapper.getNextStopIndex(stations));
        var thisStationName = thisStation.name;
        boolean isTerminalStation = thisStation.equals(stations.getLast());
        var thisStationNameCjk = TextUtil.getCjkParts(thisStationName);
        /*var route = !stations.isEmpty() ? stations.get(0).route : null;
        if (route == null) {
            return null;
        }*/
        String routeName = wrapper.getVehicle().vehicleExtraData.getThisRouteName();
        return new String[]{
                String.format("即将到达%s%s%s。%s We are now arriving at %s. %s",
                        isTerminalStation ? "终点站" : "",
                        thisStationNameCjk,
                        isTerminalStation || thisStationNameCjk.endsWith("站") ? "" : "站",
                        isTerminalStation ?
                                "感谢您乘坐天城轨道交通"
                                + TextUtil.getCjkParts(routeName)
                                + "。" : "",
                        TextUtil.getNonCjkParts(thisStationName),
                        isTerminalStation ?
                                "This train terminus here, Thank you for taking Tiancheng Rail Transit "
                                + TextUtil.getNonCjkParts(routeName)
                                + ". " : "")
        };
    }

    @Override
    public String[] getHeadDisplayMessage(VehicleWrapper wrapper, String[] def) {
        List<VehicleWrapper.Stop> stations = wrapper.getThisRouteStops();
        var terminalStation = stations.getLast();
        var terminalStationName = terminalStation.name;
        return new String[]{
                String.format("开往 %s  To %s",
                        TextUtil.getCjkParts(terminalStationName),
                        TextUtil.getNonCjkParts(terminalStationName))
        };
    }

    @Override
    public String[] getSideDisplayMessage(VehicleWrapper wrapper, String[] def) {
        List<VehicleWrapper.Stop> stations = wrapper.getThisRouteStops();
        var terminalStation = stations.getLast();
        var terminalStationName = terminalStation.name;
        return new String[]{
                String.format("终点站：%s", TextUtil.getCjkParts(terminalStationName)),
                String.format("Destination: %s", TextUtil.getNonCjkParts(terminalStationName))
        };
    }
}
