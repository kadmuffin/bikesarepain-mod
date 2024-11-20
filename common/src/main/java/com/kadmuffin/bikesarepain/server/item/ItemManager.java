package com.kadmuffin.bikesarepain.server.item;

import com.kadmuffin.bikesarepain.BikesArePain;
import com.kadmuffin.bikesarepain.client.helper.Utils;
import com.kadmuffin.bikesarepain.server.entity.EntityManager;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.DyedItemColor;
import software.bernie.geckolib.object.Color;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ItemManager {

    public static final List<Integer> bicycleColors = List.of(
            Color.ofRGB(240, 240, 240).getColor(),
            Color.ofRGB(240, 240, 240).getColor(),
            Color.ofRGB(59, 47, 40).getColor(),
            Color.ofRGB(59, 47, 40).getColor());

    public static final Map<String, Function<ItemStack, Integer>> bonesToColorBicycleItem = Utils.createBonesToColorMap(
            Map.of(
                    List.of("hexadecagon"), (item) -> getBicycleItemColor(item, 0),
                    List.of("hexadecagon3"), (item) -> getBicycleItemColor(item, 1),
                    List.of("hexadecagon2", "Support", "ClickyThing", "ItemInventory", "Chest", "BladesF", "BladesB", "Cover", "Union", "SteeringFixed", "WoodThing",
                            "Cover2", "RodT", "RodD2", "BarT2", "RodD1", "hexadecagon4", "WheelStuff"), (item) -> getBicycleItemColor(item, 2),
                    List.of("Cap1", "Cap2"), (item) -> getBicycleItemColor(item, 3)
            )
    );

    private static int getBicycleItemColor(ItemStack item, int index) {
        if (index < 0 || index >= 4) {
            return 0;
        }

        List<Integer> colors = Utils.completeRest(item.getComponents().getOrDefault(ComponentManager.BICYCLE_COLORS.get(), bicycleColors), bicycleColors);

        int targetColor = colors.get(index);

        // If there is a zero in the index, return the default color at that index
        if (targetColor == 0) {
            return bicycleColors.get(index);
        }

        return targetColor;
    }

    private static int getBicyclePartColor(ItemStack item, int index) {
        if (index < 0 || index >= 4) {
            return 0;
        }

        return item.getComponents().getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(bicycleColors.get(index), false)).rgb();
    }    public static final RegistrySupplier<CreativeModeTab> BIKES_MOD_TAB = BikesArePain.TABS.register("bikes_mod_tab", () ->
            CreativeTabRegistry.create(
                    Component.literal("Bikes Are Pain"),
                    () -> new ItemStack(ItemManager.BICYCLE_ITEM.get())
            )
    );


    public static final RegistrySupplier<Item> BICYCLE_ITEM = BikesArePain.ITEMS.register(ItemKeyManager.BICYCLE_ID.getPath(), () ->
            new BicycleItem(EntityManager.BICYCLE.get(),
                    ItemKeyManager.BICYCLE_ID,
                    bonesToColorBicycleItem,
                    List.of(),
                    new BikeItem.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.RARE)
                            .durability(100)
                            .arch$tab(ItemManager.BIKES_MOD_TAB)
                            .component(ComponentManager.SADDLED.get(), false)
                            .component(ComponentManager.SAVE_TIME.get(), false)
                            .component(ComponentManager.SAVE_DISTANCE.get(), false)
                            .component(ComponentManager.DISTANCE_MOVED.get(), 0.0F)
                            .component(ComponentManager.TICKS_MOVED.get(), 0)
                            .component(ComponentManager.HEALTH_AFFECTS_SPEED.get(), true)
                            .component(ComponentManager.HAS_BALLOON.get(), false)
                            .component(ComponentManager.HAS_DISPLAY.get(), false)
                            .component(ComponentManager.BICYCLE_COLORS.get(), bicycleColors)
                            .setId(ItemKeyManager.BICYCLE_KEY)
            )
    );

    public static final RegistrySupplier<Item> NUT_ITEM = BikesArePain.ITEMS.register(ItemKeyManager.NUT_ID.getPath(), () ->
            new BaseItem(
                    ItemKeyManager.NUT_ID,
                    new Item.Properties()
                            .stacksTo(64)
                            .rarity(Rarity.COMMON)
                            .arch$tab(ItemManager.BIKES_MOD_TAB)
                            .setId(ItemKeyManager.NUT_KEY)
            )
    );

    public static final RegistrySupplier<Item> WRENCH_ITEM = BikesArePain.ITEMS.register(ItemKeyManager.WRENCH_ID, () ->
            new BaseItem(
                    ItemKeyManager.WRENCH_ID,
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.UNCOMMON)
                            .arch$tab(ItemManager.BIKES_MOD_TAB)
                            .durability(100)
                            .setId(ItemKeyManager.WRENCH_KEY)
            )
    );

    public static final RegistrySupplier<Item> PEDOMETER_ITEM = BikesArePain.ITEMS.register(ItemKeyManager.PEDOMETER_ID, () ->
            new BaseItem(
                    ItemKeyManager.PEDOMETER_ID,
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.UNCOMMON)
                            .arch$tab(ItemManager.BIKES_MOD_TAB)
                            .component(ComponentManager.SAVE_TIME.get(), true)
                            .component(ComponentManager.SAVE_DISTANCE.get(), true)
                            .component(ComponentManager.DISTANCE_MOVED.get(), 0.0F)
                            .component(ComponentManager.TICKS_MOVED.get(), 0)
                            .setId(ItemKeyManager.PEDOMETER_KEY)
            )
    );

    public static final RegistrySupplier<Item> FRAME_ITEM = BikesArePain.ITEMS.register(ItemKeyManager.BICYCLE_FRAME_ID, () ->
            new TintedItem(
                    ItemKeyManager.BICYCLE_FRAME_ID,
                    Map.of("*", (item) -> getBicyclePartColor(item, 3)),
                    List.of(),
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.UNCOMMON)
                            .arch$tab(ItemManager.BIKES_MOD_TAB)
                            .setId(ItemKeyManager.BICYCLE_FRAME_KEY)
            )
    );

    public static final RegistrySupplier<Item> GEARBOX_ITEM = BikesArePain.ITEMS.register(ItemKeyManager.BICYCLE_GEARBOX_ID, () ->
            new TintedItem(
                    ItemKeyManager.BICYCLE_GEARBOX_ID,
                    Map.of("Cap1", (item) -> getBicyclePartColor(item, 2),
                            "Cap2", (item) -> getBicyclePartColor(item, 2)
                    ),
                    List.of(),
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.UNCOMMON)
                            .arch$tab(ItemManager.BIKES_MOD_TAB)
                            .setId(ItemKeyManager.BICYCLE_GEARBOX_KEY)
            )
    );

    public static final RegistrySupplier<Item> HANDLEBAR_ITEM = BikesArePain.ITEMS.register(ItemKeyManager.BICYCLE_HANDLE_ID, () ->
            new BaseItem(
                    ItemKeyManager.BICYCLE_HANDLE_ID,
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.UNCOMMON)
                            .arch$tab(ItemManager.BIKES_MOD_TAB)
                            .setId(ItemKeyManager.BICYCLE_HANDLE_KEY)
            )
    );

    public static final RegistrySupplier<Item> WHEEL_ITEM = BikesArePain.ITEMS.register(ItemKeyManager.BICYCLE_WHEEL_ID, () ->
            new TintedItem(
                    ItemKeyManager.BICYCLE_WHEEL_ID,
                    Map.of("hexadecagon", (item) -> getBicyclePartColor(item, 0)),
                    List.of(),
                    new Item.Properties()
                            .stacksTo(2)
                            .rarity(Rarity.UNCOMMON)
                            .arch$tab(ItemManager.BIKES_MOD_TAB)
                            .setId(ItemKeyManager.BICYCLE_WHEEL_KEY)
            )
    );

    public static final RegistrySupplier<Item> FLOAT_MODIFIER_ITEM = BikesArePain.ITEMS.register(ItemKeyManager.FLOAT_MODIFIER_ID, () ->
            new Item(
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
                            .arch$tab(ItemManager.BIKES_MOD_TAB)
                            .setId(ItemKeyManager.FLOAT_MODIFIER_KEY)
            )
    );


    public static void init() {
        BikesArePain.TABS.register();
        BikesArePain.ITEMS.register();
    }
}
