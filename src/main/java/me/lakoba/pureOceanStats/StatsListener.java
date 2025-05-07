package me.lakoba.pureOceanStats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class StatsListener implements Listener {
    private final PlayerStatsManager manager;

    public StatsListener(PlayerStatsManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        manager.updatePlayer(e.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        manager.updatePlayer(e.getEntity());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        manager.updateOfflinePlayer(e.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerTeleportEvent e) {
        if (e.getTo() != null) {
            manager.updatePlayer(e.getPlayer());
        }
    }

}
