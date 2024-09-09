package com.kadmuffin.bikesarepain.client.entity;

import com.kadmuffin.bikesarepain.BikesArePain;
import com.kadmuffin.bikesarepain.client.helper.DodecagonDisplayManager;
import com.kadmuffin.bikesarepain.server.entity.Bicycle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.FastBoneFilterGeoLayer;

import java.util.List;
import java.util.function.Supplier;

public class BicycleRenderer extends GeoEntityRenderer<Bicycle> {
    public final Supplier<List<String>> bones = () -> List.of("ActualRoot", "Bike", "ActualWheel", "ActualWheel2", "Cap2", "RearGears", "Pedals", "WheelUnion", "Handlebar", "SeatF", "MonitorRoot", "Display",
            "Display1", "Display2", "Display3", "Display4", "Display5", "Display6",
            "TypeScreen", "UnitDistance", "UnitTime", "UnitSpeed", "MonitorRoot", "Propellers", "SpinningThing", "SpinningThing2",
            "Chest"
    );

    public BicycleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "bicycle")));
        addRenderLayer(new FastBoneFilterGeoLayer<>(this, bones, (geoBone, bikeEntity, aFloat) -> {

            if (geoBone.getName().equals("ActualWheel")) {
                geoBone.setRotZ(bikeEntity.getBackWheelRotation());
            }
            if (geoBone.getName().equals("Cap2")) {
                geoBone.setHidden(bikeEntity.showGears);
            }
            if (geoBone.getName().equals("RearGears")) {
                geoBone.setRotZ(bikeEntity.getBackWheelRotation());
            }
            if (geoBone.getName().equals("Pedals")) {
                geoBone.setRotZ(bikeEntity.getBackWheelRotation());
            }
            if (geoBone.getName().equals("WheelUnion")) {
                geoBone.setRotY(bikeEntity.getSteeringYaw());
            }
            if (geoBone.getName().equals("Handlebar")) {
                geoBone.setRotY(bikeEntity.getSteeringYaw());
            }

            if (geoBone.getName().equals("ActualWheel2")) {
                geoBone.setRotZ(bikeEntity.getFrontWheelRotation());
            }

            if (geoBone.getName().equals("SeatF")) {
                geoBone.setHidden(!bikeEntity.isSaddled());
            }

            if (geoBone.getName().equals("Bike")) {
                geoBone.setRotZ(bikeEntity.getTilt());
            }

            if (geoBone.getName().equals("ActualRoot")) {
                // TODO: Replace hacky old way with just doing radian math
                float pitch = bikeEntity.bikePitch - 0.4F;
                pitch = Math.max(0, pitch);

                if (pitch > 0) {
                    geoBone.setRotX(pitch/100);
                }

                bikeEntity.bikePitch = Math.max(0, bikeEntity.bikePitch - 0.4F);
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
                            DodecagonDisplayManager.DisplayType.fromType(bikeEntity.getCurrentDisplayStat())
                            , 0.25f, bikeEntity);
                }

                if (geoBone.getName().matches("(UnitDistance|UnitTime|UnitSpeed)")) {
                    bikeEntity.getDisplayManager().updateUnitDisplay(geoBone,
                            DodecagonDisplayManager.DisplayType.fromType(bikeEntity.getCurrentDisplayStat())
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
    public void render(Bicycle entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.scale(entity.getModelScalingFactor(), entity.getModelScalingFactor(), entity.getModelScalingFactor());

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }


}
