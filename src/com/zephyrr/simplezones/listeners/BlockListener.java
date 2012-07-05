package com.zephyrr.simplezones.listeners;

import com.zephyrr.simplezones.OwnedLand;
import com.zephyrr.simplezones.Plot;
import com.zephyrr.simplezones.SimpleZones;
import com.zephyrr.simplezones.Town;
import com.zephyrr.simplezones.ZonePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 *
 * @author Phoenix
 */
public class BlockListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(ZonePlayer.findUser(event.getPlayer()).isDefining()) {
            ZonePlayer.findUser(event.getPlayer()).setCorner(event.getBlock().getLocation());
            event.setCancelled(true);
            return;
        }
        OwnedLand owned = OwnedLand.getLandAtPoint(event.getBlock().getLocation());
        if(owned == null) {
        	if(!SimpleZones.getPlugConfig().getBoolean("wild.break"))
        		event.setCancelled(true);
            return;
        }
        if(!owned.canBuild(event.getPlayer()))
            event.setCancelled(true);
    }
    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if(ZonePlayer.findUser(event.getPlayer()).isDefining()) {
            ZonePlayer.findUser(event.getPlayer()).setCorner(event.getBlock().getLocation());
            event.setCancelled(true);
            return;
        }
        OwnedLand owned = OwnedLand.getLandAtPoint(event.getBlock().getLocation());
        if(owned == null)
            return;
        if(!owned.canBuild(event.getPlayer()))
            event.setCancelled(true);
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        OwnedLand owned = OwnedLand.getLandAtPoint(event.getBlock().getLocation());
        if(owned == null) {
        	if(!SimpleZones.getPlugConfig().getBoolean("wild.build"))
        		event.setCancelled(true);
            return;
        }
        if(!owned.canBuild(event.getPlayer())) {
            event.setCancelled(true);
            return;
        }
        if(owned instanceof Plot)
            owned = ((Plot)owned).getTown();
        if(((Town)owned).isBlocked(event.getBlock().getType()))
            event.setCancelled(true);
    }
}
