package com.zephyrr.simplezones.listeners;

import com.zephyrr.simplezones.OwnedLand;
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
        }
        OwnedLand owned = OwnedLand.getLandAtPoint(event.getBlock().getLocation());
        if(owned == null)
            return;
        if(!owned.canBuild(event.getPlayer()))
            event.setCancelled(true);
    }
    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if(ZonePlayer.findUser(event.getPlayer()).isDefining()) {
            ZonePlayer.findUser(event.getPlayer()).setCorner(event.getBlock().getLocation());
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
        if(owned == null)
            return;
        if(!owned.canBuild(event.getPlayer()))
            event.setCancelled(true);
    }
}
