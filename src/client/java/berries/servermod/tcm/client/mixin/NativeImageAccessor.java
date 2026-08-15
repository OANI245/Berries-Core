package berries.servermod.tcm.client.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(NativeImage.class)
public interface NativeImageAccessor {
    @Accessor
    long getPixels();
}