package com.bucketsupdate.fabric;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

/**
 * Bamboo bucket: same idea as the wooden bucket but twice as durable — 32 uses
 * (vanilla durability) before it breaks. Like wood it holds water + milk only
 * (no powder snow; that's copper's trick).
 */
public class BambooBucketItem extends BaseBucketItem {
    public static final int MAX_USES = 32;

    public BambooBucketItem(
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
