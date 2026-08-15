package berries.servermod.tcm.packet;

import berries.servermod.tcm.data.vehicle.MTRDatasetHolder;
import berries.servermod.tcm.data.vehicle.VehicleDataCache;
import org.mtr.mapping.registry.PacketHandler;
import org.mtr.mapping.tool.PacketBufferReceiver;
import org.mtr.mapping.tool.PacketBufferSender;

public class MTRDataS2CPacket extends PacketHandler {
    private final MTRDatasetHolder MTRDatasetHolder;

    public MTRDataS2CPacket(PacketBufferReceiver packetBufferReceiver) {
        String jsonString = packetBufferReceiver.readString();
        VehicleDataCache.mtrDataByteCounter += 4;
        VehicleDataCache.mtrDataByteCounter += jsonString.length() * 2;
        this.MTRDatasetHolder = new MTRDatasetHolder(jsonString);
    }

    public MTRDataS2CPacket(MTRDatasetHolder dataInstance) {
        this.MTRDatasetHolder = dataInstance;
    }

    @Override
    public void write(PacketBufferSender packetBufferSender) {
        packetBufferSender.writeString(this.MTRDatasetHolder.serialize());
    }

    @Override
    public void runClient() {
        VehicleDataCache.putMTRDataCache(this.MTRDatasetHolder);
    }
}