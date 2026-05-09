package com.bucketsupdate.fabric;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluids;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Fabric-style item registration. Each item is registered with its id pre-set on Item.Properties,
 * which is required since MC 1.21.4+. Counterparts are wired via lazy suppliers so cross-references
 * resolve correctly regardless of declaration order.
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

    public static final WaxedCopperBucketItem WAXED_COPPER_BUCKET = registerItem(
            "waxed_copper_bucket",
            props -> new WaxedCopperBucketItem(
                    Fluids.EMPTY,
                    props.stacksTo(1),
                    () -> ModItems.WAXED_COPPER_WATER_BUCKET,
                    () -> ModItems.WAXED_COPPER_BUCKET));

    public static final WaxedCopperBucketItem WAXED_COPPER_WATER_BUCKET = registerItem(
            "waxed_copper_water_bucket",
            props -> new WaxedCopperBucketItem(
                    Fluids.WATER,
                    props.stacksTo(1).craftRemainder(ModItems.WAXED_COPPER_BUCKET),
                    () -> ModItems.WAXED_COPPER_WATER_BUCKET,
                    () -> ModItems.WAXED_COPPER_BUCKET));

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
