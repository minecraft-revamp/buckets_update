package com.bucketsupdate.feature.buckets;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.bucketsupdate.BucketsUpdate;
import com.bucketsupdate.registry.ModDataComponents;
import com.bucketsupdate.registry.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ModifyRecipeJsonsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Bucket-related event hooks:
 * <ul>
 *   <li>Wax / scrub a copper bucket via right-click with honeycomb / axe in the other hand.</li>
 *   <li>Override the vanilla iron bucket recipe at runtime.</li>
 * </ul>
 */
public final class BucketEvents {
    private BucketEvents() {}

    private static final Identifier IRON_BUCKET_ID = Identifier.withDefaultNamespace("bucket");

    /** New iron bucket pattern: 3 ingots in V + iron chain centred above. */
    private static final String IRON_BUCKET_RECIPE_JSON = """
            {
              "type": "minecraft:crafting_shaped",
              "category": "misc",
              "pattern": [" H ", "I I", " I "],
              "key": {
                "H": "minecraft:iron_chain",
                "I": "minecraft:iron_ingot"
              },
              "result": {"id": "minecraft:bucket", "count": 1}
            }
            """;

    @SubscribeEvent
    public static void onModifyRecipeJsons(ModifyRecipeJsonsEvent event) {
        if (event.getRecipeJsons().containsKey(IRON_BUCKET_ID)) {
            JsonElement replacement = JsonParser.parseString(IRON_BUCKET_RECIPE_JSON);
            event.getRecipeJsons().put(IRON_BUCKET_ID, replacement);
            BucketsUpdate.LOGGER.info("Overrode vanilla {} recipe with chain pattern", IRON_BUCKET_ID);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        // We process from the main-hand event so we always see both stacks together;
        // returning early on the off-hand event prevents double-processing.
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player player = event.getEntity();
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        // Locate the copper bucket and the tool, accepting either hand order.
        ItemStack bucket;
        ItemStack tool;
        InteractionHand bucketHand;
        if (isCopperBucket(mainHand)) {
            bucket = mainHand;
            tool = offHand;
            bucketHand = InteractionHand.MAIN_HAND;
        } else if (isCopperBucket(offHand)) {
            bucket = offHand;
            tool = mainHand;
            bucketHand = InteractionHand.OFF_HAND;
        } else {
            return;
        }

        boolean handled;
        if (tool.is(Items.HONEYCOMB)) {
            handled = applyWax(player, bucket, bucketHand, tool);
        } else if (tool.getItem() instanceof AxeItem) {
            handled = applyScrub(player, bucket, tool);
        } else {
            return;
        }

        if (handled) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    private static boolean applyWax(Player player, ItemStack bucket, InteractionHand bucketHand, ItemStack honeycomb) {
        boolean filled = bucket.getItem() == ModItems.COPPER_WATER_BUCKET.get();
        ItemStack waxed = new ItemStack(filled
                ? ModItems.WAXED_COPPER_WATER_BUCKET.get()
                : ModItems.WAXED_COPPER_BUCKET.get());
        OxidationStage stage = bucket.get(ModDataComponents.OXIDATION_STAGE.get());
        if (stage != null) {
            waxed.set(ModDataComponents.OXIDATION_STAGE.get(), stage);
        }
        if (!player.getAbilities().instabuild) {
            honeycomb.shrink(1);
        }
        player.setItemInHand(bucketHand, waxed);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.HONEYCOMB_WAX_ON, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private static boolean applyScrub(Player player, ItemStack bucket, ItemStack axe) {
        OxidationStage stage = bucket.get(ModDataComponents.OXIDATION_STAGE.get());
        if (stage == null || !stage.canScrub()) return false;

        bucket.set(ModDataComponents.OXIDATION_STAGE.get(), stage.previous());

        if (!player.getAbilities().instabuild && player.level() instanceof ServerLevel serverLevel) {
            axe.hurtAndBreak(1, serverLevel,
                    player instanceof ServerPlayer sp ? sp : null,
                    brokenItem -> {});
        }
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.AXE_SCRAPE, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private static boolean isCopperBucket(ItemStack stack) {
        return stack.getItem() == ModItems.COPPER_BUCKET.get()
                || stack.getItem() == ModItems.COPPER_WATER_BUCKET.get();
    }
}
