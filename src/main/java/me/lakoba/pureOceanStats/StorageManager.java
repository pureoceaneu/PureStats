package me.lakoba.pureOceanStats;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StorageManager {
    private final File file;
    private final FileConfiguration config;

    public StorageManager(File dataFolder) {
        this.file = new File(dataFolder, "players.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public Map<UUID, PlayerStats> loadStats() {
        Map<UUID, PlayerStats> map = new HashMap<>();
        for (String key : config.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            String name = config.getString(key + ".name");
            int kills = config.getInt(key + ".kills");
            int deaths = config.getInt(key + ".deaths");
            int playtime = config.getInt(key + ".playtime");
            boolean isOnline = config.getBoolean(key + ".isOnline");
            String world = config.getString(key + ".world");
            Long time = config.getLong(key + ".time");
            int lastDeath = config.getInt(key + ".lastDeath");
            int mobsKilled = config.getInt(key + ".mobsKilled");
            boolean isAfk = config.getBoolean(key + ".isAfk", false);
            Long afkTime = config.getLong(key + ".afkTime", 0L);
            Long activeTime = config.getLong(key + ".activeTime", 0L);
            
            map.put(uuid, new PlayerStats(uuid, name, kills, deaths, playtime, isOnline, world, time, lastDeath, mobsKilled, isAfk, afkTime, activeTime));
        }
        return map;
    }

    public void saveStats(Map<UUID, PlayerStats> statsMap) {
        for (Map.Entry<UUID, PlayerStats> entry : statsMap.entrySet()) {
            UUID uuid = entry.getKey();
            PlayerStats stats = entry.getValue();
            String path = uuid.toString();
            config.set(path + ".name", stats.getName());
            config.set(path + ".kills", stats.getKills());
            config.set(path + ".deaths", stats.getDeaths());
            config.set(path + ".playtime", stats.getPlaytimeTicks());
            config.set(path + ".isOnline", stats.isOnline());
            if (stats.getWorld() == null) {
                System.out.println("⚠️ World je null u hráče: " + stats.getName());
            } else {
                config.set(path + ".world", stats.getWorld());
            }
            config.set(path + ".time", stats.getLastPlayed());
            config.set(path + ".lastDeath", stats.getLastDeath());
            config.set(path + ".mobsKilled", stats.getMobsKilled());
            config.set(path + ".isAfk", stats.isAfk());
            config.set(path + ".afkTime", stats.getAfkTime());
            config.set(path + ".activeTime", stats.getActiveTime());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
