/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.zephyrr.simplezones.listeners;

import com.zephyrr.simplezones.OwnedLand;
import com.zephyrr.simplezones.Plot;
import com.zephyrr.simplezones.SimpleZones;
import com.zephyrr.simplezones.Town;
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
        if(land == null)
            return;
        if(land instanceof Plot)
            land = ((Plot)land).getTown();
        if(((Town)land).isBlocked(event.getEntityType()))
            event.setCancelled(true);
        else event.setCancelled(false);
    }
}
