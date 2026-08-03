package com.bucketsupdate.registry;

import com.bucketsupdate.BucketsUpdate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
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
                        output.accept(ModItems.GOLD_BUCKET.get());
                        output.accept(ModItems.GOLD_WATER_BUCKET.get());
                        output.accept(ModItems.GOLD_LAVA_BUCKET.get());
                        output.accept(ModItems.GOLD_MILK_BUCKET.get());
                        output.accept(ModItems.GOLD_POWDER_SNOW_BUCKET.get());
                        output.accept(ModItems.DIAMOND_BUCKET.get());
                        output.accept(ModItems.DIAMOND_WATER_BUCKET.get());
                        output.accept(ModItems.DIAMOND_LAVA_BUCKET.get());
                        output.accept(ModItems.DIAMOND_MILK_BUCKET.get());
                        output.accept(ModItems.DIAMOND_POWDER_SNOW_BUCKET.get());
                        
                        try {
                            var enchantments = parameters.holders().lookupOrThrow(Registries.ENCHANTMENT);
                            
                            var infinityKey = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("buckets_update", "fluid_infinity"));
                            var conservationKey = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("buckets_update", "conservation"));
                            var thermalShieldKey = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("buckets_update", "thermal_shield"));
                            
                            // Add enchanted books
                            enchantments.get(infinityKey).ifPresent(holder -> {
                                for (int level = 1; level <= 3; level++) {
                                    output.accept(EnchantmentHelper.createBook(new EnchantmentInstance(holder, level)));
                                }
                            });
                            enchantments.get(conservationKey).ifPresent(holder -> {
                                for (int level = 1; level <= 3; level++) {
                                    output.accept(EnchantmentHelper.createBook(new EnchantmentInstance(holder, level)));
                                }
                            });
                            enchantments.get(thermalShieldKey).ifPresent(holder -> {
                                output.accept(EnchantmentHelper.createBook(new EnchantmentInstance(holder, 1)));
                            });

                            // Add pre-enchanted diamond buckets for quick testing
                            enchantments.get(infinityKey).ifPresent(infinityHolder -> {
                                enchantments.get(thermalShieldKey).ifPresent(thermalHolder -> {
                                    ItemStack infinityShieldStack = new ItemStack(ModItems.DIAMOND_BUCKET.get());
                                    infinityShieldStack.enchant(infinityHolder, 3);
                                    infinityShieldStack.enchant(thermalHolder, 1);
                                    output.accept(infinityShieldStack);
                                });
                            });
                            enchantments.get(conservationKey).ifPresent(conservationHolder -> {
                                enchantments.get(thermalShieldKey).ifPresent(thermalHolder -> {
                                    ItemStack conservationShieldStack = new ItemStack(ModItems.DIAMOND_BUCKET.get());
                                    conservationShieldStack.enchant(conservationHolder, 3);
                                    conservationShieldStack.enchant(thermalHolder, 1);
                                    output.accept(conservationShieldStack);
                                });
                            });
                        } catch (Exception e) {
                            // Safe fallback
                        }
                    }).build());

    private ModCreativeTabs() {}
}
