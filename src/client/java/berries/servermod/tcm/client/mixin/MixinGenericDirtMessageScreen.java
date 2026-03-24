package berries.servermod.tcm.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(GenericDirtMessageScreen.class)
public class MixinGenericDirtMessageScreen extends Screen {
    protected MixinGenericDirtMessageScreen(Component component) {
        super(component);
    }

    @WrapOperation(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V")
    )
    protected void injected01(GuiGraphics instance, Font font, Component component, int i, int j, int k, Operation<Void> original) {
        int x = this.width / 2 - font.width(component) / 2;
        int y = this.height / 2 - font.lineHeight / 2;

        boolean test = false;

        int l = x - 12;
        int m = y - 12;
        int n = font.width(component) + 12 * 2;
        int o = font.lineHeight + 12 * 2;
        int p = test ? -1 : -6250336;
        instance.fill(l + 1, m, l + n, m + o, -16777216);
        renderOutline(instance, l, m, n, o, p);

        original.call(instance, font, component, i, this.height / 2 - font.lineHeight / 2, k);
    }

    @Unique
    public void renderOutline(GuiGraphics guiGraphics, int i, int j, int k, int l, int m) {
        guiGraphics.fill(i, j, i + k, j + 1, m);
        guiGraphics.fill(i, j + l - 1, i + k, j + l, m);
        guiGraphics.fill(i, j + 1, i + 1, j + l - 1, m);
        guiGraphics.fill(i + k - 1, j + 1, i + k, j + l - 1, m);
    }
}
