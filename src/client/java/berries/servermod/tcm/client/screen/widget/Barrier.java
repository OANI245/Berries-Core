package berries.servermod.tcm.client.screen.widget;

import berries.servermod.tcm.util.TCMComponent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class Barrier extends AbstractWidget {
    public Barrier(int i, int j, int k, int l) {
        super(i, j, k, l, TCMComponent.text(""));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        int argb = 0x886B6B6B;
        guiGraphics.fill(getX(), getY(), getX() + this.width, getY() + this.height, -(0xFFFFFFFF - argb));
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
