package com.bucketsupdate.fabric;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/**
 * Base for our three milk-bucket variants. The vanilla CONSUMABLE component
 * (set in {@link ModItems}) handles the drinking animation, sound and effects-clear.
 * <p>
 * We intentionally do NOT use {@code usingConvertsTo} (i.e. no {@code USE_REMAINDER}
 * component): vanilla's USE_REMAINDER replacement fires AFTER {@link #finishUsingItem}
 * via {@code ItemStack#applyAfterUseComponentSideEffects}, which would overwrite our
 * state-preserved empty stack with a fresh full-durability one. Instead we build
 * the empty counterpart manually here, copy material state across, and apply drink wear.
 */
public abstract class BaseMilkBucketItem extends Item {
    protected final Supplier<? extends Item> emptyCounterpart;

    public BaseMilkBucketItem(Properties properties, Supplier<? extends Item> emptyCounterpart) {
        super(properties);
        this.emptyCounterpart = emptyCounterpart;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        // Snapshot BEFORE the Consumable runs: Consumable.onConsume ends with
        // stack.consume(1, user) which shrinks count to 0 (milk buckets stack to 1),
        // and an empty stack returns default (0) for any data-component read.
        ItemStack stackBefore = stack.copy();

        // Run the Consumable component (clear effects, particles, sound, stack.consume).
        super.finishUsingItem(stack, level, entity);

        if (!(entity instanceof Player player)) return stack;
        if (player.getAbilities().instabuild) return stack;

        ItemStack empty = new ItemStack(emptyCounterpart.get());
        copyState(stackBefore, empty);
        return finalizeDrink(stackBefore, empty, level, player);
    }

    protected void copyState(ItemStack from, ItemStack to) {}

    /** Apply per-material drink wear. Return {@link ItemStack#EMPTY} if the bucket broke. */
    protected ItemStack finalizeDrink(ItemStack drunk, ItemStack empty, Level level, Player player) {
        return empty;
    }
}
