package com.bucketsupdate.fabric;

import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class WoodenMilkBucketItem extends BaseMilkBucketItem {
    public WoodenMilkBucketItem(Properties properties, Supplier<? extends Item> emptyCounterpart) {
        super(properties.durability(WoodenBucketItem.MAX_USES), emptyCounterpart);
    }

    @Override
    protected int maxUses() {
        return WoodenBucketItem.MAX_USES;
    }
}
