package dev.playmonkeei.chunkmobchallenge.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class RunSession {
    public UUID id = UUID.randomUUID();
    public RunStatus status = RunStatus.RUNNING;
    public UUID leader;
    public long accumulatedMillis;
    public long timerStartedAt;
    public int deaths;
    public int defeatedMobs;
    public int startedRounds;
    public boolean nameTagsVisible;
    public boolean glowing;
    public final Map<ChunkKey, RoundState> rounds = new LinkedHashMap<>();
    public final Map<UUID, PlayerState> players = new LinkedHashMap<>();

    public boolean running() {
        return status == RunStatus.RUNNING;
    }

    public void resumeTimer(long now) {
        if (timerStartedAt == 0) {
            timerStartedAt = now;
        }
    }

    public void pauseTimer(long now) {
        if (timerStartedAt != 0) {
            accumulatedMillis += Math.max(0, now - timerStartedAt);
            timerStartedAt = 0;
        }
    }

    public long activeMillis(long now) {
        return accumulatedMillis + (timerStartedAt == 0 ? 0 : Math.max(0, now - timerStartedAt));
    }
}
