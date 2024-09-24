package com.kadmuffin.bikesarepain.client.item;

import com.kadmuffin.bikesarepain.server.item.BaseItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

@Environment(EnvType.CLIENT)
public class BaseItemRenderer<T extends BaseItem> extends GeoItemRenderer<T> {

    public BaseItemRenderer(ResourceLocation model) {
        super(new DefaultedItemGeoModel<>(model));
    }
}
