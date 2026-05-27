package com.bucketsupdate.registry;

import com.bucketsupdate.BucketsUpdate;
import com.bucketsupdate.feature.buckets.CopperBucketItem;
import com.bucketsupdate.feature.buckets.CopperMilkBucketItem;
import com.bucketsupdate.feature.buckets.WoodenBucketItem;
import com.bucketsupdate.feature.buckets.WoodenMilkBucketItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BucketsUpdate.MOD_ID);

    // ---- Wooden bucket family ----
    public static final DeferredItem<WoodenBucketItem> WOODEN_BUCKET = ITEMS.registerItem(
            "wooden_bucket",
            props -> new WoodenBucketItem(
                    Fluids.EMPTY,
                    props.stacksTo(1),
                    () -> ModItems.WOODEN_WATER_BUCKET.get(),
                    () -> ModItems.WOODEN_BUCKET.get()));

    public static final DeferredItem<WoodenBucketItem> WOODEN_WATER_BUCKET = ITEMS.registerItem(
            "wooden_water_bucket",
            props -> new WoodenBucketItem(
                    Fluids.WATER,
                    props.stacksTo(1).craftRemainder(ModItems.WOODEN_BUCKET.get()),
                    () -> ModItems.WOODEN_WATER_BUCKET.get(),
                    () -> ModItems.WOODEN_BUCKET.get()));

    public static final DeferredItem<WoodenMilkBucketItem> WOODEN_MILK_BUCKET = ITEMS.registerItem(
            "wooden_milk_bucket",
            props -> new WoodenMilkBucketItem(
                    props.stacksTo(1).component(DataComponents.CONSUMABLE, Consumables.MILK_BUCKET),
                    () -> ModItems.WOODEN_BUCKET.get()));

    // ---- Copper bucket family ----
    public static final DeferredItem<CopperBucketItem> COPPER_BUCKET = ITEMS.registerItem(
            "copper_bucket",
            props -> new CopperBucketItem(
                    Fluids.EMPTY,
                    props.stacksTo(1),
                    () -> ModItems.COPPER_WATER_BUCKET.get(),
                    () -> ModItems.COPPER_BUCKET.get()));

    public static final DeferredItem<CopperBucketItem> COPPER_WATER_BUCKET = ITEMS.registerItem(
            "copper_water_bucket",
            props -> new CopperBucketItem(
                    Fluids.WATER,
                    props.stacksTo(1).craftRemainder(ModItems.COPPER_BUCKET.get()),
                    () -> ModItems.COPPER_WATER_BUCKET.get(),
                    () -> ModItems.COPPER_BUCKET.get()));

    public static final DeferredItem<CopperMilkBucketItem> COPPER_MILK_BUCKET = ITEMS.registerItem(
            "copper_milk_bucket",
            props -> new CopperMilkBucketItem(
                    props.stacksTo(1).component(DataComponents.CONSUMABLE, Consumables.MILK_BUCKET),
                    () -> ModItems.COPPER_BUCKET.get()));

    private ModItems() {}
}
