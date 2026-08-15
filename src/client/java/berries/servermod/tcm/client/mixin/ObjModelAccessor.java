package berries.servermod.tcm.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.mtr.mapping.mapper.OptimizedModel;
import org.mtr.mapping.render.model.RawMesh;
import org.mtr.mapping.render.model.RawModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(OptimizedModel.ObjModel.class)
public interface ObjModelAccessor {
    @Accessor
    RawModel getRawModel();

    @Accessor
    List<RawMesh> getRawMeshes();
}
