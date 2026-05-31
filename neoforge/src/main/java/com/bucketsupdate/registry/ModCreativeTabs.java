package com.bucketsupdate.registry;

import com.bucketsupdate.BucketsUpdate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BucketsUpdate.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB =
            CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.buckets_update.main"))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .icon(() -> ModItems.COPPER_BUCKET.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.WOODEN_BUCKET.get());
                        output.accept(ModItems.WOODEN_WATER_BUCKET.get());
                        output.accept(ModItems.WOODEN_MILK_BUCKET.get());
                        output.accept(ModItems.BAMBOO_BUCKET.get());
                        output.accept(ModItems.BAMBOO_WATER_BUCKET.get());
                        output.accept(ModItems.BAMBOO_MILK_BUCKET.get());
                        output.accept(ModItems.COPPER_BUCKET.get());
                        output.accept(ModItems.COPPER_WATER_BUCKET.get());
                        output.accept(ModItems.COPPER_MILK_BUCKET.get());
                        output.accept(ModItems.COPPER_POWDER_SNOW_BUCKET.get());
                    }).build());

    private ModCreativeTabs() {}
}
