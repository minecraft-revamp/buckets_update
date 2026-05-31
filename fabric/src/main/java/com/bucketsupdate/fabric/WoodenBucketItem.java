package com.bucketsupdate.fabric;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

/**
 * Wooden bucket: cheapest tier, 16 uses then breaks (vanilla durability — see
 * {@link BaseBucketItem}).
 */
public class WoodenBucketItem extends BaseBucketItem {
    public static final int MAX_USES = 16;

    public WoodenBucketItem(
            Fluid content,
            Properties properties,
            Supplier<? extends BucketItem> filledCounterpart,
            Supplier<? extends BucketItem> emptyCounterpart) {
        super(content, properties.durability(MAX_USES), filledCounterpart, emptyCounterpart);
    }

    @Override
    protected int maxUses() {
        return MAX_USES;
    }
}
