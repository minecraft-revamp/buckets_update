package com.bucketsupdate.fabric;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

/**
 * Copper bucket: mid-tier between wooden and iron. 190 uses (matching vanilla
 * {@code ToolMaterial.COPPER} durability) then breaks. No oxidation — Mojang chose
 * not to oxidize copper tools/armor in vanilla MC 26.1, so we align. Like the vanilla
 * iron bucket it can also scoop powder snow (the wooden bucket cannot).
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
    protected int maxUses() {
        return MAX_USES;
    }

    @Override
    public SoundEvent getBreakSound() {
        return SoundEvents.COPPER_BREAK;
    }

    @Override
    protected boolean canSolidPickup(BlockState state) {
        return state.getBlock() instanceof PowderSnowBlock;
    }

    @Override
    protected ItemStack buildSolidResult(ItemStack stack) {
        if (wouldBreakAfterWear(stack)) {
            return ItemStack.EMPTY;
        }
        ItemStack filled = new ItemStack(ModItems.COPPER_POWDER_SNOW_BUCKET);
        copyState(stack, filled);
        return filled;
    }
}
