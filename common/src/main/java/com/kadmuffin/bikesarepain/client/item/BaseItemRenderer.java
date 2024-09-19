package com.kadmuffin.bikesarepain.client.item;

import com.kadmuffin.bikesarepain.server.item.BaseItem;
import com.kadmuffin.bikesarepain.server.item.ItemManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.types.Func;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class BaseItemRenderer<T extends BaseItem> extends GeoItemRenderer<T> {
    private final List<String> bonesToIgnore;
    private final List<String> bonesToColor;
    private final Boolean colorEveryBone;

    /**
     * An item renderer that colors bones based on a provided map
     * @param modelName The path to the model to render
     * @param bonesToColor A list of bone names to color
     * @param bonesToIgnore A list of bone names to ignore when coloring bones every bone (with "*")
     */
    public BaseItemRenderer(ResourceLocation modelName, List<String> bonesToColor, List<String> bonesToIgnore) {
        super(new DefaultedItemGeoModel<>(modelName));
        this.bonesToColor = bonesToColor;
        this.colorEveryBone = bonesToColor.contains("*");
        if (this.colorEveryBone) {
            this.bonesToIgnore = bonesToIgnore;
        } else {
            this.bonesToIgnore = List.of();
        }
    }

    @Override
    public void renderCubesOfBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, int colour) {
        ItemStack stack = this.getCurrentItemStack();
        T animatable = this.getAnimatable();

        // If the bone is in the bonesToColor map, color it
        if (bonesToColor.contains(bone.getName())) {
            super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, animatable.evaluateBoneColor(bone.getName(), stack));
            return;
        } else if (this.colorEveryBone && !this.bonesToIgnore.contains(bone.getName())) {
            super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, animatable.evaluateBoneColor("*", stack));
            return;
        }

        super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, colour);
    }
}
