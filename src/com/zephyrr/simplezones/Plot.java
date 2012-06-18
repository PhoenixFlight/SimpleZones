package com.zephyrr.simplezones;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import sqlibrary.Database;

/**
 *
 * @author Phoenix
 */
public class Plot extends OwnedLand {
    public static void fill(Database db, String prefix) {
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
                if(!members.isEmpty())
                    members = members.substring(0, members.length() - 1);
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
