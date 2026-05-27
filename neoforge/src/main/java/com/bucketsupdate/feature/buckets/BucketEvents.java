package com.bucketsupdate.feature.buckets;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.bucketsupdate.BucketsUpdate;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ModifyRecipeJsonsEvent;

/**
 * Overrides the vanilla iron bucket recipe at runtime — the only remaining
 * bucket-related event hook now that oxidation/wax/scrub have been removed.
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
}
