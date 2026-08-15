package berries.servermod.tcm.client.flueroui;

import berries.servermod.tcm.UFEInfo;
import net.minecraft.resources.ResourceLocation;

public interface Icons {
    ResourceLocation EDIT = of("edit");
    ResourceLocation ARROW_DOWN = of("arrow_down");
    ResourceLocation ARROW_UP = of("arrow_up");
    ResourceLocation PLAY_ARROW = of("play_arrow");
    ResourceLocation PAUSE = of("pause");
    ResourceLocation CHECK = of("check");
    ResourceLocation ARROW_FORWARD = of("arrow_forward");
    ResourceLocation ARROW_BACK = of("arrow_back");
    ResourceLocation ARROW_BACK_BLUE = of("arrow_back_blue");

    static ResourceLocation of(String filename) {
        return new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/icon/" + filename + ".png");
    }
}
