package dev.playmonkeei.chunkmobchallenge;

import dev.playmonkeei.chunkmobchallenge.mob.MobPool;
import dev.playmonkeei.chunkmobchallenge.persistence.StateStore;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChunkMobChallengePlugin extends JavaPlugin {
    private ChallengeService service;

    @Override
    public void onEnable() {
        MobPool mobPool = new MobPool(getDataFolder(), getLogger());
        if (!mobPool.load()) {
            getLogger().severe("Kein gültiger Mob-Pool verfügbar; Plugin wird deaktiviert.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        StateStore stateStore = new StateStore(getDataFolder(), getLogger());
        service = new ChallengeService(this, mobPool, stateStore);
        Bukkit.getPluginManager().registerEvents(service, this);
        service.startTasks();

        PluginCommand command = Objects.requireNonNull(getCommand("chunkchallenge"));
        ChallengeCommand handler = new ChallengeCommand(service);
        command.setExecutor(handler);
        command.setTabCompleter(handler);
        getLogger().info("ChunkMobChallenge ist bereit.");
    }

    @Override
    public void onDisable() {
        if (service != null) service.shutdown();
    }
}
