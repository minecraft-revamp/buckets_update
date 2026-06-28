package com.bucketsupdate.feature.buckets;

import com.bucketsupdate.registry.ModItems;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.function.Supplier;

/**
 * Gold bucket: 32 uses (same as bamboo), but carries water, lava, milk, and
 * powder snow — the most versatile bucket in the mod. Unlike copper (permanent)
 * and wood/bamboo (water + milk only), gold handles both fluid types from a
 * single empty bucket.
 */
public class GoldBucketItem extends BaseBucketItem {
    public static final int MAX_USES = 32;

    private final Supplier<? extends BucketItem> lavaFilled;

    public GoldBucketItem(
            Fluid content,
            Properties properties,
            Supplier<? extends BucketItem> waterFilled,
            Supplier<? extends BucketItem> lavaFilled,
            Supplier<? extends BucketItem> emptyCounterpart) {
        super(content, properties.durability(MAX_USES), waterFilled, emptyCounterpart);
        this.lavaFilled = lavaFilled;
    }

    @Override
    protected int maxUses() {
        return MAX_USES;
    }

    @Override
    protected boolean isFluidAllowed(Fluid fluid) {
        return fluid == Fluids.WATER || fluid == Fluids.LAVA;
    }

    @Override
    protected ItemStack buildFilledResult(ItemStack stack, Fluid fluid) {
        if (fluid == Fluids.LAVA) {
            ItemStack filled = new ItemStack(lavaFilled.get());
            copyState(stack, filled);
            return filled;
        }
        return toFilled(stack);
    }

    @Override
    protected boolean canSolidPickup(BlockState state) {
        return state.getBlock() instanceof PowderSnowBlock;
    }

    @Override
    protected ItemStack buildSolidResult(ItemStack stack) {
        ItemStack filled = new ItemStack(ModItems.GOLD_POWDER_SNOW_BUCKET.get());
        copyState(stack, filled);
        return filled;
    }
}
