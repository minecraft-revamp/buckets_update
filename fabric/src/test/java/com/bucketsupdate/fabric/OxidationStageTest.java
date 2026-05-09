package com.bucketsupdate.fabric;

import org.junit.jupiter.api.Test;

import static com.bucketsupdate.fabric.OxidationStage.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the OxidationStage enum (pure logic, no MC runtime needed).
 * Mirrors the NeoForge tests at neoforge/src/test/java/.../OxidationStageTest.java.
 */
class OxidationStageTest {

    @Test
    void next_advances_one_stage() {
        assertEquals(EXPOSED, UNOXIDIZED.next());
        assertEquals(WEATHERED, EXPOSED.next());
        assertEquals(OXIDIZED, WEATHERED.next());
    }

    @Test
    void next_clamps_at_oxidized() {
        assertEquals(OXIDIZED, OXIDIZED.next());
    }

    @Test
    void previous_recedes_one_stage() {
        assertEquals(WEATHERED, OXIDIZED.previous());
        assertEquals(EXPOSED, WEATHERED.previous());
        assertEquals(UNOXIDIZED, EXPOSED.previous());
    }

    @Test
    void previous_clamps_at_unoxidized() {
        assertEquals(UNOXIDIZED, UNOXIDIZED.previous());
    }

    @Test
    void canScrub_only_for_oxidised_stages() {
        assertFalse(UNOXIDIZED.canScrub());
        assertTrue(EXPOSED.canScrub());
        assertTrue(WEATHERED.canScrub());
        assertTrue(OXIDIZED.canScrub());
    }

    @Test
    void isOxidized_only_for_full_oxidation() {
        assertFalse(UNOXIDIZED.isOxidized());
        assertFalse(EXPOSED.isOxidized());
        assertFalse(WEATHERED.isOxidized());
        assertTrue(OXIDIZED.isOxidized());
    }

    @Test
    void serialized_names_match_lowercase_enum() {
        assertEquals("unoxidized", UNOXIDIZED.getSerializedName());
        assertEquals("exposed", EXPOSED.getSerializedName());
        assertEquals("weathered", WEATHERED.getSerializedName());
        assertEquals("oxidized", OXIDIZED.getSerializedName());
    }

    @Test
    void roundtrip_next_then_previous_returns_to_origin_for_middle_stages() {
        assertEquals(EXPOSED, EXPOSED.next().previous());
        assertEquals(WEATHERED, WEATHERED.next().previous());
        assertEquals(EXPOSED, EXPOSED.previous().next());
    }
}
