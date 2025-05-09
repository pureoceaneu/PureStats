package me.lakoba.pureOceanStats;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStatsManager {
    private final Map<UUID, PlayerStats> statsMap = new ConcurrentHashMap<>();
    private final Map<String, Long> lastActivityMap = new ConcurrentHashMap<>();
    private final Map<String, Long> lastStatusChangeMap = new ConcurrentHashMap<>();
    private final Map<String, Long> totalAfkTimeMap = new ConcurrentHashMap<>();
    private final Map<String, Long> totalActiveTimeMap = new ConcurrentHashMap<>();
    private final StorageManager storage;
    private final Plugin plugin;
    
    private long afkTimeoutSeconds;
    private boolean broadcastMessages;
    private String afkMessage;
    private String backMessage;
    private boolean detectMovement;
    private boolean detectHeadRotation;
    private boolean detectChat;

    public PlayerStatsManager(StorageManager storage, Plugin plugin) {
        this.storage = storage;
        this.plugin = plugin;
        this.statsMap.putAll(storage.loadStats());
        loadAfkConfig();
        
        for (PlayerStats stats : statsMap.values()) {
            String playerName = stats.getName();
            if (stats.getAfkTime() != null) {
                totalAfkTimeMap.put(playerName, stats.getAfkTime());
            } else {
                totalAfkTimeMap.put(playerName, 0L);
            }
            
            if (stats.getActiveTime() != null) {
                totalActiveTimeMap.put(playerName, stats.getActiveTime());
            } else {
                totalActiveTimeMap.put(playerName, 0L);
            }
            
            lastStatusChangeMap.put(playerName, System.currentTimeMillis());
        }
    }
    
    private void loadAfkConfig() {
        FileConfiguration config = plugin.getConfig();
        this.afkTimeoutSeconds = config.getLong("afk.timeout-seconds", 60);
        this.broadcastMessages = config.getBoolean("afk.broadcast-messages", true);
        this.afkMessage = config.getString("afk.afk-message", "§7[§6PureStats§7] §6{player} §7je teraz AFK");
        this.backMessage = config.getString("afk.back-message", "§7[§6PureStats§7] §6{player} §7už nie je AFK");
        this.detectMovement = config.getBoolean("afk.detect.movement", true);
        this.detectHeadRotation = config.getBoolean("afk.detect.head-rotation", true);
        this.detectChat = config.getBoolean("afk.detect.chat", true);
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
        String playerName = p.getName();
        boolean isAfk = isPlayerAfk(playerName);
        
        Long afkTime = totalAfkTimeMap.getOrDefault(playerName, 0L);
        Long activeTime = totalActiveTimeMap.getOrDefault(playerName, 0L);
        
        PlayerStats stats = new PlayerStats(
                uuid,
                playerName,
                p.getStatistic(Statistic.PLAYER_KILLS),
                p.getStatistic(Statistic.DEATHS),
                p.getStatistic(Statistic.PLAY_ONE_MINUTE),
                true,
                p.getWorld().getName(),
                System.currentTimeMillis(),
                p.getStatistic(Statistic.TIME_SINCE_DEATH),
                p.getStatistic(Statistic.MOB_KILLS),
                isAfk,
                afkTime,
                activeTime
        );
        statsMap.put(uuid, stats);
    }

    public void updateOfflinePlayer(OfflinePlayer p) {
        UUID uuid = p.getUniqueId();
        String path = uuid.toString();
        String playerName = p.getName();

        int kills = p.getStatistic(Statistic.PLAYER_KILLS);
        int deaths = p.getStatistic(Statistic.DEATHS);
        int playtime = p.getStatistic(Statistic.PLAY_ONE_MINUTE);
        String world = p.getLocation().getWorld() != null ? p.getLocation().getWorld().getName() : null;
        Long time = System.currentTimeMillis();
        int lastDeath = p.getStatistic(Statistic.TIME_SINCE_DEATH);
        int mobsKilled = p.getStatistic(Statistic.MOB_KILLS);

        FileConfiguration config = storage.getConfig();

        if (config.contains(path)) {
            world = config.getString(path + ".world", null);
        }

        Long afkTime = totalAfkTimeMap.getOrDefault(playerName, 0L);
        Long activeTime = totalActiveTimeMap.getOrDefault(playerName, 0L);

        PlayerStats stats = new PlayerStats(
                uuid,
                playerName,
                kills,
                deaths,
                playtime,
                false,
                world,
                time,
                lastDeath,
                mobsKilled,
                false,
                afkTime,
                activeTime
        );

        statsMap.put(uuid, stats);
    }

    public void updateOfflinePlayerAuto(OfflinePlayer p) {
        UUID uuid = p.getUniqueId();
        String path = uuid.toString();
        String playerName = p.getName();

        int kills = p.getStatistic(Statistic.PLAYER_KILLS);
        int deaths = p.getStatistic(Statistic.DEATHS);
        int playtime = p.getStatistic(Statistic.PLAY_ONE_MINUTE);
        String world = p.getLocation().getWorld() != null ? p.getLocation().getWorld().getName() : null;
        Long time = null;
        int lastDeath = p.getStatistic(Statistic.TIME_SINCE_DEATH);
        int mobsKilled = p.getStatistic(Statistic.MOB_KILLS);

        FileConfiguration config = storage.getConfig();

        if (config.contains(path)) {
            time = config.getLong(path + ".time");
            world = config.getString(path + ".world", null);
        }

        Long afkTime = config.contains(path + ".afkTime") ? 
                      config.getLong(path + ".afkTime") : 
                      totalAfkTimeMap.getOrDefault(playerName, 0L);
        
        Long activeTime = config.contains(path + ".activeTime") ? 
                         config.getLong(path + ".activeTime") : 
                         totalActiveTimeMap.getOrDefault(playerName, 0L);

        PlayerStats stats = new PlayerStats(
                uuid,
                playerName,
                kills,
                deaths,
                playtime,
                false,
                world,
                time,
                lastDeath,
                mobsKilled,
                false,
                afkTime,
                activeTime
        );

        statsMap.put(uuid, stats);
        
        totalAfkTimeMap.put(playerName, afkTime);
        totalActiveTimeMap.put(playerName, activeTime);
        
        lastActivityMap.remove(playerName);
        lastStatusChangeMap.remove(playerName);
    }
    
    public void updatePlayerActivity(String playerName) {
        if (playerName == null) return;
        
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) return;
        
        UUID uuid = player.getUniqueId();
        PlayerStats existingStats = statsMap.get(uuid);
        
        boolean wasAfk = (existingStats != null) && existingStats.isAfk();
        
        long currentTime = System.currentTimeMillis();
        if (lastStatusChangeMap.containsKey(playerName)) {
            long lastChange = lastStatusChangeMap.get(playerName);
            long timeDiff = currentTime - lastChange;
            
            if (wasAfk) {
                long currentAfkTime = totalAfkTimeMap.getOrDefault(playerName, 0L);
                totalAfkTimeMap.put(playerName, currentAfkTime + timeDiff);
            } else {
                long currentActiveTime = totalActiveTimeMap.getOrDefault(playerName, 0L);
                totalActiveTimeMap.put(playerName, currentActiveTime + timeDiff);
            }
        } else {
            totalAfkTimeMap.putIfAbsent(playerName, 0L);
            totalActiveTimeMap.putIfAbsent(playerName, 0L);
        }
        
        lastActivityMap.put(playerName, currentTime);
        
        boolean isAfk = false;
        
        if (wasAfk && !isAfk) {
            lastStatusChangeMap.put(playerName, currentTime);
            updatePlayer(player);
            
            if (broadcastMessages) {
                String message = backMessage.replace("{player}", playerName);
                Bukkit.broadcastMessage(message);
            }
        }
    }
    
    public boolean isPlayerAfk(String playerName) {
        if (!lastActivityMap.containsKey(playerName)) {
            return false;
        }
        
        long lastActivity = lastActivityMap.get(playerName);
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastActivity) > (afkTimeoutSeconds * 1000);
    }
    
    public void checkAndUpdateAfkStatus() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String playerName = player.getName();
            UUID uuid = player.getUniqueId();
            
            boolean wasAfk = false;
            PlayerStats existingStats = statsMap.get(uuid);
            if (existingStats != null) {
                wasAfk = existingStats.isAfk();
            }
            
            boolean isAfk = isPlayerAfk(playerName);
            
            if (wasAfk != isAfk) {
                long currentTime = System.currentTimeMillis();
                
                if (lastStatusChangeMap.containsKey(playerName)) {
                    long lastChange = lastStatusChangeMap.get(playerName);
                    long timeDiff = currentTime - lastChange;
                    
                    if (wasAfk) {
                        long currentAfkTime = totalAfkTimeMap.getOrDefault(playerName, 0L);
                        totalAfkTimeMap.put(playerName, currentAfkTime + timeDiff);
                    } else {
                        long currentActiveTime = totalActiveTimeMap.getOrDefault(playerName, 0L);
                        totalActiveTimeMap.put(playerName, currentActiveTime + timeDiff);
                    }
                }
                
                lastStatusChangeMap.put(playerName, currentTime);
                
                updatePlayer(player);
                
                if (broadcastMessages) {
                    String message = isAfk ? afkMessage : backMessage;
                    message = message.replace("{player}", playerName);
                    Bukkit.broadcastMessage(message);
                }
            } else {
                long currentTime = System.currentTimeMillis();
                
                if (lastStatusChangeMap.containsKey(playerName)) {
                    long lastChange = lastStatusChangeMap.get(playerName);
                    long timeDiff = currentTime - lastChange;
                    
                    if (timeDiff > 1000) {
                        if (isAfk) {
                            long currentAfkTime = totalAfkTimeMap.getOrDefault(playerName, 0L);
                            totalAfkTimeMap.put(playerName, currentAfkTime + timeDiff);
                        } else {
                            long currentActiveTime = totalActiveTimeMap.getOrDefault(playerName, 0L);
                            totalActiveTimeMap.put(playerName, currentActiveTime + timeDiff);
                        }
                        
                        lastStatusChangeMap.put(playerName, currentTime);
                        
                        updatePlayer(player);
                    }
                } else {
                    lastStatusChangeMap.put(playerName, currentTime);
                    totalAfkTimeMap.putIfAbsent(playerName, 0L);
                    totalActiveTimeMap.putIfAbsent(playerName, 0L);
                }
            }
        }
    }
    
    public long getPlayerAfkTime(String playerName) {
        return totalAfkTimeMap.getOrDefault(playerName, 0L);
    }
    
    public long getPlayerActiveTime(String playerName) {
        return totalActiveTimeMap.getOrDefault(playerName, 0L);
    }
    
    public boolean isDetectMovementEnabled() {
        return detectMovement;
    }
    
    public boolean isDetectHeadRotationEnabled() {
        return detectHeadRotation;
    }
    
    public boolean isDetectChatEnabled() {
        return detectChat;
    }

    public PlayerStats getPlayerStats(UUID uuid) {
        return statsMap.get(uuid);
    }

    public Collection<PlayerStats> getAllStats() {
        return statsMap.values();
    }

    public void save() {
        storage.saveStats(statsMap);
    }
}
