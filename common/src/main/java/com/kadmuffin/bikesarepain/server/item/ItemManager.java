package com.kadmuffin.bikesarepain.server.item;

import com.kadmuffin.bikesarepain.BikesArePain;
import com.kadmuffin.bikesarepain.server.entity.EntityManager;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;

public class ItemManager {
    public static final RegistrySupplier<CreativeModeTab> BIKES_MOD_TAB = BikesArePain.TABS.register("bikes_mod_tab", () ->
            CreativeTabRegistry.create(
                    Component.literal("Bikes Are Pain"),
                    () -> new ItemStack(Items.SADDLE)
            )
    );

    public static final RegistrySupplier<Item> BICYCLE_ITEM = BikesArePain.ITEMS.register("bicycle_item", () ->
            new BikeItem(EntityManager.BICYCLE.get(),
                    ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "bicycle_item"),
                    new BikeItem.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.UNCOMMON)
                            .arch$tab(BIKES_MOD_TAB.get())
            )
    );

    public static void init() {
        BikesArePain.TABS.register();
        BikesArePain.ITEMS.register();
    }

}
