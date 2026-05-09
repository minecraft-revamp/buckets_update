package com.bucketsupdate.feature.buckets;

import com.bucketsupdate.registry.ModDataComponents;
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
        // Wax preserves the visual oxidation stage but freezes it — copy across so transforms keep the look.
        OxidationStage stage = from.get(ModDataComponents.OXIDATION_STAGE.get());
        if (stage != null) {
            to.set(ModDataComponents.OXIDATION_STAGE.get(), stage);
        }
    }

    // No canUseFor / applyWear overrides: waxed copper is perfectly inert — infinite uses.
}
