package com.kadmuffin.bikesarepain.server;

import com.kadmuffin.bikesarepain.server.item.ItemManager;
import dev.architectury.event.events.common.LootEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;

public class LootManager {
    public static void init() {
        LootEvent.MODIFY_LOOT_TABLE.register((key, context, builtin) -> {
            if(key.location().equals(BuiltInLootTables.SHIPWRECK_TREASURE.location())
                && builtin
            ) {
                LootPool.Builder builder = new LootPool.Builder()
                        .setRolls(BinomialDistributionGenerator.binomial(1, 0.14f))
                                .add(LootItem.lootTableItem(ItemManager.FLOAT_MODIFIER_ITEM.get()));

                context.addPool(builder);
            }
        });
    }
}
