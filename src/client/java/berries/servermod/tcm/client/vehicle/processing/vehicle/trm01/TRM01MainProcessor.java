package berries.servermod.tcm.client.vehicle.processing.vehicle.trm01;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.client.mixin.OptimizedModelWrapperAccessor;
import berries.servermod.tcm.client.render.GraphicsTexture;
import berries.servermod.tcm.client.render.MTRModdedRenderManager;
import berries.servermod.tcm.client.util.model.ModelManager;
import berries.servermod.tcm.client.vehicle.processing.Processor;
import berries.servermod.tcm.client.vehicle.processing.ProcessorName;
import berries.servermod.tcm.client.vehicle.processing.vehicle.TRM0102SpeedScreenDrawer;
import berries.servermod.tcm.client.vehicle.textpreset.CachedTextPreset;
import berries.servermod.tcm.client.vehicle.textpreset.vehicle.TRM01TextPreset;
import berries.servermod.tcm.util.VehicleWrapper;
import berries.servermod.tcm.client.vehicle.processing.screens.ScreenDisplayDrawer;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.mtr.core.tool.Utilities;
import org.mtr.mapping.holder.ClientPlayerEntity;
import org.mtr.mapping.holder.MinecraftClient;
import org.mtr.mapping.render.model.RawModel;
import org.mtr.mod.client.VehicleRidingMovement;
import org.mtr.mod.render.StoredMatrixTransformations;
import org.mtr.mod.resource.OptimizedModelWrapper;

import java.awt.*;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@ProcessorName("trm01_main")
public class TRM01MainProcessor extends Processor {
    public static final byte DRAW_EXECUTOR = -0x80;
    public static final byte CACHED_TEXT_PRESET = -0x7f;
    public static final byte LED_DRAWER = -0x7e;
    public static final byte SCREEN_MODELS = -0x7d;
    public static final byte TRAIN_NUMBER_TEXTURE = -0x7c;
    public static final byte TRAIN_NUMBER_MODELS = -0x7b;
    public static final byte WRAPPER_NOW = 0x00;
    public static final byte TRAIN_NUMBER_IN_DEPOT = 0x7a;
    public static final byte LED_START_TIMES = 0x7b;
    public static final byte PREVIOUS_MESSAGES = 0x7c;
    public static final byte TRAIN_CAR_TYPE = 0x7d;
    public static final byte READY = 0x7e;

    private final Map<String, OptimizedModelWrapper> ledScreenPartedModels = new HashMap<>();
    private final Map<String, OptimizedModelWrapper> dynamicTrainNumberPartedModels = new HashMap<>();

    private boolean trainNumberTextureDirty = true;
    private long lastFrameMillis = System.currentTimeMillis();

    public TRM01MainProcessor(Map<String, Object> inputs) {
        super(inputs);
        for (Map.Entry<String, RawModel> model : ModelManager
                .loadPartedModel(new ResourceLocation("mtr", "train/trm01/trm01_led_screens.obj"), false).entrySet()) {
            ledScreenPartedModels.put(model.getKey(),
                    model.getKey().endsWith("base") ? ModelManager.upload(model.getValue(), false)
                            : ModelManager.uploadLight(model.getValue()));
        }
        for (Map.Entry<String, RawModel> model : ModelManager
                .loadPartedModel(new ResourceLocation("mtr", "train/trm01/trm01_train_numbers.obj"), false)
                .entrySet()) {
            dynamicTrainNumberPartedModels.put(model.getKey(),
                    ModelManager.upload(model.getValue(), model.getKey().endsWith("_in")));
        }
    }

    @Override
    public boolean load(VehicleWrapper wrapper, Map<Byte, Object> st, List<MTRModdedRenderManager> renderManagers) {
        if (wrapper.getAvailableCars().length == 0) {
            return false;
        }

        var service = Executors.newScheduledThreadPool(1);
        var drawer = new ScreenDisplayDrawer(640, 640, new ResourceLocation[] {
                new ResourceLocation("mtr", "font/simsun.ttf")
        });
        var trainNumberTextures = new HashMap<Integer, GraphicsTexture>();

        drawer.createGraphicsTexture();

        var screenModels = new HashMap<String, OptimizedModelWrapper>();
        for (Map.Entry<String, OptimizedModelWrapper> somwe : ledScreenPartedModels.entrySet()) {
            var temp = OptimizedModelWrapperAccessor.get(ModelManager.copyForMaterialChanges(
                    ((OptimizedModelWrapperAccessor) ((Object) somwe.getValue())).getOptimizedModel()));
            ModelManager.replaceTexture(temp, "template_640_640.png", drawer.getTexture().identifier);
            screenModels.put(somwe.getKey(), temp);
        }

        var trainNumberModels = new HashMap<ObjectObjectImmutablePair<Integer, String>, OptimizedModelWrapper>();
        Map<Byte, TRM01CarTypes> carTypes = new Byte2ObjectAVLTreeMap<>();
        for (int cn : wrapper.getAvailableCars()) {
            try {
                var temp0 = new GraphicsTexture(640, 640);
                var graphics = temp0.graphics;
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                trainNumberTextures.put(cn, temp0);
                for (Map.Entry<String, OptimizedModelWrapper> somwe : dynamicTrainNumberPartedModels.entrySet()) {
                    var temp1 = OptimizedModelWrapperAccessor.get(ModelManager.copyForMaterialChanges(
                            ((OptimizedModelWrapperAccessor) ((Object) somwe.getValue())).getOptimizedModel()));
                    ModelManager.replaceTexture(temp1, "template_640_640_tn.png", temp0.identifier);
                    trainNumberModels.put(new ObjectObjectImmutablePair<>(cn, somwe.getKey()), temp1);
                }
            } catch (Exception e) {
                TCM.LOGGER.error("Failed to load graphics texture: ", e);
            }
        }
        st.put(TRAIN_NUMBER_TEXTURE, trainNumberTextures);
        for (int cn : wrapper.getAvailableCars()) {
            carTypes.put((byte) Math.min(cn, Byte.MAX_VALUE - 1), TRM01CarTypes
                    .getFromId(wrapper.getVehicle().vehicleExtraData.immutableVehicleCars.get(cn).getVehicleId()));
        }
        var tp = new CachedTextPreset<>(TRM01TextPreset.INSTANCE);
        tp.startTick();
        st.put(WRAPPER_NOW, wrapper);
        // RateLimit rl = new RateLimit(1);
        service.scheduleAtFixedRate(() -> {
            var currentMillis = System.currentTimeMillis();
            if (currentMillis - lastFrameMillis >= 1000) {
                trainNumberTextureDirty = true;
                lastFrameMillis = currentMillis;
            }
            var previousMessages = (String[]) st.getOrDefault(PREVIOUS_MESSAGES, new String[4]);
            var ledStartTimes = (long[]) st.getOrDefault(LED_START_TIMES,
                    new long[] { currentMillis, currentMillis, currentMillis });
            var wrapperNow = (VehicleWrapper) st.get(WRAPPER_NOW);
            var siding = wrapperNow.getSiding();
            if (siding != null) {
                st.put(TRAIN_NUMBER_IN_DEPOT, siding.getName());
            }
            boolean onRoute = wrapperNow.getVehicle().getIsOnRoute();
            var stops = wrapperNow.getThisRouteStops();
            var paths = wrapperNow.getPathData();
            var path = paths.isEmpty() ? null
                    : paths.get(
                            Utilities.getIndexFromConditionalList(
                                    wrapperNow.getVehicle().vehicleExtraData.immutablePath,
                                    wrapperNow.getRailProgress(0) - 1.0F));
            try {
                /* Inner TextLED */
                {
                    var previousMessage = previousMessages[0];
                    String message;
                    if (!onRoute) {
                        message = tp.getIdleMessage(wrapperNow)[0];
                    } else {
                        if (path.getDwellTime() > 0) {
                            var a = tp.getArrivingMessage(wrapperNow);
                            message = a == null || a.length == 0 ? previousMessage : a[0];
                        } else {
                            var b = tp.getNextStationMessage(wrapperNow);
                            message = b == null || b.length == 0 ? previousMessage : b[0];
                        }
                    }
                    if (previousMessage == null || !previousMessage.equals(message)) {
                        ledStartTimes[0] = currentMillis;
                    }
                    boolean shouldResetStartTime = drawer.shouldResetStartTime(
                            0, 19, 158, 19 + 18,
                            message, Color.BLACK, Color.RED, 16,
                            currentMillis - ledStartTimes[0], 1.0);
                    if (shouldResetStartTime) {
                        ledStartTimes[0] = currentMillis;
                    }

                    previousMessages[0] = message;
                }

                /* Outer TextLED */
                {
                    var previousMessage = previousMessages[1];
                    String message = onRoute ? tp.getHeadDisplayMessage(wrapperNow,
                            new String[] { previousMessage == null ? "华容路站" : previousMessage })[0] : "回库";
                    if (message.length() < 5) {
                        ledStartTimes[1] = currentMillis;

                        drawer.drawConstantMessage(
                                0, 0, 89, 18,
                                message, Color.BLACK, Color.RED, 16);
                    } else {
                        if (previousMessage == null || !previousMessage.equals(message)) {
                            ledStartTimes[1] = currentMillis;
                        }
                        boolean shouldResetStartTime = drawer.shouldResetStartTime(
                                0, 0, 89, 18,
                                message, Color.BLACK, Color.RED, 16,
                                currentMillis - ledStartTimes[1], 1.0);
                        if (shouldResetStartTime) {
                            ledStartTimes[1] = currentMillis;
                        }
                    }

                    previousMessages[1] = message;
                }

                /* Side TextLED */
                {
                    var previousMessage0 = previousMessages[2];
                    var previousMessage1 = previousMessages[3];
                    String[] messages = tp.getSideDisplayMessage(wrapperNow, null);
                    if (messages != null && messages.length == 2) {
                        if (previousMessage0 == null || !previousMessage0.equals(messages[0]) ||
                                previousMessage1 == null || !previousMessage1.equals(messages[1])) {
                            ledStartTimes[2] = currentMillis;
                        }
                        var shouldResetStartTime0 = drawer.shouldResetStartTime(
                                0, 57, 81, 18 + 57,
                                messages[0], Color.BLACK, Color.RED, 16,
                                currentMillis - ledStartTimes[2], 1.0);
                        var shouldResetStartTime1 = drawer.shouldResetStartTime(
                                0, 101, 81, 18 + 101,
                                messages[1], Color.BLACK, Color.RED, 16,
                                currentMillis - ledStartTimes[2], 1.2);
                        if (shouldResetStartTime0 && shouldResetStartTime1) {
                            ledStartTimes[2] = currentMillis;
                        }
                    }
                    if (messages != null) {
                        previousMessages[2] = messages[0];
                        previousMessages[3] = messages[1];
                    }
                }

                drawer.executeCustomDraw((g) -> TRM0102SpeedScreenDrawer.draw(wrapperNow, g, 2, st, path));

                if (trainNumberTextureDirty) {
                    var trainNumbersTextures = (Map<?, ?>) st.get(TRAIN_NUMBER_TEXTURE);
                    for (int cn : wrapperNow.getAvailableCars()) {
                        var trainNumbersTexture = (GraphicsTexture) trainNumbersTextures.get(cn);
                        TRM01TrainNumberDrawer.draw(wrapperNow, trainNumbersTexture, st, cn);
                        trainNumbersTexture.upload();
                    }
                    trainNumberTextureDirty = false;
                }

                drawer.getTexture().upload();
            } catch (Exception e) {
                TCM.LOGGER.error("Cannot to render vehicle: ", e);
            }

            st.put(PREVIOUS_MESSAGES, previousMessages);
            st.put(LED_START_TIMES, ledStartTimes);
        }, 0, 1000 / 27, TimeUnit.MILLISECONDS);
        st.put(SCREEN_MODELS, screenModels);
        st.put(TRAIN_NUMBER_MODELS, trainNumberModels);
        st.put(LED_DRAWER, drawer);
        st.put(DRAW_EXECUTOR, service);
        st.put(CACHED_TEXT_PRESET, tp);
        st.put(TRAIN_CAR_TYPE, carTypes);
        st.put(READY, true);
        return true;
    }

    @Override
    public void submit(VehicleWrapper wrapper, Map<Byte, Object> st, List<MTRModdedRenderManager> renderManagers,
            long millisElapsed) {
        if (!(boolean) st.getOrDefault(READY, false)) {
            return;
        }
        st.put(WRAPPER_NOW, wrapper);

        ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().getPlayerMapped();
        AtomicBoolean isPlayerRiding = new AtomicBoolean(false);
        AtomicLong ridingCar = new AtomicLong(-1);
        wrapper.getVehicle().vehicleExtraData.iterateRidingEntities(vehicleRidingEntity -> {
            if (isPlayerRiding.get())
                return;
            if (clientPlayerEntity != null && vehicleRidingEntity.isOnVehicle()
                    && vehicleRidingEntity.uuid.equals(clientPlayerEntity.getUuid())) {
                isPlayerRiding.set(true);

                ridingCar.set(vehicleRidingEntity.getRidingCar());
            }
        });

        boolean isThirdPerson = Minecraft.getInstance().options.getCameraType() != CameraType.FIRST_PERSON;

        var copiedModels0 = (Map<?, ?>) st.getOrDefault(SCREEN_MODELS, null);
        if (copiedModels0 == null) {
            return;
        }

        var outerLEDScreen = (OptimizedModelWrapper) copiedModels0.get("outled");
        var innerLEDScreen = (OptimizedModelWrapper) copiedModels0.get("innled");
        var sideLEDScreen = (OptimizedModelWrapper) copiedModels0.get("sideled_type_1");
        var sideLEDScreenBase = (OptimizedModelWrapper) copiedModels0.get("sideled_type_1_base");
        var speedScreen = (OptimizedModelWrapper) copiedModels0.get("speedscreen");

        var copiedModels1 = (Map<?, ?>) st.getOrDefault(TRAIN_NUMBER_MODELS, null);
        if (copiedModels1 == null) {
            return;
        }

        var trainNumberHead = (OptimizedModelWrapper) copiedModels1
                .get(new ObjectObjectImmutablePair<>(0, "train_number_head"));
        var trainNumberProperting = (OptimizedModelWrapper) copiedModels1
                .get(new ObjectObjectImmutablePair<>(0, "train_number_properting_in"));

        var trainCarTypes = (Map<?, ?>) st.get(TRAIN_CAR_TYPE);

        var stack = new Stack<StoredMatrixTransformations>();
        stack.add(new StoredMatrixTransformations());
        for (int cn : wrapper.getAvailableCars()) {
            if (!(wrapper.getVehicle().persistentVehicleData.rayTracing[cn] || VehicleRidingMovement.isRiding(wrapper.getVehicle().getId()))) {
                continue;
            }

            var ct = (TRM01CarTypes) trainCarTypes.get((byte) cn);
            if (ct == null) continue;
            var renderManager = renderManagers.get(cn);
            switch (ct) {
                // end 偏移 innled 11
                case TK01 -> {
                    stack.push(new StoredMatrixTransformations());
                    stack.peek().add((gh) -> gh.rotateYRadians((float) Math.PI));
                    renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(innerLEDScreen)
                            .matrices(stack));
                    if (ridingCar.get() < 0L || ridingCar.get() == cn || isThirdPerson) {
                        renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(outerLEDScreen)
                                .matrices(stack));
                        renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(speedScreen)
                                .matrices(stack));
                        renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(trainNumberHead)
                                .matrices(stack));
                        renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(trainNumberProperting)
                                .matrices(stack));
                    }
                    stack.pop();
                    stack.push(new StoredMatrixTransformations());
                    stack.peek().add((gh) -> gh.translate(0, 0, -3.0 / 16));
                    renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(innerLEDScreen)
                            .matrices(stack));
                    stack.pop();
                }
                case TK01FLIP -> {
                    stack.push(new StoredMatrixTransformations());
                    renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(innerLEDScreen)
                            .matrices(stack));
                    if (ridingCar.get() < 0L || ridingCar.get() == cn || isThirdPerson) {
                        renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(outerLEDScreen)
                                .matrices(stack));
                        renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(speedScreen)
                                .matrices(stack));
                        renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(trainNumberHead)
                                .matrices(stack));
                        renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(trainNumberProperting)
                                .matrices(stack));
                    }
                    stack.pop();
                    stack.push(new StoredMatrixTransformations());
                    stack.peek().add((gh) -> {
                        gh.rotateYRadians((float) Math.PI);
                        gh.translate(0, 0, -3.0 / 16);
                    });
                    renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(innerLEDScreen)
                            .matrices(stack));
                    stack.pop();
                }
                case TK02 -> {
                    stack.push(new StoredMatrixTransformations());
                    stack.peek().add((gh) -> {
                        gh.rotateYRadians((float) Math.PI);
                        gh.translate(0, 0, -3.0 / 16);
                    });
                    renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(innerLEDScreen)
                            .matrices(stack));
                    stack.pop();
                    stack.push(new StoredMatrixTransformations());
                    stack.peek().add((gh) -> gh.translate(0, 0, -11.0 / 16));
                    renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(innerLEDScreen)
                            .matrices(stack));
                    stack.pop();
                }
                case TK03 -> {
                    stack.push(new StoredMatrixTransformations());
                    stack.peek().add((gh) -> {
                        gh.rotateYRadians((float) Math.PI);
                        gh.translate(0, 0, -3.0 / 16);
                    });
                    renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(innerLEDScreen)
                            .matrices(stack));
                    stack.pop();
                    stack.push(new StoredMatrixTransformations());
                    stack.peek().add((gh) -> gh.translate(0, 0, -3.0 / 16));
                    renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(innerLEDScreen)
                            .matrices(stack));
                    stack.pop();
                }
                case TK04 -> {
                    stack.push(new StoredMatrixTransformations());
                    stack.peek().add((gh) -> {
                        gh.rotateYRadians((float) Math.PI);
                        gh.translate(0, 0, -11.0 / 16);
                    });
                    renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(innerLEDScreen)
                            .matrices(stack));
                    stack.pop();
                    stack.push(new StoredMatrixTransformations());
                    stack.peek().add((gh) -> gh.translate(0, 0, -11.0 / 16));
                    renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(innerLEDScreen)
                            .matrices(stack));
                    stack.pop();
                }
            }
            var trainNumberDoorsIn = (OptimizedModelWrapper) copiedModels1
                    .get(new ObjectObjectImmutablePair<>(cn, "train_number_doors_in"));
            stack.push(new StoredMatrixTransformations());
            renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(trainNumberDoorsIn)
                    .matrices(stack));
            if (ridingCar.get() < 0L || isThirdPerson) {
                var trainNumberSide = (OptimizedModelWrapper) copiedModels1
                        .get(new ObjectObjectImmutablePair<>(cn, "train_number_side_style1"));
                var trainNumberDoors = (OptimizedModelWrapper) copiedModels1
                        .get(new ObjectObjectImmutablePair<>(cn, "train_number_doors"));

                renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(sideLEDScreen)
                        .matrices(stack));
                renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(sideLEDScreenBase)
                        .matrices(stack));
                renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(trainNumberSide)
                        .matrices(stack));
                renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(trainNumberDoors)
                        .matrices(stack));
                stack.pop();
                stack.push(new StoredMatrixTransformations());
                stack.peek().add((gh) -> gh.rotateYRadians((float) Math.PI));
                renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(sideLEDScreen)
                        .matrices(stack));
                renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(sideLEDScreenBase)
                        .matrices(stack));
                renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(trainNumberSide)
                        .matrices(stack));
            }
            stack.pop();
        }
    }

    @Override
    public void destroy(VehicleWrapper wrapper, Map<Byte, Object> st) {
        var service = (ScheduledExecutorService) st.getOrDefault(DRAW_EXECUTOR, null);
        if (service != null) {
            service.shutdown();
            service.close();
        }

        var drawer = (ScreenDisplayDrawer) st.getOrDefault(LED_DRAWER, null);
        if (drawer != null) {
            drawer.closeGraphicsTexture();
        }

        var trainNumberTextures = (Map<?, ?>) st.getOrDefault(TRAIN_NUMBER_TEXTURE, null);
        if (trainNumberTextures != null) {
            for (Object value : trainNumberTextures.values()) {
                ((GraphicsTexture) value).close();
            }
        }

        CachedTextPreset<?> tp = (CachedTextPreset<?>) st.getOrDefault(CACHED_TEXT_PRESET, null);
        if (tp != null) {
            tp.endTick();
        }
    }

    public enum TRM01CarTypes {
        TK01("tk01"), TK01FLIP("tk01f"), TK02("tk02"), TK03("tk03"), TK04("tk04");

        private final String id;

        private TRM01CarTypes(String id) {
            this.id = id;
        }

        public static TRM01CarTypes getFromId(String id0) {
            String id = id0.trim();

            for (TRM01CarTypes value : values()) {
                if (value.id.equals(id)) {
                    return value;
                }
            }
            return null;
        }
    }
}
