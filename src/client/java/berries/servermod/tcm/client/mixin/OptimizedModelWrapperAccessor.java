package berries.servermod.tcm.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.mtr.mapping.mapper.OptimizedModel;
import org.mtr.mod.resource.OptimizedModelWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(OptimizedModelWrapper.class)
public interface OptimizedModelWrapperAccessor {
    @Invoker("<init>")
    static OptimizedModelWrapper get(OptimizedModel base) {
        return null;
    }

    @Accessor
    OptimizedModel getOptimizedModel();
}
