package com.bucketsupdate.fabric;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;

public final class ModCreativeTabs {
    public static final CreativeModeTab MAIN_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(BucketsUpdateFabric.MOD_ID, "main"),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 7)
                    .title(Component.translatable("itemGroup.buckets_update.main"))
                    .icon(() -> ModItems.COPPER_BUCKET.getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.WOODEN_BUCKET);
                        output.accept(ModItems.WOODEN_WATER_BUCKET);
                        output.accept(ModItems.WOODEN_MILK_BUCKET);
                        output.accept(ModItems.COPPER_BUCKET);
                        output.accept(ModItems.COPPER_WATER_BUCKET);
                        output.accept(ModItems.COPPER_MILK_BUCKET);
                        output.accept(ModItems.COPPER_POWDER_SNOW_BUCKET);
                    })
                    .build());

    private ModCreativeTabs() {}

    public static void bootstrap() {}
}
