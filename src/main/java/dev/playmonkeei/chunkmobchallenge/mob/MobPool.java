package dev.playmonkeei.chunkmobchallenge.mob;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;

public final class MobPool {
    private final File file;
    private final Logger logger;
    private final Random random = new Random();
    private List<EntityType> active = List.of();

    public MobPool(File dataFolder, Logger logger) {
        this.file = new File(dataFolder, "mobs.yml");
        this.logger = logger;
    }

    public boolean load() {
        if (!file.isFile() && !generate()) {
            return false;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        Map<String, EntityType> available = availableTypes();
        Map<String, EntityType> selected = new LinkedHashMap<>();
        for (String raw : yaml.getStringList("mobs")) {
            String key = raw.trim().toLowerCase(Locale.ROOT);
            EntityType type = available.get(key);
            if (type == null) {
                logger.warning("Unbekannter oder ungeeigneter EntityType in mobs.yml wird ignoriert: " + raw);
            } else if (selected.putIfAbsent(key, type) != null) {
                logger.warning("Doppelter EntityType in mobs.yml wird ignoriert: " + raw);
            }
        }
        if (selected.isEmpty()) {
            logger.severe("mobs.yml enthält keinen gültigen Mob; der bisherige Pool bleibt aktiv.");
            return false;
        }
        active = List.copyOf(selected.values());
        logger.info("Mob-Pool geladen: " + active.size() + " EntityTypes");
        return true;
    }

    public EntityType randomType() {
        if (active.isEmpty()) throw new IllegalStateException("Mob-Pool ist leer");
        return active.get(random.nextInt(active.size()));
    }

    public EntityType resolve(String key) {
        return Arrays.stream(EntityType.values())
                .filter(type -> type.getKey().toString().equals(key))
                .findFirst().orElse(null);
    }

    public int size() {
        return active.size();
    }

    private boolean generate() {
        YamlConfiguration yaml = new YamlConfiguration();
        List<String> keys = new ArrayList<>(availableTypes().keySet());
        keys.sort(Comparator.naturalOrder());
        yaml.options().setHeader(List.of(
                "Alle Einträge haben dieselbe Wahrscheinlichkeit.",
                "Zeilen dürfen entfernt werden und werden nicht automatisch ergänzt.",
                "Zum Wiederherstellen des vollständigen Pools diese Datei löschen und den Server starten."
        ));
        yaml.set("mobs", keys);
        try {
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                throw new IOException("Plugin-Verzeichnis konnte nicht angelegt werden");
            }
            yaml.save(file);
            logger.info("Neue mobs.yml mit " + keys.size() + " EntityTypes erzeugt.");
            return true;
        } catch (IOException exception) {
            logger.severe("mobs.yml konnte nicht erzeugt werden: " + exception.getMessage());
            return false;
        }
    }

    private static Map<String, EntityType> availableTypes() {
        Map<String, EntityType> result = new LinkedHashMap<>();
        for (EntityType type : EntityType.values()) {
            Class<? extends Entity> entityClass = type.getEntityClass();
            if (type.isAlive() && type.isSpawnable() && entityClass != null
                    && Mob.class.isAssignableFrom(entityClass)) {
                result.put(type.getKey().toString(), type);
            }
        }
        return result;
    }
}
