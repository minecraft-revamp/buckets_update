package com.bucketsupdate.feature.buckets;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/**
 * Base for our two milk-bucket variants. The vanilla CONSUMABLE component
 * (set in {@link com.bucketsupdate.registry.ModItems}) handles drink animation,
 * sound and effects-clear.
 * <p>
 * We intentionally do NOT use {@code usingConvertsTo} (i.e. no {@code USE_REMAINDER}
 * component): vanilla's USE_REMAINDER replacement fires AFTER {@link #finishUsingItem}
 * via {@code ItemStack#applyAfterUseComponentSideEffects}, which would overwrite our
 * state-preserved empty stack with a fresh full-durability one. Instead we build the
 * empty counterpart manually here, copy damage state across, and apply drink wear.
 */
public abstract class BaseMilkBucketItem extends Item {
    protected final Supplier<? extends Item> emptyCounterpart;

    public BaseMilkBucketItem(Properties properties, Supplier<? extends Item> emptyCounterpart) {
        super(properties);
        this.emptyCounterpart = emptyCounterpart;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        // Snapshot BEFORE the Consumable consumes the stack (it shrinks to 0 on
        // single-stack milk buckets), since empty stacks return defaults for component reads.
        ItemStack stackBefore = stack.copy();

        // Run the CONSUMABLE component (clear effects, particles, sound, stack.consume).
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
