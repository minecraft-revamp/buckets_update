package com.bucketsupdate.fabric;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.material.Fluids;

import java.util.function.Function;

/**
 * Fabric-style item registration. Each item gets its id pre-set on Item.Properties
 * (required since MC 1.21.4+). Cross-references between paired items (empty/filled)
 * use lazy suppliers so declaration order doesn't matter; milk variants are
 * declared after their empty counterpart so the supplier returns a non-null value
 * by the time it's first invoked.
 */
public final class ModItems {
    public static final WoodenBucketItem WOODEN_BUCKET = registerItem(
            "wooden_bucket",
            props -> new WoodenBucketItem(
                    Fluids.EMPTY,
                    props.stacksTo(1),
                    () -> ModItems.WOODEN_WATER_BUCKET,
                    () -> ModItems.WOODEN_BUCKET));

    public static final WoodenBucketItem WOODEN_WATER_BUCKET = registerItem(
            "wooden_water_bucket",
            props -> new WoodenBucketItem(
                    Fluids.WATER,
                    props.stacksTo(1).craftRemainder(ModItems.WOODEN_BUCKET),
                    () -> ModItems.WOODEN_WATER_BUCKET,
                    () -> ModItems.WOODEN_BUCKET));

    public static final WoodenMilkBucketItem WOODEN_MILK_BUCKET = registerItem(
            "wooden_milk_bucket",
            props -> new WoodenMilkBucketItem(
                    props.stacksTo(1)
                            .component(DataComponents.CONSUMABLE, Consumables.MILK_BUCKET),
                    () -> ModItems.WOODEN_BUCKET));

    public static final CopperBucketItem COPPER_BUCKET = registerItem(
            "copper_bucket",
            props -> new CopperBucketItem(
                    Fluids.EMPTY,
                    props.stacksTo(1),
                    () -> ModItems.COPPER_WATER_BUCKET,
                    () -> ModItems.COPPER_BUCKET));

    public static final CopperBucketItem COPPER_WATER_BUCKET = registerItem(
            "copper_water_bucket",
            props -> new CopperBucketItem(
                    Fluids.WATER,
                    props.stacksTo(1).craftRemainder(ModItems.COPPER_BUCKET),
                    () -> ModItems.COPPER_WATER_BUCKET,
                    () -> ModItems.COPPER_BUCKET));

    public static final CopperMilkBucketItem COPPER_MILK_BUCKET = registerItem(
            "copper_milk_bucket",
            props -> new CopperMilkBucketItem(
                    props.stacksTo(1)
                            .component(DataComponents.CONSUMABLE, Consumables.MILK_BUCKET),
                    () -> ModItems.COPPER_BUCKET));

    public static final CopperPowderSnowBucketItem COPPER_POWDER_SNOW_BUCKET = registerItem(
            "copper_powder_snow_bucket",
            props -> new CopperPowderSnowBucketItem(
                    props.durability(CopperBucketItem.MAX_USES),
                    () -> ModItems.COPPER_BUCKET));

    private ModItems() {}

    private static <T extends Item> T registerItem(String name, Function<Item.Properties, T> factory) {
        Identifier id = Identifier.fromNamespaceAndPath(BucketsUpdateFabric.MOD_ID, name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        Item.Properties props = new Item.Properties().setId(key);
        T item = factory.apply(props);
        Registry.register(BuiltInRegistries.ITEM, id, item);
        return item;
    }

    public static void bootstrap() {
        // class-load trigger for static init
    }
}
