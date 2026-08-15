package berries.servermod.tcm.client.vehicle.processing.screens.lcd.general;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.client.mixin.OptimizedModelWrapperAccessor;
import berries.servermod.tcm.client.render.MTRModdedRenderManager;
import berries.servermod.tcm.client.util.model.ModelManager;
import berries.servermod.tcm.client.vehicle.processing.Processor;
import berries.servermod.tcm.client.vehicle.processing.ProcessorName;
import berries.servermod.tcm.util.VehicleWrapper;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.booleans.BooleanBooleanImmutablePair;
import net.minecraft.resources.ResourceLocation;
import org.mtr.mod.render.StoredMatrixTransformations;
import org.mtr.mod.resource.OptimizedModelWrapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ProcessorName("tc_general_lcd")
@SuppressWarnings("unused")
public class GeneralLCDProcessor extends Processor {
    public static final byte DRAW_EXECUTOR = -0x80;
    public static final byte LCD_DRAWER = -0x7f;
    public static final byte SCREEN_MODEL = -0x7e;
    public static final byte WRAPPER_NOW = 0x00;
    public static final byte FLIP_COPY = 0x6f;
    public static final byte READY = 0x7e;

    private static final boolean DISABLED = true;

    private final OptimizedModelWrapper lcdScreenModelPreCopy;

    public GeneralLCDProcessor(Map<String, Object> inputs) throws IOException {
        super(inputs);

        if (!inputs.containsKey("screen_model_location")) {
            throw new IllegalArgumentException("Required Screen Model Resource Location");
        }

        var m = ModelManager.loadModel(
                new ResourceLocation(((JsonPrimitive)inputs.get("screen_model_location"))
                        .getAsString()), false);
        if (m == null) {
            throw new IOException("Cannot load LCD Screen Model");
        }
        this.lcdScreenModelPreCopy = ModelManager.uploadLight(m);
    }

    @Override
    public boolean load(VehicleWrapper wrapper, Map<Byte, Object> st, List<MTRModdedRenderManager> renderManagers) {
        if (DISABLED) {
            return false;
        }

        boolean flipCopy = ((JsonPrimitive)inputs.getOrDefault("flip", new JsonPrimitive(true))).getAsBoolean();

        var service = Executors.newScheduledThreadPool(1);
        var lcdWidth = ((JsonPrimitive)inputs.getOrDefault("lcd_width",
                new JsonPrimitive(1024))).getAsInt();
        var lcdHeight = ((JsonPrimitive)inputs.getOrDefault("lcd_height",
                new JsonPrimitive(341))).getAsInt();
        var texDiameter = Math.max(((JsonPrimitive)inputs.getOrDefault("texture_dia",
                new JsonPrimitive(1024))).getAsInt(), lcdWidth);
        var drawer = new GeneralLCDDrawer(lcdWidth, lcdHeight, texDiameter, texDiameter);
        drawer.init();

        var screenModel = OptimizedModelWrapperAccessor.get(ModelManager.copyForMaterialChanges(
                ((OptimizedModelWrapperAccessor) ((Object) lcdScreenModelPreCopy)).getOptimizedModel()));
        ModelManager.replaceAllTexture(screenModel, drawer.getIdentifier());
        st.put(SCREEN_MODEL, screenModel);

        service.scheduleAtFixedRate(() -> {
            var wrapperNow = (VehicleWrapper) st.getOrDefault(WRAPPER_NOW, wrapper);
            drawer.draw(wrapperNow != null ? wrapperNow : wrapper, st);
        }, 0, 1000 / 24, TimeUnit.MILLISECONDS);

        st.put(LCD_DRAWER, drawer);
        st.put(DRAW_EXECUTOR, service);
        st.put(FLIP_COPY, flipCopy);
        st.put(READY, true);
        return true;
    }

    @Override
    public void submit(VehicleWrapper wrapper, Map<Byte, Object> st, List<MTRModdedRenderManager> renderManagers, long millisElapsed) {
        if (DISABLED || !(boolean) st.getOrDefault(READY, false)) {
            return;
        }

        st.put(WRAPPER_NOW, wrapper);

        var screenModel = (OptimizedModelWrapper)st.getOrDefault(SCREEN_MODEL, null);
        if (screenModel == null) {
            return;
        }

        var poseStack = new Stack<StoredMatrixTransformations>();
        poseStack.add(new StoredMatrixTransformations());
        for (int cn : wrapper.getAvailableCars()) {
            var renderManager = renderManagers.get(cn);
            poseStack.push(new StoredMatrixTransformations());
            renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent()
                    .model(screenModel)
                    .matrices(poseStack));
            poseStack.pop();
            if ((boolean)st.getOrDefault(FLIP_COPY, true)) {
                poseStack.push(new StoredMatrixTransformations());
                poseStack.peek().add((gh) -> gh.rotateYRadians((float) Math.PI));
                renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent()
                        .model(screenModel)
                        .matrices(poseStack));
                poseStack.pop();
            }
        }
    }

    @Override
    public void destroy(VehicleWrapper wrapper, Map<Byte, Object> st) {
        var service = (ScheduledExecutorService) st.getOrDefault(DRAW_EXECUTOR, null);
        if (service != null) {
            service.shutdown();
            service.close();
        }

        var drawer = (GeneralLCDDrawer) st.getOrDefault(LCD_DRAWER, null);
        if (drawer != null) {
            drawer.close();
        }
    }
}
