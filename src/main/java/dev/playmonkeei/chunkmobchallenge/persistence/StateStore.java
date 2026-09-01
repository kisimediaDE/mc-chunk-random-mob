package dev.playmonkeei.chunkmobchallenge.persistence;

import dev.playmonkeei.chunkmobchallenge.model.ChunkKey;
import dev.playmonkeei.chunkmobchallenge.model.EffectSnapshot;
import dev.playmonkeei.chunkmobchallenge.model.MobSnapshot;
import dev.playmonkeei.chunkmobchallenge.model.PlayerState;
import dev.playmonkeei.chunkmobchallenge.model.RoundPhase;
import dev.playmonkeei.chunkmobchallenge.model.RoundState;
import dev.playmonkeei.chunkmobchallenge.model.RunSession;
import dev.playmonkeei.chunkmobchallenge.model.RunStatus;
import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class StateStore {
    private final File file;
    private final Logger logger;

    public StateStore(File dataFolder, Logger logger) {
        this.file = new File(dataFolder, "state.yml");
        this.logger = logger;
    }

    public RunSession load() {
        if (!file.isFile()) {
            return null;
        }
        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            RunSession run = new RunSession();
            run.id = UUID.fromString(require(yaml.getString("run.id"), "run.id"));
            run.status = RunStatus.valueOf(yaml.getString("run.status", RunStatus.RUNNING.name()));
            String leader = yaml.getString("run.leader");
            run.leader = leader == null ? null : UUID.fromString(leader);
            run.accumulatedMillis = yaml.getLong("run.accumulated-millis");
            run.timerStartedAt = 0; // Offline time is deliberately never counted.
            run.deaths = yaml.getInt("run.deaths");
            run.defeatedMobs = yaml.getInt("run.defeated-mobs");
            run.startedRounds = yaml.getInt("run.started-rounds");
            boolean hasVisibilitySettings = yaml.getInt("run.visibility-settings-version", 0) >= 1;
            run.nameTagsVisible = hasVisibilitySettings && yaml.getBoolean("run.name-tags-visible", false);
            run.glowing = hasVisibilitySettings && yaml.getBoolean("run.glowing", false);

            ConfigurationSection rounds = yaml.getConfigurationSection("rounds");
            if (rounds != null) {
                for (String idText : rounds.getKeys(false)) {
                    ConfigurationSection section = rounds.getConfigurationSection(idText);
                    if (section == null) continue;
                    UUID id = UUID.fromString(idText);
                    ChunkKey chunk = ChunkKey.decode(require(section.getString("chunk"), "round.chunk"));
                    RoundState round = new RoundState(id, chunk,
                            RoundPhase.valueOf(section.getString("phase", RoundPhase.DORMANT.name())),
                            require(section.getString("entity-type"), "round.entity-type"));
                    String mobUuid = section.getString("mob-uuid");
                    round.mobUuid = mobUuid == null ? null : UUID.fromString(mobUuid);
                    for (String player : section.getStringList("participants")) {
                        round.participants.add(UUID.fromString(player));
                    }
                    readSnapshot(section.getConfigurationSection("snapshot"), round.snapshot);
                    // No entity may tick before its player explicitly resumes it.
                    round.phase = RoundPhase.DORMANT;
                    run.rounds.put(chunk, round);
                }
            }

            ConfigurationSection players = yaml.getConfigurationSection("players");
            if (players != null) {
                for (String idText : players.getKeys(false)) {
                    ConfigurationSection section = players.getConfigurationSection(idText);
                    if (section == null) continue;
                    PlayerState state = new PlayerState(UUID.fromString(idText));
                    String roundChunk = section.getString("round-chunk");
                    String lastChunk = section.getString("last-chunk");
                    state.roundChunk = roundChunk == null ? null : ChunkKey.decode(roundChunk);
                    state.lastChunk = lastChunk == null ? null : ChunkKey.decode(lastChunk);
                    state.worldKey = section.getString("location.world");
                    state.x = section.getDouble("location.x");
                    state.y = section.getDouble("location.y");
                    state.z = section.getDouble("location.z");
                    state.yaw = (float) section.getDouble("location.yaw");
                    state.pitch = (float) section.getDouble("location.pitch");
                    state.pendingRespawn = section.getBoolean("pending-respawn");
                    run.players.put(state.playerId, state);
                }
            }
            return run;
        } catch (Exception exception) {
            logger.severe("state.yml konnte nicht geladen werden: " + exception.getMessage());
            return null;
        }
    }

    public void save(RunSession run, long now) {
        if (run == null) return;
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("run.id", run.id.toString());
        yaml.set("run.status", run.status.name());
        yaml.set("run.leader", run.leader == null ? null : run.leader.toString());
        yaml.set("run.accumulated-millis", run.activeMillis(now));
        yaml.set("run.deaths", run.deaths);
        yaml.set("run.defeated-mobs", run.defeatedMobs);
        yaml.set("run.started-rounds", run.startedRounds);
        yaml.set("run.name-tags-visible", run.nameTagsVisible);
        yaml.set("run.glowing", run.glowing);
        yaml.set("run.visibility-settings-version", 1);

        for (RoundState round : run.rounds.values()) {
            String base = "rounds." + round.id;
            yaml.set(base + ".chunk", round.chunk.encoded());
            yaml.set(base + ".phase", round.phase.name());
            yaml.set(base + ".entity-type", round.entityTypeKey);
            yaml.set(base + ".mob-uuid", round.mobUuid == null ? null : round.mobUuid.toString());
            yaml.set(base + ".participants", round.participants.stream().map(UUID::toString).toList());
            writeSnapshot(yaml, base + ".snapshot", round.snapshot);
        }
        for (PlayerState state : run.players.values()) {
            String base = "players." + state.playerId;
            yaml.set(base + ".round-chunk", state.roundChunk == null ? null : state.roundChunk.encoded());
            yaml.set(base + ".last-chunk", state.lastChunk == null ? null : state.lastChunk.encoded());
            yaml.set(base + ".location.world", state.worldKey);
            yaml.set(base + ".location.x", state.x);
            yaml.set(base + ".location.y", state.y);
            yaml.set(base + ".location.z", state.z);
            yaml.set(base + ".location.yaw", state.yaw);
            yaml.set(base + ".location.pitch", state.pitch);
            yaml.set(base + ".pending-respawn", state.pendingRespawn);
        }

        try {
            Files.createDirectories(file.toPath().getParent());
            File temporary = new File(file.getParentFile(), "state.yml.tmp");
            yaml.save(temporary);
            try {
                Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            logger.severe("state.yml konnte nicht atomar gespeichert werden: " + exception.getMessage());
        }
    }

    public void delete() {
        try {
            Files.deleteIfExists(file.toPath());
            Files.deleteIfExists(new File(file.getParentFile(), "state.yml.tmp").toPath());
        } catch (IOException exception) {
            logger.warning("state.yml konnte nicht gelöscht werden: " + exception.getMessage());
        }
    }

    private static void writeSnapshot(YamlConfiguration yaml, String base, MobSnapshot s) {
        yaml.set(base + ".x", s.x);
        yaml.set(base + ".y", s.y);
        yaml.set(base + ".z", s.z);
        yaml.set(base + ".yaw", s.yaw);
        yaml.set(base + ".pitch", s.pitch);
        yaml.set(base + ".health", s.health);
        yaml.set(base + ".fire-ticks", s.fireTicks);
        yaml.set(base + ".remaining-air", s.remainingAir);
        yaml.set(base + ".gravity", s.gravity);
        yaml.set(base + ".ai", s.ai);
        yaml.set(base + ".invulnerable", s.invulnerable);
        yaml.set(base + ".velocity", List.of(s.velocityX, s.velocityY, s.velocityZ));
        int index = 0;
        for (EffectSnapshot effect : s.effects) {
            String path = base + ".effects." + index++;
            yaml.set(path + ".type", effect.typeKey());
            yaml.set(path + ".duration", effect.duration());
            yaml.set(path + ".amplifier", effect.amplifier());
            yaml.set(path + ".ambient", effect.ambient());
            yaml.set(path + ".particles", effect.particles());
            yaml.set(path + ".icon", effect.icon());
        }
    }

    private static void readSnapshot(ConfigurationSection section, MobSnapshot s) {
        if (section == null) return;
        s.x = section.getDouble("x");
        s.y = section.getDouble("y");
        s.z = section.getDouble("z");
        s.yaw = (float) section.getDouble("yaw");
        s.pitch = (float) section.getDouble("pitch");
        s.health = section.getDouble("health", 1.0);
        s.fireTicks = section.getInt("fire-ticks");
        s.remainingAir = section.getInt("remaining-air", 300);
        s.gravity = section.getBoolean("gravity", true);
        s.ai = section.getBoolean("ai", true);
        s.invulnerable = section.getBoolean("invulnerable");
        List<Double> velocity = section.getDoubleList("velocity");
        if (velocity.size() == 3) {
            s.velocityX = velocity.get(0);
            s.velocityY = velocity.get(1);
            s.velocityZ = velocity.get(2);
        }
        ConfigurationSection effects = section.getConfigurationSection("effects");
        if (effects != null) {
            for (String key : effects.getKeys(false)) {
                ConfigurationSection effect = effects.getConfigurationSection(key);
                if (effect == null) continue;
                s.effects.add(new EffectSnapshot(require(effect.getString("type"), "effect.type"),
                        effect.getInt("duration"), effect.getInt("amplifier"),
                        effect.getBoolean("ambient"), effect.getBoolean("particles", true),
                        effect.getBoolean("icon", true)));
            }
        }
    }

    private static String require(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Fehlender Wert: " + name);
        return value;
    }
}
