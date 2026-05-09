package com.bucketsupdate.fabric;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Fabric event hooks: handle right-click waxing/scrubbing of copper buckets.
 * The vanilla iron bucket recipe override is delivered as a static datapack at
 * data/minecraft/recipe/bucket.json (loaded with mod priority over vanilla).
 */
public final class BucketEvents {
    private BucketEvents() {}

    public static void register() {
        UseItemCallback.EVENT.register(BucketEvents::onUseItem);
    }

    private static InteractionResult onUseItem(Player player, Level level, InteractionHand hand) {
        // Process from MAIN_HAND so we always inspect both stacks at once.
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        ItemStack bucket;
        ItemStack tool;
        InteractionHand bucketHand;
        if (isCopperBucket(mainHand)) {
            bucket = mainHand;
            tool = offHand;
            bucketHand = InteractionHand.MAIN_HAND;
        } else if (isCopperBucket(offHand)) {
            bucket = offHand;
            tool = mainHand;
            bucketHand = InteractionHand.OFF_HAND;
        } else {
            return InteractionResult.PASS;
        }

        if (tool.is(Items.HONEYCOMB)) {
            return applyWax(player, bucket, bucketHand, tool)
                    ? InteractionResult.SUCCESS.heldItemTransformedTo(player.getItemInHand(bucketHand))
                    : InteractionResult.PASS;
        } else if (tool.getItem() instanceof AxeItem) {
            return applyScrub(player, bucket, bucketHand, tool)
                    ? InteractionResult.SUCCESS.heldItemTransformedTo(player.getItemInHand(bucketHand))
                    : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    private static boolean applyWax(Player player, ItemStack bucket, InteractionHand bucketHand, ItemStack honeycomb) {
        boolean filled = bucket.getItem() == ModItems.COPPER_WATER_BUCKET;
        ItemStack waxed = new ItemStack(filled ? ModItems.WAXED_COPPER_WATER_BUCKET : ModItems.WAXED_COPPER_BUCKET);
        OxidationStage stage = bucket.get(ModDataComponents.OXIDATION_STAGE);
        if (stage != null) {
            waxed.set(ModDataComponents.OXIDATION_STAGE, stage);
        }
        if (!player.getAbilities().instabuild) {
            honeycomb.shrink(1);
        }
        player.setItemInHand(bucketHand, waxed);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.HONEYCOMB_WAX_ON, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private static boolean applyScrub(Player player, ItemStack bucket, InteractionHand bucketHand, ItemStack axe) {
        OxidationStage stage = bucket.get(ModDataComponents.OXIDATION_STAGE);
        if (stage == null || !stage.canScrub()) return false;

        // Build a new stack and assign explicitly — component-only in-place mutations
        // don't always resync to the client on MC 26.1.
        ItemStack scrubbed = bucket.copy();
        scrubbed.set(ModDataComponents.OXIDATION_STAGE, stage.previous());
        player.setItemInHand(bucketHand, scrubbed);

        if (!player.getAbilities().instabuild && player.level() instanceof ServerLevel serverLevel) {
            axe.hurtAndBreak(1, serverLevel,
                    player instanceof ServerPlayer sp ? sp : null,
                    brokenItem -> {});
        }
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.AXE_SCRAPE, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private static boolean isCopperBucket(ItemStack stack) {
        return stack.getItem() == ModItems.COPPER_BUCKET
                || stack.getItem() == ModItems.COPPER_WATER_BUCKET;
    }
}
