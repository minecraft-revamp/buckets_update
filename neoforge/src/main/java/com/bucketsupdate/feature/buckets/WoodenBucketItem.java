package com.bucketsupdate.feature.buckets;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

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
    protected void copyState(ItemStack from, ItemStack to) {
        Integer damage = from.get(DataComponents.DAMAGE);
        if (damage != null) {
            to.set(DataComponents.DAMAGE, damage);
        }
    }

    @Override
    protected void applyWear(ItemStack stack, Level level, Player player, boolean fillingAction) {
        if (player.getAbilities().instabuild) return;
        stack.setDamageValue(stack.getDamageValue() + 1);
    }

    @Override
    protected ItemStack buildResult(ItemStack stack, boolean fillingAction) {
        if (stack.getDamageValue() >= MAX_USES) {
            return ItemStack.EMPTY;
        }
        return super.buildResult(stack, fillingAction);
    }

    @Override
    protected boolean wouldBreakAfterWear(ItemStack stack) {
        return stack.getDamageValue() >= MAX_USES;
    }
}
