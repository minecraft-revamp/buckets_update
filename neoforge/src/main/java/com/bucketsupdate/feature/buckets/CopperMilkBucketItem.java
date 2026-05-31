package com.bucketsupdate.feature.buckets;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/**
 * Copper milk bucket: shares the empty copper bucket's 190-use wear pool
 * across fills + drinks. On the drink that pushes wear past MAX_USES the bucket
 * breaks (returns {@link net.minecraft.world.item.ItemStack#EMPTY}) instead of
 * returning the empty counterpart.
 */
public class CopperMilkBucketItem extends BaseMilkBucketItem {
    public CopperMilkBucketItem(Properties properties, Supplier<? extends Item> emptyCounterpart) {
        super(properties.durability(CopperBucketItem.MAX_USES), emptyCounterpart);
    }

    @Override
    protected int maxUses() {
        return CopperBucketItem.MAX_USES;
    }

    @Override
    protected SoundEvent getBreakSound() {
        return SoundEvents.COPPER_BREAK;
    }
}
