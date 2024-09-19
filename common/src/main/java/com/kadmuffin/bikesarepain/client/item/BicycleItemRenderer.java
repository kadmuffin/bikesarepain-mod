package com.kadmuffin.bikesarepain.client.item;

import com.kadmuffin.bikesarepain.server.item.BikeItem;
import com.kadmuffin.bikesarepain.server.item.ItemManager;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.layer.FastBoneFilterGeoLayer;

import java.util.List;
import java.util.function.Supplier;

public class BicycleItemRenderer<T extends BikeItem> extends BaseItemRenderer<T> {
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
                geoBone.setHidden(isEnabledComponentDef(stack, ItemManager.HAS_BALLOON.get()));
            }

            if (geoBone.getName().equals("MonitorRoot")) {
                geoBone.setHidden(isEnabledComponentDef(stack, ItemManager.HAS_DISPLAY.get()));
            }

            if (geoBone.getName().equals("SeatF")) {
                geoBone.setHidden(isEnabledComponentDef(stack, ItemManager.SADDLED.get()));
            }
        }));
    }

    public static boolean isEnabledComponentDef(ItemStack stack, DataComponentType<Boolean> type) {
        return stack.getComponents().getOrDefault(type, false);
    }
}
