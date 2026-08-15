package berries.servermod.tcm.client.util.model;

import berries.servermod.tcm.client.mixin.OptimizedModelAccessor;
import berries.servermod.tcm.client.mixin.OptimizedModelWrapperAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import org.mtr.mapping.mapper.OptimizedModel;
import org.mtr.mapping.render.batch.MaterialProperties;
import org.mtr.mapping.render.model.RawModel;
import org.mtr.mapping.render.object.VertexArray;
import org.mtr.mod.client.CustomResourceLoader;
import org.mtr.mod.resource.OptimizedModelWrapper;

import java.util.List;

public class DynamicModelHolder {
    private OptimizedModelWrapper uploadedModel;

    public DynamicModelHolder() {
    }

    public void uploadLater(RawModel modelData) {
        if(modelData == null) throw new IllegalArgumentException("modelData cannot be null!");

        RenderSystem.recordRenderCall(() -> {
            OptimizedModelWrapper lastUploadedModel = uploadedModel;
            CustomResourceLoader.OPTIMIZED_RENDERER_WRAPPER.beginReload();
            List<VertexArray> vertexArrays = modelData.upload(OptimizedModelAccessor.getDefaultMapping());
            OptimizedModel optimizedModel = new OptimizedModel(vertexArrays);
            uploadedModel = OptimizedModelWrapperAccessor.get(optimizedModel);
            if (lastUploadedModel != null) ((OptimizedModelWrapperAccessor)((Object)lastUploadedModel)).getOptimizedModel().close();
            CustomResourceLoader.OPTIMIZED_RENDERER_WRAPPER.finishReload();
        });
    }

    public void uploadCopyForMaterialChangesLater(RawModel modelData) {
        if(modelData == null) throw new IllegalArgumentException("modelData cannot be null!");

        RenderSystem.recordRenderCall(() -> {
            OptimizedModelWrapper lastUploadedModel = uploadedModel;
            CustomResourceLoader.OPTIMIZED_RENDERER_WRAPPER.beginReload();
            List<VertexArray> vertexArrays = modelData.upload(OptimizedModelAccessor.getDefaultMapping()).stream().map(e -> {
                MaterialProperties newProp = new MaterialProperties(e.materialProperties.shaderType, e.materialProperties.getTexture(), e.materialProperties.vertexAttributeState.color);
                return new VertexArray(e, newProp);
            }).toList();
            OptimizedModel optimizedModel = new OptimizedModel(vertexArrays);
            uploadedModel = OptimizedModelWrapperAccessor.get(optimizedModel);
            if (lastUploadedModel != null) ((OptimizedModelWrapperAccessor)((Object)lastUploadedModel)).getOptimizedModel().close();
            CustomResourceLoader.OPTIMIZED_RENDERER_WRAPPER.finishReload();
        });
    }

    public OptimizedModelWrapper getUploadedModel() {
        return uploadedModel;
    }

    public void close() {
        ((OptimizedModelWrapperAccessor)((Object)uploadedModel)).getOptimizedModel().close();
    }
}