package com.zephyrr.simplezones.land;

import com.zephyrr.simplezones.SimpleZones;
import com.zephyrr.simplezones.ZonePlayer;
import com.zephyrr.simplezones.flags.FlagSet;
import com.zephyrr.simplezones.ymlIO.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
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
        if(db == null) {
            fillYML();
            return;
        }
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
                String monsters = rs.getString("Monsters");
                String animals = rs.getString("Animals");
                String blocks = rs.getString("Blocks");
                boolean fire = rs.getBoolean("Fire");
                boolean explode = rs.getBoolean("Bomb");
                World w = SimpleZones.getWorld(world);
                Location warp = new Location(w, warpX, warpY, warpZ);
                String[] members = rs.getString("Members").split(",");
                String[] supers = rs.getString("SuperUsers").split(",");
                Location low = new Location(w, lowX, OwnedLand.YCHECK, lowZ);
                Location high = new Location(w, highX, OwnedLand.YCHECK, highZ);
                Town t = new Town(id, low, high, name);
                t.setOwner(owner);
                t.setWarp(warp);
                for(String s : members)
                    t.addMember(s);
                for(String s : supers)
                	t.modSuper(s, true);
                t.putFlags(animals, monsters, blocks, fire, explode);
                townList.put(name, t);
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
    }
    public static void save(Database db, String prefix) {
        if(db == null) {
            saveYML();
            return;
        }
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
                    + "'" + members + "',"
                    + "'" + t.getData('a') + "',"
                    + "'" + t.getData('m') + "',"
                    + "'" + t.getData('b') + "',"
                    + "" + t.getData('f') + ","
                    + "" + t.getData('e') + ","
                    + "" + t.supers.toString().substring(1, t.supers.toString().length() - 1) + ""
                    + ")";
            db.query(query);
        }
    }

    public static void fillYML() {
        File in = new File("plugins/SimpleZones/towns.yml");
        if(!in.exists())
            return;
        try {
            InputStream input = new FileInputStream(in);
            Yaml yaml = new Yaml(new CustomClassLoaderConstructor(TownYml.class.getClassLoader()));
            for(Object o : yaml.loadAll(input)) {
                TownYml ty = (TownYml)o;
                Location low = new Location(SimpleZones.getWorld(ty.world), ty.lowX, OwnedLand.YCHECK, ty.lowZ);
                Location high = new Location(SimpleZones.getWorld(ty.world), ty.highX, OwnedLand.YCHECK, ty.highZ);
                Location warp = new Location(low.getWorld(), ty.warpX, ty.warpY, ty.warpZ);
                int id = ty.tid;
                String[] members = ty.members.split(",");
                String[] supers = ty.supers.split(",");
                Town t = new Town(id, low, high, ty.name);
                for(String s : members)
                    t.addMember(s.replaceAll(" ", ""));
                for(String s : supers)
                	t.modSuper(s, true);
                t.setOwner(ty.owner);
                t.setWarp(warp);
                t.putFlags(ty.animals, ty.monsters, ty.blocks, ty.fire, ty.bomb);
                townList.put(ty.name, t);
            }
            input.close();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void saveYML() {
        ArrayList<TownYml> val = new ArrayList<TownYml>();
        for(Town t : Town.getTownList().values()) {
            TownYml ty = new TownYml();
            ty.highX = t.getUpperBound().getBlockX();
            ty.highZ = t.getUpperBound().getBlockZ();
            ty.lowX = t.getLowerBound().getBlockX();
            ty.lowZ = t.getLowerBound().getBlockZ();
            ty.members = t.getMembers().toString().substring(1, t.getMembers().toString().length() - 1);
            ty.name = t.getName();
            ty.owner = t.getOwner();
            ty.tid = t.getID();
            ty.warpX = t.getWarp().getX();
            ty.warpY = t.getWarp().getY();
            ty.warpZ = t.getWarp().getZ();
            ty.world = t.getWarp().getWorld().getName();
            ty.supers = t.supers.toString().substring(1, t.supers.toString().length() - 1);
            ty.animals = t.getData('a');
            ty.blocks = t.getData('b');
            ty.bomb = Boolean.parseBoolean(t.getData('e'));
            ty.fire = Boolean.parseBoolean(t.getData('f'));
            ty.monsters = t.getData('m');
            val.add(ty);
        }
        try {
            File out = new File("plugins/SimpleZones/towns.yml");
            if(!out.exists())
                out.createNewFile();
            FileWriter fw = new FileWriter(out);
            Yaml yml = new Yaml();
            yml.dumpAll(val.iterator(), fw);
            fw.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void fillBans(Database db, String prefix) {
        if(db == null) {
            fillBansYML();
            return;
        }
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
        if(db == null) {
            saveBansYML();
            return;
        }
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
    public static void fillBansYML() {
        File in = new File("plugins/SimpleZones/bans.yml");
        if(!in.exists())
            return;
        try {
            InputStream fis = new FileInputStream(in);
            Yaml yaml = new Yaml(new CustomClassLoaderConstructor(BanYml.class.getClassLoader()));
            for(Object o : yaml.loadAll(fis)) {
                BanYml ban = (BanYml)o;
                String user = ban.user;
                int tid = ban.tid;
                for(Town t : Town.townList.values())
                    if(t.getID() == tid)
                        t.addBan(ZonePlayer.findUser(user));
            }
            fis.close();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
    public static void saveBansYML() {
        ArrayList<BanYml> list = new ArrayList<BanYml>();
        for(Town t : Town.getTownList().values()) {
            for(ZonePlayer zp : t.getBans()) {
                BanYml yml = new BanYml();
                yml.tid = t.getID();
                yml.user = zp.getName();
                list.add(yml);
            }
        }
        try {
            File out = new File("plugins/SimpleZones/bans.yml");
            if(!out.exists())
                out.createNewFile();
            Yaml yml = new Yaml();
            FileWriter fw = new FileWriter(out);
            yml.dumpAll(list.iterator(), fw);
            fw.close();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
    private String name, owner;
    private ArrayList<ZonePlayer> bans;
    private ArrayList<String> supers;
    private Location warp;
    private ArrayList<Plot> plots;
    private FlagSet flags;
    public Town(int id, Location first, Location second, String name) {
        super(id, first, second);
        this.name = name;
        supers = new ArrayList<String>();
        bans = new ArrayList<ZonePlayer>();
        plots = new ArrayList<Plot>();
        flags = new FlagSet();
    }
    public void putFlags(String animals, String monsters, String blocks, boolean fire, boolean bomb) {
        flags.loadStarts(animals, monsters, blocks, fire, bomb);
    }
    public boolean isSuper(ZonePlayer zp) {
    	return supers.contains(zp);
    }
    public boolean modSuper(String zp, boolean change) {
    	if(change)
    		return supers.add(zp);
    	return supers.remove(zp);
    }
    public boolean isBlocked(Object o) {
        return flags.isBlocked(o);
    }
    public String getData(char flag) {
        return flags.getData(flag);
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
        return supers.contains(zp.getName());
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
    public boolean setFlag(String s) {
        return flags.setFlag(s);
    }
}
