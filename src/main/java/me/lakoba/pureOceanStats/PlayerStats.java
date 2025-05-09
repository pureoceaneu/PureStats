package me.lakoba.pureOceanStats;

import java.util.UUID;

public class PlayerStats {
    private final UUID uuid;
    private final String name;
    private final int kills;
    private final int deaths;
    private final int playtimeTicks;
    private final Boolean isOnline;
    private final String world;
    private final Long lastPlayed;
    private final Integer lastDeath;
    private final Integer mobsKilled;
    private final Boolean isAfk;
    private final Long afkTime;
    private final Long activeTime;

    public PlayerStats(UUID uuid, String name, int kills, int deaths, int playtimeTicks, boolean isOnline, String world, 
                      Long lastPlayed, int lastDeath, int mobsKilled, boolean isAfk, Long afkTime, Long activeTime) {
        this.uuid = uuid;
        this.name = name;
        this.kills = kills;
        this.deaths = deaths;
        this.playtimeTicks = playtimeTicks;
        this.isOnline = isOnline;
        this.world = world;
        this.lastPlayed = lastPlayed;
        this.lastDeath = lastDeath;
        this.mobsKilled = mobsKilled;
        this.isAfk = isAfk;
        this.afkTime = afkTime;
        this.activeTime = activeTime;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getPlaytimeTicks() {
        return playtimeTicks;
    }

    public String getAvatarUrl() {
        return "https://cravatar.eu/avatar/" + name.toString();
    }

    public Boolean isOnline() {
        return isOnline;
    }

    public String getWorld() {
        return world;
    }

    public Long getLastPlayed() {
        return lastPlayed;
    }

    public Integer getLastDeath() {
        return lastDeath;
    }

    public Integer getMobsKilled() {
        return mobsKilled;
    }

    public Boolean isAfk() {
        return isAfk;
    }

    public Long getAfkTime() {
        return afkTime;
    }

    public Long getActiveTime() {
        return activeTime;
    }

    public double getActiveTimeRatio() {
        long totalTime = activeTime + afkTime;
        if (totalTime == 0) return 1.0;
        return (double) activeTime / totalTime;
    }
}
