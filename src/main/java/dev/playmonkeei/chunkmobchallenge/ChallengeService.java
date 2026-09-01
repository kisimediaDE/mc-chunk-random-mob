package dev.playmonkeei.chunkmobchallenge;

import dev.playmonkeei.chunkmobchallenge.mob.MobPool;
import dev.playmonkeei.chunkmobchallenge.model.ChunkKey;
import dev.playmonkeei.chunkmobchallenge.model.EffectSnapshot;
import dev.playmonkeei.chunkmobchallenge.model.MobSnapshot;
import dev.playmonkeei.chunkmobchallenge.model.PlayerState;
import dev.playmonkeei.chunkmobchallenge.model.RoundPhase;
import dev.playmonkeei.chunkmobchallenge.model.RoundState;
import dev.playmonkeei.chunkmobchallenge.model.RunSession;
import dev.playmonkeei.chunkmobchallenge.model.RunStatus;
import dev.playmonkeei.chunkmobchallenge.persistence.StateStore;
import io.papermc.paper.event.entity.EntityMoveEvent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class ChallengeService implements Listener {
    private static final Component PREFIX = Component.text("[Chunk Challenge] ", NamedTextColor.GOLD);
    private static final double BORDER_EPSILON = 0.05;

    private final JavaPlugin plugin;
    private final MobPool mobPool;
    private final StateStore stateStore;
    private final org.bukkit.NamespacedKey roundTag;
    private final org.bukkit.NamespacedKey runTag;
    private final Set<UUID> internalTeleports = new HashSet<>();
    private final Set<UUID> internalRemovals = new HashSet<>();
    private final Map<UUID, Long> onlineSince = new HashMap<>();
    private final Map<UUID, Long> dragonNextAttackAt = new HashMap<>();
    private RunSession run;

    public ChallengeService(JavaPlugin plugin, MobPool mobPool, StateStore stateStore) {
        this.plugin = plugin;
        this.mobPool = mobPool;
        this.stateStore = stateStore;
        this.roundTag = new org.bukkit.NamespacedKey(plugin, "round-id");
        this.runTag = new org.bukkit.NamespacedKey(plugin, "run-id");
        this.run = stateStore.load();
        if (run != null && run.running()) {
            plugin.getLogger().info("Pausierter Challenge-Run " + run.id + " mit "
                    + run.rounds.size() + " Runde(n) geladen.");
        }
    }

    public void startTasks() {
        Bukkit.getScheduler().runTask(plugin, this::reconcileLoadedChallengeMobs);
        Bukkit.getScheduler().runTaskTimer(plugin, this::watchdog, 20L, 20L);
        Bukkit.getScheduler().runTaskTimer(plugin, this::checkpoint, 200L, 200L);
    }

    public RunSession run() {
        return run;
    }

    public boolean start(Player starter) {
        if (run != null && run.running()) {
            message(starter, NamedTextColor.RED, "Es läuft bereits eine Challenge.");
            return false;
        }
        for (World world : Bukkit.getWorlds()) {
            DragonBattle battle = world.getEnderDragonBattle();
            if (battle != null && battle.hasBeenPreviouslyKilled()) {
                message(starter, NamedTextColor.RED,
                        "Der ursprüngliche Enderdragon wurde in dieser Welt bereits besiegt.");
                return false;
            }
        }

        run = new RunSession();
        run.leader = starter.getUniqueId();
        long now = System.currentTimeMillis();
        run.resumeTimer(now);
        Location start = starter.getLocation().clone();
        List<Player> participants = new ArrayList<>(Bukkit.getOnlinePlayers());
        for (Player player : participants) {
            onlineSince.putIfAbsent(player.getUniqueId(), now);
            PlayerState state = new PlayerState(player.getUniqueId());
            run.players.put(player.getUniqueId(), state);
            if (!player.equals(starter)) teleportInternal(player, start);
            savePlayerLocation(player, state);
        }
        RoundState round = createRound(ChunkKey.from(start), start, participants.stream()
                .map(Player::getUniqueId).toList());
        if (round == null) {
            run = null;
            message(starter, NamedTextColor.RED, "Es konnte kein Challenge-Mob gespawnt werden.");
            return false;
        }
        broadcast(NamedTextColor.GREEN,
                "Challenge gestartet! Ziel: " + displayName(round.entityTypeKey) + " besiegen.");
        checkpoint();
        return true;
    }

    public boolean stop() {
        if (run == null || !run.running()) return false;
        finishRun(null, "Challenge gestoppt.", false, true);
        return true;
    }

    public void shutdown() {
        if (run == null || !run.running()) return;
        // Capture positions before Paper disconnects/saves the players.
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerState state = run.players.get(player.getUniqueId());
            if (state != null) savePlayerLocation(player, state);
            player.setWorldBorder(null);
        }
        for (RoundState round : run.rounds.values()) {
            if (round.phase == RoundPhase.ACTIVE) makeDormant(round);
            removeBossBar(round);
        }
        run.pauseTimer(System.currentTimeMillis());
        stateStore.save(run, System.currentTimeMillis());
    }

    public boolean reloadMobs() {
        return mobPool.load();
    }

    public boolean setNameTagsVisible(boolean visible) {
        if (run == null || !run.running()) return false;
        run.nameTagsVisible = visible;
        for (RoundState round : run.rounds.values()) {
            Mob mob = mob(round);
            if (mob != null) mob.setCustomNameVisible(visible);
        }
        checkpoint();
        return true;
    }

    public boolean setGlowing(boolean glowing) {
        if (run == null || !run.running()) return false;
        run.glowing = glowing;
        for (RoundState round : run.rounds.values()) {
            Mob mob = mob(round);
            if (mob != null) mob.setGlowing(glowing);
        }
        checkpoint();
        return true;
    }

    public Component status(UUID requester) {
        if (run == null) return PREFIX.append(Component.text("Keine Challenge vorhanden.", NamedTextColor.GRAY));
        StringBuilder text = new StringBuilder();
        text.append("Status: ").append(run.status)
                .append(" | Spielzeit: ").append(formatDuration(run.activeMillis(System.currentTimeMillis())))
                .append(" | Mobs: ").append(run.defeatedMobs)
                .append(" | Runden: ").append(run.startedRounds)
                .append(" | Tode: ").append(run.deaths)
                .append(" | Nametags: ").append(run.nameTagsVisible ? "AN" : "AUS")
                .append(" | Glowing: ").append(run.glowing ? "AN" : "AUS");
        PlayerState state = requester == null ? null : run.players.get(requester);
        if (state != null && state.roundChunk != null) {
            RoundState round = run.rounds.get(state.roundChunk);
            if (round != null) {
                text.append(" | Eigene Runde: ").append(displayName(round.entityTypeKey))
                        .append(" (").append(round.phase).append(") @ ")
                        .append(round.chunk.x()).append('/').append(round.chunk.z());
            }
        } else if (requester == null) {
            text.append(" | Aktive/pausierte Chunk-Runden: ").append(run.rounds.size());
        }
        return PREFIX.append(Component.text(text.toString(), NamedTextColor.YELLOW));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        onlineSince.put(player.getUniqueId(), System.currentTimeMillis());
        if (run == null || !run.running()) return;
        run.resumeTimer(System.currentTimeMillis());
        PlayerState state = run.players.get(player.getUniqueId());
        if (state == null) {
            state = new PlayerState(player.getUniqueId());
            run.players.put(player.getUniqueId(), state);
            Player target = preferredOnlineTarget(player);
            Location destination = target == null ? player.getLocation() : target.getLocation();
            PlayerState finalState = state;
            Bukkit.getScheduler().runTask(plugin, () -> {
                teleportInternal(player, destination);
                savePlayerLocation(player, finalState);
                enterOrCreateRound(player, destination, true);
            });
        } else {
            PlayerState restored = state;
            Bukkit.getScheduler().runTask(plugin, () -> restorePlayer(player, restored));
        }
        checkpoint();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        onlineSince.remove(event.getPlayer().getUniqueId());
        if (run == null || !run.running()) return;
        PlayerState state = run.players.get(event.getPlayer().getUniqueId());
        if (state != null) savePlayerLocation(event.getPlayer(), state);
        if (state != null && state.roundChunk != null) {
            RoundState round = run.rounds.get(state.roundChunk);
            if (round != null && !hasOnlineParticipant(round, event.getPlayer().getUniqueId())) {
                makeDormant(round);
            }
        }
        if (onlineParticipantCount(event.getPlayer().getUniqueId()) == 0) {
            run.pauseTimer(System.currentTimeMillis());
        }
        checkpoint();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() == null || run == null || !run.running()) return;
        // PlayerTeleportEvent extends PlayerMoveEvent. Teleports need their own
        // authorization path (including the internal restore bypass) below.
        if (event instanceof PlayerTeleportEvent) return;
        PlayerState state = run.players.get(event.getPlayer().getUniqueId());
        if (state == null) return;
        if (state.roundChunk != null) {
            RoundState round = run.rounds.get(state.roundChunk);
            if (round != null && round.phase == RoundPhase.ACTIVE && !round.chunk.contains(event.getTo())) {
                event.setCancelled(true);
                return;
            }
        }
        ChunkKey destination = ChunkKey.from(event.getTo());
        if (state.roundChunk == null && state.lastChunk != null && !state.lastChunk.equals(destination)) {
            enterOrCreateRound(event.getPlayer(), event.getTo(), false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getTo() == null || run == null || !run.running()) return;
        UUID id = event.getPlayer().getUniqueId();
        if (internalTeleports.remove(id)) return;
        PlayerState state = run.players.get(id);
        if (state == null) return;
        if (state.roundChunk != null) {
            RoundState round = run.rounds.get(state.roundChunk);
            if (round != null && round.phase == RoundPhase.ACTIVE && !round.chunk.contains(event.getTo())) {
                event.setCancelled(true);
                message(event.getPlayer(), NamedTextColor.RED, "Du musst zuerst den Challenge-Mob besiegen.");
                return;
            }
        }
        Location destination = event.getTo().clone();
        Bukkit.getScheduler().runTask(plugin, () -> enterOrCreateRound(event.getPlayer(), destination, false));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityMove(EntityMoveEvent event) {
        RoundState round = roundFor(event.getEntity());
        if (round == null) return;
        if (round.phase == RoundPhase.DORMANT) {
            event.setCancelled(true);
            return;
        }
        Mob movingMob = (Mob) event.getEntity();
        double margin = movementMargin(movingMob);
        double minX = round.chunk.x() * 16.0 + margin;
        double maxX = round.chunk.x() * 16.0 + 16.0 - margin;
        double minZ = round.chunk.z() * 16.0 + margin;
        double maxZ = round.chunk.z() * 16.0 + 16.0 - margin;
        if (event.getTo().getX() < minX || event.getTo().getX() > maxX
                || event.getTo().getZ() < minZ || event.getTo().getZ() > maxZ) {
            Location redirected = clampInsideChunk(event.getTo(), round.chunk, margin);
            Vector velocity = movingMob.getVelocity().clone();
            if (event.getTo().getX() < minX || event.getTo().getX() > maxX) {
                velocity.setX(inwardSpeed(velocity.getX(), round.chunk.centerX() - redirected.getX()));
            }
            if (event.getTo().getZ() < minZ || event.getTo().getZ() > maxZ) {
                velocity.setZ(inwardSpeed(velocity.getZ(), round.chunk.centerZ() - redirected.getZ()));
            }
            movingMob.getPathfinder().stopPathfinding();
            movingMob.setVelocity(velocity);
            event.setTo(redirected);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityTeleport(EntityTeleportEvent event) {
        if (event.getTo() == null) return;
        RoundState round = roundFor(event.getEntity());
        if (round != null && (round.phase == RoundPhase.DORMANT || !round.chunk.contains(event.getTo()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        RoundState round = roundFor(event.getEntity());
        if (round != null && round.phase == RoundPhase.DORMANT) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChallengeDragonDamaged(EntityDamageByEntityEvent event) {
        RoundState round = roundFor(event.getEntity());
        if (round == null || !(event.getEntity() instanceof EnderDragon dragon)) return;
        Player attacker = null;
        if (event.getDamager() instanceof Player player) attacker = player;
        if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            attacker = player;
        }
        if (attacker == null || !round.participants.contains(attacker.getUniqueId())) return;
        provokeChallengeDragon(round, dragon, attacker);
        dragonNextAttackAt.put(round.id, System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRegain(EntityRegainHealthEvent event) {
        RoundState round = roundFor(event.getEntity());
        if (round != null && round.phase == RoundPhase.DORMANT) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCombust(EntityCombustEvent event) {
        RoundState round = roundFor(event.getEntity());
        if (round != null && round.phase == RoundPhase.DORMANT) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTransform(EntityTransformEvent event) {
        RoundState round = roundFor(event.getEntity());
        if (round == null || round.phase == RoundPhase.DORMANT || event.getTransformedEntities().isEmpty()) return;
        Entity transformed = event.getTransformedEntities().getFirst();
        if (!(transformed instanceof Mob mob)) return;
        Bukkit.getScheduler().runTask(plugin, () -> {
            tagMob(mob, round);
            round.mobUuid = mob.getUniqueId();
            round.entityTypeKey = mob.getType().getKey().toString();
            captureSnapshot(round, mob);
            checkpoint();
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDormantTransform(EntityTransformEvent event) {
        RoundState round = roundFor(event.getEntity());
        if (round != null && round.phase == RoundPhase.DORMANT) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if (run == null || !run.running()) return;
        LivingEntity entity = event.getEntity();
        if (entity instanceof EnderDragon dragon && roundFor(dragon) == null && isVanillaDragon(dragon)) {
            finishRun(RunStatus.COMPLETED, "CHALLENGE GESCHAFFT! Der Enderdragon ist besiegt.", false, false);
            return;
        }
        RoundState round = roundFor(entity);
        if (round == null || internalRemovals.remove(entity.getUniqueId())) return;
        if (entity instanceof Slime) scheduleSplitCleanup(round, entity);
        completeRound(round);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) reconcileChallengeEntity(entity);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (run == null || !run.running()) return;
        Player player = event.getPlayer();
        PlayerState state = run.players.get(player.getUniqueId());
        if (state == null) return;
        run.deaths++;
        savePlayerLocation(player, state);
        if (Bukkit.getServer().isHardcore()) {
            finishRun(RunStatus.FAILED, "RUN GESCHEITERT! " + player.getName() + " ist gestorben.", false, false);
            return;
        }
        if (state.roundChunk != null) {
            RoundState round = run.rounds.get(state.roundChunk);
            if (round != null) {
                round.participants.remove(player.getUniqueId());
                if (round.participants.isEmpty()) dissolveRound(round);
                else if (!hasOnlineParticipant(round, player.getUniqueId())) makeDormant(round);
            }
        }
        state.roundChunk = null;
        state.pendingRespawn = true;
        player.setWorldBorder(null);
        checkpoint();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (run == null || !run.running()) return;
        PlayerState state = run.players.get(event.getPlayer().getUniqueId());
        if (state == null || !state.pendingRespawn) return;
        state.pendingRespawn = false;
        Location respawn = event.getRespawnLocation().clone();
        Bukkit.getScheduler().runTask(plugin, () -> {
            savePlayerLocation(event.getPlayer(), state);
            enterOrCreateRound(event.getPlayer(), respawn, true);
        });
    }

    private RoundState createRound(ChunkKey key, Location anchor, List<UUID> participants) {
        RoundState existing = run.rounds.get(key);
        if (existing != null) {
            for (UUID id : participants) attachPlayer(existing, id);
            activate(existing);
            return existing;
        }
        World world = world(key.worldKey());
        if (world == null) return null;
        RoundState round = null;
        Mob mob = null;
        addTicket(key);
        for (int attempt = 0; attempt < Math.max(8, Math.min(32, mobPool.size())); attempt++) {
            EntityType type = mobPool.randomType();
            RoundState candidate = new RoundState(UUID.randomUUID(), key, RoundPhase.ACTIVE,
                    type.getKey().toString());
            Location spawn = findSpawn(world, key, anchor, type);
            try {
                Entity entity = world.spawnEntity(spawn, type);
                if (entity instanceof Mob spawned) {
                    round = candidate;
                    mob = spawned;
                    break;
                }
                entity.remove();
            } catch (RuntimeException exception) {
                plugin.getLogger().warning("Spawn von " + type.getKey() + " fehlgeschlagen: " + exception.getMessage());
            }
        }
        if (round == null || mob == null) {
            removeTicket(key);
            return null;
        }
        run.rounds.put(key, round);
        run.startedRounds++;
        tagMob(mob, round);
        round.mobUuid = mob.getUniqueId();
        for (UUID id : participants) attachPlayer(round, id);
        // Players already standing in the target chunk join the same atomic round.
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (run.players.containsKey(online.getUniqueId()) && key.equals(ChunkKey.from(online.getLocation()))) {
                attachPlayer(round, online.getUniqueId());
            }
        }
        captureSnapshot(round, mob);
        createBossBar(round, mob);
        checkpoint();
        return round;
    }

    private void completeRound(RoundState round) {
        dragonNextAttackAt.remove(round.id);
        removeRoundProjectiles(round);
        round.phase = RoundPhase.CLEARED;
        removeBossBar(round);
        removeTicket(round.chunk);
        run.defeatedMobs++;
        run.rounds.remove(round.chunk);
        for (UUID id : new ArrayList<>(round.participants)) {
            PlayerState state = run.players.get(id);
            if (state != null) {
                state.roundChunk = null;
                state.lastChunk = round.chunk;
            }
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                player.setWorldBorder(null);
                message(player, NamedTextColor.GREEN, "Mob besiegt! Du darfst den Chunk verlassen.");
            }
        }
        checkpoint();
    }

    private void dissolveRound(RoundState round) {
        dragonNextAttackAt.remove(round.id);
        removeRoundProjectiles(round);
        Entity mob = entity(round);
        if (mob != null) {
            internalRemovals.add(mob.getUniqueId());
            mob.remove();
        }
        removeBossBar(round);
        removeTicket(round.chunk);
        run.rounds.remove(round.chunk);
        checkpoint();
    }

    private void makeDormant(RoundState round) {
        if (round.phase == RoundPhase.DORMANT) return;
        Mob mob = mob(round);
        if (mob != null) {
            captureSnapshot(round, mob);
            mob.setAI(false);
            mob.setInvulnerable(true);
            mob.setGravity(false);
            mob.setVelocity(new Vector());
            mob.setFireTicks(0);
        }
        round.phase = RoundPhase.DORMANT;
        removeBossBar(round);
        removeTicket(round.chunk);
    }

    private void activate(RoundState round) {
        if (round.phase == RoundPhase.ACTIVE) {
            refreshRoundViewers(round);
            return;
        }
        addTicket(round.chunk);
        Mob mob = mob(round);
        if (mob == null) mob = recoverMob(round);
        if (mob == null) return;
        restoreSnapshot(round, mob);
        keepDragonAboveTerrain(round, mob);
        tagMob(mob, round);
        captureSnapshot(round, mob);
        round.phase = RoundPhase.ACTIVE;
        createBossBar(round, mob);
        refreshRoundViewers(round);
    }

    private void restorePlayer(Player player, PlayerState state) {
        Location saved = savedLocation(state);
        if (saved != null) teleportInternal(player, saved);
        if (state.roundChunk != null) {
            RoundState round = run.rounds.get(state.roundChunk);
            if (round != null) {
                round.participants.add(player.getUniqueId());
                activate(round);
                applyBorder(player, round.chunk);
                if (round.bossBar != null) round.bossBar.addPlayer(player);
                message(player, NamedTextColor.GREEN, "Deine pausierte Runde wurde fortgesetzt.");
                checkpoint();
                return;
            }
            state.roundChunk = null;
        }
        state.lastChunk = saved == null ? ChunkKey.from(player.getLocation()) : ChunkKey.from(saved);
        player.setWorldBorder(null);
        message(player, NamedTextColor.GREEN, "Dein Challenge-State wurde wiederhergestellt.");
        checkpoint();
    }

    private void enterOrCreateRound(Player player, Location destination, boolean force) {
        if (run == null || !run.running() || !run.players.containsKey(player.getUniqueId())) return;
        PlayerState state = run.players.get(player.getUniqueId());
        ChunkKey key = ChunkKey.from(destination);
        if (!force && state.roundChunk == null && key.equals(state.lastChunk)) return;
        RoundState round = run.rounds.get(key);
        boolean created = round == null;
        if (created) round = createRound(key, destination, List.of(player.getUniqueId()));
        else {
            attachPlayer(round, player.getUniqueId());
            activate(round);
        }
        if (round != null) {
            state.lastChunk = key;
            savePlayerLocation(player, state);
            message(player, NamedTextColor.GREEN, (created ? "Neue Runde! Ziel: " : "Runde beigetreten! Ziel: ")
                    + displayName(round.entityTypeKey) + " besiegen.");
        }
    }

    private void attachPlayer(RoundState round, UUID id) {
        round.participants.add(id);
        PlayerState state = run.players.computeIfAbsent(id, PlayerState::new);
        state.roundChunk = round.chunk;
        state.lastChunk = round.chunk;
        Player player = Bukkit.getPlayer(id);
        if (player != null) {
            applyBorder(player, round.chunk);
            if (round.bossBar != null) round.bossBar.addPlayer(player);
        }
    }

    private void applyBorder(Player player, ChunkKey key) {
        WorldBorder border = Bukkit.getServer().createWorldBorder();
        border.setCenter(key.centerX(), key.centerZ());
        border.setSize(16.0);
        border.setWarningDistance(0);
        border.setDamageAmount(0.0);
        player.setWorldBorder(border);
    }

    private void createBossBar(RoundState round, Mob mob) {
        removeBossBar(round);
        BossBar bar = null;
        if (mob instanceof Boss boss) {
            bar = boss.getBossBar();
        }
        if (bar == null) {
            bar = Bukkit.createBossBar(displayName(round.entityTypeKey), BarColor.RED, BarStyle.SOLID);
        }
        bar.setTitle(displayName(round.entityTypeKey));
        bar.setVisible(true);
        bar.setProgress(progress(mob));
        round.bossBar = bar;
        refreshRoundViewers(round);
    }

    private void refreshRoundViewers(RoundState round) {
        if (round.bossBar == null) return;
        round.bossBar.removeAll();
        for (UUID id : round.participants) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) round.bossBar.addPlayer(player);
        }
    }

    private void removeBossBar(RoundState round) {
        if (round.bossBar != null) {
            round.bossBar.removeAll();
            round.bossBar.setVisible(false);
            round.bossBar = null;
        }
    }

    private void tagMob(Mob mob, RoundState round) {
        mob.getPersistentDataContainer().set(roundTag, PersistentDataType.STRING, round.id.toString());
        mob.getPersistentDataContainer().set(runTag, PersistentDataType.STRING, run.id.toString());
        mob.setRemoveWhenFarAway(false);
        mob.setPersistent(true);
        if (mob instanceof EnderDragon) {
            AttributeInstance scale = mob.getAttribute(Attribute.SCALE);
            if (scale != null) scale.setBaseValue(0.5);
        }
        mob.customName(Component.text(displayName(round.entityTypeKey), NamedTextColor.RED));
        mob.setCustomNameVisible(run.nameTagsVisible);
        mob.setGlowing(run.glowing);
    }

    private void normalizeMob(Entity entity) {
        entity.getPersistentDataContainer().remove(roundTag);
        entity.getPersistentDataContainer().remove(runTag);
        entity.customName(null);
        entity.setCustomNameVisible(false);
        entity.setPersistent(false);
        entity.setInvulnerable(false);
        entity.setGravity(true);
        entity.setGlowing(false);
        if (entity instanceof Boss boss && boss.getBossBar() != null) boss.getBossBar().setVisible(true);
        if (entity instanceof Mob mob) {
            if (mob instanceof EnderDragon) {
                AttributeInstance scale = mob.getAttribute(Attribute.SCALE);
                if (scale != null) scale.setBaseValue(Attribute.SCALE.getDefaultValue());
            }
            mob.setAI(true);
            mob.setRemoveWhenFarAway(true);
        }
    }

    private void reconcileLoadedChallengeMobs() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) reconcileChallengeEntity(entity);
        }
    }

    private void reconcileChallengeEntity(Entity entity) {
        if (!(entity instanceof Mob) || roundFor(entity) != null) return;
        boolean staleTag = entity.getPersistentDataContainer().has(roundTag)
                || entity.getPersistentDataContainer().has(runTag);
        String visibleName = plainName(entity);
        boolean staleLegacyName = visibleName != null && visibleName.startsWith("Challenge: ");
        if (staleTag || staleLegacyName) normalizeMob(entity);
    }

    private void scheduleSplitCleanup(RoundState round, LivingEntity source) {
        Location origin = source.getLocation().clone();
        EntityType type = source.getType();
        String inheritedName = plainName(source);
        String roundId = round.id.toString();
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Entity nearby : origin.getWorld().getNearbyEntities(origin, 8, 8, 8)) {
                if (!(nearby instanceof Mob) || nearby.getType() != type) continue;
                String taggedRound = nearby.getPersistentDataContainer().get(roundTag, PersistentDataType.STRING);
                boolean inheritedTag = roundId.equals(taggedRound);
                boolean freshNamedChild = nearby.getTicksLived() <= 20 && inheritedName != null
                        && inheritedName.equals(plainName(nearby));
                if (inheritedTag || freshNamedChild) normalizeMob(nearby);
            }
        });
    }

    private static String plainName(Entity entity) {
        Component name = entity.customName();
        return name == null ? null : PlainTextComponentSerializer.plainText().serialize(name);
    }

    private void removeRoundProjectiles(RoundState round) {
        String roundId = round.id.toString();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof Projectile)) continue;
                String taggedRound = entity.getPersistentDataContainer().get(roundTag, PersistentDataType.STRING);
                if (roundId.equals(taggedRound)) entity.remove();
            }
        }
    }

    private void captureSnapshot(RoundState round, Mob mob) {
        MobSnapshot s = new MobSnapshot();
        Location location = mob.getLocation();
        s.x = location.getX(); s.y = location.getY(); s.z = location.getZ();
        s.yaw = location.getYaw(); s.pitch = location.getPitch();
        s.health = mob.getHealth();
        s.fireTicks = mob.getFireTicks();
        s.remainingAir = mob.getRemainingAir();
        s.gravity = mob.hasGravity();
        s.ai = mob.hasAI();
        s.invulnerable = mob.isInvulnerable();
        Vector velocity = mob.getVelocity();
        s.velocityX = velocity.getX(); s.velocityY = velocity.getY(); s.velocityZ = velocity.getZ();
        for (PotionEffect effect : mob.getActivePotionEffects()) {
            s.effects.add(new EffectSnapshot(effect.getType().getKey().toString(), effect.getDuration(),
                    effect.getAmplifier(), effect.isAmbient(), effect.hasParticles(), effect.hasIcon()));
        }
        round.snapshot = s;
    }

    private void restoreSnapshot(RoundState round, Mob mob) {
        MobSnapshot s = round.snapshot;
        World world = world(round.chunk.worldKey());
        if (world == null) return;
        Location location = new Location(world, s.x, s.y, s.z, s.yaw, s.pitch);
        if (!round.chunk.contains(location)) location = clampToChunk(location, round.chunk);
        mob.teleport(location);
        mob.setGravity(s.gravity);
        mob.setAI(s.ai);
        mob.setInvulnerable(s.invulnerable);
        mob.setFireTicks(s.fireTicks);
        mob.setRemainingAir(s.remainingAir);
        mob.setHealth(Math.max(0.01, Math.min(s.health, mob.getMaxHealth())));
        mob.setVelocity(new Vector(s.velocityX, s.velocityY, s.velocityZ));
        for (PotionEffect current : mob.getActivePotionEffects()) mob.removePotionEffect(current.getType());
        for (EffectSnapshot effect : s.effects) {
            PotionEffectType type = potionType(effect.typeKey());
            if (type != null) {
                mob.addPotionEffect(new PotionEffect(type, effect.duration(), effect.amplifier(),
                        effect.ambient(), effect.particles(), effect.icon()));
            }
        }
    }

    private Mob recoverMob(RoundState round) {
        World world = world(round.chunk.worldKey());
        if (world == null) return null;
        EntityType type = mobPool.resolve(round.entityTypeKey);
        if (type == null) {
            type = mobPool.randomType();
            round.entityTypeKey = type.getKey().toString();
            plugin.getLogger().warning("Gespeicherter Mobtyp fehlt; Runde verwendet nun " + round.entityTypeKey);
        }
        Location location = new Location(world, round.snapshot.x, round.snapshot.y, round.snapshot.z,
                round.snapshot.yaw, round.snapshot.pitch);
        if (!round.chunk.contains(location)) location = findSpawn(world, round.chunk, location, type);
        try {
            Entity entity = world.spawnEntity(location, type);
            if (!(entity instanceof Mob mob)) {
                entity.remove();
                return null;
            }
            tagMob(mob, round);
            round.mobUuid = mob.getUniqueId();
            restoreSnapshot(round, mob);
            checkpoint();
            return mob;
        } catch (RuntimeException exception) {
            plugin.getLogger().severe("Recovery-Spawn fehlgeschlagen: " + exception.getMessage());
            return null;
        }
    }

    private void watchdog() {
        if (run == null || !run.running()) return;
        for (RoundState round : new ArrayList<>(run.rounds.values())) {
            if (round.phase != RoundPhase.ACTIVE) continue;
            Mob mob = mob(round);
            if (mob == null || mob.isDead()) {
                mob = recoverMob(round);
                if (mob == null) continue;
            }
            if (!round.chunk.contains(mob.getLocation())) {
                mob.teleport(clampToChunk(mob.getLocation(), round.chunk));
                mob.setVelocity(new Vector());
            }
            keepDragonAboveTerrain(round, mob);
            updateChallengeDragonAttack(round, mob);
            captureSnapshot(round, mob);
            if (round.bossBar != null) {
                round.bossBar.setTitle(displayName(round.entityTypeKey));
                round.bossBar.setProgress(progress(mob));
            }
        }
    }

    private void checkpoint() {
        if (run != null) stateStore.save(run, System.currentTimeMillis());
    }

    private void finishRun(RunStatus status, String text, boolean removeEntities, boolean deleteState) {
        if (run == null) return;
        run.pauseTimer(System.currentTimeMillis());
        if (status != null) run.status = status;
        for (RoundState round : new ArrayList<>(run.rounds.values())) {
            removeRoundProjectiles(round);
            removeBossBar(round);
            // A dormant round may currently be unloaded. Load it once so /stop can
            // reliably turn the persisted challenge entity back into a normal mob.
            addTicket(round.chunk);
            Entity mob = entity(round);
            if (mob != null) {
                if (removeEntities) mob.remove();
                else normalizeMob(mob);
            }
            removeTicket(round.chunk);
        }
        for (Player player : Bukkit.getOnlinePlayers()) player.setWorldBorder(null);
        run.rounds.clear();
        broadcast(status == RunStatus.FAILED ? NamedTextColor.RED : NamedTextColor.GREEN,
                text + " Spielzeit: " + formatDuration(run.activeMillis(System.currentTimeMillis()))
                        + ", Mobs: " + run.defeatedMobs + ", Runden: " + run.startedRounds
                        + ", Tode: " + run.deaths);
        if (deleteState) {
            stateStore.delete();
            run = null;
            reconcileLoadedChallengeMobs();
        } else {
            checkpoint();
        }
    }

    private boolean isVanillaDragon(EnderDragon dragon) {
        DragonBattle battle = dragon.getWorld().getEnderDragonBattle();
        if (battle == null) return false;
        EnderDragon managed = battle.getEnderDragon();
        return managed == null ? !battle.hasBeenPreviouslyKilled()
                : managed.getUniqueId().equals(dragon.getUniqueId());
    }

    private RoundState roundFor(Entity entity) {
        if (run == null || !run.running()) return null;
        String id = entity.getPersistentDataContainer().get(roundTag, PersistentDataType.STRING);
        if (id != null) {
            try {
                UUID uuid = UUID.fromString(id);
                for (RoundState round : run.rounds.values()) if (round.id.equals(uuid)) return round;
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
        for (RoundState round : run.rounds.values()) {
            if (entity.getUniqueId().equals(round.mobUuid)) return round;
        }
        return null;
    }

    private Mob mob(RoundState round) {
        Entity entity = entity(round);
        return entity instanceof Mob mob ? mob : null;
    }

    private Entity entity(RoundState round) {
        if (round.mobUuid == null) return null;
        World world = world(round.chunk.worldKey());
        if (world == null) return null;
        Entity direct = world.getEntity(round.mobUuid);
        if (direct != null) return direct;

        Entity selected = null;
        Chunk chunk = world.getChunkAt(round.chunk.x(), round.chunk.z());
        for (Entity candidate : chunk.getEntities()) {
            String taggedRound = candidate.getPersistentDataContainer().get(roundTag, PersistentDataType.STRING);
            if (!round.id.toString().equals(taggedRound)) continue;
            if (selected == null) {
                selected = candidate;
                round.mobUuid = candidate.getUniqueId();
            } else {
                internalRemovals.add(candidate.getUniqueId());
                candidate.remove();
                plugin.getLogger().warning("Doppelten Challenge-Mob für Runde " + round.id + " entfernt.");
            }
        }
        return selected;
    }

    private Location findSpawn(World world, ChunkKey key, Location anchor, EntityType type) {
        if (type == EntityType.ENDER_DRAGON) return dragonSpawn(world, key);

        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int attempt = 0; attempt < 96; attempt++) {
            int x = key.x() * 16 + random.nextInt(1, 15);
            int z = key.z() * 16 + random.nextInt(1, 15);
            int y = surfaceY(world, x, z);
            double distance = Math.hypot(x + .5 - anchor.getX(), z + .5 - anchor.getZ());
            if (distance >= 4 && distance <= 10 && safeSurface(world, x, y, z)) {
                return new Location(world, x + .5, y, z + .5);
            }
        }

        Location best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int x = key.x() * 16 + 1; x <= key.x() * 16 + 14; x++) {
            for (int z = key.z() * 16 + 1; z <= key.z() * 16 + 14; z++) {
                int y = surfaceY(world, x, z);
                if (!safeSurface(world, x, y, z)) continue;
                double distance = Math.abs(7.0 - Math.hypot(x + .5 - anchor.getX(), z + .5 - anchor.getZ()));
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = new Location(world, x + .5, y, z + .5);
                }
            }
        }
        if (best != null) return best;

        return new Location(world, Math.max(key.x() * 16 + 1.5, Math.min(key.x() * 16 + 14.5, anchor.getX())),
                Math.max(world.getMinHeight() + 1, Math.min(world.getMaxHeight() - 3, anchor.getY())),
                Math.max(key.z() * 16 + 1.5, Math.min(key.z() * 16 + 14.5, anchor.getZ())));
    }

    private static int surfaceY(World world, int x, int z) {
        return Math.min(world.getMaxHeight() - 3,
                world.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES) + 1);
    }

    private static boolean safeSurface(World world, int x, int y, int z) {
        Material floor = world.getBlockAt(x, y - 1, z).getType();
        return safe(world, x, y, z) && !Tag.LEAVES.isTagged(floor) && !Tag.LOGS.isTagged(floor);
    }

    private static Location dragonSpawn(World world, ChunkKey key) {
        double y = Math.min(world.getMaxHeight() - 3, highestTerrain(world, key) + 12.0);
        return new Location(world, key.centerX(), y, key.centerZ());
    }

    private static int highestTerrain(World world, ChunkKey key) {
        int highest = world.getMinHeight() + 1;
        for (int x = key.x() * 16 + 1; x <= key.x() * 16 + 14; x++) {
            for (int z = key.z() * 16 + 1; z <= key.z() * 16 + 14; z++) {
                highest = Math.max(highest,
                        world.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES));
            }
        }
        return highest;
    }

    private static void keepDragonAboveTerrain(RoundState round, Mob mob) {
        if (mob.getType() != EntityType.ENDER_DRAGON) return;
        double minimumY = Math.min(mob.getWorld().getMaxHeight() - 3,
                highestTerrain(mob.getWorld(), round.chunk) + 3.0);
        if (mob.getY() >= minimumY) return;
        Location safe = clampToChunkStatic(mob.getLocation(), round.chunk, 1.0);
        safe.setY(minimumY);
        mob.teleport(safe);
        mob.setVelocity(new Vector());
    }

    private void updateChallengeDragonAttack(RoundState round, Mob mob) {
        if (!(mob instanceof EnderDragon dragon) || dragon.getDragonBattle() != null) return;
        Player target = round.participants.stream()
                .map(Bukkit::getPlayer)
                .filter(player -> player != null && !player.isDead() && player.getWorld().equals(dragon.getWorld()))
                .min(Comparator.comparingDouble(player -> player.getLocation().distanceSquared(dragon.getLocation())))
                .orElse(null);
        if (target == null) return;

        provokeChallengeDragon(round, dragon, target);
        long now = System.currentTimeMillis();
        long nextAttack = dragonNextAttackAt.computeIfAbsent(round.id, ignored -> now + 2_000L);
        if (now < nextAttack) return;

        Vector direction = target.getEyeLocation().toVector().subtract(dragon.getEyeLocation().toVector());
        if (direction.lengthSquared() > 0.0001) {
            DragonFireball fireball = dragon.launchProjectile(DragonFireball.class,
                    direction.normalize().multiply(1.1));
            fireball.getPersistentDataContainer().set(roundTag, PersistentDataType.STRING, round.id.toString());
            fireball.getPersistentDataContainer().set(runTag, PersistentDataType.STRING, run.id.toString());
        }
        dragonNextAttackAt.put(round.id, now + 5_000L);
    }

    private static void provokeChallengeDragon(RoundState round, EnderDragon dragon, Player target) {
        if (!round.participants.contains(target.getUniqueId())) return;
        dragon.setTarget(target);
        dragon.lookAt(target);
    }

    private static Location clampToChunkStatic(Location location, ChunkKey key, double margin) {
        Location result = location.clone();
        result.setX(Math.max(key.x() * 16.0 + margin,
                Math.min(key.x() * 16.0 + 16.0 - margin, result.getX())));
        result.setZ(Math.max(key.z() * 16.0 + margin,
                Math.min(key.z() * 16.0 + 16.0 - margin, result.getZ())));
        return result;
    }

    private static boolean safe(World world, int x, int y, int z) {
        Block feet = world.getBlockAt(x, y, z);
        Block head = world.getBlockAt(x, y + 1, z);
        Material floor = world.getBlockAt(x, y - 1, z).getType();
        return feet.isPassable() && head.isPassable() && floor.isSolid()
                && feet.getType() != Material.LAVA && head.getType() != Material.LAVA;
    }

    private Location clampToChunk(Location location, ChunkKey key) {
        return clampInsideChunk(location, key, BORDER_EPSILON);
    }

    private Location clampInsideChunk(Location location, ChunkKey key, double margin) {
        double minX = key.x() * 16.0 + margin;
        double maxX = key.x() * 16.0 + 16.0 - margin;
        double minZ = key.z() * 16.0 + margin;
        double maxZ = key.z() * 16.0 + 16.0 - margin;
        Location result = location.clone();
        result.setX(Math.max(minX, Math.min(maxX, result.getX())));
        result.setZ(Math.max(minZ, Math.min(maxZ, result.getZ())));
        return result;
    }

    private static double inwardSpeed(double currentSpeed, double directionToCenter) {
        double speed = Math.max(0.15, Math.min(0.6, Math.abs(currentSpeed)));
        return Math.copySign(speed, directionToCenter);
    }

    private static double movementMargin(Mob mob) {
        double halfWidth = Math.max(mob.getBoundingBox().getWidthX(), mob.getBoundingBox().getWidthZ()) / 2.0;
        return Math.max(0.75, Math.min(7.5, halfWidth + 0.25));
    }

    private void addTicket(ChunkKey key) {
        World world = world(key.worldKey());
        if (world != null) world.getChunkAt(key.x(), key.z()).addPluginChunkTicket(plugin);
    }

    private void removeTicket(ChunkKey key) {
        World world = world(key.worldKey());
        if (world == null) return;
        Chunk chunk = world.getChunkAt(key.x(), key.z());
        chunk.removePluginChunkTicket(plugin);
    }

    private World world(String key) {
        for (World world : Bukkit.getWorlds()) {
            if (world.getKey().toString().equals(key)) return world;
        }
        return null;
    }

    private void savePlayerLocation(Player player, PlayerState state) {
        Location location = player.getLocation();
        state.worldKey = location.getWorld().getKey().toString();
        state.x = location.getX(); state.y = location.getY(); state.z = location.getZ();
        state.yaw = location.getYaw(); state.pitch = location.getPitch();
        if (state.roundChunk == null) state.lastChunk = ChunkKey.from(location);
    }

    private Location savedLocation(PlayerState state) {
        if (state.worldKey == null) return null;
        World world = world(state.worldKey);
        return world == null ? null : new Location(world, state.x, state.y, state.z, state.yaw, state.pitch);
    }

    private void teleportInternal(Player player, Location destination) {
        internalTeleports.add(player.getUniqueId());
        player.teleport(destination);
        Bukkit.getScheduler().runTask(plugin, () -> internalTeleports.remove(player.getUniqueId()));
    }

    private Player preferredOnlineTarget(Player joining) {
        Player leader = run.leader == null ? null : Bukkit.getPlayer(run.leader);
        if (leader != null && !leader.equals(joining)) return leader;
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> !player.equals(joining) && run.players.containsKey(player.getUniqueId()))
                .min(Comparator.comparingLong(player -> onlineSince.getOrDefault(player.getUniqueId(), Long.MAX_VALUE)))
                .orElse(null);
    }

    private boolean hasOnlineParticipant(RoundState round, UUID excluding) {
        for (UUID id : round.participants) {
            if (!id.equals(excluding) && Bukkit.getPlayer(id) != null) return true;
        }
        return false;
    }

    private int onlineParticipantCount(UUID excluding) {
        int count = 0;
        for (UUID id : run.players.keySet()) {
            if (!id.equals(excluding) && Bukkit.getPlayer(id) != null) count++;
        }
        return count;
    }

    private static double progress(LivingEntity entity) {
        return Math.max(0.0, Math.min(1.0, entity.getHealth() / entity.getMaxHealth()));
    }

    private static PotionEffectType potionType(String key) {
        for (PotionEffectType type : PotionEffectType.values()) {
            if (type.getKey().toString().equals(key)) return type;
        }
        return null;
    }

    private static String displayName(String key) {
        String value = key.contains(":") ? key.substring(key.indexOf(':') + 1) : key;
        StringBuilder result = new StringBuilder();
        for (String part : value.split("_")) {
            if (!result.isEmpty()) result.append(' ');
            result.append(part.substring(0, 1).toUpperCase(Locale.ROOT)).append(part.substring(1));
        }
        return result.toString();
    }

    private static String formatDuration(long millis) {
        Duration duration = Duration.ofMillis(Math.max(0, millis));
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return "%02d:%02d:%02d".formatted(hours, minutes, seconds);
    }

    private void broadcast(NamedTextColor color, String text) {
        Component message = PREFIX.append(Component.text(text, color));
        for (Player player : Bukkit.getOnlinePlayers()) player.sendMessage(message);
        plugin.getLogger().info(text);
    }

    private static void message(Player player, NamedTextColor color, String text) {
        player.sendMessage(PREFIX.append(Component.text(text, color)));
    }
}
