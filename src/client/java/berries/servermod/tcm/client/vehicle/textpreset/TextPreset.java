package berries.servermod.tcm.client.vehicle.textpreset;

import berries.servermod.tcm.util.VehicleWrapper;

public interface TextPreset {
    byte IDLE = 0x00;
    byte NEXT_STATION = 0x01;
    byte ARRIVING = 0x02;
    byte ARRIVED = 0x03;
    byte HEAD_DISPLAY = 0x04;
    byte SIDE_DISPLAY = 0x05;

    default String[] getIdleMessage(VehicleWrapper wrapper) { return new String[0]; };
    String[] getNextStationMessage(VehicleWrapper wrapper);
    String[] getArrivingMessage(VehicleWrapper wrapper);
    default String[] getArrivedMessage(VehicleWrapper wrapper) { return getArrivingMessage(wrapper); };
    String[] getHeadDisplayMessage(VehicleWrapper wrapper, String[] def);
    default String[] getSideDisplayMessage(VehicleWrapper wrapper, String[] def) { return new String[0]; };
}
