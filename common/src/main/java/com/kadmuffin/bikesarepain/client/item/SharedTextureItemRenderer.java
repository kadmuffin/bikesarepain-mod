package com.kadmuffin.bikesarepain.client.item;

import com.kadmuffin.bikesarepain.server.item.BaseItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

@Environment(EnvType.CLIENT)
public class SharedTextureItemRenderer<T extends BaseItem> extends GeoItemRenderer<T> {

    public SharedTextureItemRenderer(ResourceLocation model, ResourceLocation texture) {
        super(new DefaultedItemGeoModel<T>(model).withAltTexture(texture));
    }
}
