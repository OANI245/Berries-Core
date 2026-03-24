package berries.servermod.tcm.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;

import java.util.function.Consumer;

public class TCMMessageAndOKScreen extends Screen {
    public Component buttonText;
    public Consumer<Button> buttonEvent;

    public boolean isBlackBackground = false;
    public final boolean shouldCloseOnEsc;

    public TCMMessageAndOKScreen(Component message, Component buttonText, Consumer<Button> buttonEvent, boolean shouldCloseOnEsc) {
        super(message);
        this.buttonText = buttonText;
        this.buttonEvent = buttonEvent;
        this.shouldCloseOnEsc = shouldCloseOnEsc;
    }

    public TCMMessageAndOKScreen(Component message, Component buttonText, Consumer<Button> buttonEvent, boolean isBlackBackground, boolean shouldCloseOnEsc) {
        this(message, buttonText, buttonEvent, shouldCloseOnEsc);
        this.isBlackBackground = isBlackBackground;
    }

    @Override
    protected void init() {
        int buttonWidth = 120;
        this.addRenderableWidget(
            Button.builder(buttonText, (Button.OnPress) (button) -> {
                buttonEvent.accept(button);
            }).size(buttonWidth, 20).pos(width / 2 - buttonWidth / 2, height - 80).build()
        );
        super.init();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
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
