package com.bucketsupdate.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.function.Supplier;

/**
 * Base class shared by our wooden, copper and waxed-copper buckets.
 * Restricts pickup to water sources only and provides hooks for wear/refusal/transform logic.
 */
public abstract class BaseBucketItem extends BucketItem {
    /** Local copy because vanilla {@code BucketItem.content} is private (NeoForge exposes it, Fabric doesn't). */
    protected final Fluid bucketContent;
    private final Supplier<? extends BucketItem> filledCounterpart;
    private final Supplier<? extends BucketItem> emptyCounterpart;

    protected BaseBucketItem(
            Fluid content,
            Properties properties,
            Supplier<? extends BucketItem> filledCounterpart,
            Supplier<? extends BucketItem> emptyCounterpart) {
        super(content, properties);
        this.bucketContent = content;
        this.filledCounterpart = filledCounterpart;
        this.emptyCounterpart = emptyCounterpart;
    }

    public boolean isEmpty() {
        return this.bucketContent == Fluids.EMPTY;
    }

    public ItemStack toFilled(ItemStack source) {
        ItemStack filled = new ItemStack(filledCounterpart.get());
        copyState(source, filled);
        return filled;
    }

    public ItemStack toEmpty(ItemStack source) {
        ItemStack empty = new ItemStack(emptyCounterpart.get());
        copyState(source, empty);
        return empty;
    }

    protected void copyState(ItemStack from, ItemStack to) {}

    protected boolean canUseFor(ItemStack stack, Player player, boolean fillingAction) {
        return true;
    }

    protected void applyWear(ItemStack stack, Level level, Player player, boolean fillingAction) {}

    protected ItemStack buildResult(ItemStack stack, boolean fillingAction) {
        return fillingAction ? toFilled(stack) : toEmpty(stack);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(
                level, player,
                isEmpty() ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);

        if (hit.getType() == HitResult.Type.MISS) return InteractionResult.PASS;
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResult.PASS;

        BlockPos pos = hit.getBlockPos();
        Direction direction = hit.getDirection();
        BlockPos relPos = pos.relative(direction);
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(relPos, direction, held)) {
            return InteractionResult.FAIL;
        }

        return isEmpty()
                ? doFill(level, player, held, hit, pos)
                : doEmpty(level, player, held, hit, pos, relPos);
    }

    private InteractionResult doFill(Level level, Player player, ItemStack held, BlockHitResult hit, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BucketPickup pickup)) {
            return InteractionResult.FAIL;
        }
        Fluid sourceFluid = level.getFluidState(pos).getType();
        if (sourceFluid != Fluids.WATER) {
            player.sendOverlayMessage(Component.translatable("item.buckets_update.bucket.water_only"));
            return InteractionResult.FAIL;
        }
        if (!canUseFor(held, player, true)) {
            return InteractionResult.FAIL;
        }

        ItemStack taken = pickup.pickupBlock(player, level, pos, state);
        if (taken.isEmpty()) {
            return InteractionResult.FAIL;
        }

        applyWear(held, level, player, true);
        player.awardStat(Stats.ITEM_USED.get(this));
        pickup.getPickupSound().ifPresent(s -> player.playSound(s, 1.0F, 1.0F));
        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);

        return finishUse(held, player, true);
    }

    private InteractionResult doEmpty(Level level, Player player, ItemStack held, BlockHitResult hit, BlockPos pos, BlockPos relPos) {
        if (!canUseFor(held, player, false)) {
            return InteractionResult.FAIL;
        }
        BlockState clicked = level.getBlockState(pos);
        boolean canPlaceInside = clicked.getBlock() instanceof LiquidBlockContainer container
                && container.canPlaceLiquid(player, level, pos, clicked, this.bucketContent);
        BlockPos placePos = (canPlaceInside && this.bucketContent == Fluids.WATER) ? pos : relPos;

        // Vanilla Fabric only has the 4-arg overload; NeoForge added an ItemStack-aware variant.
        if (!emptyContents(player, level, placePos, hit)) {
            return InteractionResult.FAIL;
        }

        applyWear(held, level, player, false);
        player.awardStat(Stats.ITEM_USED.get(this));

        return finishUse(held, player, false);
    }

    private InteractionResult finishUse(ItemStack held, Player player, boolean fillingAction) {
        ItemStack ourResult = buildResult(held, fillingAction);
        if (ourResult.isEmpty()) {
            if (player instanceof ServerPlayer sp) {
                sp.level().playSound(null, sp.blockPosition(),
                        net.minecraft.sounds.SoundEvents.WOOD_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            held.shrink(1);
            return InteractionResult.SUCCESS.heldItemTransformedTo(ItemStack.EMPTY);
        }
        ItemStack result = ItemUtils.createFilledResult(held, player, ourResult);
        return InteractionResult.SUCCESS.heldItemTransformedTo(result);
    }
}
