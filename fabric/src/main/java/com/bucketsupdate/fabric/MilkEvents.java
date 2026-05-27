package com.bucketsupdate.fabric;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Right-click a cow with one of our empty buckets → consume the empty bucket and
 * give the matching milk variant (damage state preserved). Vanilla {@code Cow#mobInteract}
 * only milks into {@code Items.BUCKET}; this callback fires before that and handles
 * our two custom empty variants (wooden / copper).
 */
public final class MilkEvents {
    private MilkEvents() {}

    public static void register() {
        UseEntityCallback.EVENT.register(MilkEvents::onUseEntity);
    }

    private static InteractionResult onUseEntity(Player player, Level level, InteractionHand hand, Entity entity, EntityHitResult hit) {
        if (!(entity instanceof Cow cow) || cow.isBaby()) return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);
        Item milkVariant = getMilkVariant(stack.getItem());
        if (milkVariant == null) return InteractionResult.PASS;
        if (!(stack.getItem() instanceof BaseBucketItem bucket)) return InteractionResult.PASS;

        // Mutate state on the server only. Client gets the new stack via the regular
        // item-sync packet.
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        bucket.applyMilkingWear(stack, level, player);

        ItemStack milkResult = bucket.buildMilkResult(stack, milkVariant);

        level.playSound(null, player.blockPosition(),
                SoundEvents.COW_MILK, SoundSource.PLAYERS, 1.0F, 1.0F);

        if (milkResult.isEmpty()) {
            if (level instanceof ServerLevel sl) {
                sl.playSound(null, player.blockPosition(),
                        bucket.getBreakSound(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            stack.shrink(1);
            return InteractionResult.SUCCESS;
        }

        ItemStack newStack = ItemUtils.createFilledResult(stack, player, milkResult);
        player.setItemInHand(hand, newStack);
        return InteractionResult.SUCCESS;
    }

    private static Item getMilkVariant(Item emptyBucket) {
        if (emptyBucket == ModItems.WOODEN_BUCKET) return ModItems.WOODEN_MILK_BUCKET;
        if (emptyBucket == ModItems.COPPER_BUCKET) return ModItems.COPPER_MILK_BUCKET;
        return null;
    }
}
