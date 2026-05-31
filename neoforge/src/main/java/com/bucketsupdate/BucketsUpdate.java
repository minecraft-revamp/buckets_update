package com.bucketsupdate;

import com.bucketsupdate.feature.buckets.BucketEvents;
import com.bucketsupdate.feature.buckets.MilkEvents;
import com.bucketsupdate.registry.ModCreativeTabs;
import com.bucketsupdate.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(BucketsUpdate.MOD_ID)
public class BucketsUpdate {
    public static final String MOD_ID = "buckets_update";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BucketsUpdate(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.register(BucketEvents.class);
        NeoForge.EVENT_BUS.register(MilkEvents.class);

        LOGGER.info("Bucketry initialised");
    }
}
