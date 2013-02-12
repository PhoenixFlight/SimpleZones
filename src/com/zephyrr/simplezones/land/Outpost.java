package com.zephyrr.simplezones.land;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

import com.zephyrr.simplezones.SimpleZones;
import com.zephyrr.simplezones.flags.FlagSet;
import com.zephyrr.simplezones.ymlIO.OutpostYml;

import sqlibrary.Database;

public class Outpost extends OwnedLand {
	private static HashMap<Integer, Outpost> outposts;
	static {
		outposts = new HashMap<Integer, Outpost>();
	}
	
	public static HashMap<Integer, Outpost> getOutposts() {
		return outposts;
	}
	
	public static void fill(Database db, String prefix) {
		if(db == null) {
			fillYML();
			return;
		}
		try {
            ResultSet rs = db.query("SELECT * FROM " + prefix + "outposts");
            while(rs.next()) {
            	int id = rs.getInt("O_Id");
            	Location first = new Location(
            			SimpleZones.getWorld(rs.getString("World")),
            			rs.getInt("LowX"),
            			OwnedLand.YCHECK,
            			rs.getInt("LowZ")
            			);
            	Location second = new Location(
            			SimpleZones.getWorld(rs.getString("World")),
            			rs.getInt("HighX"),
            			OwnedLand.YCHECK,
            			rs.getInt("HighZ")
            			);
            	Outpost out = new Outpost(id, first, second);
            	String members = rs.getString("Members");
            	for(String s : members.split(","))
            		out.addMember(s);
            	out.setOwner(rs.getString("Owner"));
            	out.putFlags(rs.getString("Animals"),
            			rs.getString("Monsters"), 
            			rs.getString("Blocks"), 
            			rs.getBoolean("Fire"), 
            			rs.getBoolean("Bomb"), 
            			rs.getBoolean("PvP"));
            	outposts.put(out.getID(), out);
            }
		} catch(SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	private static void fillYML() {
        File in = new File("plugins/SimpleZones/outposts.yml");
        if(!in.exists())
            return;
        try {
            InputStream fis = new FileInputStream(in);
            Yaml yaml = new Yaml(new CustomClassLoaderConstructor(OutpostYml.class.getClassLoader()));
            for(Object o : yaml.loadAll(fis)) {
                OutpostYml outpost = (OutpostYml)o;
                Location first = new Location(
                		SimpleZones.getWorld(outpost.world),
                		outpost.lowX,
                		OwnedLand.YCHECK,
                		outpost.lowZ);
                Location second = new Location(
                		SimpleZones.getWorld(outpost.world),
                		outpost.highX,
                		OwnedLand.YCHECK,
                		outpost.highZ);
                Outpost out = new Outpost(outpost.oid, first, second);
                out.setOwner(outpost.owner);
                out.putFlags(outpost.animals, outpost.monsters, outpost.blocks, outpost.fire, outpost.bomb, outpost.pvp);
                for(String s : outpost.members.split(","))
                	out.addMember(s);
                outposts.put(out.getID(), out);
            }
            fis.close();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
	}
	
	public static void save(Database db, String prefix) {
        if(db == null) {
            saveYML();
            return;
        }
        db.wipeTable(prefix + "outposts");
        for(Outpost t : outposts.values()) {
            String owner = t.getOwner();
            int id = t.getID();
            int lowX = t.getLowerBound().getBlockX();
            int lowZ = t.getLowerBound().getBlockZ();
            int highX = t.getUpperBound().getBlockX();
            int highZ = t.getUpperBound().getBlockZ();
            String world = t.getUpperBound().getWorld().getName();
            String members = "";
            for(String s : t.getMembers())
                members += s + ",";
            if(!members.isEmpty())
                members = members.substring(0, members.length() - 1);
            String query = "INSERT INTO " + prefix + "towns VALUES("
                    + id + ","
                    + "'" + owner + "',"
                    + lowX + ","
                    + highX + ","
                    + lowZ + ","
                    + highZ + ","
                    + "'" + world + "',"
                    + "'" + t.getData('a') + "',"
                    + "'" + t.getData('m') + "',"
                    + "'" + t.getData('b') + "',"
                    + "" + t.getData('f') + ","
                    + "" + t.getData('e') + ","
                    + "" + t.getData('p') + ","
                    + "'" + members + "'"
                    + ")";
            db.query(query);
        }
	}
	
	private static void saveYML() {
        ArrayList<OutpostYml> val = new ArrayList<OutpostYml>();
        for(Outpost t : outposts.values()) {
            OutpostYml ty = new OutpostYml();
            ty.highX = t.getUpperBound().getBlockX();
            ty.highZ = t.getUpperBound().getBlockZ();
            ty.lowX = t.getLowerBound().getBlockX();
            ty.lowZ = t.getLowerBound().getBlockZ();
            ty.members = t.getMembers().toString().substring(1, t.getMembers().toString().length() - 1);
            ty.owner = t.getOwner();
            ty.world = t.getLowerBound().getWorld().getName();
            ty.animals = t.getData('a');
            ty.blocks = t.getData('b');
            ty.bomb = Boolean.parseBoolean(t.getData('e'));
            ty.fire = Boolean.parseBoolean(t.getData('f'));
            ty.monsters = t.getData('m');
            ty.pvp = Boolean.parseBoolean(t.getData('p'));
            val.add(ty);
        }
        try {
            File out = new File("plugins/SimpleZones/outposts.yml");
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
	
	private String owner;
	private FlagSet flags;
	public Outpost(int id, Location first, Location second) {
		super(id, first, second);
		flags = new FlagSet();
	}

	@Override
	public boolean canBuild(Player p) {
		return getMembers().contains(p.getName()) || p.getName().equals(owner);
	}
	
	@Override
	public boolean isBlocked(Object o) {
		return flags.isBlocked(o, getLandType());
	}
	
    public boolean setFlag(String s) {
        return flags.setFlag(s);
    }    
    
    public void putFlags(String animals, String monsters, String blocks, boolean fire, boolean bomb, boolean pvp) {
        flags.loadStarts(animals, monsters, blocks, fire, bomb, pvp);
    }
    
    public String getData(char flag) {
        return flags.getData(flag);
    }
	
	public String getOwner() {
		return owner;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	@Override
	public LandType getLandType() {
		return LandType.OUTPOST;
	}
}
