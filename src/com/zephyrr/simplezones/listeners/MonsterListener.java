package com.zephyrr.simplezones.listeners;

import com.zephyrr.simplezones.land.OwnedLand;
import com.zephyrr.simplezones.land.Plot;
import com.zephyrr.simplezones.land.Sanctuary;
import com.zephyrr.simplezones.land.Town;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 *
 * @author Peter
 */
public class MonsterListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        OwnedLand land = OwnedLand.getLandAtPoint(event.getLocation());
        if(land == null || land instanceof Sanctuary)
            return;
        if(land instanceof Plot)
            land = ((Plot)land).getTown();
        if(((Town)land).isBlocked(event.getEntityType()))
            event.setCancelled(true);
        else event.setCancelled(false);
    }
}
