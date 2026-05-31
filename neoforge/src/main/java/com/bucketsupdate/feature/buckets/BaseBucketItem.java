package com.bucketsupdate.feature.buckets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
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
 * Base class shared by our wooden and copper buckets. Restricts liquid pickup to water
 * sources, and uses vanilla durability (the {@code DAMAGE}/{@code MAX_DAMAGE} components):
 * the buckets wear out and break after {@link #maxUses()} uses, show the vanilla durability
 * bar, and can be repaired by combining two damaged ones in the crafting grid
 * ({@code RepairItemRecipe}). Because they're damageable they follow the vanilla rule of
 * not stacking.
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

    /** Uses before this bucket breaks. Default {@code MAX_VALUE} = unbreakable. */
    protected int maxUses() {
        return Integer.MAX_VALUE;
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

    /** Carries accrued durability damage across the empty/filled transition. */
    protected void copyState(ItemStack from, ItemStack to) {
        Integer damage = from.get(DataComponents.DAMAGE);
        if (damage != null) {
            to.set(DataComponents.DAMAGE, damage);
        }
    }

    /** Refusal check evaluated BEFORE any side effect (drain water, place water). */
    protected boolean canUseFor(ItemStack stack, Player player, boolean fillingAction) {
        return true;
    }

    /** Wear-and-tear after a successful action: one durability point, unless in creative. */
    protected void applyWear(ItemStack stack, Level level, Player player, boolean fillingAction) {
        if (maxUses() == Integer.MAX_VALUE || player.getAbilities().instabuild) return;
        stack.setDamageValue(stack.getDamageValue() + 1);
    }

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

    /** True once durability damage has reached the cap (bucket should break). */
    protected boolean wouldBreakAfterWear(ItemStack stack) {
        return stack.getDamageValue() >= maxUses();
    }

    /** Sound played when the bucket breaks from wear exhaustion. Defaults to wood. */
    public SoundEvent getBreakSound() {
        return SoundEvents.WOOD_BREAK;
    }

    /**
     * Builds the resulting item after a successful action.
     * Returns {@link ItemStack#EMPTY} to signal the bucket broke (no replacement).
     */
    protected ItemStack buildResult(ItemStack stack, boolean fillingAction) {
        if (wouldBreakAfterWear(stack)) {
            return ItemStack.EMPTY;
        }
        return fillingAction ? toFilled(stack) : toEmpty(stack);
    }

    // ---- Solid (non-fluid) pickup, e.g. powder snow. Default: unsupported. ----

    /** Whether an empty bucket may scoop this non-fluid {@link BucketPickup} block. */
    protected boolean canSolidPickup(BlockState state) {
        return false;
    }

    /** Result of a solid pickup (called AFTER wear is applied). {@link ItemStack#EMPTY} = broke. */
    protected ItemStack buildSolidResult(ItemStack stack) {
        return ItemStack.EMPTY;
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
            // Non-water source: try a solid pickup (powder snow on copper) before refusing.
            if (canSolidPickup(state)) {
                return doSolidPickup(level, player, held, pickup, pos, state);
            }
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

        return finishUseWithResult(held, player, buildResult(held, true));
    }

    private InteractionResult doSolidPickup(Level level, Player player, ItemStack held, BucketPickup pickup, BlockPos pos, BlockState state) {
        if (!canUseFor(held, player, true)) {
            return InteractionResult.FAIL;
        }
        // Remove the block via vanilla pickup; we discard its (vanilla) result item and build our own.
        ItemStack taken = pickup.pickupBlock(player, level, pos, state);
        if (taken.isEmpty()) {
            return InteractionResult.FAIL;
        }
        applyWear(held, level, player, true);
        player.awardStat(Stats.ITEM_USED.get(this));
        pickup.getPickupSound(state).ifPresent(s -> player.playSound(s, 1.0F, 1.0F));
        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);

        return finishUseWithResult(held, player, buildSolidResult(held));
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

        return finishUseWithResult(held, player, buildResult(held, false));
    }

    private InteractionResult finishUseWithResult(ItemStack held, Player player, ItemStack ourResult) {
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
