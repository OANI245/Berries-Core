package berries.servermod.tcm.client.mixin;

import berries.servermod.tcm.util.TCMComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.mtr.mapping.holder.MinecraftClient;
import org.mtr.mapping.holder.MutableText;
import org.mtr.mapping.holder.Screen;
import org.mtr.mapping.holder.Text;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mod.generated.lang.TranslationProvider;
import org.mtr.mod.packet.PacketAddBalance;
import org.mtr.mod.screen.MTRScreenBase;
import org.mtr.mod.screen.TicketMachineScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = TicketMachineScreen.class, remap = false)
public class MixinTicketMachineScreen extends MTRScreenBase {
    @Shadow @Final private MutableText balanceText;

    protected MixinTicketMachineScreen() {
        super();
    }

    @Redirect(
            method = "lambda$new$0",
            at = @At(value = "INVOKE", target = "Lorg/mtr/mapping/holder/MinecraftClient;openScreen(Lorg/mtr/mapping/holder/Screen;)V")
    )
    private static void ln0(MinecraftClient instance, Screen screen) {
        //do nothing...
    }

    @Inject(
            method = "tick2",
            at = @At("HEAD"),
            cancellable = true)
    public void tick2(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true)
    public void render2(GraphicsHolder graphicsHolder, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.renderBackground(graphicsHolder);
        graphicsHolder.drawText(this.balanceText, 6, 6, -1, false, GraphicsHolder.getDefaultLight());

        for(int i = 0; i < 10; ++i) {
            graphicsHolder.drawText(new Text(TCMComponent.translatable("gui.tcm.ticket_machine.top_up", String.valueOf(PacketAddBalance.getAddAmount(i)))).asOrderedText(), 6, (i + 1) * 20 + 6, -1, false, GraphicsHolder.getDefaultLight());
        }

        super.render(graphicsHolder, mouseX, mouseY, delta);
        ci.cancel();
    }
}
