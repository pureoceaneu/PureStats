package me.lakoba.pureOceanStats;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class StatsListener implements Listener {
    private final PlayerStatsManager manager;

    public StatsListener(PlayerStatsManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        manager.updatePlayerActivity(e.getPlayer().getName());
        manager.updatePlayer(e.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        manager.updatePlayerActivity(e.getEntity().getName());
        manager.updatePlayer(e.getEntity());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        manager.updateOfflinePlayer(e.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerTeleportEvent e) {
        if (e.getTo() != null) {
            manager.updatePlayerActivity(e.getPlayer().getName());
            manager.updatePlayer(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();
        
        if (to == null) return;
        
        boolean isBodyMovement = from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ();
        boolean isHeadRotation = from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch();
        
        if ((isBodyMovement && manager.isDetectMovementEnabled()) || 
            (isHeadRotation && manager.isDetectHeadRotationEnabled())) {
            manager.updatePlayerActivity(e.getPlayer().getName());
        }
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (manager.isDetectChatEnabled()) {
            manager.updatePlayerActivity(e.getPlayer().getName());
        }
    }
}
