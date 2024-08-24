package com.kadmuffin.bikesarepain.client.item;

import com.kadmuffin.bikesarepain.server.item.BikeItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class BikeItemRenderer extends GeoItemRenderer<BikeItem> {

    public BikeItemRenderer(ResourceLocation modelName) {
        super(new DefaultedItemGeoModel<>(modelName));
    }
}
