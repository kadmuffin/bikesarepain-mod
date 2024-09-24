package com.kadmuffin.bikesarepain.client.entity;

import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.Map;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class AbstractBikeRenderer<T extends AbstractBike & GeoAnimatable> extends GeoEntityRenderer<T> {
    private final Map<String, Function<T, Integer>> bonesToColor;

    public AbstractBikeRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model, Map<String, Function<T, Integer>> bonesToColor) {
        super(renderManager, model);
        this.bonesToColor = bonesToColor;
    }

    @Override
    public void renderCubesOfBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, int colour) {
        // If the bone is in the bonesToColor map, color it
        if (this.bonesToColor.containsKey(bone.getName())) {
            super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, this.bonesToColor.get(bone.getName()).apply(this.getAnimatable()));
            return;
        }

        super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, colour);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.scale(entity.getModelScalingFactor(), entity.getModelScalingFactor(), entity.getModelScalingFactor());

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
