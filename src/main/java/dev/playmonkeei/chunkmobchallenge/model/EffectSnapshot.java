package dev.playmonkeei.chunkmobchallenge.model;

public record EffectSnapshot(
        String typeKey,
        int duration,
        int amplifier,
        boolean ambient,
        boolean particles,
        boolean icon
) {}
