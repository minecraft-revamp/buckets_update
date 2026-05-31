package com.bucketsupdate.feature.buckets;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
 * (block-placement) behaviour; we only override the empty-out: instead of vanilla's iron
 * bucket, the player gets the empty {@link CopperBucketItem} back, carrying one more point
 * of vanilla durability (breaking with the copper sound at {@link CopperBucketItem#MAX_USES}).
 * Scooping is handled on the empty copper bucket's fill flow (see {@link CopperBucketItem}).
 * Durability is set in {@link com.bucketsupdate.registry.ModItems}, giving the vanilla bar
 * and crafting-grid repair.
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
        ItemStack before = context.getItemInHand().copy();
        // super places the powder snow block and swaps the hand to a vanilla bucket.
        InteractionResult result = super.useOn(context);
        if (player == null || !result.consumesAction() || player.getAbilities().instabuild) {
            return result;
        }

        int newDamage = before.getDamageValue() + 1;
        ItemStack replacement;
        if (newDamage >= CopperBucketItem.MAX_USES) {
            if (player.level() instanceof ServerLevel sl) {
                sl.playSound(null, player.blockPosition(),
                        SoundEvents.COPPER_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            replacement = ItemStack.EMPTY;
        } else {
            replacement = new ItemStack(emptyCounterpart.get());
            replacement.set(DataComponents.DAMAGE, newDamage);
        }
        player.setItemInHand(context.getHand(), replacement);
        return result;
    }
}
