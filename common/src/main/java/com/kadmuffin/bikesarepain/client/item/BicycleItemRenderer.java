package com.kadmuffin.bikesarepain.client.item;

import com.kadmuffin.bikesarepain.server.item.BicycleItem;
import com.kadmuffin.bikesarepain.server.item.ComponentManager;
import com.kadmuffin.bikesarepain.server.item.ItemManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.layer.FastBoneFilterGeoLayer;

import java.util.List;
import java.util.function.Supplier;

public class BicycleItemRenderer extends TintedItemRenderer<BicycleItem> {
    public final Supplier<List<String>> bones = () -> List.of(
            "Chest", "Propellers", "MonitorRoot", "SeatF"
    );
    /**
     * An item renderer that colors bones based on a provided map
     *
     * @param modelName     The path to the model to render
     * @param bonesToColor  A list of bone names to color
     * @param bonesToIgnore A list of bone names to ignore when coloring bones every bone (with "*")
     */
    public BicycleItemRenderer(ResourceLocation modelName, List<String> bonesToColor, List<String> bonesToIgnore) {
        super(modelName, bonesToColor, bonesToIgnore);
        addRenderLayer(new FastBoneFilterGeoLayer<>(this, bones, (geoBone, item, afloat) -> {
            ItemStack stack = this.getCurrentItemStack();

            if (geoBone.getName().equals("Chest")) {
                geoBone.setHidden(true);
            }

            if (geoBone.getName().equals("Propellers")) {
                boolean hide = stack.getOrDefault(ComponentManager.HAS_BALLOON.getOrNull(), false);
                geoBone.setHidden(!hide);
            }

            if (geoBone.getName().equals("MonitorRoot")) {
                boolean hide = stack.getOrDefault(ComponentManager.HAS_DISPLAY.getOrNull(), false);
                geoBone.setHidden(!hide);
            }

            if (geoBone.getName().equals("SeatF")) {
                boolean hide = stack.getOrDefault(ComponentManager.SADDLED.getOrNull(), false);
                geoBone.setHidden(!hide);
            }
        }));
    }

}
