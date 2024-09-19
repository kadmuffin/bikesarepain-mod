package com.kadmuffin.bikesarepain.client.item;

import com.kadmuffin.bikesarepain.server.item.BaseItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class BaseItemRenderer<T extends BaseItem> extends GeoItemRenderer<T> {

    public BaseItemRenderer(ResourceLocation model) {
        super(new DefaultedItemGeoModel<>(model));
    }
}
