package me.lakoba.pureOceanStats;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStatsManager {
    private final Map<UUID, PlayerStats> statsMap = new ConcurrentHashMap<>();
    private final StorageManager storage;

    public PlayerStatsManager(StorageManager storage) {
        this.storage = storage;
        this.statsMap.putAll(storage.loadStats());
    }

    public void updateAll() {
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            updateOfflinePlayerAuto(player);
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayer(player);
        }
    }

    public void updatePlayer(Player p) {
        UUID uuid = p.getUniqueId();
        PlayerStats stats = new PlayerStats(
                uuid,
                p.getName(),
                p.getStatistic(Statistic.PLAYER_KILLS),
                p.getStatistic(Statistic.DEATHS),
                p.getStatistic(Statistic.PLAY_ONE_MINUTE),
                true,
                p.getWorld().getName(),
                System.currentTimeMillis(),
                p.getStatistic(Statistic.TIME_SINCE_DEATH),
                p.getStatistic(Statistic.MOB_KILLS)
        );
        statsMap.put(uuid, stats);
    }

    public void updateOfflinePlayer(OfflinePlayer p) {
        UUID uuid = p.getUniqueId();
        String path = uuid.toString();

        // Defaultní hodnoty
        String name = p.getName();
        int kills = p.getStatistic(Statistic.PLAYER_KILLS);
        int deaths = p.getStatistic(Statistic.DEATHS);
        int playtime = p.getStatistic(Statistic.PLAY_ONE_MINUTE);
        String world = p.getLocation().getWorld() != null ? p.getLocation().getWorld().getName() : null;
        Long time = System.currentTimeMillis();
        int lastDeath = p.getStatistic(Statistic.TIME_SINCE_DEATH);
        int mobsKilled = p.getStatistic(Statistic.MOB_KILLS);

        // Zkus načíst z configu, pokud existuje

        FileConfiguration config = storage.getConfig();

        if (config.contains(path)) {
            world = config.getString(path + ".world", null);
        }

        PlayerStats stats = new PlayerStats(
                uuid,
                name,
                kills,
                deaths,
                playtime,
                false,
                world,
                time,
                lastDeath,
                mobsKilled
        );

        statsMap.put(uuid, stats);
    }

    public void updateOfflinePlayerAuto(OfflinePlayer p) {
        UUID uuid = p.getUniqueId();
        String path = uuid.toString();

        // Defaultní hodnoty
        String name = p.getName();
        int kills = p.getStatistic(Statistic.PLAYER_KILLS);
        int deaths = p.getStatistic(Statistic.DEATHS);
        int playtime = p.getStatistic(Statistic.PLAY_ONE_MINUTE);
        String world = p.getLocation().getWorld() != null ? p.getLocation().getWorld().getName() : null;
        Long time = null;
        int lastDeath = p.getStatistic(Statistic.TIME_SINCE_DEATH);
        int mobsKilled = p.getStatistic(Statistic.MOB_KILLS);

        // Zkus načíst z configu, pokud existuje

        FileConfiguration config = storage.getConfig();

        if (config.contains(path)) {
            time = config.getLong(path + ".time");
            world = config.getString(path + ".world", null);
        }

        PlayerStats stats = new PlayerStats(
                uuid,
                name,
                kills,
                deaths,
                playtime,
                false,
                world,
                time,
                lastDeath,
                mobsKilled
        );

        statsMap.put(uuid, stats);
    }


    public Collection<PlayerStats> getAllStats() {
        return statsMap.values();
    }

    public void save() {
        storage.saveStats(statsMap);
    }
}
