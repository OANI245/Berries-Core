package berries.servermod.tcm.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;

public class AbstractScreen extends Screen {
    protected Field fieldRenderables;

    public AbstractScreen(Component component) {
        super(component);
        try {
            this.fieldRenderables = Screen.class.getDeclaredField("renderables");
            fieldRenderables.setAccessible(true);
        } catch (Throwable e) {
            fieldRenderables = null;
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
        try {
            for(Renderable renderable : (List<Renderable>) fieldRenderables.get(this)) {
                renderable.render(guiGraphics, i, j, f);
            }
        } catch (Throwable ignored) {
        }
    }
}
