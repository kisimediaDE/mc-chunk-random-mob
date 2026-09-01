package dev.playmonkeei.chunkmobchallenge.model;

import java.util.ArrayList;
import java.util.List;

public final class MobSnapshot {
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public double health;
    public int fireTicks;
    public int remainingAir;
    public boolean gravity = true;
    public boolean ai = true;
    public boolean invulnerable;
    public double velocityX;
    public double velocityY;
    public double velocityZ;
    public final List<EffectSnapshot> effects = new ArrayList<>();
}
