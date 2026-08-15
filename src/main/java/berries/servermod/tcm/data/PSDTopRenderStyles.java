package berries.servermod.tcm.data;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public enum PSDTopRenderStyles {
    MTR(0, "gui.tcm.psd_top.default_style"),
    BEIJING_1(1, "gui.tcm.psd_top.beijing_1"),
    BEIJING_2(2, "gui.tcm.psd_top.beijing_2"),
    BEIJING_3(3, "gui.tcm.psd_top.beijing_3");

    private final int id;
    private final String translate;

    private PSDTopRenderStyles(int id, String translate) {
        this.id = id;
        this.translate = translate;
    }

    public int getId() {
        return id;
    }

    public Component getComponent() {
        return Component.translatable(translate);
    }

    @NotNull
    public static PSDTopRenderStyles getById(int id) {
        for (PSDTopRenderStyles value : values()) {
            if (value.id == id) {
                return value;
            }
        }
        return MTR;
    }
}
