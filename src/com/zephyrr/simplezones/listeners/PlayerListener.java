package com.zephyrr.simplezones.listeners;

import com.zephyrr.simplezones.ZonePlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import com.zephyrr.simplezones.Channel;
import com.zephyrr.simplezones.land.*;

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
        	if(from != null && from.getLandType() == LandType.OUTPOST)
        		event.getPlayer().sendMessage(ChatColor.GOLD + "[SimpleZones] You are now leaving an outpost.");
        	if(to != null && to.getLandType() == LandType.OUTPOST)
        		event.getPlayer().sendMessage(ChatColor.GOLD + "[SimpleZones] You are now entering an outpost belonging to " + ((Outpost)to).getOwner());
        	if(from != null && from.getLandType() == LandType.SANCTUARY) {
        		event.getPlayer().sendMessage(ChatColor.GOLD + "[SimpleZones] You are now leaving a Sanctuary.");
        	}
            if(from != null && from.getLandType() == LandType.TOWN) {
                if(to != null && to.getLandType() == LandType.PLOT)
                    if(((Plot)to).getTown() == from)
                        return;
                event.getPlayer().sendMessage(ChatColor.GOLD + "[SimpleZones] You are now leaving " + ((Town)from).getName());
            }
            if(to != null && to.getLandType() == LandType.SANCTUARY) {
            	event.getPlayer().sendMessage(ChatColor.GOLD + "[SimpleZones] You are now entering a Sanctuary.");
            }
            if(to != null && to.getLandType() == LandType.TOWN) {
                if(from != null && from.getLandType() == LandType.PLOT)
                    if(((Plot)from).getTown() == to)
                        return;
                ZonePlayer zp = ZonePlayer.findUser(event.getPlayer());
                if(!event.getPlayer().isOp() && ((Town)to).getBans().contains(zp)) {
                    event.getPlayer().sendMessage(ChatColor.RED + "[SimpleZones] You have been banned from accessing " + ((Town)to).getName());
                    event.setCancelled(true);
                } else event.getPlayer().sendMessage(ChatColor.GOLD + "[SimpleZones] " + ((Town)to).getEntryMessage());
            }
        }
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(event.getClickedBlock() == null)
            return;
        OwnedLand ol = OwnedLand.getLandAtPoint(event.getClickedBlock().getLocation());
        if(ol == null)
            return;
        String owner = "";
        if(ol.getLandType() == LandType.PLOT)
            owner = ((Plot)ol).getTown().getOwner();
        else if(ol.getLandType() == LandType.SANCTUARY)
        	return;
        else if(ol.getLandType() == LandType.TOWN)
        	owner = ((Town)ol).getOwner();
        else if(ol.getLandType() == LandType.OUTPOST)
        	owner = ((Outpost)ol).getOwner();
        event.getPlayer().sendMessage(ol.getMembers().toString());
        if(!(owner.equals(event.getPlayer().getName()) || ol.getMembers().contains(event.getPlayer().getName())))
            event.setCancelled(true);
    }
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
    	OwnedLand ol = OwnedLand.getLandAtPoint(event.getEntity().getLocation());
    	if(ol == null)
    		return;
    	if(ol.isBlocked(event))
    		event.setCancelled(true);
    }
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
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
