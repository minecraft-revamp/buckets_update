package com.bucketsupdate.registry;

import com.bucketsupdate.BucketsUpdate;
import com.bucketsupdate.feature.buckets.OxidationStage;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, BucketsUpdate.MOD_ID);

    public static final Supplier<DataComponentType<OxidationStage>> OXIDATION_STAGE =
            DATA_COMPONENTS.registerComponentType("oxidation_stage", builder -> builder
                    .persistent(OxidationStage.CODEC)
                    .networkSynchronized(OxidationStage.STREAM_CODEC));

    private ModDataComponents() {}
}
