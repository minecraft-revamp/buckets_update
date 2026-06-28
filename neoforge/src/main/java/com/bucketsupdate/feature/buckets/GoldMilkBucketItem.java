package com.bucketsupdate.feature.buckets;

import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class GoldMilkBucketItem extends BaseMilkBucketItem {
    public GoldMilkBucketItem(Properties properties, Supplier<? extends Item> emptyCounterpart) {
        super(properties.durability(GoldBucketItem.MAX_USES), emptyCounterpart);
    }

    @Override
    protected int maxUses() {
        return GoldBucketItem.MAX_USES;
    }
}
