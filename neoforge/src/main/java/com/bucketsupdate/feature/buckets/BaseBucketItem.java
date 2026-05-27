package com.bucketsupdate.feature.buckets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.function.Supplier;

/**
 * Base class shared by our wooden and copper buckets. Restricts pickup to water
 * sources only; subclasses hook wear / break-on-wear / state-copy.
 */
public abstract class BaseBucketItem extends BucketItem {
    private final Supplier<? extends BucketItem> filledCounterpart;
    private final Supplier<? extends BucketItem> emptyCounterpart;

    protected BaseBucketItem(
            Fluid content,
            Properties properties,
            Supplier<? extends BucketItem> filledCounterpart,
            Supplier<? extends BucketItem> emptyCounterpart) {
        super(content, properties);
        this.filledCounterpart = filledCounterpart;
        this.emptyCounterpart = emptyCounterpart;
    }

    public boolean isEmpty() {
        return this.content == Fluids.EMPTY;
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

    /** Refusal check evaluated BEFORE any side effect (drain water, place water). */
    protected boolean canUseFor(ItemStack stack, Player player, boolean fillingAction) {
        return true;
    }

    /** Wear-and-tear after a successful action. Subclasses may mutate {@code stack} in place. */
    protected void applyWear(ItemStack stack, Level level, Player player, boolean fillingAction) {}

    /** Public entry point for the milking flow (acts as a fill action). */
    public void applyMilkingWear(ItemStack stack, Level level, Player player) {
        applyWear(stack, level, player, true);
    }

    /**
     * Builds the milk-filled counterpart for a cow-milking action, or
     * {@link ItemStack#EMPTY} if this bucket has just broken from wear.
     */
    public ItemStack buildMilkResult(ItemStack stack, Item milkItem) {
        if (wouldBreakAfterWear(stack)) {
            return ItemStack.EMPTY;
        }
        ItemStack milk = new ItemStack(milkItem);
        copyState(stack, milk);
        return milk;
    }

    /** Subclasses signal that wear has just exhausted the bucket (wood/copper at MAX_USES). */
    protected boolean wouldBreakAfterWear(ItemStack stack) {
        return false;
    }

    /** Sound played when the bucket breaks from durability exhaustion. Defaults to wood. */
    public SoundEvent getBreakSound() {
        return SoundEvents.WOOD_BREAK;
    }

    /**
     * Builds the resulting item after a successful action.
     * Return {@link ItemStack#EMPTY} to signal the bucket broke (no replacement).
     */
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
        pickup.getPickupSound(state).ifPresent(s -> player.playSound(s, 1.0F, 1.0F));
        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);

        return finishUse(held, player, true);
    }

    private InteractionResult doEmpty(Level level, Player player, ItemStack held, BlockHitResult hit, BlockPos pos, BlockPos relPos) {
        if (!canUseFor(held, player, false)) {
            return InteractionResult.FAIL;
        }
        BlockState clicked = level.getBlockState(pos);
        BlockPos placePos = canBlockContainFluid(player, level, pos, clicked) && this.content == Fluids.WATER ? pos : relPos;

        if (!emptyContents(player, level, placePos, hit, held)) {
            return InteractionResult.FAIL;
        }

        applyWear(held, level, player, false);
        checkExtraContent(player, level, held, placePos);
        player.awardStat(Stats.ITEM_USED.get(this));

        return finishUse(held, player, false);
    }

    private InteractionResult finishUse(ItemStack held, Player player, boolean fillingAction) {
        ItemStack ourResult = buildResult(held, fillingAction);
        if (ourResult.isEmpty()) {
            if (player instanceof ServerPlayer sp) {
                sp.level().playSound(null, sp.blockPosition(),
                        getBreakSound(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            held.shrink(1);
            return InteractionResult.SUCCESS.heldItemTransformedTo(ItemStack.EMPTY);
        }
        ItemStack result = ItemUtils.createFilledResult(held, player, ourResult);
        return InteractionResult.SUCCESS.heldItemTransformedTo(result);
    }
}
