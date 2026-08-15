package berries.servermod.tcm.client.screen;

import berries.servermod.tcm.UFEInfo;
import berries.servermod.tcm.client.flueroui.widget.FlueroButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class TCMMessageAndOKScreen extends Screen {
    public Component buttonText;
    public Consumer<AbstractButton> buttonEvent;

    public boolean isBlackBackground = false;
    public final boolean shouldCloseOnEsc;

    public TCMMessageAndOKScreen(Component message, Component buttonText, Consumer<AbstractButton> buttonEvent, boolean shouldCloseOnEsc) {
        super(message.plainCopy().setStyle(Style.EMPTY.withFont(new ResourceLocation(UFEInfo.MOD_ID, "vga"))));
        this.buttonText = buttonText;
        this.buttonEvent = buttonEvent;
        this.shouldCloseOnEsc = shouldCloseOnEsc;
    }

    public TCMMessageAndOKScreen(Component message, Component buttonText, Consumer<AbstractButton> buttonEvent, boolean isBlackBackground, boolean shouldCloseOnEsc) {
        this(message, buttonText, buttonEvent, shouldCloseOnEsc);
        this.isBlackBackground = isBlackBackground;
    }

    @Override
    protected void init() {
        int buttonWidth = 120;
        this.addRenderableWidget(
                new FlueroButton(width / 2 - buttonWidth / 2, height - 80, buttonWidth, 16, buttonText.plainCopy().setStyle(Style.EMPTY.withFont(new ResourceLocation(UFEInfo.MOD_ID, "vga"))), (button) -> {
                    buttonEvent.accept(button);
                })
        ).setLightMode(true);
        super.init();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
        if (minecraft == null) return;

        if (isBlackBackground) {
            guiGraphics.fill(0, 0, width, height, FastColor.ARGB32.color(255, 0, 0, 0));
        } else {
            this.renderBackground(guiGraphics);
        }
        guiGraphics.drawCenteredString(this.minecraft.font, this.title, width / 2, height * 6 / 16, 0xFFFFFF);
        super.render(guiGraphics, i, j, f);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return shouldCloseOnEsc;
    }
}
