package com.bucketsupdate.client;

import com.bucketsupdate.BucketsUpdate;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/** Client-only entry point. Item model variants are wired up via JSON model definitions. */
@Mod(value = BucketsUpdate.MOD_ID, dist = Dist.CLIENT)
public class BucketsUpdateClient {
    public BucketsUpdateClient(ModContainer container) {
        BucketsUpdate.LOGGER.info("Minecraft 2.0 client setup");
    }
}
