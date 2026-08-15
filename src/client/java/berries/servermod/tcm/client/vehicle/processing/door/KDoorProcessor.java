package berries.servermod.tcm.client.vehicle.processing.door;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.client.render.MTRModdedRenderManager;
import berries.servermod.tcm.client.util.model.ModelManager;
import berries.servermod.tcm.client.vehicle.processing.Processor;
import berries.servermod.tcm.client.vehicle.processing.ProcessorName;
import berries.servermod.tcm.util.VehicleWrapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.booleans.BooleanBooleanImmutablePair;
import net.minecraft.resources.ResourceLocation;
import org.mtr.libraries.it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import org.mtr.libraries.it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import org.mtr.mapping.holder.*;
import org.mtr.mod.client.CustomResourceLoader;
import org.mtr.mod.client.VehicleRidingMovement;
import org.mtr.mod.data.VehicleExtension;
import org.mtr.mod.render.*;
import org.mtr.mod.resource.OptimizedModelWrapper;
import org.mtr.mod.resource.VehicleResource;
import org.mtr.mod.resource.VehicleResourceCache;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author OANI245
 * 仿康尼机电内藏电磁门门控模组化处理器
 */
@ProcessorName("kn_door")
public class KDoorProcessor extends Processor {
    public static final byte RENDER_EXECUTOR = -0x80;
    public static final byte READY = 0x7F;

    /*public static final byte DOOR_MODEL_LEFT_OUT = 0x0;
    public static final byte DOOR_MODEL_LEFT_IN = 0x1;
    public static final byte DOOR_MODEL_RIGHT_OUT = 0x2;
    public static final byte DOOR_MODEL_RIGHT_IN = 0x3;*/

    public static final byte PREVIOUS_DOOR_VALUE = 0x04;
    public static final byte PREVIOUS_DOOR_BLOCKED_AMOUNT = 0x05;

    private final Map<BooleanBooleanImmutablePair, OptimizedModelWrapper> partedDoorModels = new HashMap<>();

    public KDoorProcessor(Map<String, Object> in) {
        super(in);
        ModelManager.loadPartedModel(new ResourceLocation(((JsonElement)this.inputs.get("door_model")).getAsString()), false).forEach((k0, v) -> {
            var k = k0.trim();
            if (!k.startsWith("door_")) return;
            boolean interior = k.endsWith("_in");
            boolean reverseX = k.contains("_right");
            partedDoorModels.put(new BooleanBooleanImmutablePair(reverseX, interior), ModelManager.upload(v, interior));
        });
    }

    @Override
    public boolean load(VehicleWrapper sc, Map<Byte, Object> st, List<MTRModdedRenderManager> renderManagers) {
        /*try {
            for (byte b = DOOR_MODEL_START; b <= DOOR_MODEL_END; b++) {
                //var holder = new DynamicModelHolder();
                RawModel model;
                try {
                    model = ModelManager.loadModel(
                            new ResourceLocation(((JsonElement) this.inputs.get("model_" + b)).getAsString()), false);
                } catch (Throwable e) {
                    return false;
                }
                if (model == null) return false;
                *//*var type = b % 2 != 0 ? RenderStage.INTERIOR : RenderStage.EXTERIOR;
                model.iterateRawMeshList(rawMesh -> {
                    MaterialProperties originalProp = rawMesh.materialProperties;
                    ((RawMeshAccessor) (Object) rawMesh).setMaterialProperties(new MaterialProperties(type.shaderType, originalProp.getTexture(), originalProp.vertexAttributeState.color));
                });
                model.applyUVMirror(false, true);
                holder.uploadLater(model);*//*
                var uploadedModel = uploadModel(model, b % 2 != 0);
                st.put(b, uploadedModel);
            }

            st.put(READY, true);
            //TCM.LOGGER.info("created vehicle: {}", sc.getHexId());
        } catch (Exception e) {
            TCM.LOGGER.error("Failed to load vehicle: ", e);
            st.put(READY, false);
        }
*/
        st.put(RENDER_EXECUTOR, null);
        st.put(READY, true);
        return true;
    }

    @Override
    public void submit(VehicleWrapper wrapper, Map<Byte, Object> st, List<MTRModdedRenderManager> renderManagers, long me) {
        VehicleExtension sc = wrapper.getVehicle();
        if (!(boolean) st.getOrDefault(READY, false)) {
            return;
        }
        var service = (ExecutorService) st.getOrDefault(RENDER_EXECUTOR, null);
        ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().getPlayerMapped();
        var posAndRotations = sc.getSmoothedVehicleCarsAndPositions(me).stream()
                .map(vehicleCarAndPosition -> {
                    final ObjectArrayList<PositionAndRotation> bogiePositions = vehicleCarAndPosition.right()
                            .stream()
                            .map(bogiePositionPair -> new PositionAndRotation(bogiePositionPair.left(), bogiePositionPair.right(), true))
                            .collect(Collectors.toCollection(ObjectArrayList::new));
                    return new PositionAndRotation(bogiePositions, vehicleCarAndPosition.left(), sc.getTransportMode().hasPitchAscending || sc.getTransportMode().hasPitchDescending);
                })
                .collect(Collectors.toCollection(ObjectArrayList::new));

        /*var doorModels = new OptimizedModelWrapper[(DOOR_MODEL_END - DOOR_MODEL_START) + 1];
        for (byte b = DOOR_MODEL_START; b <= DOOR_MODEL_END; b++) {
            doorModels[b - DOOR_MODEL_START] = (OptimizedModelWrapper) st.get(b);
        }*/

        var doorValue = sc.persistentVehicleData.getDoorValue();
        var previousDoorValue = (Double) st.getOrDefault(PREVIOUS_DOOR_VALUE, null);
        st.put(PREVIOUS_DOOR_VALUE, doorValue);
        var adjustedDoorMultiplier = sc.persistentVehicleData.getAdjustedDoorMultiplier(sc.vehicleExtraData);
        var doorMaxValue = ((JsonPrimitive) this.inputs.getOrDefault("door_max_value", new JsonPrimitive(0.796))).getAsDouble();
        var doorZ = ((JsonPrimitive) this.inputs.getOrDefault("door_outer_padding", new JsonPrimitive(0.0))).getAsDouble();
        var doorRelativeX = ((JsonPrimitive) this.inputs.getOrDefault("door_relative_x", new JsonPrimitive(0.0))).getAsDouble();
        boolean isDoorOpening = previousDoorValue != null && doorValue > previousDoorValue;
        boolean isDoorClosing = previousDoorValue != null && doorValue < previousDoorValue;
        //boolean isJustDoorOpening = previousDoorValue != null && previousDoorValue == 0 && isDoorOpening;
        //boolean isJustDoorClosing = previousDoorValue != null && previousDoorValue == doorMaxValue && isDoorClosing;

        Map<IntIntImmutablePair, Double> previousDoorBlockedAmount = (Map<IntIntImmutablePair, Double>) st.getOrDefault(PREVIOUS_DOOR_BLOCKED_AMOUNT, new HashMap<IntIntImmutablePair, Double>());

        var stack = new Stack<StoredMatrixTransformations>();
        stack.add(new StoredMatrixTransformations());
        for (int cn : wrapper.getAvailableCars()) {
            stack.push(new StoredMatrixTransformations());
            var doorX = getDoorValue(doorMaxValue, sc.persistentVehicleData.getDoorValue(), isDoorOpening);
            var renderManager = renderManagers.get(cn);
            var vehicleId = sc.vehicleExtraData.immutableVehicleCars.get(cn).getVehicleId();
            var posAndRotation = posAndRotations.get(cn);

            final IntObjectImmutablePair<ObjectObjectImmutablePair<Vector3d, Double>> ridingVehicleCarNumberAndOffset = VehicleRidingMovement.getRidingVehicleCarNumberAndOffset(sc.getId());
            final int ridingCarNumber;
            final Vector3d offsetVector;
            if (ridingVehicleCarNumberAndOffset == null) {
                ridingCarNumber = -1;
                offsetVector = null;
            } else {
                ridingCarNumber = ridingVehicleCarNumberAndOffset.leftInt();
                offsetVector = ridingVehicleCarNumberAndOffset.right().left();
            }
            CustomResourceLoader.getVehicleById(sc.getTransportMode(), vehicleId, (dtl) -> {
                VehicleResource vr = dtl.left();
                VehicleResourceCache vrc;
                vrc = vr.getCachedVehicleResource(
                        cn, sc.vehicleExtraData.immutableVehicleCars.size(), false
                );
                if (vrc == null) {
                    return;
                }

                Runnable rn = () -> {
                    final ObjectArrayList<Vector3d> doorHoldingPlayerOffsetPositions = new ObjectArrayList<>();
                    // Check if this player is holding doors
                    if (ridingCarNumber == cn && offsetVector != null) {
                        doorHoldingPlayerOffsetPositions.add(offsetVector);
                    }
                    // Check if other players are holding doors
                    sc.vehicleExtraData.iterateRidingEntities(vehicleRidingEntity -> {
                        if (vehicleRidingEntity.getRidingCar() == cn && !vehicleRidingEntity.uuid.equals(clientPlayerEntity.getUuid())) {
                            doorHoldingPlayerOffsetPositions.add(new Vector3d(vehicleRidingEntity.getX(), vehicleRidingEntity.getY(), vehicleRidingEntity.getZ()));
                        }
                    });
                    for (byte i = 0; i < vrc.doorways.size(); i++) {
                        var dvy = vrc.doorways.get(i);
                        if ((dvy.getMaxZMapped() - dvy.getMinZMapped()) < (doorMaxValue * 2)) continue;

                        final double[] doorBlockedAmount = {0};
                        doorHoldingPlayerOffsetPositions.forEach(doorHoldingPlayerOffset -> {
                            final double thisDoorBlockedAmount = RenderVehicleHelper.getDoorBlockedAmount(dvy, doorHoldingPlayerOffset.getXMapped(), doorHoldingPlayerOffset.getYMapped(), doorHoldingPlayerOffset.getZMapped());
                            if (thisDoorBlockedAmount > 0 && doorHoldingPlayerOffset == offsetVector) {
                                VehicleRidingMovement.overrideDoors();
                            }
                            if (thisDoorBlockedAmount > doorBlockedAmount[0]) {
                                doorBlockedAmount[0] = thisDoorBlockedAmount;
                            }
                        });
                        final boolean canOpenDoors = RenderVehicleHelper.canOpenDoors(dvy, posAndRotation, Math.max(doorBlockedAmount[0], doorValue * 2));
                        boolean flip;
                        if (dvy.getMinXMapped() < 0) {
                            flip = true;
                        } else {
                            flip = false;
                        }

                        double doorXFinal = (doorBlockedAmount[0] > 0 || sc.persistentVehicleData.checkCanOpenDoors()) &&
                                canOpenDoors ? Math.min(doorMaxValue, Math.max(doorX, doorBlockedAmount[0])) : 0.0;

                        for (Map.Entry<BooleanBooleanImmutablePair, OptimizedModelWrapper> doorModel :
                                partedDoorModels.entrySet()) {
                            if (doorModel == null) continue;
                            boolean reverseX = doorModel.getKey().leftBoolean();
                            var z = (reverseX ? -doorXFinal : doorXFinal) + (flip ? dvy.getMinZMapped() + doorRelativeX : -dvy.getMinZMapped() - doorRelativeX);
                            stack.peek().add((gh) -> {
                                if (flip) {
                                    gh.rotateYRadians((float) Math.PI);
                                }
                                gh.translate(doorZ, 0, z);
                                gh.scale(1, 1, 1);
                            });
                            renderManager.put(new MTRModdedRenderManager.MatricesDrawEvent().model(doorModel.getValue()).matrices(stack));
                            stack.pop();
                            stack.push(new StoredMatrixTransformations());
                        }

                        previousDoorBlockedAmount.put(new IntIntImmutablePair(cn, i), doorBlockedAmount[0]);
                    /*if ((doorBlockedAmount[0] > 0 || sc.persistentVehicleData.checkCanOpenDoors()) && canOpenDoors) {
                    }*/
                    }
                };
                if (service != null) {
                    service.submit(rn);
                } else {
                    rn.run();
                }
            });
        }

        if (service != null) {
            try {
                if (!service.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                    TCM.LOGGER.error("{} Timed out", sc.getHexId());
                }
            } catch (Exception ignored) {}
        }
        st.put(PREVIOUS_DOOR_BLOCKED_AMOUNT, previousDoorBlockedAmount);
    }

    @Override
    public void destroy(VehicleWrapper sc, Map<Byte, Object> st) {
        try {
            var service = (ExecutorService) st.getOrDefault(RENDER_EXECUTOR, null);
            if (service != null) {
                service.shutdown();
                service.close();
            }
            st.put(READY, false);/*
            for (byte b = 0; b < doorModels.length; b++) {
                if (st.containsKey(b)) {
                    var holder = (DynamicModelHolder) st.get(b);
                    holder.close();
                }
            }*/

            //TCM.LOGGER.info("destroyed vehicle: {}", sc.getHexId());
        } catch (Exception e) {
            TCM.LOGGER.error("Fatal error: Cannot destroy unrenderable vehicle", e);
        }
    }

    private double getDoorValue(double doorMax, double value, boolean opening) {
        if (value <= 0.0) {
            return 0.0;
        } else if (opening) {
            if (value < 0.34) {
                return 0;
            } else if (value >= 1) {
                return doorMax;
            } else {
                return (value - 0.34) * 1.5 * doorMax;
            }
        } else {
            if (value < 0.1) {
                return smoothEnds(-0.08, 0.08, -0.25, 0.25, value);
            } else if (value < 0.3) {
                return value * 1.15 * 0.37;
            } else if (value < 0.42) {
                return smoothEnds(0.12, 1.72, 0.22, 1.52, value);
            } else if (value >= 1) {
                return doorMax;
            } else {
                return (value - 0.2) * 1.25 * doorMax;
            }
        }
    }

    private double smoothEnds(double startValue, double endValue, double startTime, double endTime, double time) {
        if (time < startTime) return startValue;
        if (time > endTime) return endValue;
        var timeChange = endTime - startTime;
        var valueChange = endValue - startValue;
        return valueChange * (1 - Math.cos(Math.PI * (time - startTime) / timeChange)) / 2 + startValue;
    }

    public double getLength(VehicleExtension sc, int carIndex) {
        return sc.vehicleExtraData.immutableVehicleCars.get(carIndex).getLength();
    }

    public double getWidth(VehicleExtension sc, int carIndex) {
        return sc.vehicleExtraData.immutableVehicleCars.get(carIndex).getWidth();
    }
}
