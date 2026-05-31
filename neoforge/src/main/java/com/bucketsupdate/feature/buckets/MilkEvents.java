package com.bucketsupdate.feature.buckets;

import com.bucketsupdate.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Right-click a cow with one of our empty buckets → consume the empty bucket and
 * give the matching milk variant (damage state preserved). Vanilla {@code Cow#mobInteract}
 * only milks into {@code Items.BUCKET}; this handler runs first and routes our two
 * custom empty variants (wooden / copper) to their milk-bucket counterparts.
 */
public final class MilkEvents {
    private MilkEvents() {}

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Cow cow) || cow.isBaby()) return;

        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack stack = player.getItemInHand(hand);
        Item milkVariant = getMilkVariant(stack.getItem());
        if (milkVariant == null) return;
        if (!(stack.getItem() instanceof BaseBucketItem bucket)) return;

        Level level = event.getLevel();
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        // Server-only mutation. Client gets the new stack via the item-sync packet
        // following the cancellation result.
        if (level.isClientSide()) return;

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
            return;
        }

        ItemStack newStack = ItemUtils.createFilledResult(stack, player, milkResult);
        player.setItemInHand(hand, newStack);
    }

    private static Item getMilkVariant(Item emptyBucket) {
        if (emptyBucket == ModItems.WOODEN_BUCKET.get()) return ModItems.WOODEN_MILK_BUCKET.get();
        if (emptyBucket == ModItems.BAMBOO_BUCKET.get()) return ModItems.BAMBOO_MILK_BUCKET.get();
        if (emptyBucket == ModItems.COPPER_BUCKET.get()) return ModItems.COPPER_MILK_BUCKET.get();
        return null;
    }
}
