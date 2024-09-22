package com.kadmuffin.bikesarepain.client.entity;

import com.kadmuffin.bikesarepain.BikesArePain;
import com.kadmuffin.bikesarepain.client.ClientConfig;
import com.kadmuffin.bikesarepain.client.helper.DecagonDisplayManager;
import com.kadmuffin.bikesarepain.client.helper.Utils;
import com.kadmuffin.bikesarepain.server.entity.Bicycle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.layer.FastBoneFilterGeoLayer;

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

    // A method that is meant to be run at preRender and requires partial ticks
    private float smoothRotationPartialTicks(float currentRot, float targetRot, float partialTicks) {
        currentRot = (float) (((currentRot % (2 * Math.PI)) + (2 * Math.PI)) % (2 * Math.PI));
        targetRot = (float) (((targetRot % (2 * Math.PI)) + (2 * Math.PI)) % (2 * Math.PI));

        float diff = targetRot - currentRot;

        if (diff > Math.PI) {
            diff -= 2 * Math.PI;
        } else if (diff < -Math.PI) {
            diff += 2 * Math.PI;
        }

        // Minecraft ticks run at 20 ticks per second, so 1/20 is 1 tick
        // in this case we are getting partial ticks, meaning that we are getting how far we are into the current tick
        // this theoretically based on the current frame time, it is not delta time exactly though

        float newRotation = currentRot + diff * partialTicks;

        newRotation = (float) (((newRotation % (2 * Math.PI)) + (2 * Math.PI)) % (2 * Math.PI));

        return newRotation;
    }

    // Method that doesn't use anything other than the current and target rotation
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
                geoBone.setRotZ(bikeEntity.rotations.get("backWheelRotation").rotation);
            }
            if (geoBone.getName().equals("Cap2")) {
                geoBone.setHidden(bikeEntity.showGears);
            }
            if (geoBone.getName().equals("RearGears")) {
                geoBone.setRotZ(bikeEntity.rotations.get("backWheelRotation").rotation);
            }
            if (geoBone.getName().equals("Pedals")) {
                geoBone.setRotZ(bikeEntity.rotations.get("backWheelRotation").rotation);
            }
            if (geoBone.getName().equals("WheelUnion")) {
                geoBone.setRotY(bikeEntity.rotations.get("steeringYaw").rotation);
            }
            if (geoBone.getName().equals("Handlebar")) {
                geoBone.setRotY(bikeEntity.rotations.get("steeringYaw").rotation);
            }

            if (geoBone.getName().equals("ActualWheel2")) {
                geoBone.setRotZ(bikeEntity.rotations.get("backWheelRotation").rotation);
            }

            if (geoBone.getName().equals("SeatF")) {
                geoBone.setHidden(!bikeEntity.isSaddled());
            }

            if (geoBone.getName().equals("Bike")) {
                geoBone.setRotZ(bikeEntity.rotations.get("tilt").rotation);
            }

            if (geoBone.getName().equals("ActualRoot")) {
                geoBone.setRotX(bikeEntity.rotations.get("pitch").rotation);
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

    @Override
    public void preRender(PoseStack poseStack, Bicycle animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        if (ClientConfig.CONFIG.instance().useInterpolation()) {
            animatable.rotations.get("backWheelRotation").setRotation(this.smoothRotationPartialTicks(animatable.rotations.get("backWheelRotation").rotation, animatable.getBackWheelRotation(), partialTick));
            animatable.rotations.get("steeringYaw").setRotation(this.smoothRotationPartialTicks(animatable.rotations.get("steeringYaw").rotation, animatable.getSteeringYaw(), partialTick));
            animatable.rotations.get("tilt").setRotation(this.smoothRotationPartialTicks(animatable.rotations.get("tilt").rotation, animatable.getTilt(), partialTick));
            animatable.rotations.get("pitch").setRotation(this.smoothRotationPartialTicks(animatable.rotations.get("pitch").rotation, animatable.clientOnlyBikePitch, partialTick));
        } else {
            // Just set the rotation directly
            animatable.rotations.get("backWheelRotation").setRotation(animatable.getBackWheelRotation());
            animatable.rotations.get("steeringYaw").setRotation(animatable.getSteeringYaw());
            animatable.rotations.get("tilt").setRotation(animatable.getTilt());
            animatable.rotations.get("pitch").setRotation(animatable.clientOnlyBikePitch);
        }
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }
}
