package berries.servermod.tcm.client.mixin;

import berries.servermod.tcm.client.TCMClient;
import io.netty.channel.Channel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Connection.class)
public abstract class MixinConnection {
    @Shadow
    private Channel channel;

    @Inject(
            method = "disconnect",
            at = @At("HEAD")
    )
    private void injected01(CallbackInfo ci) {
        if (!TCMClient.syncTeleports.isEmpty()) {
            TCMClient.syncTeleports.clear();
        }
        if (!TCMClient.syncTimeMessages.isEmpty()) {
            TCMClient.syncTimeMessages.clear();
        }
    }
}
