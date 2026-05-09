package com.bucketsupdate.feature.buckets;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum OxidationStage implements StringRepresentable {
    UNOXIDIZED("unoxidized"),
    EXPOSED("exposed"),
    WEATHERED("weathered"),
    OXIDIZED("oxidized");

    public static final Codec<OxidationStage> CODEC = StringRepresentable.fromEnum(OxidationStage::values);
    public static final StreamCodec<ByteBuf, OxidationStage> STREAM_CODEC =
            ByteBufCodecs.idMapper(i -> values()[i], OxidationStage::ordinal);

    private final String name;

    OxidationStage(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public OxidationStage next() {
        int idx = ordinal();
        return idx + 1 < values().length ? values()[idx + 1] : this;
    }

    public OxidationStage previous() {
        int idx = ordinal();
        return idx > 0 ? values()[idx - 1] : this;
    }

    public boolean isOxidized() {
        return this == OXIDIZED;
    }

    public boolean canScrub() {
        return this != UNOXIDIZED;
    }
}
