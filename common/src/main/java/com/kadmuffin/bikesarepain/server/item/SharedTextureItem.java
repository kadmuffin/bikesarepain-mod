package com.kadmuffin.bikesarepain.server.item;

import com.kadmuffin.bikesarepain.client.item.BaseItemRenderer;
import com.kadmuffin.bikesarepain.client.item.SharedTextureItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

import java.util.function.Consumer;

public class SharedTextureItem extends BaseItem {
    ResourceLocation texture;

    public SharedTextureItem(ResourceLocation model, ResourceLocation texture, Properties properties) {
        super(model, properties);
        this.texture = texture;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private SharedTextureItemRenderer<BaseItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new SharedTextureItemRenderer<>(getModel(), texture);

                return this.renderer;
            }
        });
    }
}
