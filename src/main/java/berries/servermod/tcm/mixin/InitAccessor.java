package berries.servermod.tcm.mixin;

import org.mtr.core.Main;
import org.mtr.mod.Init;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Init.class, remap = false)
public interface InitAccessor {
    @Accessor("main")
    static Main getMain() {
        throw new AssertionError();
    }
}