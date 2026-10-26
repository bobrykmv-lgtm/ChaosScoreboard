package com.chaossmp.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ChaosScoreboard extends JavaPlugin implements Listener {

    private final Set<UUID> hidden = new HashSet<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        if (getCommand("sb") != null) {
            getCommand("sb").setExecutor(this);
        }

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!hidden.contains(player.getUniqueId())) {
                    updateScoreboard(player);
                }
            }
        }, 1L, 20L);

        getLogger().info("ChaosScoreboard включён!");
    }

    @Override
    public void onDisable() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();

        if (manager != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setScoreboard(manager.getMainScoreboard());
            }
        }
    }

    private void updateScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();

        if (manager == null) {
            return;
        }

        Scoreboard board = manager.getNewScoreboard();

        Objective objective = board.registerNewObjective(
                "chaos",
                "dummy",
                ChatColor.AQUA + "" + ChatColor.BOLD + "CHAOS SMP"
        );

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();

        int deaths = player.getStatistic(Statistic.DEATHS);
        int kills = player.getStatistic(Statistic.PLAYER_KILLS);

        // Начальное значение хаоса — 0.
        int chaos = 0;

        String[] lines = {
                ChatColor.GRAY + "╭──────────────╮",
                ChatColor.WHITE + "Ник: " + ChatColor.AQUA + player.getName(),
                ChatColor.WHITE + "XYZ: " + ChatColor.YELLOW
                        + x + " " + y + " " + z,
                ChatColor.DARK_GRAY + "──────────────",
                ChatColor.WHITE + "Смерти: " + ChatColor.RED + deaths,
                ChatColor.WHITE + "Киллы: " + ChatColor.GREEN + kills,
                ChatColor.WHITE + "Хаос: " + ChatColor.LIGHT_PURPLE + chaos,
                ChatColor.GRAY + "╰──────────────╯"
        };

        int score = lines.length;

        for (String line : lines) {
            String entry = ChatColor.values()[score].toString();

            Team team = board.registerNewTeam("line" + score);
            team.addEntry(entry);
            team.setPrefix(line.length() > 64
                    ? line.substring(0, 64)
                    : line);

            objective.getScore(entry).setScore(score);
            score--;
        }

        player.setScoreboard(board);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();

        if (killer != null) {
            killer.sendMessage(
                    ChatColor.GREEN + "Убийство засчитано!"
            );
        }
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    "Эту команду может использовать только игрок."
            );
            return true;
        }

        UUID uuid = player.getUniqueId();

        if (hidden.remove(uuid)) {
            updateScoreboard(player);
            player.sendMessage(
                    ChatColor.AQUA + "Скорборд включён."
            );
        } else {
            hidden.add(uuid);
            player.setScoreboard(
                    Bukkit.getScoreboardManager().getMainScoreboard()
            );
            player.sendMessage(
                    ChatColor.GRAY + "Скорборд выключен."
            );
        }

        return true;
    }
}
