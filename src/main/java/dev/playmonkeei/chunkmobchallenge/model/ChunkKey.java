package dev.playmonkeei.chunkmobchallenge.model;

import org.bukkit.Location;

public record ChunkKey(String worldKey, int x, int z) {
    public static ChunkKey from(Location location) {
        return new ChunkKey(location.getWorld().getKey().toString(),
                location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public double centerX() {
        return x * 16.0 + 8.0;
    }

    public double centerZ() {
        return z * 16.0 + 8.0;
    }

    public boolean contains(Location location) {
        return location.getWorld() != null
                && worldKey.equals(location.getWorld().getKey().toString())
                && (location.getBlockX() >> 4) == x
                && (location.getBlockZ() >> 4) == z;
    }

    public String encoded() {
        return worldKey + ";" + x + ";" + z;
    }

    public static ChunkKey decode(String value) {
        int last = value.lastIndexOf(';');
        int previous = value.lastIndexOf(';', last - 1);
        if (previous < 1 || last < 0) {
            throw new IllegalArgumentException("Ungültiger ChunkKey: " + value);
        }
        return new ChunkKey(value.substring(0, previous),
                Integer.parseInt(value.substring(previous + 1, last)),
                Integer.parseInt(value.substring(last + 1)));
    }
}
