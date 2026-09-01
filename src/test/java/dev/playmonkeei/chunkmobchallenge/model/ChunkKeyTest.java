package dev.playmonkeei.chunkmobchallenge.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ChunkKeyTest {
    @Test
    void encodingRoundTripsNamespacedWorldAndNegativeCoordinates() {
        ChunkKey original = new ChunkKey("minecraft:the_nether", -12, 45);
        assertEquals(original, ChunkKey.decode(original.encoded()));
    }

    @Test
    void chunkCenterIsExact() {
        ChunkKey key = new ChunkKey("minecraft:overworld", -1, 2);
        assertEquals(-8.0, key.centerX());
        assertEquals(40.0, key.centerZ());
    }
}
