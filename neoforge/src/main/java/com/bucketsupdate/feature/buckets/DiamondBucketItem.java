package com.bucketsupdate.feature.buckets;

import com.bucketsupdate.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class DiamondBucketItem extends BaseBucketItem {
    private final Supplier<? extends BucketItem> waterFilled;
    private final Supplier<? extends BucketItem> lavaFilled;

    public DiamondBucketItem(
            Fluid content,
            Properties properties,
            Supplier<? extends BucketItem> waterFilled,
            Supplier<? extends BucketItem> lavaFilled,
            Supplier<? extends BucketItem> emptyCounterpart) {
        super(content, properties, waterFilled, emptyCounterpart);
        this.waterFilled = waterFilled;
        this.lavaFilled = lavaFilled;
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
        ItemStack filled = new ItemStack(ModItems.DIAMOND_POWDER_SNOW_BUCKET.get());
        copyState(stack, filled);
        return filled;
    }



    @Override
    protected InteractionResult doEmpty(Level level, Player player, ItemStack held, BlockHitResult hit, BlockPos pos, BlockPos relPos) {
        if (!canUseFor(held, player, false)) {
            return InteractionResult.FAIL;
        }
        BlockState clicked = level.getBlockState(pos);
        
        // NeoForge signature/method checks
        boolean canPlaceInside = canBlockContainFluid(player, level, pos, clicked) && this.content == Fluids.WATER;
        BlockPos placePos = canPlaceInside ? pos : relPos;

        // Custom Enchantments check (Infinity and Conservation)
        int infinityLevel = getEnchantmentLevel(level, held, "fluid_infinity");
        int conservationLevel = getEnchantmentLevel(level, held, "conservation");

        boolean useInfinity = false;
        int xpCost = 0;
        if (infinityLevel > 0) {
            if (this.content == Fluids.LAVA) {
                if (infinityLevel == 1) xpCost = 20;
                else if (infinityLevel == 2) xpCost = 10;
                else xpCost = 5;
            } else {
                if (infinityLevel == 1) xpCost = 5;
                else if (infinityLevel == 2) xpCost = 3;
                else xpCost = 1;
            }
        }

        if (!level.isClientSide() && infinityLevel > 0 && (player.getAbilities().instabuild || player.totalExperience >= xpCost)) {
            useInfinity = true;
        }

        boolean useConservation = false;
        if (!level.isClientSide() && conservationLevel > 0 && level.getRandom().nextFloat() < (conservationLevel * 0.15f)) {
            useConservation = true;
        }

        if (!emptyContents(player, level, placePos, hit, held)) {
            return InteractionResult.FAIL;
        }

        applyWear(held, level, player, false);
        checkExtraContent(player, level, held, placePos);
        player.awardStat(Stats.ITEM_USED.get(this));

        if (!level.isClientSide() && (useInfinity || useConservation)) {
            if (useInfinity && !player.getAbilities().instabuild) {
                player.giveExperiencePoints(-xpCost);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 0.5F);
            } else if (useConservation) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.2F);
            }
            return finishUseWithResult(held, player, held.copy());
        }

        if (!level.isClientSide() && infinityLevel > 0 && !useInfinity) {
            player.sendOverlayMessage(Component.translatable("item.buckets_update.diamond_bucket.no_xp"));
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.PLAYERS, 1.0F, 0.5F);
        }

        return finishUseWithResult(held, player, buildResult(held, false));
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, Entity entity, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.EquipmentSlot slot) {
        if (entity instanceof LivingEntity living) {
            if (slot == net.minecraft.world.entity.EquipmentSlot.MAINHAND || slot == net.minecraft.world.entity.EquipmentSlot.OFFHAND) {
                if (this.content == Fluids.LAVA) {
                    int shieldLevel = getEnchantmentLevel(level, stack, "thermal_shield");
                    if (shieldLevel > 0) {
                        living.addEffect(new MobEffectInstance(
                                MobEffects.FIRE_RESISTANCE,
                                220, // resets every tick to maintain 11s duration
                                0,
                                false,
                                false,
                                true
                        ));
                    }
                }
            }
        }
    }



    // Helper to extract enchantment level safely without crash during startup/sync
    protected static int getEnchantmentLevel(Level level, ItemStack stack, String id) {
        if (stack.isEmpty()) return 0;
        try {
            var registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            var key = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("buckets_update", id));
            var holder = registry.get(key);
            if (holder.isPresent()) {
                return EnchantmentHelper.getItemEnchantmentLevel(holder.get(), stack);
            }
        } catch (Exception e) {
            // Safe fallback
        }
        return 0;
    }
}
