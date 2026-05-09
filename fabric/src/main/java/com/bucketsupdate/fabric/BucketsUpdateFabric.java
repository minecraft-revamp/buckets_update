package com.bucketsupdate.fabric;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

public class BucketsUpdateFabric implements ModInitializer {
    public static final String MOD_ID = "buckets_update";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        // Class-load triggers force the static initializers to run, performing registration.
        ModDataComponents.bootstrap();
        ModItems.bootstrap();
        ModCreativeTabs.bootstrap();

        BucketEvents.register();

        LOGGER.info("Buckets Update (Fabric) initialised");
    }
}
