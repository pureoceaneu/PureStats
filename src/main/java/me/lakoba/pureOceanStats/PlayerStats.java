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

    public PlayerStats(UUID uuid, String name, int kills, int deaths, int playtimeTicks, boolean isOnline, String world, Long lastPlayed, int lastDeath, int mobsKilled) {
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
        return "https://crafatar.com/avatars/" + uuid.toString();
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
}
