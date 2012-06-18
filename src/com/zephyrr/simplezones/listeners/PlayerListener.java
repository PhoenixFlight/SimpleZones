package com.zephyrr.simplezones.listeners;

import com.zephyrr.simplezones.OwnedLand;
import com.zephyrr.simplezones.Plot;
import com.zephyrr.simplezones.Town;
import com.zephyrr.simplezones.ZonePlayer;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 *
 * @author Phoenix
 */
public class PlayerListener implements Listener {
    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        if(ZonePlayer.findUser(event.getPlayer().getName()) == null) {
            ZonePlayer.registerUser(event.getPlayer());
        }
        ZonePlayer.findUser(event.getPlayer()).setPlayer(event.getPlayer());
    }
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        OwnedLand from = OwnedLand.getLandAtPoint(event.getFrom());
        OwnedLand to = OwnedLand.getLandAtPoint(event.getTo());
        if(from != to) {
            if(from instanceof Town) {
                if(to instanceof Plot)
                    if(((Plot)to).getTown() == from)
                        return;
                event.getPlayer().sendMessage(ChatColor.GOLD + "[SimpleZones] You are now leaving " + ((Town)from).getName());
            }
            if(to instanceof Town) {
                if(from instanceof Plot)
                    if(((Plot)from).getTown() == to)
                        return;
                ZonePlayer zp = ZonePlayer.findUser(event.getPlayer());
                if(!event.getPlayer().isOp() && ((Town)to).getBans().contains(zp)) {
                    event.getPlayer().sendMessage(ChatColor.RED + "[SimpleZones] You have been banned from accessing " + ((Town)to).getName());
                    event.setCancelled(true);
                } else event.getPlayer().sendMessage(ChatColor.GOLD + "[SimpleZones] You are now entering " + ((Town)to).getName());
            }
        }
    }
}
