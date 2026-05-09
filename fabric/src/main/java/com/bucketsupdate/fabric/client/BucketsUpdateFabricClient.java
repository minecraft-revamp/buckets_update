package com.bucketsupdate.fabric.client;

import com.bucketsupdate.fabric.BucketsUpdateFabric;
import net.fabricmc.api.ClientModInitializer;

public class BucketsUpdateFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BucketsUpdateFabric.LOGGER.info("Buckets Update (Fabric) client setup");
    }
}
