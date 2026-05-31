package com.bucketsupdate.feature.buckets;

import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/**
 * Copper milk bucket: like the iron tier, the copper bucket has no durability, so
 * drinking just returns the empty copper bucket (no wear). Inheriting
 * {@code maxUses() == Integer.MAX_VALUE} makes {@link BaseMilkBucketItem} skip wear.
 */
public class CopperMilkBucketItem extends BaseMilkBucketItem {
    public CopperMilkBucketItem(Properties properties, Supplier<? extends Item> emptyCounterpart) {
        super(properties, emptyCounterpart);
    }
}
