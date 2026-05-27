package com.bucketsupdate.feature.buckets;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class WoodenMilkBucketItem extends BaseMilkBucketItem {
    public WoodenMilkBucketItem(Properties properties, Supplier<? extends Item> emptyCounterpart) {
        super(properties.durability(WoodenBucketItem.MAX_USES), emptyCounterpart);
    }

    @Override
    protected void copyState(ItemStack from, ItemStack to) {
        Integer damage = from.get(DataComponents.DAMAGE);
        if (damage != null) {
            to.set(DataComponents.DAMAGE, damage);
        }
    }

    @Override
    protected ItemStack finalizeDrink(ItemStack drunk, ItemStack empty, Level level, Player player) {
        int newDamage = drunk.getDamageValue() + 1;
        if (newDamage >= WoodenBucketItem.MAX_USES) {
            if (level instanceof ServerLevel sl) {
                sl.playSound(null, player.blockPosition(),
                        SoundEvents.WOOD_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            return ItemStack.EMPTY;
        }
        empty.set(DataComponents.DAMAGE, newDamage);
        return empty;
    }
}
