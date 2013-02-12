package com.zephyrr.simplezones.land;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

import sqlibrary.Database;

import com.zephyrr.simplezones.SimpleZones;
import com.zephyrr.simplezones.ymlIO.SanctuaryYml;

public class Sanctuary extends OwnedLand {
	private static ArrayList<Sanctuary> sancts;
	
	static {
		sancts = new ArrayList<Sanctuary>();
	}
	
	public static void modSancts(Sanctuary s) {
		if(!sancts.remove(s)) {
			sancts.add(s);
		}
	}
	
	public static ArrayList<Sanctuary> getSancts() {
		return sancts;
	}
	
	public static void fill(Database db, String prefix) {
		if(db == null) {
			fillYML();
			return;
		}
		try {
			ResultSet rs = db.query("SELECT * FROM " + prefix + "sanctuaries");
			while(rs.next()) {
				int id = rs.getInt("S_Id");
				int lowX = rs.getInt("LowX");
				int highX = rs.getInt("HighX");
				int lowZ = rs.getInt("LowZ");
				int highZ = rs.getInt("HighZ");
				String world = rs.getString("World");
				Location first = new Location(SimpleZones.getWorld(world), lowX, OwnedLand.YCHECK, lowZ);
				Location second = new Location(SimpleZones.getWorld(world), highX, OwnedLand.YCHECK, highZ);
				sancts.add(new Sanctuary(id, first, second));
			}
		} catch(SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	private static void fillYML() {
		try {
	        File f = new File("plugins/SimpleZones/sanctuaries.yml");
	        if(!f.exists())
	            return;
	        InputStream in = new FileInputStream(f);
	        Yaml yaml = new Yaml(new CustomClassLoaderConstructor(SanctuaryYml.class.getClassLoader()));
	        for(Object o : yaml.loadAll(in)) {
	        	SanctuaryYml syml = (SanctuaryYml)o;
	        	int xLow = syml.lowX;
	        	int zLow = syml.lowZ;
	        	int xHigh = syml.highX;
	        	int zHigh = syml.highZ;
	        	int id = syml.sid;
	        	String world = syml.world;
	        	Location first = new Location(SimpleZones.getWorld(world), xLow, OwnedLand.YCHECK, zLow);
	        	Location second = new Location(SimpleZones.getWorld(world), xHigh, OwnedLand.YCHECK, zHigh);
	        	sancts.add(new Sanctuary(id, first, second));
	        }
	        in.close();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static void save(Database db, String prefix) {
		if(db == null) {
			saveYML();
			return;
		}
		db.wipeTable(prefix + "sanctuaries");
		for(Sanctuary s : sancts) {
			String query = "INSERT INTO " + prefix + "sanctuaries VALUES(" +
					"" + s.getID() +
					", " + s.getLowerBound().getBlockX() +
					", " + s.getUpperBound().getBlockX() +
					", " + s.getLowerBound().getBlockZ() +
					", " + s.getUpperBound().getBlockZ() +
					", '" + s.getLowerBound().getWorld().getName() + "'" +
					")";
			db.query(query);
		}
	}
	
	private static void saveYML() {
		try {
			File f = new File("plugins/SimpleZones/sanctuaries.yml");
			if(!f.exists())
				f.createNewFile();
			ArrayList<SanctuaryYml> symls = new ArrayList<SanctuaryYml>();
			for(Sanctuary s : sancts){
				SanctuaryYml syml = new SanctuaryYml();
				syml.highX = s.getUpperBound().getBlockX();
				syml.highZ = s.getUpperBound().getBlockZ();
				syml.lowX = s.getLowerBound().getBlockX();
				syml.lowZ = s.getLowerBound().getBlockZ();
				syml.sid = s.getID();
				syml.world = s.getLowerBound().getWorld().getName();
				symls.add(syml);
			}
			Yaml yml = new Yaml();
			yml.dumpAll(symls.iterator(), new FileWriter(f));
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public Sanctuary(int id, Location first, Location second) {
		super(id, first, second);
	}

	@Override
	public boolean canBuild(Player p) {
		if(p.isOp() && SimpleZones.getPlugConfig().getBoolean("sanctuary.opBuild"))
			return true;
		else if(SimpleZones.getPlugConfig().getBoolean("sanctuary.playerBuild"))
			return true;
		return false;
	}
	@Override
	public boolean isBlocked(Object o) {
		return false;
	}
	@Override
	public LandType getLandType() {
		return LandType.SANCTUARY;
	}
}
