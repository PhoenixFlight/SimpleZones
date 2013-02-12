package com.zephyrr.simplezones.listeners;

import com.zephyrr.simplezones.land.OwnedLand;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

/**
 *
 * @author Phoenix
 */
public class DestructoListener implements Listener {
    @EventHandler
    public void onSpread(BlockSpreadEvent event) {
        if(event.getSource().getType() != Material.FIRE)
            return;
        OwnedLand owned = OwnedLand.getLandAtPoint(event.getSource().getLocation());
        if(owned == null)
            return;
        if(owned.isBlocked(event))
            event.setCancelled(true);
    }
    @EventHandler
    public void onBurn(BlockBurnEvent event) {
        OwnedLand owned = OwnedLand.getLandAtPoint(event.getBlock().getLocation());
        if(owned == null)
            return;
        if(owned.isBlocked(event))
            event.setCancelled(true);
    }
    @EventHandler
    public void onBoom(ExplosionPrimeEvent event) {
        OwnedLand owned = OwnedLand.getLandAtPoint(event.getEntity().getLocation());
        if(owned == null)
            return;
        if(owned.isBlocked(event))
            event.setRadius(0.0f);
        
    }
}
