package dev.playmonkeei.chunkmobchallenge;

import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ChallengeCommand implements CommandExecutor, TabCompleter {
    private static final List<String> SUBCOMMANDS = List.of("start", "stop", "status", "reload", "tags", "glowing");
    private static final List<String> TOGGLE_OPTIONS = List.of("enable", "disable");
    private final ChallengeService service;

    public ChallengeCommand(ChallengeService service) {
        this.service = service;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (!sender.hasPermission("chunkchallenge." + sub)
                && !sender.hasPermission("chunkchallenge.admin")) {
            sender.sendMessage(Component.text("Dafür fehlt dir die Berechtigung.", NamedTextColor.RED));
            return true;
        }
        if (!sub.equals("tags") && !sub.equals("glowing") && args.length != 1) {
            sendUsage(sender, label);
            return true;
        }
        switch (sub) {
            case "start" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("Die Challenge muss von einem Spieler gestartet werden.", NamedTextColor.RED));
                } else service.start(player);
            }
            case "stop" -> {
                if (!service.stop()) sender.sendMessage(Component.text("Es läuft keine Challenge.", NamedTextColor.GRAY));
            }
            case "status" -> sender.sendMessage(service.status(sender instanceof Player p ? p.getUniqueId() : null));
            case "reload" -> {
                boolean success = service.reloadMobs();
                sender.sendMessage(Component.text(success
                        ? "mobs.yml wurde neu geladen." : "Reload fehlgeschlagen; bisheriger Pool bleibt aktiv.",
                        success ? NamedTextColor.GREEN : NamedTextColor.RED));
            }
            case "tags" -> {
                if (args.length != 2 || !TOGGLE_OPTIONS.contains(args[1].toLowerCase(Locale.ROOT))) {
                    sender.sendMessage(Component.text("Verwendung: /" + label + " tags <enable|disable>",
                            NamedTextColor.YELLOW));
                    return true;
                }
                boolean visible = args[1].equalsIgnoreCase("enable");
                if (!service.setNameTagsVisible(visible)) {
                    sender.sendMessage(Component.text("Es läuft keine Challenge.", NamedTextColor.GRAY));
                } else {
                    sender.sendMessage(Component.text("Mob-Nametags sind jetzt " + (visible ? "aktiviert." : "deaktiviert."),
                            NamedTextColor.GREEN));
                }
            }
            case "glowing" -> {
                if (args.length != 2 || !TOGGLE_OPTIONS.contains(args[1].toLowerCase(Locale.ROOT))) {
                    sender.sendMessage(Component.text("Verwendung: /" + label + " glowing <enable|disable>",
                            NamedTextColor.YELLOW));
                    return true;
                }
                boolean glowing = args[1].equalsIgnoreCase("enable");
                if (!service.setGlowing(glowing)) {
                    sender.sendMessage(Component.text("Es läuft keine Challenge.", NamedTextColor.GRAY));
                } else {
                    sender.sendMessage(Component.text("Mob-Glowing ist jetzt " + (glowing ? "aktiviert." : "deaktiviert."),
                            NamedTextColor.GREEN));
                }
            }
            default -> sender.sendMessage(Component.text("Unbekannter Unterbefehl.", NamedTextColor.RED));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return SUBCOMMANDS.stream().filter(value -> value.startsWith(prefix)).toList();
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("tags") || args[0].equalsIgnoreCase("glowing"))) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            return TOGGLE_OPTIONS.stream().filter(value -> value.startsWith(prefix)).toList();
        }
        return List.of();
    }

    private static void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(Component.text(
                "Verwendung: /" + label + " <start|stop|status|reload|tags|glowing>", NamedTextColor.YELLOW));
    }
}
