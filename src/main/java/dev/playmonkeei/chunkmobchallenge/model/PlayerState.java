package dev.playmonkeei.chunkmobchallenge.model;

import java.util.UUID;

public final class PlayerState {
    public final UUID playerId;
    public ChunkKey roundChunk;
    public ChunkKey lastChunk;
    public String worldKey;
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public boolean pendingRespawn;

    public PlayerState(UUID playerId) {
        this.playerId = playerId;
    }
}
