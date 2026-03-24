package berries.servermod.tcm.client.screen.widget;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class MetroButton extends Button {
    public MetroButton(int i, int j, int k, int l, Component component, OnPress onPress) {
        super(i, j, k, l, component, onPress, Button.DEFAULT_NARRATION);
    }
}
