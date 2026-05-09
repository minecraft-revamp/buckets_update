package com.bucketsupdate.fabric;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

public class WaxedCopperBucketItem extends BaseBucketItem {
    public WaxedCopperBucketItem(
            Fluid content,
            Properties properties,
            Supplier<? extends BucketItem> filledCounterpart,
            Supplier<? extends BucketItem> emptyCounterpart) {
        super(content, properties, filledCounterpart, emptyCounterpart);
    }

    @Override
    protected void copyState(ItemStack from, ItemStack to) {
        OxidationStage stage = from.get(ModDataComponents.OXIDATION_STAGE);
        if (stage != null) {
            to.set(ModDataComponents.OXIDATION_STAGE, stage);
        }
    }
}
