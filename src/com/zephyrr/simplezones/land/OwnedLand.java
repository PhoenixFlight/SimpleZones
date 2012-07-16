package com.zephyrr.simplezones.land;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author Phoenix
 */
public abstract class OwnedLand {
    private static HashMap<Location, OwnedLand> locIndex;
    public static int YCHECK = 60;
    static {
        locIndex = new HashMap<Location, OwnedLand>();
    }
    public static void defineLocations(OwnedLand sender) {
        Location low = sender.getLowerBound();
        Location high = sender.getUpperBound();
        for(int r = low.getBlockX(); r <= high.getBlockX(); r++) {
            for(int c = low.getBlockZ(); c <= high.getBlockZ(); c++) {
                Location toPlace = new Location(low.getWorld(), r, YCHECK, c);
                if(getLandAtPoint(toPlace) == null || sender instanceof Plot)
                    locIndex.put(toPlace, sender);
            }
        }
    }
    public static boolean hasOverlap(Location first, Location second, boolean errorTowns) {
        int lowX = Math.min(first.getBlockX(), second.getBlockX());
        int lowZ = Math.min(first.getBlockZ(), second.getBlockZ());
        int highX = Math.max(first.getBlockX(), second.getBlockZ());
        int highZ = Math.max(first.getBlockZ(), second.getBlockZ());
        for(int r = lowX; r <= highX; r++) {
            for(int c = lowZ; c <= highZ; c++) {
                Location check = new Location(first.getWorld(), r, YCHECK, c);
                if(locIndex.containsKey(check))
                    if(!(locIndex.get(check) instanceof Town && errorTowns))
                    return true;
            }
        }
        return false;
    }
    public static void stripLocations(OwnedLand land) {
        for(int r = land.getLowerBound().getBlockX(); r <= land.getUpperBound().getBlockX(); r++) {
            for(int c = land.getLowerBound().getBlockZ(); c <= land.getUpperBound().getBlockZ(); c++) {
                Location ret = new Location(land.getLowerBound().getWorld(), r, YCHECK, c);
                locIndex.remove(ret);
            }
        }
    }
    public static OwnedLand getLandAtPoint(Location check) {
        Location checkFix = new Location(check.getWorld(), check.getBlockX(), OwnedLand.YCHECK, check.getBlockZ());
        return locIndex.get(checkFix);
    }

    private Location high, low;
    private int id;
    private ArrayList<String> members;
    public OwnedLand(int id, Location first, Location second) {
        this.id = id;
        members = new ArrayList<String>();
        setBounds(first, second);
        OwnedLand.defineLocations(this);
    }
    public void setBounds(Location first, Location second) {
        low = new Location(first.getWorld(),
                Math.min(first.getBlockX(), second.getBlockX()),
                OwnedLand.YCHECK,
                Math.min(first.getBlockZ(), second.getBlockZ()));
        high = new Location(first.getWorld(),
                Math.max(first.getBlockX(), second.getBlockX()),
                OwnedLand.YCHECK,
                Math.max(first.getBlockZ(), second.getBlockZ()));
    }
    public Location getLowerBound() {
        return low;
    }
    public Location getUpperBound() {
        return high;
    }
    public ArrayList<String> getMembers() {
        return members;
    }
    public boolean addMember(String name) {
        if(!members.contains(name)) {
            members.add(name);
            return true;
        }
        return false;
    }
    public boolean removeMember(String name) {
        return members.remove(name);
    }
    public boolean hasMember(String name) {
        return members.contains(name);
    }
    public int getID() {
        return id;
    }
    public abstract boolean canBuild(Player p);
}
