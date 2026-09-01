package dev.playmonkeei.chunkmobchallenge.model;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.boss.BossBar;

public final class RoundState {
    public final UUID id;
    public final ChunkKey chunk;
    public RoundPhase phase;
    public String entityTypeKey;
    public UUID mobUuid;
    public final Set<UUID> participants = new LinkedHashSet<>();
    public MobSnapshot snapshot = new MobSnapshot();
    public transient BossBar bossBar;

    public RoundState(UUID id, ChunkKey chunk, RoundPhase phase, String entityTypeKey) {
        this.id = id;
        this.chunk = chunk;
        this.phase = phase;
        this.entityTypeKey = entityTypeKey;
    }
}
