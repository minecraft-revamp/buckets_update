package com.bucketsupdate.fabric;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

/**
 * Copper bucket: permanent like the iron bucket — no durability, never breaks, and
 * the empty bucket stacks (stack size set in {@link ModItems}). Like iron it can also
 * scoop powder snow (the wooden/bamboo buckets cannot). Inheriting
 * {@code maxUses() == Integer.MAX_VALUE} means {@link BaseBucketItem} applies no wear
 * and never breaks it.
 */
public class CopperBucketItem extends BaseBucketItem {
    public CopperBucketItem(
            Fluid content,
            Properties properties,
            Supplier<? extends BucketItem> filledCounterpart,
            Supplier<? extends BucketItem> emptyCounterpart) {
        super(content, properties, filledCounterpart, emptyCounterpart);
    }

    @Override
    protected boolean canSolidPickup(BlockState state) {
        return state.getBlock() instanceof PowderSnowBlock;
    }

    @Override
    protected ItemStack buildSolidResult(ItemStack stack) {
        ItemStack filled = new ItemStack(ModItems.COPPER_POWDER_SNOW_BUCKET);
        copyState(stack, filled);
        return filled;
    }
}
