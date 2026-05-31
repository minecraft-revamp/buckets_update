package com.bucketsupdate.feature.buckets;

import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class BambooMilkBucketItem extends BaseMilkBucketItem {
    public BambooMilkBucketItem(Properties properties, Supplier<? extends Item> emptyCounterpart) {
        super(properties.durability(BambooBucketItem.MAX_USES), emptyCounterpart);
    }

    @Override
    protected int maxUses() {
        return BambooBucketItem.MAX_USES;
    }
}
