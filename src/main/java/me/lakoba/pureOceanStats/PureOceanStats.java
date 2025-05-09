package me.lakoba.pureOceanStats;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import spark.Spark;

public final class PureOceanStats extends JavaPlugin {

    private PlayerStatsManager statsManager;
    private long startTime;

    @Override
    public void onEnable() {
        getLogger().info("[PureOceanStats] Plugin enabled");
        this.startTime = System.currentTimeMillis();

        saveDefaultConfig();
        
        StorageManager storage = new StorageManager(getDataFolder());
        this.statsManager = new PlayerStatsManager(storage, this);
        statsManager.updateAll();

        Bukkit.getPluginManager().registerEvents(new StatsListener(statsManager), this);

        new BukkitRunnable() {
            @Override
            public void run() {
                statsManager.updateAll();
                statsManager.save();
                getLogger().info("[PureOceanStats] Autosave complete.");
            }
        }.runTaskTimer(this, 0, 6000);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                statsManager.checkAndUpdateAfkStatus();
            }
        }.runTaskTimer(this, 100, 100);

        Spark.port(4576);

        Spark.get("/api/stats", (req, res) -> {
            res.type("application/json");
            JsonObject json = new JsonObject();
            json.addProperty("playersOnline", Bukkit.getOnlinePlayers().size());
            json.addProperty("uptimeSeconds", (System.currentTimeMillis() - startTime) / 1000);
            return json.toString();
        });

        Spark.get("/api/players", (req, res) -> {
            res.type("application/json");
            JsonArray arr = new JsonArray();
            for (PlayerStats stats : statsManager.getAllStats()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("uuid", stats.getUuid().toString());
                obj.addProperty("name", stats.getName());
                obj.addProperty("kills", stats.getKills());
                obj.addProperty("deaths", stats.getDeaths());
                obj.addProperty("playtimeTicks", stats.getPlaytimeTicks());
                obj.addProperty("avatarUrl", stats.getAvatarUrl());
                obj.addProperty("isOnline", stats.isOnline());
                obj.addProperty("isAfk", stats.isAfk());
                obj.addProperty("afkTimeMs", stats.getAfkTime());
                obj.addProperty("activeTimeMs", stats.getActiveTime());
                obj.addProperty("afkTimeSec", stats.getAfkTime() / 1000);
                obj.addProperty("activeTimeSec", stats.getActiveTime() / 1000);
                obj.addProperty("activeTimeRatio", stats.getActiveTimeRatio());
                obj.addProperty("world", stats.getWorld());
                obj.addProperty("lastPlayed", stats.getLastPlayed());
                obj.addProperty("lastDeath", stats.getLastDeath());
                obj.addProperty("mobsKilled", stats.getMobsKilled());
                arr.add(obj);
            }
            JsonObject json = new JsonObject();
            json.add("players", arr);
            return json.toString();
        });
    }

    @Override
    public void onDisable() {
        Spark.stop();
        statsManager.save();
        getLogger().info("[PureOceanStats] Plugin disabled");
    }
}
