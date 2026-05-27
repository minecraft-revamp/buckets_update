package com.bucketsupdate.fabric;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/**
 * Copper milk bucket: same durability model as the empty copper bucket (190 uses
 * shared between fills + drinks). On the drink that pushes damage past MAX_USES,
 * the bucket breaks instead of returning the empty counterpart.
 */
public class CopperMilkBucketItem extends BaseMilkBucketItem {
    public CopperMilkBucketItem(Properties properties, Supplier<? extends Item> emptyCounterpart) {
        super(properties.durability(CopperBucketItem.MAX_USES), emptyCounterpart);
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
        if (newDamage >= CopperBucketItem.MAX_USES) {
            if (level instanceof ServerLevel sl) {
                sl.playSound(null, player.blockPosition(),
                        SoundEvents.COPPER_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            return ItemStack.EMPTY;
        }
        empty.set(DataComponents.DAMAGE, newDamage);
        return empty;
    }
}
