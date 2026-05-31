package com.bucketsupdate.fabric;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Supplier;

/**
 * Powder-snow-filled copper bucket. Placing the snow is genuine {@link SolidBucketItem}
 * (block-placement) behaviour; we only override the empty-out so the player gets the
 * empty {@link CopperBucketItem} back instead of vanilla's iron bucket. Copper has no
 * durability (it's permanent like iron), so there's no wear to track. Scooping is
 * handled on the empty copper bucket's fill flow (see {@link CopperBucketItem}).
 */
public class CopperPowderSnowBucketItem extends SolidBucketItem {
    private final Supplier<? extends Item> emptyCounterpart;

    public CopperPowderSnowBucketItem(Properties properties, Supplier<? extends Item> emptyCounterpart) {
        super(Blocks.POWDER_SNOW, SoundEvents.BUCKET_EMPTY_POWDER_SNOW, properties);
        this.emptyCounterpart = emptyCounterpart;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        // super places the powder snow block and swaps the hand to a vanilla bucket.
        InteractionResult result = super.useOn(context);
        if (player == null || !result.consumesAction() || player.getAbilities().instabuild) {
            return result;
        }
        player.setItemInHand(context.getHand(), new ItemStack(emptyCounterpart.get()));
        return result;
    }
}
