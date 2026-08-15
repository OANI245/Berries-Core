package berries.servermod.tcm.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.mtr.mapping.render.batch.MaterialProperties;
import org.mtr.mapping.render.model.Face;
import org.mtr.mapping.render.model.RawMesh;
import org.mtr.mapping.render.vertex.Vertex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(value = RawMesh.class, remap = false)
public interface RawMeshAccessor {
    @Accessor("materialProperties")
    @Mutable
    void setMaterialProperties(MaterialProperties properties);

    @Accessor("vertices")
    @Mutable
    void setVertices(List<Vertex> vertices);

    @Accessor("faces")
    @Mutable
    void setFaces(List<Face> faces);
}