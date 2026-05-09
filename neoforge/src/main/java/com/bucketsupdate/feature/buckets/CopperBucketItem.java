package com.bucketsupdate.feature.buckets;

import com.bucketsupdate.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

public class CopperBucketItem extends BaseBucketItem {
    /** Probability of advancing one stage per use. ~30 uses expected before reaching OXIDIZED. */
    public static final float OXIDATION_CHANCE_PER_USE = 0.10F;

    public CopperBucketItem(
            Fluid content,
            Properties properties,
            Supplier<? extends BucketItem> filledCounterpart,
            Supplier<? extends BucketItem> emptyCounterpart) {
        super(content, properties, filledCounterpart, emptyCounterpart);
    }

    public static OxidationStage getStage(ItemStack stack) {
        OxidationStage stage = stack.get(ModDataComponents.OXIDATION_STAGE.get());
        return stage == null ? OxidationStage.UNOXIDIZED : stage;
    }

    @Override
    protected void copyState(ItemStack from, ItemStack to) {
        OxidationStage stage = from.get(ModDataComponents.OXIDATION_STAGE.get());
        if (stage != null) {
            to.set(ModDataComponents.OXIDATION_STAGE.get(), stage);
        }
    }

    @Override
    protected boolean canUseFor(ItemStack stack, Player player, boolean fillingAction) {
        if (fillingAction && getStage(stack).isOxidized()) {
            player.sendOverlayMessage(Component.translatable("item.buckets_update.copper_bucket.too_rusty"));
            return false;
        }
        return true;
    }

    @Override
    protected void applyWear(ItemStack stack, Level level, Player player, boolean fillingAction) {
        if (player.getAbilities().instabuild) return;
        OxidationStage stage = getStage(stack);
        if (!stage.isOxidized() && level.getRandom().nextFloat() < OXIDATION_CHANCE_PER_USE) {
            stack.set(ModDataComponents.OXIDATION_STAGE.get(), stage.next());
        }
    }
}
