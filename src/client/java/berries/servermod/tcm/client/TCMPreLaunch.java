package berries.servermod.tcm.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

@Environment(EnvType.CLIENT)
public class TCMPreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
    }
}
