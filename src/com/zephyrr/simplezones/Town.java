package com.zephyrr.simplezones;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import sqlibrary.Database;

/**
 *
 * @author Phoenix
 */
public class Town extends OwnedLand {
    private static HashMap<String, Town> townList;
    static {
        townList = new HashMap<String, Town>();
    }
    public static void addTown(Town t) {
        townList.put(t.getName(), t);
    }
    public static HashMap<String, Town> getTownList() {
        return townList;
    }
    public static Town getTown(String name) {
        if(townList.containsKey(name))
            return townList.get(name);
        return null;
    }
    public static void fill(Database db, String prefix) {
        try {
            ResultSet rs = db.query("SELECT * FROM " + prefix + "towns");
            while(rs.next()) {
                int id = rs.getInt("T_Id");
                String owner = rs.getString("Owner");
                String name = rs.getString("TownName");
                int lowX = rs.getInt("LowX");
                int lowZ = rs.getInt("LowZ");
                int highX = rs.getInt("HighX");
                int highZ = rs.getInt("HighZ");
                double warpX = rs.getInt("WarpX");
                double warpY = rs.getInt("WarpY");
                double warpZ = rs.getInt("WarpZ");
                String world = rs.getString("World");
                World w = SimpleZones.getWorld(world);
                Location warp = new Location(w, warpX, warpY, warpZ);
                String[] members = rs.getString("Members").split(",");
                Location low = new Location(w, lowX, OwnedLand.YCHECK, lowZ);
                Location high = new Location(w, highX, OwnedLand.YCHECK, highZ);
                Town t = new Town(id, low, high, name);
                t.setOwner(owner);
                t.setWarp(warp);
                for(String s : members)
                    t.addMember(s);
                townList.put(name, t);
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
    }
    public static void save(Database db, String prefix) {
        db.wipeTable(prefix + "towns");
        for(Town t : townList.values()) {
            String name = t.getName();
            String owner = t.getOwner();
            int id = t.getID();
            int lowX = t.getLowerBound().getBlockX();
            int lowZ = t.getLowerBound().getBlockZ();
            int highX = t.getUpperBound().getBlockX();
            int highZ = t.getUpperBound().getBlockZ();
            double warpX = t.getWarp().getX();
            double warpY = t.getWarp().getY();
            double warpZ = t.getWarp().getZ();
            String world = t.getWarp().getWorld().getName();
            String members = "";
            for(String s : t.getMembers())
                members += s + ",";
            if(!members.isEmpty())
                members = members.substring(0, members.length() - 1);
            String query = "INSERT INTO " + prefix + "towns VALUES("
                    + id + ","
                    + "'" + owner + "',"
                    + "'" + name + "',"
                    + lowX + ","
                    + lowZ + ","
                    + highX + ","
                    + highZ + ","
                    + warpX + ","
                    + warpY + ","
                    + warpZ + ","
                    + "'" + world + "',"
                    + "'" + members + "'"
                    + ")";
            db.query(query);
        }
    }
    public static void fillBans(Database db, String prefix) {
        ResultSet rs = db.query("SELECT * FROM " + prefix + "bans");
        try {
            while(rs.next()) {
                String user = rs.getString("User");
                ResultSet rs2 = db.query("SELECT TownName FROM " + prefix + "towns WHERE T_Id=" + rs.getInt("TownID"));
                rs2.next();
                Town t = Town.getTown(rs2.getString("TownName"));
                t.addBan(ZonePlayer.findUser(user));
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
    }
    public static void saveBans(Database db, String prefix) {
        db.wipeTable(prefix + "bans");
        int id = 0;
        for(Town t : townList.values()) {
            for(ZonePlayer z : t.getBans()) {
                String name = z.getName();
                int town = t.getID();
                String query = "INSERT INTO " + prefix + "bans VALUES ("
                        + id + ","
                        + "'" + name + "',"
                        + town
                        + ")";
                db.query(query);
                id++;
            }
        }
    }
    private String name, owner;
    private ArrayList<ZonePlayer> bans;
    private Location warp;
    private ArrayList<Plot> plots;
    public Town(int id, Location first, Location second, String name) {
        super(id, first, second);
        this.name = name;
        bans = new ArrayList<ZonePlayer>();
        plots = new ArrayList<Plot>();
    }
    public void addPlot(Plot p) {
        plots.add(p);
    }
    public ArrayList<Plot> getPlots() {
        return plots;
    }
    public String getName() {
        return name;
    }
    public String getOwner() {
        return owner;
    }
    public void setOwner(String newOwner) {
        if(owner == null) {
            owner = newOwner;
            return;
        }
        ZonePlayer zp = ZonePlayer.findUser(newOwner);
        if(zp == null) {
            ZonePlayer.findUser(owner).getPlayer().sendMessage(ChatColor.RED + "[SimpleZones] " + newOwner + " does not exist.");
            return;
        }
        if(zp.getTown() != this) {
            ZonePlayer.findUser(owner).getPlayer().sendMessage(ChatColor.RED + "[SimpleZones] " + newOwner + " is not a member of this town.");
            return;
        }
        addMember(newOwner);
        if(zp.getPlayer().isOnline())
            zp.getPlayer().sendMessage(ChatColor.GOLD + "[SimpleZones] You are the new owner of " + name);
        ZonePlayer.findUser(owner).getPlayer().sendMessage(ChatColor.GOLD + "[SimpleZones] You no longer own " + name);
        owner = newOwner;
    }
    public boolean canBuild(Player p) {
        ZonePlayer zp = ZonePlayer.findUser(p);
        if(owner.equals(zp.getName()))
            return true;
        return false;
    }
    public ArrayList<ZonePlayer> getBans() {
        return bans;
    }
    public boolean addBan(ZonePlayer zp) {
        if(bans.contains(zp))
            return false;
        bans.add(zp);
        removeMember(zp.getName());
        for(Plot p : plots) {
            p.removeMember(zp.getName());
        }
        return true;
    }
    public boolean unban(ZonePlayer zp) {
        return bans.remove(zp);
    }
    public Location getWarp() {
        return warp;
    }
    public void setWarp(Location warp) {
        this.warp = warp;
    }
}
