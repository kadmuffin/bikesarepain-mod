package com.kadmuffin.bikesarepain.server.item;

import com.kadmuffin.bikesarepain.client.item.BikeItemRenderer;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.entity.Bicycle;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Objects;
import java.util.function.Consumer;

public class BikeItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final EntityType<? extends AbstractBike> entityType;
    private final ResourceLocation modelName;

    public BikeItem(EntityType<? extends AbstractBike> defaultType, ResourceLocation modelName, Properties properties) {
        super(properties);
        this.entityType = defaultType;
        this.modelName = modelName;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        ResourceLocation modelName = this.modelName;
        consumer.accept(new GeoRenderProvider() {
            private BikeItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new BikeItemRenderer(modelName);

                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        } else {
            ItemStack itemStack = context.getItemInHand();
            BlockPos blockPos = context.getClickedPos();
            Direction direction = context.getClickedFace();
            BlockState blockState = level.getBlockState(blockPos);
            BlockPos blockPos2;
            if (blockState.getCollisionShape(level, blockPos).isEmpty()) {
                blockPos2 = blockPos;
            } else {
                blockPos2 = blockPos.relative(direction);
            }

            // We need to assign the owner to the spawned entity
            AbstractBike entity = this.entityType.spawn(
                    (ServerLevel)level,
                    itemStack,
                    context.getPlayer(),
                    blockPos2,
                    MobSpawnType.SPAWN_EGG,
                    true,
                    !Objects.equals(blockPos, blockPos2) && direction == Direction.UP
            );

            // Scale the health based on the durability
            if (entity instanceof AbstractBike) {
                entity.setHealth(entity.getMaxHealth() * (itemStack.getMaxDamage() - Math.min(itemStack.getDamageValue(), itemStack.getMaxDamage()-1)) / itemStack.getMaxDamage());

                if (itemStack.has(ItemManager.SADDLED.get()) && Boolean.TRUE.equals(itemStack.get(ItemManager.SADDLED.get()))) {
                    ItemStack saddle = new ItemStack(Items.SADDLE);
                    saddle.setCount(1);
                    entity.equipSaddle(saddle, null);
                }

                if (itemStack.has(ItemManager.SAVE_TIME.get()) && Boolean.TRUE.equals(itemStack.get(ItemManager.SAVE_TIME.get()))) {
                    entity.setTicksTravelled(Objects.requireNonNullElse(itemStack.get(ItemManager.TICKS_MOVED.get()), 0));
                    entity.setSaveTime(true);
                }

                if (itemStack.has(ItemManager.SAVE_DISTANCE.get()) && Boolean.TRUE.equals(itemStack.get(ItemManager.SAVE_DISTANCE.get()))) {
                    entity.setBlocksTravelled(Objects.requireNonNullElse(itemStack.get(ItemManager.DISTANCE_MOVED.get()), 0.0F));
                    entity.setSaveDistance(true);
                }

                if (entity instanceof Bicycle && itemStack.has(ItemManager.HAS_BALLOON.get()) && Boolean.TRUE.equals(itemStack.get(ItemManager.HAS_BALLOON.get()))) {
                    ((Bicycle) entity).setHasBalloon(true);
                }

                if (entity instanceof Bicycle && itemStack.has(ItemManager.HAS_DISPLAY.get()) && Boolean.TRUE.equals(itemStack.get(ItemManager.HAS_DISPLAY.get()))) {
                    ((Bicycle) entity).setHasDisplay(true);
                }

                entity.setHealthAffectsSpeed(itemStack.has(ItemManager.HEALTH_AFFECTS_SPEED.get()) && Boolean.TRUE.equals(itemStack.get(ItemManager.HEALTH_AFFECTS_SPEED.get())));

                // Make the bike look the same direction as the player
                if (context.getPlayer() != null && entity.isSaddled()) {
                    entity.setYRot(context.getPlayer().getYRot());
                }
            }

            if (entity != null) {
                itemStack.shrink(1);
                level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, blockPos);
                if (context.getPlayer() != null) {
                    entity.setOwnerUUID(context.getPlayer().getUUID());
                    entity.setTamed(true);
                }

            }

            return InteractionResult.CONSUME;
        }
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.getItem() == Items.IRON_NUGGET;
    }

}
