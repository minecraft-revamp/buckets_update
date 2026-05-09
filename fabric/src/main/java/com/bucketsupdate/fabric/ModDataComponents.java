package com.bucketsupdate.fabric;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class ModDataComponents {
    public static final DataComponentType<OxidationStage> OXIDATION_STAGE =
            Registry.register(
                    BuiltInRegistries.DATA_COMPONENT_TYPE,
                    Identifier.fromNamespaceAndPath(BucketsUpdateFabric.MOD_ID, "oxidation_stage"),
                    DataComponentType.<OxidationStage>builder()
                            .persistent(OxidationStage.CODEC)
                            .networkSynchronized(OxidationStage.STREAM_CODEC)
                            .build());

    private ModDataComponents() {}

    public static void bootstrap() {
        // touching the class triggers static init → registration
    }
}
