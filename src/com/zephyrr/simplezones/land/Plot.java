package com.zephyrr.simplezones.land;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.Yaml;
import sqlibrary.Database;

import com.zephyrr.simplezones.ZonePlayer;
import com.zephyrr.simplezones.ymlIO.PlotYml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

/**
 *
 * @author Phoenix
 */
public class Plot extends OwnedLand {
    public static void fill(Database db, String prefix) {
        if(db == null) {
            fillYML();
            return;
        }
        try {
            ResultSet rs = db.query("SELECT * FROM " + prefix + "plots");
            while(rs.next()) {
                int id = rs.getInt("P_Id");
                ResultSet rs2 = db.query("SELECT TownName FROM " + prefix + "towns WHERE T_Id=" + rs.getInt("TownID"));
                rs2.next();
                String townName = rs2.getString("TownName");
                int lowX = rs.getInt("LowX");
                int lowZ = rs.getInt("LowZ");
                int highX = rs.getInt("HighX");
                int highZ = rs.getInt("HighZ");
                String[] members = rs.getString("Members").split(",");
                Town t = Town.getTown(townName);
                Location low = new Location(t.getLowerBound().getWorld(), lowX, OwnedLand.YCHECK, lowZ);
                Location high = new Location(t.getUpperBound().getWorld(), highX, OwnedLand.YCHECK, highZ);
                Plot p = new Plot(id, low, high, t);
                for(String s : members) 
                    p.addMember(s);
                t.addPlot(p);
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
        db.wipeTable(prefix + "plots");
        for(Town t : Town.getTownList().values()) {
            for(Plot p : t.getPlots()) {
                int id = p.getID();
                int town = t.getID();
                int lowX = p.getLowerBound().getBlockX();
                int lowZ = p.getLowerBound().getBlockZ();
                int highX = p.getUpperBound().getBlockX();
                int highZ = p.getUpperBound().getBlockZ();
                String members = "";
                for(String s : p.getMembers())
                    members += s + ",";
                String query = "INSERT INTO " + prefix + "plots VALUES("
                        + id + ","
                        + town + ","
                        + lowX + ","
                        + lowZ + ","
                        + highX + ","
                        + highZ + ","
                        + "'" + members + "'"
                        + ")";
                db.query(query);
            }
        }
    }

    private static void fillYML() {
        try {
            File f = new File("plugins/SimpleZones/plots.yml");
            if(!f.exists())
                return;
            InputStream in = new FileInputStream(f);
            Yaml yaml = new Yaml(new CustomClassLoaderConstructor(PlotYml.class.getClassLoader()));
            for(Object o : yaml.loadAll(in)) {
                PlotYml pyml = (PlotYml)o;
                int id = pyml.pid;
                int tid = pyml.tid;
                Town town = null;
                for(Town t : Town.getTownList().values())
                    if(t.getID() == tid)
                        town = t;
                int lowX = pyml.lowX;
                int lowZ = pyml.lowZ;
                int highX = pyml.highX;
                int highZ = pyml.highZ;
                String[] members = pyml.members.split(",");
                Location low = new Location(town.getWarp().getWorld(), lowX, OwnedLand.YCHECK, lowZ);
                Location high = new Location(town.getWarp().getWorld(), highX, OwnedLand.YCHECK, highZ);
                Plot p = new Plot(id, low, high, town);
                for(String s : members)
                    p.addMember(s);
                town.addPlot(p);
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void saveYML() {
        try {
        File f = new File("plugins/SimpleZones/plots.yml");
        if(!f.exists())
            f.createNewFile();
        ArrayList<PlotYml> al = new ArrayList<PlotYml>();
        for(Town t : Town.getTownList().values())
            for(Plot p : t.getPlots()) {
                PlotYml yml = new PlotYml();
                yml.highX = p.getUpperBound().getBlockX();
                yml.highZ = p.getUpperBound().getBlockZ();
                yml.lowX = p.getLowerBound().getBlockX();
                yml.lowZ = p.getLowerBound().getBlockZ();
                yml.members = p.getMembers().toString().substring(1, p.getMembers().toString().length() - 1);
                yml.pid = p.getID();
                yml.tid = t.getID();
                al.add(yml);
            }
        Yaml yml = new Yaml();
        yml.dumpAll(al.iterator(), new FileWriter(f));
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    private Town town;
    public Plot(int id, Location first, Location second, Town town) {
        super(id, first, second);
        this.town = town;
    }
    public Town getTown() {
        return town;
    }
    public boolean canBuild(Player p) {
        ZonePlayer zp = ZonePlayer.findUser(p);
        if(getMembers().contains(p.getName()))
            return true;
        if(town.getOwner().equals(zp.getName()))
            return true;
        return false;
    }
}
