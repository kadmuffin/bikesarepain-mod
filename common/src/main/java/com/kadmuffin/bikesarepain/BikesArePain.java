package com.kadmuffin.bikesarepain;

import com.kadmuffin.bikesarepain.client.KeybindManager;
import com.kadmuffin.bikesarepain.common.SoundManager;
import com.kadmuffin.bikesarepain.packets.PacketManager;
import com.kadmuffin.bikesarepain.server.GameRuleManager;
import com.kadmuffin.bikesarepain.server.StatsManager;
import com.kadmuffin.bikesarepain.server.entity.EntityManager;
import com.kadmuffin.bikesarepain.server.item.ItemManager;
import com.kadmuffin.bikesarepain.server.item.TooltipManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public final class BikesArePain {
    public static final String MOD_ID = "bikesarepain";
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(MOD_ID, Registries.ENTITY_TYPE);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(MOD_ID, Registries.SOUND_EVENT);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(MOD_ID, Registries.DATA_COMPONENT_TYPE);
    public static final DeferredRegister<ResourceLocation> STATS = DeferredRegister.create(MOD_ID, Registries.CUSTOM_STAT);

    public static void init() {
        EntityManager.init();
        ItemManager.init();
        PacketManager.init();
        KeybindManager.init();
        TooltipManager.init();
        SoundManager.init();
        GameRuleManager.init();
        StatsManager.init();

        CommandRegistrationEvent.EVENT.register((dispatcher, dedicated, commands) -> {
            dispatcher.register(Commands.literal("bikes_ops")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.literal("pretend")
                            // /bikes pretend <speedKMH> <distanceM> <kcalories> <wheelRadiusM> <scaleWheel> <scaleSpeed>
                            .then(
                                    Commands.argument("speedKph", FloatArgumentType.floatArg(0))
                                            .then(
                                                    Commands.argument("distanceMeters", FloatArgumentType.floatArg(0))
                                                            .then(
                                                                    Commands.argument("kCalories", FloatArgumentType.floatArg(0))
                                                                            .then(
                                                                                    Commands.argument("wheelRadiusM", FloatArgumentType.floatArg(0))
                                                                                            .then(
                                                                                                    Commands.argument("scaleWheel", FloatArgumentType.floatArg(0))
                                                                                                            .then(
                                                                                                                    Commands.argument("scaleSpeed", FloatArgumentType.floatArg(0))
                                                                                                                            .executes(context -> {
                                                                                                                                try {
                                                                                                                                    CommandSourceStack source = context.getSource();
                                                                                                                                    float speed = FloatArgumentType.getFloat(context, "speedKph");
                                                                                                                                    float distance = FloatArgumentType.getFloat(context, "distanceMeters");
                                                                                                                                    float kcalories = FloatArgumentType.getFloat(context, "kCalories");
                                                                                                                                    float wheelRadius = FloatArgumentType.getFloat(context, "wheelRadiusM");
                                                                                                                                    float scaleWheel = FloatArgumentType.getFloat(context, "scaleWheel");
                                                                                                                                    float scaleSpeed = FloatArgumentType.getFloat(context, "scaleSpeed");

                                                                                                                                    if (source.getPlayer() == null) {
                                                                                                                                        return 0;
                                                                                                                                    }
                                                                                                                                    PacketManager.processArduinoData(new PacketManager.ArduinoData(
                                                                                                                                            true,
                                                                                                                                            speed,
                                                                                                                                            distance,
                                                                                                                                            kcalories,
                                                                                                                                            wheelRadius,
                                                                                                                                            scaleWheel,
                                                                                                                                            scaleSpeed
                                                                                                                                    ), source.getPlayer(), source.getPlayer().level());

                                                                                                                                    return 1;
                                                                                                                                } catch (Exception e) {
                                                                                                                                    System.out.println("Error: " + e);
                                                                                                                                    return 0;
                                                                                                                                }
                                                                                                                            })
                                                                                                            )
                                                                                            )
                                                                            )
                                                            )
                                            )
                            )
                    )
            );

        });
    }
}
