package berries.servermod.tcm.client.util.model;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.client.mixin.ObjModelAccessor;
import berries.servermod.tcm.client.mixin.OptimizedModelAccessor;
import berries.servermod.tcm.client.mixin.OptimizedModelWrapperAccessor;
import berries.servermod.tcm.client.mixin.RawMeshAccessor;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.mtr.libraries.de.javagl.obj.Obj;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.mtr.mapping.holder.Identifier;
import org.mtr.mapping.mapper.OptimizedModel;
import org.mtr.mapping.mapper.ResourceManagerHelper;
import org.mtr.mapping.render.batch.MaterialProperties;
import org.mtr.mapping.render.model.RawModel;
import org.mtr.mapping.render.object.VertexArray;
import org.mtr.mapping.render.vertex.VertexAttributeMapping;
import org.mtr.mapping.render.vertex.VertexAttributeSource;
import org.mtr.mapping.render.vertex.VertexAttributeType;
import org.mtr.mod.client.CustomResourceLoader;
import org.mtr.mod.resource.CustomResourceTools;
import org.mtr.mod.resource.OptimizedModelWrapper;
import org.mtr.mod.resource.RenderStage;

import java.util.*;

public class ModelManager {
    private static final Map<ResourceLocation, RawModel> rawModelCache = new HashMap<>();
    private static final Map<RawModel, OptimizedModel> modelCache = new HashMap<>();

    public static RawModel loadModel(ResourceLocation id, boolean flipTextureV) {
        if (rawModelCache.containsKey(id)) return rawModelCache.get(id);

        try {
            RawModel rawModel = new RawModel();

            Map<String, RawModel> modelDataParts = new HashMap<>(loadPartedModel(id, flipTextureV));
            modelDataParts.values().forEach(rawModel::append);
            rawModelCache.put(id, rawModel);
            return rawModel;
        } catch (Exception e) {
            TCM.LOGGER.error("", e);
            return null;
        }
    }

    public static OptimizedModelWrapper upload(RawModel model, boolean interior) {
        var type = interior ? RenderStage.INTERIOR : RenderStage.EXTERIOR;
        model.iterateRawMeshList(rawMesh -> {
            MaterialProperties originalProp = rawMesh.materialProperties;
            ((RawMeshAccessor) (Object) rawMesh).setMaterialProperties(new MaterialProperties(type.shaderType, originalProp.getTexture(), originalProp.vertexAttributeState.color));
        });
        model.applyUVMirror(false, true);
        return OptimizedModelWrapperAccessor.get(ModelManager.upload(model));
    }

    public static OptimizedModelWrapper uploadLight(RawModel model) {
        var type = RenderStage.ALWAYS_ON_LIGHT;
        model.iterateRawMeshList(rawMesh -> {
            MaterialProperties originalProp = rawMesh.materialProperties;
            ((RawMeshAccessor) (Object) rawMesh).setMaterialProperties(new MaterialProperties(type.shaderType, originalProp.getTexture(), originalProp.vertexAttributeState.color));
        });
        model.applyUVMirror(false, true);
        return OptimizedModelWrapperAccessor.get(ModelManager.upload(model));
    }

    public static OptimizedModel upload(RawModel data) {
        if (modelCache.containsKey(data)) return modelCache.get(data);
        final List<VertexArray> uploadedParts = new ArrayList<>(data.upload(OptimizedModelAccessor.getDefaultMapping()));
        var value = new OptimizedModel(uploadedParts);
        modelCache.put(data, value);
        return value;
    }

    public static Map<String, RawModel> loadPartedModel(ResourceLocation modelLocation, boolean flipTextureV) {
        Map<String, RawModel> vl = new HashMap<>();

        String ns = modelLocation.getNamespace();
        String ph = ns + ":" + modelLocation.getPath();
        boolean isSupportedFormat = ph.endsWith(".obj");
        ResourceLocation textureId = CustomResourceTools.formatIdentifierWithDefault("", "png").data;
        if (!isSupportedFormat) {
            throw new IllegalArgumentException("Unsupported model format " + ph + ".");
        } else {
            OptimizedModel.ObjModel.loadModel(
                    ResourceManagerHelper.readResource(CustomResourceTools.formatIdentifierWithDefault(ph, "obj")),
                    (mtlString) ->
                            ResourceManagerHelper.readResource(
                                    CustomResourceTools.getResourceFromSamePath(ph, mtlString, "mtl")
                            ),
                    (textureString) ->
                            StringUtils.isEmpty(textureString) ?
                                    OptimizedModelWrapper.WHITE_TEXTURE :
                                    (StringUtils.equals(textureString, "default.png") ?
                                     new Identifier(textureId) :
                                            CustomResourceTools.getResourceFromSamePath(ph, textureString, "png")),
                    null, true, flipTextureV).forEach((k, v) -> {
                        //if (((ObjModelAccessor) ((Object) v)).getRawMeshes())

                        RawModel model = new RawModel();
                        (((ObjModelAccessor) ((Object) v)).getRawMeshes()).forEach(model::append);
                        vl.put(k, model);
                    });
            return vl;
        }
    }

    public static OptimizedModel copyForMaterialChanges(OptimizedModel original) {
        List<VertexArray> vertexArrays =
                ((OptimizedModelAccessor)(Object)original).getUploadedParts().stream().map(e -> {
                    MaterialProperties newProp = new MaterialProperties(e.materialProperties.shaderType, e.materialProperties.getTexture(), e.materialProperties.vertexAttributeState.color);
                    return new VertexArray(e, newProp);
                }).toList();

        return new OptimizedModel(vertexArrays);
    }

    public static void replaceTexture(OptimizedModelWrapper model, String oldTexture, Identifier newTexture) {
        OptimizedModel optimizedModel = ((OptimizedModelWrapperAccessor)(Object)model).getOptimizedModel();
        List<VertexArray> vertexArrays = ((OptimizedModelAccessor)(Object)optimizedModel).getUploadedParts();

        vertexArrays.forEach(e -> {
            Identifier id = e.materialProperties.getTexture();
            if (id == null) return;

            String oldPath = id.getPath();
            if (oldPath.substring(oldPath.lastIndexOf("/") + 1).equals(oldTexture)) {
                e.materialProperties.setTexture(newTexture);
            }
        });
    }

    public static void replaceAllTexture(OptimizedModelWrapper model, Identifier id) {
        OptimizedModel optimizedModel = ((OptimizedModelWrapperAccessor)(Object)model).getOptimizedModel();
        List<VertexArray> vertexArrays = ((OptimizedModelAccessor)(Object)optimizedModel).getUploadedParts();

        vertexArrays.forEach(e -> {
            e.materialProperties.setTexture(id);
        });
    }

    public static void reset() {
        modelCache.forEach((k, v) -> v.close());
        modelCache.clear();
        rawModelCache.clear();
    }
}
