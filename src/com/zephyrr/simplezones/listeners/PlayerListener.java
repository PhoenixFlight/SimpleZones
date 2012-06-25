package com.zephyrr.simplezones.listeners;

import com.zephyrr.simplezones.OwnedLand;
import com.zephyrr.simplezones.Plot;
import com.zephyrr.simplezones.Town;
import com.zephyrr.simplezones.ZonePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import com.zephyrr.simplezones.Channel;
import java.util.HashSet;

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
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(event.getClickedBlock() == null)
            return;
        OwnedLand ol = OwnedLand.getLandAtPoint(event.getClickedBlock().getLocation());
        if(ol == null || event.getClickedBlock().getType() != Material.CHEST)
            return;
        Town toCheck;
        if(ol instanceof Plot)
            toCheck = ((Plot)ol).getTown();
        else toCheck = (Town)ol;
        if(!toCheck.getOwner().equals(event.getPlayer().getName()) && !ol.getMembers().contains(event.getPlayer().getName()))
            event.setCancelled(true);
    }
    @EventHandler
    public void onChat(PlayerChatEvent event) {
        if(ZonePlayer.findUser(event.getPlayer()).getChannel() == Channel.TOWN) {
            event.setMessage(ChatColor.AQUA + event.getMessage());
            HashSet<Player> set = new HashSet<Player>();
            for(Player p : event.getRecipients()) {
                if(ZonePlayer.findUser(p).getTown() != ZonePlayer.findUser(event.getPlayer()).getTown())
                    set.add(p);
            }
            for(Player p : set)
                event.getRecipients().remove(p);
        }
    }
}
