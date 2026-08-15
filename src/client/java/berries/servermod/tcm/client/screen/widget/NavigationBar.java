package berries.servermod.tcm.client.screen.widget;

import berries.servermod.tcm.client.flueroui.FlueroUI;
import berries.servermod.tcm.client.screen.GUILocations;
import berries.servermod.tcm.util.TCMComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NavigationBar extends AbstractWidget {
    private final int itemWidth = width / 3 - 8;

    public boolean expanded = false;
    public List<AbstractButton> items = new ArrayList<>();

    public NavigationBar(int i, int j, int k, int l) {
        super(i, j, k, l, TCMComponent.text(""));
    }

    public NavigationBar addItem(AbstractButton item) {
        if (!items.contains(item)) {
            items.add(item);
        }

        return this;
    }

    public NavigationBar addNavItem(Component text, ResourceLocation icon, Consumer<NavigationBarItem> onPress) {
        int i = items.size();
        return addItem(new NavigationBarItem(
                ((width / 3) - itemWidth) / 2 - 1,
                ((itemWidth + 4) * i) + 4,
                itemWidth,
                itemWidth,
                text,
                icon,
                onPress
        ));
    }

    public NavigationBar addBottomNavItem(Component text, ResourceLocation icon, Consumer<NavigationBarItem> onPress) {
        int i = items.size();
        return addItem(new NavigationBarItem(
                ((width / 3) - itemWidth) / 2 - 1,
                height - itemWidth - 4,
                itemWidth,
                itemWidth,
                text,
                icon,
                onPress
        ));
    }

    public void addAllRenderable(Consumer<AbstractWidget> fun) {
        for (AbstractButton item : items) {
            fun.accept(item);
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        return false;
    }

    @Override
    public void renderWidget(GuiGraphics poseStack, int i, int j, float f) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, expanded ? GUILocations.MAIN_SCREEN_NAVIGATION_BAR_EXPANDED_LOCATION : GUILocations.MAIN_SCREEN_NAVIGATION_BAR_LOCATION);

        int barWidth = expanded ? width : width / 3;
        poseStack.blit(expanded ? GUILocations.MAIN_SCREEN_NAVIGATION_BAR_EXPANDED_LOCATION : GUILocations.MAIN_SCREEN_NAVIGATION_BAR_LOCATION, getX(), getY(), 0, 0, barWidth, height, barWidth, height);
        //poseStack.fill(0, 0, barWidth, height, FlueroUI.BACKGROUND_HIGH);
    }
}
