package com.bucketsupdate.fabric;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Supplier;

/**
 * Powder-snow-filled gold bucket. Unlike the copper variant (permanent, no wear),
 * this one has 32 uses shared with the rest of the gold bucket family. Scooping
 * is handled by {@link GoldBucketItem#canSolidPickup}; this class only handles
 * placement and the resulting wear.
 */
public class GoldPowderSnowBucketItem extends SolidBucketItem {
    public static final int MAX_USES = GoldBucketItem.MAX_USES;
    private final Supplier<? extends Item> emptyCounterpart;

    public GoldPowderSnowBucketItem(Properties properties, Supplier<? extends Item> emptyCounterpart) {
        super(Blocks.POWDER_SNOW, SoundEvents.BUCKET_EMPTY_POWDER_SNOW, properties.durability(MAX_USES));
        this.emptyCounterpart = emptyCounterpart;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return super.useOn(context);

        // Snapshot damage before super replaces the held item with a vanilla bucket.
        int currentDamage = player.getItemInHand(context.getHand()).getDamageValue();

        InteractionResult result = super.useOn(context);
        if (!result.consumesAction() || player.getAbilities().instabuild) {
            return result;
        }

        int newDamage = currentDamage + 1;
        if (newDamage >= MAX_USES) {
            player.setItemInHand(context.getHand(), ItemStack.EMPTY);
            Level level = context.getLevel();
            if (!level.isClientSide()) {
                level.playSound(null, player.blockPosition(),
                        SoundEvents.WOOD_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        } else {
            ItemStack empty = new ItemStack(emptyCounterpart.get());
            empty.set(DataComponents.DAMAGE, newDamage);
            player.setItemInHand(context.getHand(), empty);
        }
        return result;
    }
}
