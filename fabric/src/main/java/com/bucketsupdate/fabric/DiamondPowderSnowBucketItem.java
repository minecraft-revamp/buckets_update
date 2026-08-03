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

public class DiamondPowderSnowBucketItem extends SolidBucketItem {
    private final Supplier<? extends Item> emptyCounterpart;

    public DiamondPowderSnowBucketItem(Properties properties, Supplier<? extends Item> emptyCounterpart) {
        super(Blocks.POWDER_SNOW, SoundEvents.BUCKET_EMPTY_POWDER_SNOW, properties);
        this.emptyCounterpart = emptyCounterpart;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        InteractionResult result = super.useOn(context);
        if (player == null || !result.consumesAction() || player.getAbilities().instabuild) {
            return result;
        }
        player.setItemInHand(context.getHand(), new ItemStack(emptyCounterpart.get()));
        return result;
    }
}
