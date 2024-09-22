package com.kadmuffin.bikesarepain.client.entity;

import com.kadmuffin.bikesarepain.BikesArePain;
import com.kadmuffin.bikesarepain.client.helper.DecagonDisplayManager;
import com.kadmuffin.bikesarepain.client.helper.Utils;
import com.kadmuffin.bikesarepain.server.entity.Bicycle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.layer.FastBoneFilterGeoLayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class BicycleRenderer extends AbstractBikeRenderer<Bicycle> {

    public static final Supplier<List<String>> bones = () -> List.of("ActualRoot", "Bike", "ActualWheel", "ActualWheel2", "Cap2", "RearGears", "Pedals", "WheelUnion", "Handlebar", "SeatF", "MonitorRoot", "Display",
            "Display1", "Display2", "Display3", "Display4", "Display5", "Display6",
            "TypeScreen", "UnitDistance", "UnitTime", "UnitSpeed", "MonitorRoot", "Propellers", "SpinningThing", "SpinningThing2",
            "Chest"
    );

    public static final Map<String, Function<Bicycle, Integer>> bonesToColor = Utils.createBonesToColorMap(
            Map.of(
                    List.of("hexadecagon"), Bicycle::getFWheelColor,
                    List.of("hexadecagon2", "Support", "ClickyThing", "ItemInventory", "Chest", "BladesF", "BladesB", "Cover", "Union", "SteeringFixed", "WoodThing",
                            "Cover2", "RodT", "RodD2", "BarT2", "RodD1", "hexadecagon4", "WheelStuff"), Bicycle::getFrameColor,
                    List.of("hexadecagon3"), Bicycle::getRWheelColor,
                    List.of("Cap1", "Cap2"), Bicycle::getGearboxColor
            )
    );

    private float smoothRotation(float currentRot, float targetRot) {
        float deltaTime = Minecraft.getInstance().getFrameTimeNs() / 1_000_000_000.0f;

        // Limit the delta time based on fps to prevent jumps with lag spikes
        deltaTime = Math.min(deltaTime, 1/30F);

        currentRot = (float) (((currentRot % (2 * Math.PI)) + (2 * Math.PI)) % (2 * Math.PI));
        targetRot = (float) (((targetRot % (2 * Math.PI)) + (2 * Math.PI)) % (2 * Math.PI));

        float diff = targetRot - currentRot;

        if (diff > Math.PI) {
            diff -= 2 * Math.PI;
        } else if (diff < -Math.PI) {
            diff += 2 * Math.PI;
        }

        float smoothSpeed = 80F;
        float newRotation = currentRot + diff * smoothSpeed * deltaTime;

        newRotation = (float) (((newRotation % (2 * Math.PI)) + (2 * Math.PI)) % (2 * Math.PI));

        return newRotation;
    }

    public BicycleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "bicycle")), bonesToColor);

        addRenderLayer(new FastBoneFilterGeoLayer<>(this, bones, (geoBone, bikeEntity, aFloat) -> {

            if (geoBone.getName().equals("ActualWheel")) {
                geoBone.setRotZ(bikeEntity.rotations.get("backWheelRotation").reCalculateIfOld(() -> this.smoothRotation(geoBone.getRotZ(), bikeEntity.getBackWheelRotation()), bikeEntity.getBackWheelRotation()));
            }
            if (geoBone.getName().equals("Cap2")) {
                geoBone.setHidden(bikeEntity.showGears);
            }
            if (geoBone.getName().equals("RearGears")) {
                geoBone.setRotZ(bikeEntity.rotations.get("backWheelRotation").reCalculateIfOld(() -> this.smoothRotation(geoBone.getRotZ(), bikeEntity.getBackWheelRotation()), bikeEntity.getBackWheelRotation()));
            }
            if (geoBone.getName().equals("Pedals")) {
                geoBone.setRotZ(bikeEntity.rotations.get("backWheelRotation").reCalculateIfOld(() -> this.smoothRotation(geoBone.getRotZ(), bikeEntity.getBackWheelRotation()), bikeEntity.getBackWheelRotation()));
            }
            if (geoBone.getName().equals("WheelUnion")) {
                geoBone.setRotY(bikeEntity.rotations.get("steeringYaw").reCalculateIfOld(() -> this.smoothRotation(geoBone.getRotY(), bikeEntity.getSteeringYaw()), bikeEntity.getSteeringYaw()));
            }
            if (geoBone.getName().equals("Handlebar")) {
                geoBone.setRotY(bikeEntity.rotations.get("steeringYaw").reCalculateIfOld(() -> this.smoothRotation(geoBone.getRotY(), bikeEntity.getSteeringYaw()), bikeEntity.getSteeringYaw()));
            }

            if (geoBone.getName().equals("ActualWheel2")) {
                geoBone.setRotZ(bikeEntity.rotations.get("backWheelRotation").reCalculateIfOld(() -> this.smoothRotation(geoBone.getRotZ(), bikeEntity.getBackWheelRotation()), bikeEntity.getBackWheelRotation()));
            }

            if (geoBone.getName().equals("SeatF")) {
                geoBone.setHidden(!bikeEntity.isSaddled());
            }

            if (geoBone.getName().equals("Bike")) {
                geoBone.setRotZ(bikeEntity.rotations.get("tilt").reCalculateIfOld(() -> this.smoothRotation(geoBone.getRotZ(), bikeEntity.getTilt()), bikeEntity.getTilt()));
            }

            if (geoBone.getName().equals("ActualRoot")) {
                geoBone.setRotX(bikeEntity.rotations.get("pitch").reCalculateIfOld(() -> this.smoothRotation(geoBone.getRotX(), bikeEntity.bikePitch), bikeEntity.bikePitch));
            }

            if (geoBone.getName().equals("Propellers")) {
                geoBone.setHidden(!bikeEntity.hasBalloon());
            }

            if (bikeEntity.hasDisplay()) {
                if (geoBone.getName().matches("MonitorRoot")) {
                    geoBone.setHidden(false);
                }
                // Check via regex if it matches s[1-9]{1,2}
                if (geoBone.getName().matches("(Display[1-6]|TypeScreen)")) {
                    bikeEntity.getDisplayManager().updateDisplayLerped(geoBone,
                            DecagonDisplayManager.DisplayType.fromType(bikeEntity.getCurrentDisplayStat())
                            , 0.25f, bikeEntity);
                }

                if (geoBone.getName().matches("(UnitDistance|UnitTime|UnitSpeed)")) {
                    bikeEntity.getDisplayManager().updateUnitDisplay(geoBone,
                            DecagonDisplayManager.DisplayType.fromType(bikeEntity.getCurrentDisplayStat())
                            , 0.25f);
                }
            } else {
                if (geoBone.getName().matches("MonitorRoot")) {
                    geoBone.setHidden(true);
                }
            }

            if (geoBone.getName().matches("SpinningThing|SpinningThing2")) {
                geoBone.setRotZ(bikeEntity.getBackWheelRotation());
            }

            if (geoBone.getName().equals("Chest")) {
                geoBone.setHidden(!bikeEntity.hasChest());
            }

        }));
    }
}
