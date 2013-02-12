package com.zephyrr.simplezones;

import com.zephyrr.simplezones.flags.AnimalFlag;
import com.zephyrr.simplezones.flags.AnimalFlag.AniIDs;
import com.zephyrr.simplezones.flags.MonsterFlag;
import com.zephyrr.simplezones.flags.MonsterFlag.MobIDs;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sqlibrary.*;

import com.zephyrr.simplezones.land.Outpost;
import com.zephyrr.simplezones.land.Plot;
import com.zephyrr.simplezones.land.Sanctuary;
import com.zephyrr.simplezones.land.Town;
import com.zephyrr.simplezones.listeners.*;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author Phoenix
 */
public class SimpleZones extends JavaPlugin {

    private static JavaPlugin plug;
    private static double VERSION = 0.8;

    public static Player getPlayer(String name) {
        return plug.getServer().getPlayer(name);
    }

    public static FileConfiguration getPlugConfig() {
        return plug.getConfig();
    }

    public static World getWorld(String name) {
        return plug.getServer().getWorld(name);
    }

    public static World getDefaultWorld() {
        return plug.getServer().getWorlds().get(0);
    }
    private Database db;
    private String prefix;

    @Override
	public void onEnable() {
        SimpleZones.plug = this;
        prefix = getConfig().getString("database.prefix");
        String type = getConfig().getString("database.type");
        if (type.equalsIgnoreCase("mysql")) {
            db = new MySQL(getLogger(),
                    prefix,
                    getConfig().getString("database.mysql.host"),
                    getConfig().getString("database.mysql.port"),
                    getConfig().getString("database.mysql.database"),
                    getConfig().getString("database.mysql.username"),
                    getConfig().getString("database.mysql.password"));
        }
        if (db != null) {
            db.open();
            firstRun();
        }
        updateResources();

        if (!getConfig().contains("version") || getConfig().getDouble("version") != VERSION) {
            if (new File("plugins/SimpleZones/config.yml").exists()) {
		        getConfig().options().copyDefaults(true);
		        getConfig().set("version", VERSION);
            }
        }
        Town.fill(db, prefix);
        Plot.fill(db, prefix);
        ZonePlayer.fill(db, prefix);
        Town.fillBans(db, prefix);
        Mail.fill(db, prefix);
        Sanctuary.fill(db, prefix);
        Outpost.fill(db, prefix);
        if (!new File("plugins/SimpleZones/config.yml").exists()) 
            saveDefaultConfig();
        for(World w : getServer().getWorlds()) 
        	if(!getConfig().contains("world." + w.getName())) {
        		MemorySection.createPath(getConfig().getRoot(), "world." + w.getName());
        		getConfig().set("world." + w.getName(), true);
        	}
		try {
			FileWriter fw = new FileWriter(new File("plugins/SimpleZones/config.yml"));
			fw.write(getConfig().saveToString());
			fw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
        for (Player p : getServer().getOnlinePlayers()) {
            ZonePlayer.registerUser(p);
            ZonePlayer.findUser(p.getName()).setPlayer(p);
        }

        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new DestructoListener(), this);
        getServer().getPluginManager().registerEvents(new MonsterListener(), this);
    }

    private void updateResources() {
        if (db == null) {
            return;
        }
        addColumnUnlessExists(prefix + "towns", "Animals", "text");
        addColumnUnlessExists(prefix + "towns", "Monsters", "text");
        addColumnUnlessExists(prefix + "towns", "Blocks", "text");
        addColumnUnlessExists(prefix + "towns", "Fire", "boolean");
        addColumnUnlessExists(prefix + "towns", "Bomb", "boolean");
        addColumnUnlessExists(prefix + "towns", "SuperUsers", "text");
        addColumnUnlessExists(prefix + "towns", "EntryMessage", "text");
        addColumnUnlessExists(prefix + "towns", "PvP", "boolean");
        addColumnUnlessExists(prefix + "players", "OutCount", "int");
    }

    private void firstRun() {
    	if(!db.checkTable(prefix + "sanctuaries")) {
    		db.createTable("CREATE TABLE " + prefix + "sanctuaries (" +
    				"S_Id int NOT NULL," +
    				"LowX int," +
    				"HighX int," +
    				"LowZ int," +
    				"HighZ int," +
    				"World text," +
    				"PRIMARY KEY (S_Id)" +
    				")");
    	}
    	
    	if(!db.checkTable(prefix + "outposts")) {
    		db.createTable("CREATE TABLE " + prefix + "outposts (" +
    				"O_Id int NOT NULL, " +
    				"Owner varchar(255), " +
    				"LowX int, " +
    				"HighX int, " +
    				"LowZ int, " +
    				"HighZ int, " +
    				"World varchar(255), " +
    				"Animals text, " +
    				"Blocks text, " +
    				"Monsters text, " +
    				"PvP boolean, " +
    				"Fire boolean, " +
    				"Bomb boolean, " +
    				"Members text, " +
    				"PRIMARY KEY (O_Id)" +
    				")");
    	}
    	
        if (!db.checkTable(prefix + "bans")) {
            db.createTable("CREATE TABLE " + prefix + "bans ("
                    + "P_Id int NOT NULL,"
                    + "User varchar(255),"
                    + "TownID int,"
                    + "PRIMARY KEY (P_Id)"
                    + ")");
        }

        if (!db.checkTable(prefix + "towns")) {
            db.createTable("CREATE TABLE " + prefix + "towns ("
                    + "T_Id int NOT NULL, "
                    + "Owner varchar(255), "
                    + "TownName varchar(255),"
                    + "LowX int,"
                    + "LowZ int,"
                    + "HighX int,"
                    + "HighZ int,"
                    + "WarpX double,"
                    + "WarpY double,"
                    + "WarpZ double,"
                    + "World varchar(255),"
                    + "Members TEXT,"
                    + "PRIMARY KEY (T_Id)"
                    + ")");
        }

        if (!db.checkTable(prefix + "plots")) {
            db.createTable("CREATE TABLE " + prefix + "plots ("
                    + "P_Id int NOT NULL,"
                    + "TownID int,"
                    + "LowX int,"
                    + "LowZ int,"
                    + "HighX int,"
                    + "HighZ int,"
                    + "Members TEXT,"
                    + "PRIMARY KEY (P_Id)"
                    + ")");
        }

        if (!db.checkTable(prefix + "players")) {
            db.createTable("CREATE TABLE " + prefix + "players ("
                    + "P_Id int NOT NULL,"
                    + "Name varchar(255),"
                    + "TownID int,"
                    + "PRIMARY KEY (P_Id)"
                    + ")");
        }

        if (!db.checkTable(prefix + "mail")) {
            db.createTable("CREATE TABLE " + prefix + "mail ("
                    + "M_Id int NOT NULL,"
                    + "SenderID int,"
                    + "RecipientID int,"
                    + "Contents TEXT,"
                    + "Unread boolean,"
                    + "Invite boolean,"
                    + "PRIMARY KEY (M_Id)"
                    + ")");
        }
        try {
            if (db.query("SELECT * FROM `information_schema`.`ROUTINES` where specific_name = 'AddColumnUnlessExists'").next()) {
                return;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void addColumnUnlessExists(String table, String column, String data) {
        try {
            String query = "SELECT * FROM information_schema.COLUMNS where column_name='" + column + "' and table_name='" + table + "' and table_schema='" + getConfig().getString("database.mysql.database") + "'";
            if(db.query(query).next())
                return;
            db.query("ALTER TABLE " + table + " ADD COLUMN " + column + " " + data);
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "[SimpleZones] Only players can use these commands.");
            return false;
        }

        Player send = (Player) sender;
        ZonePlayer zoneSender = ZonePlayer.findUser(send);

        if (args.length == 0) {
            return false;
        }
        if (command.getName().equalsIgnoreCase("szone")) {
        	if (args.length >= 2 && args[0].equalsIgnoreCase("outpost") && getConfig().getBoolean("outposts.enabled")) {
        		if(args[1].equalsIgnoreCase("define") && send.hasPermission("Zone.outpost.define")) {
        			return zoneSender.outpostDefine();
        		} else if(args[1].equalsIgnoreCase("create") && send.hasPermission("Zone.outpost.create")) {
        			return zoneSender.outpostCreate();
        		} else if(args[1].equalsIgnoreCase("delete") && send.hasPermission("Zone.outpost.delete")) {
        			return zoneSender.outpostDelete();
        		} else if(args[1].equalsIgnoreCase("members") && send.hasPermission("Zone.outpost.members")) { 
        			return zoneSender.outpostMembers();
        		} else if(args[1].equalsIgnoreCase("flag") && send.hasPermission("Zone.outpost.flag")) {
    				return zoneSender.outpostFlag(args);
    			} else if(args.length > 2) {
        			if(args[1].equalsIgnoreCase("addmember") && send.hasPermission("Zone.outpost.addmember")) {
        				return zoneSender.outpostAddMember(args[2]);
        			} else if(args[1].equalsIgnoreCase("removemember") && send.hasPermission("Zone.outpost.removemember")) {
        				return zoneSender.outpostRemoveMember(args[2]);
        			} else if(args[1].equalsIgnoreCase("setowner") && send.hasPermission("Zone.outpost.setowner")) {
        				return zoneSender.outpostSetOwner(args[2]);
        			} 
        		}
        	}
            if (args.length >= 2 && args[0].equalsIgnoreCase("plot")) {
                if (args[1].equalsIgnoreCase("define") && sender.hasPermission("Zone.plot.define")) {
                    return zoneSender.plotDefine();
                } else if (args[1].equalsIgnoreCase("addmember") && sender.hasPermission("Zone.plot.addmember")) {
                    if (args.length == 2) {
                        return false;
                    }
                    return zoneSender.plotAddMember(args[2]);
                } else if (args[1].equalsIgnoreCase("removemember") && sender.hasPermission("Zone.plot.removemember")) {
                    if (args.length == 2) {
                        return false;
                    }
                    return zoneSender.plotRemoveMember(args[2]);
                } else if (args[1].equalsIgnoreCase("create") && sender.hasPermission("Zone.plot.create")) {
                    return zoneSender.plotCreate();
                } else if (args[1].equalsIgnoreCase("delete") && sender.hasPermission("Zone.plot.delete")) {
                    return zoneSender.plotDelete();
                }
                return false;
            } else if(args[0].equalsIgnoreCase("setEntryMessage") && (zoneSender.getTown() != null && zoneSender.getTown().getOwner().equals(send.getName()))) { 
            	if(args.length == 1) {
            		return false;
            	} 
            	String words = "";
            	for(int i = 1; i < args.length; i++)
            		words += args[i] + " ";
            	zoneSender.getTown().setEntryMessage(words);
            	return true;
            } else if (args[0].equalsIgnoreCase("flag") && sender.hasPermission("Zone.flag")) {
                return zoneSender.flag(args);
            } else if (args[0].equalsIgnoreCase("aIdList") && (sender.hasPermission("Zone.flag") || sender.hasPermission("Zone.outpost.flag"))) {
                showAIDs(sender);
                return true;
            } else if (args[0].equalsIgnoreCase("mIdList") && (sender.hasPermission("Zone.flag") || sender.hasPermission("Zone.outpost.flag"))) {
                showMIDs(sender);
                return true;
            } else if(args[0].equalsIgnoreCase("super") && sender.hasPermission("Zone.super")) {
            	return args.length > 1 && zoneSender.superUser(args[1]);
            } else if(args[0].equalsIgnoreCase("admin")) {
            	if(args.length == 1)
            		return false;
            	if(args[1].equalsIgnoreCase("sanct")) {
            		if(args.length == 2)
            			return false;
            		if(args[2].equalsIgnoreCase("create") && sender.hasPermission("Zone.admin.sanct.create")) {
            			return zoneSender.makeSanct();
            		} else if(args[2].equalsIgnoreCase("delete") && sender.hasPermission("Zone.admin.sanct.delete")) {
            			return zoneSender.delSanct();
            		}
            	}
            }
            else if (args[0].equalsIgnoreCase("massmail") && sender.hasPermission("Zone.massmail")) {
                if (args.length == 1) {
                    return false;
                }
                String msg = "";
                for (int i = 1; i < args.length; i++) {
                    msg += args[i];
                }
                return zoneSender.massmail(msg);
            } else if (args[0].equalsIgnoreCase("define") && sender.hasPermission("Zone.define")) {
                return zoneSender.define();
            } else if (args[0].equalsIgnoreCase("create") && sender.hasPermission("Zone.create")) {
                if (args.length == 1) {
                    return false;
                }
                return zoneSender.create(args[1], db, prefix);
            } else if (args[0].equalsIgnoreCase("setowner") && sender.hasPermission("Zone.setowner")) {
                if (args.length == 1) {
                    return false;
                }
                return zoneSender.setOwner(args[1]);
            } else if (args[0].equalsIgnoreCase("invite") && sender.hasPermission("Zone.invite")) {
                if (args.length == 1) {
                    return false;
                }
                return zoneSender.invite(args[1]);
            } else if (args[0].equalsIgnoreCase("ban") && sender.hasPermission("Zone.ban")) {
                if (args.length == 1) {
                    return false;
                }
                return zoneSender.ban(args[1]);
            } else if (args[0].equalsIgnoreCase("unban") && sender.hasPermission("Zone.unban")) {
                if (args.length == 1) {
                    return false;
                }
                return zoneSender.unban(args[1]);
            } else if (args[0].equalsIgnoreCase("setwarp") && sender.hasPermission("Zone.setwarp")) {
                return zoneSender.setWarp();
            } else if (args[0].equalsIgnoreCase("delete") && sender.hasPermission("Zone.delete")) {
                return zoneSender.delete();
            } else if (args[0].equalsIgnoreCase("list") && sender.hasPermission("Zone.list")) {
                listTowns(send);
                return true;
            } else if (args[0].equalsIgnoreCase("members") && sender.hasPermission("Zone.members")) {
                if (args.length == 1) {
                    return false;
                }
                listMembers(send, args[1]);
                return true;
            } else if (args[0].equalsIgnoreCase("warp") && sender.hasPermission("Zone.warp")) {
                if (args.length == 1) {
                    return false;
                }
                warp(send, args[1]);
                return true;
            } else if (args[0].equalsIgnoreCase("join") && sender.hasPermission("Zone.join")) {
                if (args.length == 1) {
                    return false;
                }
                return zoneSender.join(args[1]);
            } else if (args[0].equalsIgnoreCase("leave") && sender.hasPermission("Zone.leave")) {
                return zoneSender.quit();
            } else if (args[0].equalsIgnoreCase("help") && sender.hasPermission("Zone.leave")) {
                showTownHelp(send, args);
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("smail")) {
            if (args[0].equalsIgnoreCase("send") && sender.hasPermission("SMail.send")) {
                if (args.length < 3) {
                    return false;
                }
                String to = args[1];
                if (ZonePlayer.findUser(to) == null) {
                    send.sendMessage(ChatColor.RED + "[SimpleZones] There is no player named " + to);
                    return true;
                }
                String msg = "";
                for (int i = 2; i < args.length; i++) {
                    msg += args[i] + " ";
                }
                ZonePlayer.findUser(to).sendMail(new Mail(msg, false, zoneSender, false));
                send.sendMessage(ChatColor.GOLD + "[SimpleZones] Message sent to " + to);
                return true;
            } else if (args[0].equalsIgnoreCase("info") && sender.hasPermission("SMail.info")) {
                if (args.length == 1) {
                    zoneSender.getMailInfo();
                    return true;
                }
                try {
                    zoneSender.getMailInfo(Integer.parseInt(args[1]));
                } catch (NumberFormatException ex) {
                }
                return true;
            } else if (args[0].equalsIgnoreCase("read") && sender.hasPermission("SMail.read")) {
                if (args.length == 1) {
                    return false;
                }
                try {
                    zoneSender.readMail(Integer.parseInt(args[1]));
                } catch (NumberFormatException ex) {
                }
                return true;
            } else if (args[0].equalsIgnoreCase("help") && sender.hasPermission("SMail.help")) {
                showMailHelp(send);
                return true;
            } else if (args[0].equalsIgnoreCase("delete") && sender.hasPermission("SMail.delete")) {
                try {
                    zoneSender.deleteMail(Integer.parseInt(args[1]));
                } catch (NumberFormatException ex) {
                }
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("zchat")) {
            if (args[0].equalsIgnoreCase("toggle") && send.hasPermission("Zchat.toggle")) {
                zoneSender.toggleChannel();
                send.sendMessage(ChatColor.GOLD + "[SimpleZones] Your active chat channel has been set to " + zoneSender.getChannel().name().toLowerCase());
                return true;
            } else if (args[0].equalsIgnoreCase("help") && send.hasPermission("Zchat.help")) {
                showChatHelp(send);
            }
        }
        return false;
    }

    private void listTowns(Player p) {
        p.sendMessage(ChatColor.GOLD + "[SimpleZones] Town List:");
        for (String s : Town.getTownList().keySet()) {
            p.sendMessage(ChatColor.GOLD + s);
        }
    }

    private void listMembers(Player p, String s) {
        if (Town.getTown(s) == null) {
            p.sendMessage(ChatColor.RED + "[SimpleZones] " + s + " does not exist.");
        } else {
            p.sendMessage(ChatColor.GOLD + "[SimpleZone] Members of " + s + ":");
            p.sendMessage(ChatColor.GOLD + "Owner: " + Town.getTown(s).getOwner());
            for (String name : Town.getTown(s).getMembers()) {
                p.sendMessage(ChatColor.GOLD + name);
            }
        }
    }

    private void warp(Player p, String s) {
        if (Town.getTown(s) == null) {
            p.sendMessage(ChatColor.RED + "[SimpleZones] " + s + " does not exist.");
        } else if(Town.getTown(s).getBans().contains(ZonePlayer.findUser(p))) {
        	p.sendMessage(ChatColor.RED + "[SimpleZones] You are banned from " + s);
        } else {
            p.teleport(Town.getTown(s).getWarp());
        }
    }

    private void showChatHelp(Player p) {
        ArrayList<String> al = new ArrayList<String>();
        if (p.hasPermission("Zchat.toggle")) {
            al.add(ChatColor.GREEN + "/zchat toggle");
            al.add(ChatColor.GOLD + "Toggles your active chat channel between town and global.");
        }
        for (String s : al) {
            p.sendMessage(s);
        }
    }

    private void showTownHelp(Player p, String[] args) {
        int page = 1;
        if (args.length != 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
            }
        }
        ArrayList<String> al = new ArrayList<String>();
        if (p.hasPermission("Zone.define")) {
            al.add(ChatColor.GREEN + "/szone define");
            al.add(ChatColor.GOLD + "Starts the cuboid selection process to define a town.");
        }
        if (p.hasPermission("Zone.create")) {
            al.add(ChatColor.GREEN + "/szone create <name>");
            al.add(ChatColor.GOLD + "Creates a town from the selection.");
        }
        if (ZonePlayer.findUser(p).getTown() != null && ZonePlayer.findUser(p).getTown().getOwner().equals(p.getName())) {
        	al.add(ChatColor.GREEN + "/szone setEntryMessage <message>");
        	al.add(ChatColor.GOLD + "Sets the message that players will see when entering your town.");
        }
        if (p.hasPermission("Zone.massmail")) {
            al.add(ChatColor.GREEN + "/szone massmail <message>");
            al.add(ChatColor.GOLD + "Sends a mail message to all members of your town.");
        }
        if (p.hasPermission("Zone.setowner")) {
            al.add(ChatColor.GREEN + "/szone setowner <player>");
            al.add(ChatColor.GOLD + "Transfers ownership to <player>. You must own the town.");
        }
        if (p.hasPermission("Zone.invite")) {
            al.add(ChatColor.GREEN + "/szone invite <player>");
            al.add(ChatColor.GOLD + "Adds <player> to your town.");
        }
        if (p.hasPermission("Zone.ban")) {
            al.add(ChatColor.GREEN + "/szone ban <player>");
            al.add(ChatColor.GOLD + "Bans <player> from your town.  They are kicked, cannot ask to join, and cannot enter the town anymore.");
        }
        if (p.hasPermission("Zone.unban")) {
            al.add(ChatColor.GREEN + "/szone unban <player>");
            al.add(ChatColor.GOLD + "Unbans <player> from your town.");
        }
        if (p.hasPermission("Zone.delete")) {
            al.add(ChatColor.GREEN + "/szone delete");
            al.add(ChatColor.GOLD + "Permanently deletes your town.");
        }
        if (p.hasPermission("Zone.setwarp")) {
            al.add(ChatColor.GREEN + "/szone setwarp");
            al.add(ChatColor.GOLD + "Sets the warp point for your town to your location.");
        }
        if (p.hasPermission("Zone.plot.define")) {
            al.add(ChatColor.GREEN + "/szone plot define");
            al.add(ChatColor.GOLD + "Starts the cuboid selection process to define a plot.");
        }
        if (p.hasPermission("Zone.plot.create")) {
            al.add(ChatColor.GREEN + "/szone plot create");
            al.add(ChatColor.GOLD + "Creates a plot from the given selection.");
        }
        if (p.hasPermission("Zone.plot.delete")) {
            al.add(ChatColor.GREEN + "/szone plot delete");
            al.add(ChatColor.GOLD + "Deletes the plot in which you are currently standing.");
        }
        if (p.hasPermission("Zone.plot.addmember")) {
            al.add(ChatColor.GREEN + "/szone plot addmember <player>");
            al.add(ChatColor.GOLD + "Allows <player to build in the plot you're standing in.");
        }
        if (p.hasPermission("Zone.plot.removemember")) {
            al.add(ChatColor.GREEN + "/szone plot removemember <player>");
            al.add(ChatColor.GOLD + "Disallows <player> to build in the plot you're standing in.");
        }
        if(p.hasPermission("Zone.outpost.define")) {
        	al.add(ChatColor.GREEN + "/szone outpost define");
        	al.add(ChatColor.GOLD + "Starts the cuboid selection process to define an outpost.");
        }
        if(p.hasPermission("Zone.outpost.create")) {
        	al.add(ChatColor.GREEN + "/szone outpost create");
        	al.add(ChatColor.GOLD + "Creates an outpost from the given selection.");
        }
        if(p.hasPermission("Zone.outpost.addmember")) {
        	al.add(ChatColor.GREEN + "/szone outpost addmember <name>");
        	al.add(ChatColor.GOLD + "Gives the specified member permission to build on your current outpost.");
        }
        if(p.hasPermission("Zone.outpost.removemember")) {
        	al.add(ChatColor.GREEN + "/szone outpost removemember <name>");
        	al.add(ChatColor.GOLD + "Removes the specified user from your current outpost's member list.");
        }
        if(p.hasPermission("Zone.outpost.delete")) {
        	al.add(ChatColor.GREEN + "/szone outpost delete");
        	al.add(ChatColor.GOLD + "Deletes the outpost that you are currently standing in.");
        }
        if(p.hasPermission("Zone.outpost.setowner")) {
        	al.add(ChatColor.GREEN + "/szone outpost setowner <owner>");
        	al.add(ChatColor.GOLD + "Transfers ownership of your current outpost to the specified user.");
        }
        if(p.hasPermission("Zone.outpost.flag")) {
        	al.add(ChatColor.GREEN + "/szone outpost flag <flags>");
        	al.add(ChatColor.GOLD + "Sets protection flags for your outpost.  Type " + ChatColor.GREEN + "/zone outpost flag" + ChatColor.GOLD + " without any parameters to see information on specific flags.");
        }
        if(p.hasPermission("Zone.outpost.members")) {
        	al.add(ChatColor.GREEN + "/szone outpost members");
        	al.add(ChatColor.GOLD + "Lists the members of the plot in which you are currently standing.");
        }
        if (p.hasPermission("Zone.list")) {
            al.add(ChatColor.GREEN + "/szone list");
            al.add(ChatColor.GOLD + "Lists all towns");
        }
        if (p.hasPermission("Zone.members")) {
            al.add(ChatColor.GREEN + "/szone members <town>");
            al.add(ChatColor.GOLD + "Lists the members in <town>");
        }
        if (p.hasPermission("Zone.warp")) {
            al.add(ChatColor.GREEN + "/szone warp <town>");
            al.add(ChatColor.GOLD + "Warps you to <town>");
        }
        if (p.hasPermission("Zone.join")) {
            al.add(ChatColor.GREEN + "/szone join <town>");
            al.add(ChatColor.GOLD + "Notifies the owner of <town> that you would like to join.");
        }
        if (p.hasPermission("Zone.leave")) {
            al.add(ChatColor.GREEN + "/szone leave");
            al.add(ChatColor.GOLD + "Leaves the current town.");
        }
        if(p.hasPermission("Zone.flag")) {
        	al.add(ChatColor.GREEN + "/szone flag <flags>");
        	al.add(ChatColor.GOLD + "Sets protection flags for your town.  Type " + ChatColor.GREEN + "/zone flag" + ChatColor.GOLD + " without any parameters to see information on specific flags.");
        }
        if(p.hasPermission("Zone.super")) {
        	al.add(ChatColor.GREEN + "/szone super <player>");
        	al.add(ChatColor.GOLD + "Toggle's the given user's Superuser status in your town.  Superusers can build in unplotted areas of the town.");
        }
        if(p.hasPermission("Zone.admin.sanct.create")) {
        	al.add(ChatColor.GREEN + "/szone admin sanct create");
        	al.add(ChatColor.GOLD + "Creates a Sanctuary where no towns can be formed using your most recent selection.");
        }
        if(p.hasPermission("Zone.admin.sanct.delete")) {
        	al.add(ChatColor.GREEN + "/szone admin sanct delete");
        	al.add(ChatColor.GOLD + "Deletes the Sanctuary in which you are standing.");
        }
        page--;
        for (int i = page * 12; i < 12 + (page * 12) && i < al.size(); i++) {
            p.sendMessage(al.get(i));
        }
        p.sendMessage(ChatColor.GOLD + "====== Page " + (page + 1) + " of " + (al.size() / 12 + 1) + " ======");
    }

    private void showMailHelp(Player p) {
        ArrayList<String> al = new ArrayList<String>();
        if (p.hasPermission("SMail.send")) {
            al.add(ChatColor.GREEN + "/smail send <player> <message>");
        }
        if (p.hasPermission("SMail.read")) {
            al.add(ChatColor.GREEN + "/smail read <index>");
        }
        if (p.hasPermission("SMail.info")) {
            al.add(ChatColor.GREEN + "/smail info [index]");
        }
        if (p.hasPermission("SMail.delete")) {
            al.add(ChatColor.GREEN + "/smail delete <index>");
        }
        for (String s : al) {
            p.sendMessage(s);
        }
        p.sendMessage(ChatColor.GOLD + "====== Page 1 of 1 ======");
    }

    private void showAIDs(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "[SimpleZones] Animal Flag IDs");
        for (AniIDs aid : AnimalFlag.AniIDs.values()) {
            sender.sendMessage(ChatColor.GOLD + aid.type.getName() + ": " + aid.name().substring(3));
        }
    }

    private void showMIDs(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "[SimpleZones] Monster Flag IDs");
        for (MobIDs aid : MonsterFlag.MobIDs.values()) {
            sender.sendMessage(ChatColor.GOLD + aid.type.getName() + ": " + aid.name().substring(3));
        }
    }

    @Override
	public void onDisable() {
        Town.save(db, prefix);
        Plot.save(db, prefix);
        ZonePlayer.save(db, prefix);
        Town.saveBans(db, prefix);
        Mail.save(db, prefix);
        Sanctuary.save(db, prefix);
        Outpost.save(db, prefix);
        if (db != null) {
            db.close();
        }

    }
}
