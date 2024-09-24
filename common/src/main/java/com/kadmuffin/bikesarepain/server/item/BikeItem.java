package com.kadmuffin.bikesarepain.server.item;

import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class BikeItem extends TintedItem {
    private final EntityType<? extends AbstractBike> entityType;

    public BikeItem(EntityType<? extends AbstractBike> entityType, ResourceLocation modelName, Map<String, Function<ItemStack, Integer>> bonesToColor, List<String> bonesToIgnore, Properties properties) {
        super(modelName, bonesToColor, bonesToIgnore, properties);
        this.entityType = entityType;
    }

    public void placementHook(AbstractBike entity, ItemStack itemStack) {
        entity.setHealth(entity.getMaxHealth() * (itemStack.getMaxDamage() - Math.min(itemStack.getDamageValue(), itemStack.getMaxDamage() - 1)) / itemStack.getMaxDamage());

        if (itemStack.has(ComponentManager.SADDLED.get()) && Boolean.TRUE.equals(itemStack.get(ComponentManager.SADDLED.get()))) {
            ItemStack saddle = new ItemStack(net.minecraft.world.item.Items.SADDLE);
            saddle.setCount(1);
            entity.equipSaddle(saddle, null);
        }

        if (itemStack.has(ComponentManager.SAVE_TIME.get()) && Boolean.TRUE.equals(itemStack.get(ComponentManager.SAVE_TIME.get()))) {
            entity.setTicksPedalled(Objects.requireNonNullElse(itemStack.get(ComponentManager.TICKS_MOVED.get()), 0));
            entity.setSaveTime(true);
        }

        if (itemStack.has(ComponentManager.SAVE_DISTANCE.get()) && Boolean.TRUE.equals(itemStack.get(ComponentManager.SAVE_DISTANCE.get()))) {
            entity.setBlocksTravelled(Objects.requireNonNullElse(itemStack.get(ComponentManager.DISTANCE_MOVED.get()), 0.0F));
            entity.setSaveDistance(true);
        }
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
                    (ServerLevel) level,
                    itemStack,
                    context.getPlayer(),
                    blockPos2,
                    MobSpawnType.SPAWN_EGG,
                    true,
                    !Objects.equals(blockPos, blockPos2) && direction == Direction.UP
            );

            // Scale the health based on the durability
            if (entity instanceof AbstractBike) {
                this.placementHook(entity, itemStack);

                entity.setHealthAffectsSpeed(itemStack.has(ComponentManager.HEALTH_AFFECTS_SPEED.get()) && Boolean.TRUE.equals(itemStack.get(ComponentManager.HEALTH_AFFECTS_SPEED.get())));

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
        return repairCandidate.getItem() == net.minecraft.world.item.Items.IRON_NUGGET;
    }

}
