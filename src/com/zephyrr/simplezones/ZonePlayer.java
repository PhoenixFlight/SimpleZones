package com.zephyrr.simplezones;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.Yaml;
import sqlibrary.Database;

import com.zephyrr.simplezones.land.OwnedLand;
import com.zephyrr.simplezones.land.Plot;
import com.zephyrr.simplezones.land.Sanctuary;
import com.zephyrr.simplezones.land.Town;
import com.zephyrr.simplezones.ymlIO.PlayerYml;
import java.io.FileWriter;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

/*
 * @author Phoenix
 */
public class ZonePlayer {

    private static HashMap<String, ZonePlayer> pMap;

    static {
        pMap = new HashMap<String, ZonePlayer>();
    }

    public static void fill(Database db, String prefix) {
        if(db == null) {
            fillYML();
            return;
        }
        ResultSet rs = db.query("SELECT * FROM " + prefix + "players");
        try {
            while (rs.next()) {
                int id = rs.getInt("P_Id");
                String name = rs.getString("Name");
                int tid = rs.getInt("TownID");
                Town t = null;
                if (tid != -1) {
                    ResultSet rs2 = db.query("SELECT TownName FROM " + prefix + "towns WHERE T_Id=" + tid);
                    rs2.next();
                    t = Town.getTown(rs2.getString("TownName"));
                }
                ZonePlayer zp = new ZonePlayer(name, id);
                zp.setTown(t);
                pMap.put(name, zp);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void save(Database db, String prefix) {
        if(db == null) {
            saveYML();
            return;
        }
        db.wipeTable(prefix + "players");
        for (ZonePlayer zp : pMap.values()) {
            int id = zp.getID();
            String name = zp.getName();
            int town = -1;
            if (zp.getTown() != null) {
                town = zp.getTown().getID();
            }
            String query = "INSERT INTO " + prefix + "players VALUES ("
                    + id + ","
                    + "'" + name + "',"
                    + town
                    + ")";
            db.query(query);
        }
    }

    public static void fillYML() {
        try {
            File f = new File("plugins/SimpleZones/players.yml");
            if(!f.exists())
                return;
            InputStream in = new FileInputStream(f);
            Yaml yml = new Yaml(new CustomClassLoaderConstructor(PlayerYml.class.getClassLoader()));
            for(Object o : yml.loadAll(in)) {
                PlayerYml pyml = (PlayerYml)o;
                String name = pyml.name;
                int id = pyml.id;
                int tid = pyml.tid;
                Town t = null;
                if(tid != -1) {
                    for(Town t2 : Town.getTownList().values())
                        if(t2.getID() == tid)
                            t = t2;
                }
                ZonePlayer zp = new ZonePlayer(name, id);
                zp.setTown(t);
                pMap.put(name, zp);
            }
            in.close();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void saveYML() {
        try {
            File f = new File("plugins/SimpleZones/players.yml");
            if(!f.exists())
                f.createNewFile();
            ArrayList<PlayerYml> al = new ArrayList<PlayerYml>();
            for(ZonePlayer zp : pMap.values()) {
                PlayerYml pyml = new PlayerYml();
                pyml.id = zp.getID();
                pyml.name = zp.getName();
                if(zp.getTown() == null)
                    pyml.tid = -1;
                else pyml.tid = zp.getTown().getID();
                al.add(pyml);
            }
            Yaml yml = new Yaml();
            yml.dumpAll(al.iterator(), new FileWriter(f));
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public static HashMap<String, ZonePlayer> getPMap() {
        return pMap;
    }

    public static void registerUser(Player p) {
        if (!pMap.containsKey(p.getName())) {
            pMap.put(p.getName(), new ZonePlayer(p.getName(), pMap.size()));
        }
    }

    public static ZonePlayer findUser(String s) {
        return pMap.get(s);
    }

    public static ZonePlayer findUser(Player p) {
        return pMap.get(p.getName());
    }
    private Player player;
    private Town town;
    private ArrayList<Mail> mail;
    private int id;
    private Location corner1, corner2;
    private String name;
    private Channel active;

    public ZonePlayer(String name, int id) {
        player = SimpleZones.getPlayer(name);
        this.id = id;
        this.name = name;
        active = Channel.GLOBAL;
        mail = new ArrayList<Mail>();
        corner1 = new Location(SimpleZones.getDefaultWorld(), 0, 0, 0);
        corner2 = corner1.clone();
    }

    public int getID() {
        return id;
    }

    public void setPlayer(Player p) {
        player = p;
    }

    public Player getPlayer() {
        return player;
    }

    public Town getTown() {
        return town;
    }

    public String getName() {
        return name;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ZonePlayer)) {
            return false;
        }
        return ((ZonePlayer) o).getName().equals(getName());
    }

    public Channel getChannel() {
        return active;
    }

    public void setTown(Town t) {
        town = t;
    }

    public void leaveTown() {
        if (town == null) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You aren't a member of any towns.");
        } else if (town.getOwner().equals(getName())) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You can't leave a town while you own it.");
        } else {
            player.sendMessage(ChatColor.GOLD + "[SimpleZones] You have left " + town.getName());
            town = null;
        }
    }

    public ArrayList<Mail> getMailList() {
        return mail;
    }

    public void sendMail(Mail message) {
        if (player != null && player.isOnline()) {
            player.sendMessage(ChatColor.GOLD + "[SimpleZones] You have received a new mail message.");
            player.sendMessage(ChatColor.GOLD + "[SimpleZones] You can read it by typing " + ChatColor.GREEN + "/mail read " + (mail.size() + 1));
        }
        mail.add(message);
    }
    public void getMailInfo() {
        int read = 0, unread = 0;
        for(Mail m : mail) {
            if(m.isUnread())
                unread++;
            else read++;
        }
        player.sendMessage(ChatColor.GOLD + "[SimpleZones] You have " + unread + " unread messages and " + read + " read messages.");
    }

    public void deleteMail(int index) {
        index--;
        if(index >= 0 && index < mail.size()) {
            mail.remove(index);
            player.sendMessage(ChatColor.GOLD + "[SimpleZones] The message has been deleted.");
        } else player.sendMessage(ChatColor.RED + "[SimpleZones] Invalid mail index.");
    }

    public void getMailInfo(int index) {
        index--;
        if (index >= 0 && index < mail.size()) {
            player.sendMessage(mail.get(index).getInfo());
        } else {
            player.sendMessage(ChatColor.RED + "That isn't a valid mail index.");
        }
    }

    public void readMail(int index) {
        index--;
        if (index >= 0 && index < mail.size()) {
            player.sendMessage(mail.get(index).read());
        } else {
            player.sendMessage(ChatColor.RED + "That isn't a valid mail index.");
        }
    }

    public boolean isDefining() {
        return corner2 == null;
    }

    public void setCorner(Location loc) {
        if (!isDefining()) {
            return;
        }
        if (corner1 == null) {
            corner1 = loc;
            player.sendMessage(ChatColor.GOLD + "[SimpleZones] First corner selected.  Please strike the second corner.");
        } else {
            if (loc.getWorld() != corner1.getWorld()) {
                player.sendMessage(ChatColor.RED + "[SimpleZones] You must define two points in the same world.");
            } else {
                corner2 = loc;
                player.sendMessage(ChatColor.GOLD + "[SimpleZones] Second corner selected.");
            }
        }
    }

    private void showFlagList() {
        ArrayList<String> flags = new ArrayList<String>();
        flags.add("f - Enable/disable the spreading and burning of objects.  Fire can still be placed, but it won't do any damage.");
        flags.add("e - Enable/disable explosions.  This includes creepers, TNT, and anything else that goes boom.");
        flags.add("b# - Blocks or unblocks the placing of the block with id # in your town.  Example: /zone flag +b17 will block users from placing Wood in your town.");
        flags.add("m[#] - Blocks or unblocks all or specific monsters.  Specific monster listings are available via /zone mIdList.  Example: /zone flag +m4 will block Creepers from your town.");
        flags.add("a[#] - Blocks or unblocks all or specific animals.  Specific animal listings are available via /zone aIdList.  Example: /zone flag +a2 will block Chickens from your town.");
        player.sendMessage(ChatColor.GOLD + "[SimpleZones] Town Flags");
        for(String s : flags)
            player.sendMessage(ChatColor.GOLD + s);
    }

    /******************************************************************************
     * SIMPLEZONES COMMANDS
     ******************************************************************************/
    public boolean define() {
        corner1 = null;
        corner2 = null;
        player.sendMessage(ChatColor.GOLD + "Strike the first corner of your new area.");
        return true;
    }

    public boolean flag(String[] args) {
        if(town == null || !town.getOwner().equals(getName())) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You are not the owner of a town.");
            return true;
        }
        if(args.length == 1) {
            showFlagList();
        } else {
            town.setFlag(args[1]);
            player.sendMessage(ChatColor.GOLD + "[SimpleZones] Flag request has been processed.");
        }
        return true;
    }

    public boolean toggleChannel() {
        if(active == Channel.GLOBAL)
            active = Channel.TOWN;
        else active = Channel.GLOBAL;
        return true;
    }

    public boolean create(String name, Database db, String prefix) {
        if (town != null) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You can only be in one town at a time.");
        } else {
            if (corner2 == null || (corner1.getBlockX() == 0 && corner1.getBlockY() == 0 && corner1.getBlockZ() == 0)) {
                player.sendMessage(ChatColor.RED + "[SimpleZones] You need to define points first.");
            } else if (OwnedLand.hasOverlap(corner1, corner2, false)) {
                player.sendMessage(ChatColor.RED + "[SimpleZones] There is another town contained in your selection.");
            } else if (Town.getTown(name) != null) {
                player.sendMessage(ChatColor.RED + "[SimpleZones] There is already a town named " + name);
            } else {
                int max = 1;
                for(Town t : Town.getTownList().values())
                    if(t.getID() >= max)
                        max = t.getID() + 1;
                Town t = new Town(max, corner1, corner2, name);
                town = t;
                t.setOwner(getName());
                town.setWarp(player.getLocation());
                Town.addTown(town);
                player.sendMessage(ChatColor.GOLD + "[SimpleZones] You are now the owner of " + name);
            }
        }
        return true;
    }

    public boolean plotDefine() {
        if (town == null || !town.getOwner().equals(name)) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You aren't the owner of a town." + town);
        } else {
            corner1 = null;
            corner2 = null;
            player.sendMessage(ChatColor.GOLD + "Strike the first corner of your new plot.");
        }
        return true;
    }

    public boolean plotCreate(Database db, String prefix) {
        if (town == null || !town.getOwner().equals(name)) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You aren't the owner of a town. ");
        } else if (corner2 == null || (corner1.getBlockX() == 0 && corner1.getBlockY() == 0 && corner1.getBlockZ() == 0)) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You need to define points first.");
        } else if (OwnedLand.getLandAtPoint(corner1) != town || OwnedLand.getLandAtPoint(corner2) != town) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] Your plot must be contained in your town.");
        } else if (OwnedLand.hasOverlap(corner1, corner2, true)) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] This plot overlaps with another.");
        } else {
            int max = 0;
            for(Town t : Town.getTownList().values())
                for(Plot p : t.getPlots())
                    if(p.getID() >= max)
                        max = p.getID() + 1;
            Plot p = new Plot(max, corner1, corner2, town);
            town.addPlot(p);
            player.sendMessage(ChatColor.GOLD + "[SimpleZones] You have added a new plot to " + town.getName());
        }
        return true;
    }

    public boolean plotDelete() {
        if(town == null || !town.getOwner().equals(name)) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You aren't the owner of a town.");
        } else if(!town.getPlots().contains(OwnedLand.getLandAtPoint(player.getLocation()))) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You aren't standing in a plot.");
        } else {
            OwnedLand.defineLocations(town);
            town.getPlots().remove(OwnedLand.getLandAtPoint(player.getLocation()));
            player.sendMessage(ChatColor.GOLD + "[SimpleZones] This plot has been deleted.");
        }
        return true;
    }

    public boolean plotAddMember(String name) {
        if(!(OwnedLand.getLandAtPoint(player.getLocation()) instanceof Plot)) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You are not currently standing in a plot.");
        } else if(!((Plot)(OwnedLand.getLandAtPoint(player.getLocation()))).getTown().getOwner().equals(getName())) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You do not own this town.");
        } else if(!town.getMembers().contains(name)) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] " + name + " is not a member of your town.");
        } else if (!OwnedLand.getLandAtPoint(player.getLocation()).addMember(name)) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] " + name + " is already a member of this plot.");
        } else {
            player.sendMessage(ChatColor.GOLD + "[SimpleZones] " + name + " has been added to this plot.");
        }
        return true;
    }

    public boolean plotRemoveMember(String name) {
        if(!(OwnedLand.getLandAtPoint(player.getLocation()) instanceof Plot)) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You are not currently standing in a plot.");
        } else if(!((Plot)(OwnedLand.getLandAtPoint(player.getLocation()))).getTown().getOwner().equals(getName())) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You do not own this town.");
        } else if(!((Plot)(OwnedLand.getLandAtPoint(player.getLocation()))).getTown().getMembers().contains(name)) {
            player.sendMessage(ChatColor.GOLD + "[SimpleZones] " + name + " is not a member of your town.");
        } else if (!OwnedLand.getLandAtPoint(player.getLocation()).removeMember(name)) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] " + name + " is not a member of this plot.");
        } else {
            player.sendMessage(ChatColor.GOLD + "[SimpleZones] " + name + " has been removed from this plot.");
        }
        return true;
    }

    public boolean setOwner(String name) {
        if(town == null || !town.getOwner().equals(getName())) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You are not the owner of a town.");
        } else if(!town.getMembers().contains(name)) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] " + name + " is not a member of " + town.getName());
        } else {
            town.setOwner(name);
            player.sendMessage(ChatColor.GOLD + "[SimpleZones] You are no longer the owner of " + town.getName());
            ZonePlayer zp = ZonePlayer.findUser(name);
            if(!zp.getPlayer().isOnline()) {
                Mail notif = new Mail("You have been given ownership of " + town.getName() + " by " + getName(), false, this, false);
                zp.sendMail(notif);
            } else {
                zp.getPlayer().sendMessage(ChatColor.GOLD + "[SimpleZones] You are now the owner of " + town.getName());
            }
        }
        return true;
    }

    public boolean invite(String name) {
        ZonePlayer zp = ZonePlayer.findUser(name);
        if(zp == null) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] There is no player named " + name);
        } else if(zp.getTown() != null) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] " + name + " is already in a town.");
        } else if(town == null || !town.getOwner().equals(getName())) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You do not own a town.");
        } else {
            for(Mail m : mail) {
                if(m.isInvite() && m.getSender().equals(zp)) {
                    zp.setTown(town);
                    player.sendMessage(ChatColor.GOLD + "[SimpleZones] You have added " + name + " to your town.");
                    if(zp.getPlayer() != null && zp.getPlayer().isOnline()) {
                        zp.getPlayer().sendMessage(ChatColor.GOLD + "[SimpleZones] You have been added to " + town.getName());
                    } else {
                        Mail notif = new Mail("You have been added to " + town.getName(), false, this, false);
                        zp.sendMail(notif);
                    }
                    town.addMember(name);
                    mail.remove(m);
                    return true;
                }
            }
            zp.sendMail(new Mail("You have been invited to " + town.getName() + ".  Use /zone join " + town.getName() + " to accept.", false, this, true));
        }
        return true;
    }

    public boolean ban(String name) {
        ZonePlayer zp = ZonePlayer.findUser(name);
        if(zp == null) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] There is no player named " + name);
        } else if(town == null || !town.getOwner().equals(getName())) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You are not the owner of a town.");
        } else if(!town.addBan(zp)) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] " + name + " is already banned from " + town.getName());
        } else {
            zp.setTown(null);
            if(zp.getPlayer() != null && zp.getPlayer().isOnline()) {
                zp.getPlayer().sendMessage(ChatColor.GOLD + "[SimpleZones] You have been banned from " + town.getName());
            } else {
                Mail notif = new Mail("You have been banned from " + town.getName(), false, this, false);
                zp.sendMail(notif);
            }
            player.sendMessage(ChatColor.GOLD + "[SimpleZones] " + name + " has been banned from " + town.getName());
        }
        return true;
    }

    public boolean unban(String name) {
        ZonePlayer zp = ZonePlayer.findUser(name);
        if(zp == null) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] There is no player named " + name);
        } else if(town == null || !town.getOwner().equals(getName())) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You are not the owner of a town.");
        } else if(!town.unban(zp)) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] " + name + " isn't currently banned in " + town.getName());
        } else {
            if(zp.getPlayer() != null && zp.getPlayer().isOnline()) {
                zp.getPlayer().sendMessage(ChatColor.GOLD + "[SimpleZones] You have been unbanned from " + town.getName());
            } else {
                Mail notif = new Mail("You have been unbanned from " + town.getName(), false, this, false);
                zp.sendMail(notif);
            }
            player.sendMessage(ChatColor.GOLD + "[SimpleZones] " + name + " has been unbanned from " + town.getName());
        }
        return true;
    }

    public boolean setWarp() {
        OwnedLand land = OwnedLand.getLandAtPoint(player.getLocation());
        if(land == null || town == null) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You are not in a town.");
            return true;
        }
        String owner;
        if(land instanceof Town)
            owner = ((Town)land).getOwner();
        else owner = ((Plot)land).getTown().getOwner();
        if(owner.equals(name)) {
            town.setWarp(player.getLocation());
            player.sendMessage(ChatColor.GOLD + "[SimpleZones] You have set the warp point for " + town.getName() + " to your location.");
        } else player.sendMessage(ChatColor.RED + "[SimpleZones] You don't own this land.");
        return true;
    }

    public boolean delete() {
        if(town == null || !town.getOwner().equals(name)) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You do not own a town.");
        } else {
            OwnedLand.stripLocations(town);
            for(String s : town.getMembers()) {
                ZonePlayer zp = ZonePlayer.findUser(s);
                if(zp == null)
                    continue;
                zp.sendMail(new Mail("The town \"" + town.getName() + "\" has been deleted.", false, this, false));
                zp.setTown(null);
            }
            Town.getTownList().remove(town.getName());
            player.sendMessage(ChatColor.GOLD + "[SimpleZones] You have deleted " + town.getName());
            town = null;
        }
        return true;
    }

    public boolean join(String s) {
        if(town != null) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You are already a member of a town.");
        } else if(Town.getTown(s) == null) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] There is no town named " + s);
        } else if(Town.getTown(s).getBans().contains(this)) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You are banned from " + s);
        } else {
            ZonePlayer owner = ZonePlayer.findUser(Town.getTown(s).getOwner());
            for(Mail m : mail) {
                if(m.getSender().equals(owner) && m.isInvite()) {
                    setTown(owner.getTown());
                    owner.getTown().addMember(name);
                    mail.remove(m);
                    return true;
                }
            }
            owner.sendMail(new Mail(name + " would like to join " + Town.getTown(s).getName(), false, this, true));
            player.sendMessage(ChatColor.GOLD + "[SimpleZones] The owner of " + s + " has been notified of your request.");
        }
        return true;
    }

    public boolean quit() {
        if(town == null) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You are not the member of a town.");
        } else if(town.getOwner().equals(name)) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You cannot quit if you own the town.");
        } else {
            town.removeMember(name);
            for(Plot p : town.getPlots())
                p.removeMember(name);
            player.sendMessage(ChatColor.GOLD + "[SimpleZones] You have been removed from " + town.getName());
            town = null;
        }
        return true;
    }

    public boolean massmail(String msg) {
        if(town == null || !town.getOwner().equals(getName())) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You are not the owner of a town.");
        } else {
            for(String s : town.getMembers()) {
                ZonePlayer zp = ZonePlayer.findUser(s);
                zp.sendMail(new Mail(msg, false, this, false));
            }
        }
        return true;
    }
    
    public boolean superUser(String name) {
    	if(town == null || !town.getOwner().equals(getName())) {
    		player.sendMessage(ChatColor.RED + "[SimpleZones] You are not the owner of a town.");
    	} else if (!town.getMembers().contains(name)) {
    		player.sendMessage(ChatColor.RED + "[SimpleZones] " + name + " is not a member of your town.");
    	} else {
    		town.modSuper(name, !town.isSuper(ZonePlayer.findUser(name)));
    		if(town.isSuper(ZonePlayer.findUser(name))) {
    			player.sendMessage(ChatColor.GOLD + "[SimpleZones] " + name + " is now a Superuser!");
    		} else player.sendMessage(ChatColor.GOLD + "[SimpleZones] " + name + " is no longer a Superuser.");
    	}
    	return true;
    }
    
    public boolean makeSanct() {
        if (corner2 == null || (corner1.getBlockX() == 0 && corner1.getBlockY() == 0 && corner1.getBlockZ() == 0)) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] You need to define points first.");
        } else if (OwnedLand.hasOverlap(corner1, corner2, false)) {
            player.sendMessage(ChatColor.RED + "[SimpleZones] There is another area contained in your selection.");
        } else {
        	int id = 1;
        	for(Sanctuary s : Sanctuary.getSancts()) {
        		if(s.getID() >= id)
        			id = s.getID() + 1;
        	}
        	Sanctuary.modSancts(new Sanctuary(id, corner1, corner2));
        	player.sendMessage(ChatColor.GOLD + "[SimpleZones] You have created a new Sanctuary.");
        }
    	return true;
    }
    
    public boolean delSanct() {
    	OwnedLand check = OwnedLand.getLandAtPoint(player.getLocation());
    	if(check instanceof Sanctuary) {
    		OwnedLand.stripLocations(check);
    		Sanctuary.modSancts((Sanctuary)check);
    		player.sendMessage(ChatColor.GOLD + "[SimpleZones] This Sanctuary has been deleted.");
    	} else player.sendMessage(ChatColor.RED + "[SimpleZones] You aren't standing in a Sanctuary!");
    	return true;
    }
}
