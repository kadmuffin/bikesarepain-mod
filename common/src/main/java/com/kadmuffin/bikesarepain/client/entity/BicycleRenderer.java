package com.kadmuffin.bikesarepain.client.entity;

import com.kadmuffin.bikesarepain.BikesArePain;
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
    public Supplier<List<String>> bones = () -> List.of("ActualRoot", "Bike", "ActualWheel", "ActualWheel2", "Cap2", "RearGears", "Pedals", "WheelUnion", "Handlebar", "SeatF");

    public BicycleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "bicycle")));
        addRenderLayer(new FastBoneFilterGeoLayer<Bicycle>(this, bones, (geoBone, bikeEntity, aFloat) -> {
            if (geoBone.getName().equals("ActualWheel")) {
                geoBone.setRotZ(bikeEntity.backWheelRotation);
            }
            if (geoBone.getName().equals("Cap2")) {
                geoBone.setHidden(bikeEntity.showGears);
            }
            if (geoBone.getName().equals("RearGears")) {
                geoBone.setRotZ(bikeEntity.backWheelRotation);
            }
            if (geoBone.getName().equals("Pedals")) {
                geoBone.setRotZ(bikeEntity.backWheelRotation);
            }
            if (geoBone.getName().equals("WheelUnion")) {
                geoBone.setRotY(bikeEntity.steeringYaw);
            }
            if (geoBone.getName().equals("Handlebar")) {
                geoBone.setRotY(bikeEntity.steeringYaw);
            }

            if (geoBone.getName().equals("ActualWheel2")) {
                geoBone.setRotZ(bikeEntity.backWheelRotation);
            }

            if (geoBone.getName().equals("SeatF")) {
                geoBone.setHidden(!bikeEntity.isSaddled());
            }

            if (geoBone.getName().equals("Bike")) {
                geoBone.setRotZ(bikeEntity.tilt);
            }

            if (geoBone.getName().equals("ActualRoot")) {
                float pitch = bikeEntity.bikePitch - 0.4F;
                pitch = Math.max(0, pitch);

                if (pitch > 0) {
                    geoBone.setRotX(pitch/100);
                }

                bikeEntity.bikePitch = Math.max(0, bikeEntity.bikePitch - 0.4F);
            }

        }));
    }

    @Override
    public void render(Bicycle entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.scale(2F, 2F, 2F);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
