package com.bucketsupdate.fabric;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

/**
 * Copper bucket: mid-tier between wooden and iron. 190 uses, matching vanilla
 * {@code ToolMaterial.COPPER} durability, then breaks. No oxidation — Mojang
 * chose not to oxidize copper tools/armor in vanilla MC 26.1, so we align.
 */
public class CopperBucketItem extends BaseBucketItem {
    public static final int MAX_USES = 190;

    public CopperBucketItem(
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

    @Override
    public SoundEvent getBreakSound() {
        return SoundEvents.COPPER_BREAK;
    }
}
