package com.zephyrr.simplezones;

import java.io.File;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sqlibrary.*;
import com.zephyrr.simplezones.listeners.*;
import java.util.ArrayList;
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author Phoenix
 */
public class SimpleZones extends JavaPlugin {

    private static JavaPlugin plug;
    private static double VERSION = 0.5;
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

        if(getConfig().getDouble("version") != VERSION)
            updateResources();

        Town.fill(db, prefix);
        Plot.fill(db, prefix);
        ZonePlayer.fill(db, prefix);
        Town.fillBans(db, prefix);
        Mail.fill(db, prefix);

        if (!new File("plugins/SimpleZones/config.yml").exists()) {
            saveDefaultConfig();
        }

        for (Player p : getServer().getOnlinePlayers()) {
            ZonePlayer.registerUser(p);
            ZonePlayer.findUser(p.getName()).setPlayer(p);
        }

        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    private void updateResources() {
        
        saveDefaultConfig();
    }

    private void firstRun() {
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
        if (command.getName().equalsIgnoreCase("zone")) {
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
                    return zoneSender.plotCreate(db, prefix);
                } else if (args[1].equalsIgnoreCase("delete") && sender.hasPermission("Zone.plot.delete")) {
                    return zoneSender.plotDelete();
                }
                return false;
            } else if(args[0].equalsIgnoreCase("flag") && sender.hasPermission("Zone.flag")) {
                return zoneSender.flag(args);
            } else if(args[0].equalsIgnoreCase("massmail") && sender.hasPermission("Zone.massmail")) {
                if(args.length == 1)
                    return false;
                String msg = "";
                for(int i = 1; i < args.length; i++)
                    msg += args[i];
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
        } else if (command.getName().equalsIgnoreCase("mail")) {
            if (args[0].equalsIgnoreCase("send") && sender.hasPermission("Mail.send")) {
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
                return true;
            } else if (args[0].equalsIgnoreCase("info") && sender.hasPermission("Mail.info")) {
                if (args.length == 1) {
                    zoneSender.getMailInfo();
                    return true;
                }
                try {
                    zoneSender.getMailInfo(Integer.parseInt(args[1]));
                } catch (NumberFormatException ex) {
                }
                return true;
            } else if (args[0].equalsIgnoreCase("read") && sender.hasPermission("Mail.read")) {
                if (args.length == 1) {
                    return false;
                }
                try {
                    zoneSender.readMail(Integer.parseInt(args[1]));
                } catch (NumberFormatException ex) {
                }
                return true;
            } else if (args[0].equalsIgnoreCase("help") && sender.hasPermission("Mail.help")) {
                showMailHelp(send);
                return true;
            } else if (args[0].equalsIgnoreCase("delete") && sender.hasPermission("Mail.delete")) {
                try {
                    zoneSender.deleteMail(Integer.parseInt(args[1]));
                } catch (NumberFormatException ex) {
                }
                return true;
            }
        } else if(command.getName().equalsIgnoreCase("zchat")) {
            if(args[0].equalsIgnoreCase("toggle") && send.hasPermission("Zchat.toggle")) {
                zoneSender.toggleChannel();
                send.sendMessage(ChatColor.GOLD + "[SimpleZones] Your active chat channel has been set to " + zoneSender.getChannel().name().toLowerCase());
                return true;
            } else if(args[0].equalsIgnoreCase("help") && send.hasPermission("Zchat.help")) {
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
        } else {
            p.teleport(Town.getTown(s).getWarp());
        }
    }

    private void showChatHelp(Player p) {
        ArrayList<String> al = new ArrayList<String>();
        if(p.hasPermission("Zchat.toggle")) {
            al.add(ChatColor.GREEN + "/zchat toggle");
            al.add(ChatColor.GOLD + "Toggles your active chat channel between town and global.");
        }
        for(String s : al)
            p.sendMessage(s);
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
            al.add(ChatColor.GREEN + "/zone define");
            al.add(ChatColor.GOLD + "Starts the cuboid selection process to define a town.");
        }
        if (p.hasPermission("Zone.create")) {
            al.add(ChatColor.GREEN + "/zone create <name>");
            al.add(ChatColor.GOLD + "Creates a town from the selection.");
        }
        if (p.hasPermission("Zone.massmail")) {
            al.add(ChatColor.GREEN + "/zone massmail <message>");
            al.add(ChatColor.GOLD + "Sends a mail message to all members of your town.");
        }
        if (p.hasPermission("Zone.setowner")) {
            al.add(ChatColor.GREEN + "/zone setowner <player>");
            al.add(ChatColor.GOLD + "Transfers ownership to <player>. You must own the town.");
        }
        if (p.hasPermission("Zone.invite")) {
            al.add(ChatColor.GREEN + "/zone invite <player>");
            al.add(ChatColor.GOLD + "Adds <player> to your town.");
        }
        if (p.hasPermission("Zone.ban")) {
            al.add(ChatColor.GREEN + "/zone ban <player>");
            al.add(ChatColor.GOLD + "Bans <player> from your town.  They are kicked, cannot ask to join, and cannot enter the town anymore.");
        }
        if (p.hasPermission("Zone.delete")) {
            al.add(ChatColor.GREEN + "/zone delete");
            al.add(ChatColor.GOLD + "Permanently deletes your town.");
        }
        if (p.hasPermission("Zone.unban")) {
            al.add(ChatColor.GREEN + "/zone unban <player>");
            al.add(ChatColor.GOLD + "Unbans <player> from your town.");
        }
        if (p.hasPermission("Zone.setwarp")) {
            al.add(ChatColor.GREEN + "/zone setwarp");
            al.add(ChatColor.GOLD + "Sets the warp point for your town to your location.");
        }
        if (p.hasPermission("Zone.plot.define")) {
            al.add(ChatColor.GREEN + "/zone plot define");
            al.add(ChatColor.GOLD + "Starts the cuboid selection process to define a plot.");
        }
        if (p.hasPermission("Zone.plot.create")) {
            al.add(ChatColor.GREEN + "/zone plot create");
            al.add(ChatColor.GOLD + "Creates a plot from the given selection.");
        }
        if(p.hasPermission("Zone.plot.delete")) {
            al.add(ChatColor.GREEN + "/zone plot delete");
            al.add(ChatColor.GOLD + "Deletes the plot in which you are currently standing.");
        }
        if (p.hasPermission("Zone.plot.addmember")) {
            al.add(ChatColor.GREEN + "/zone plot addmember <player>");
            al.add(ChatColor.GOLD + "Allows <player to build in the plot you're standing in.");
        }
        if (p.hasPermission("Zone.plot.removemember")) {
            al.add(ChatColor.GREEN + "/zone plot removemember <player>");
            al.add(ChatColor.GOLD + "Disallows <player> to build in the plot you're standing in.");
        }
        if (p.hasPermission("Zone.list")) {
            al.add(ChatColor.GREEN + "/zone list");
            al.add(ChatColor.GOLD + "Lists all towns");
        }
        if (p.hasPermission("Zone.members")) {
            al.add(ChatColor.GREEN + "/zone members <town>");
            al.add(ChatColor.GOLD + "Lists the members in <town>");
        }
        if (p.hasPermission("Zone.warp")) {
            al.add(ChatColor.GREEN + "/zone warp <town>");
            al.add(ChatColor.GOLD + "Warps you to <town>");
        }
        if (p.hasPermission("Zone.join")) {
            al.add(ChatColor.GREEN + "/zone join <town>");
            al.add(ChatColor.GOLD + "Notifies the owner of <town> that you would like to join.");
        }
        if (p.hasPermission("Zone.leave")) {
            al.add(ChatColor.GREEN + "/zone leave");
            al.add(ChatColor.GOLD + "Leaves the current town.");
        }
        page--;
        for (int i = page * 12; i < 12 + (page * 12) && i < al.size(); i++) 
            p.sendMessage(al.get(i));
        p.sendMessage(ChatColor.GOLD + "====== Page " + (page + 1) + " of " + (al.size() / 12 + 1) + " ======");
    }

    private void showMailHelp(Player p) {
        ArrayList<String> al = new ArrayList<String>();
        if (p.hasPermission("Mail.send")) {
            al.add(ChatColor.GREEN + "/mail send <player> <message>");
        }
        if (p.hasPermission("Mail.read")) {
            al.add(ChatColor.GREEN + "/mail read <index>");
        }
        if (p.hasPermission("Mail.info")) {
            al.add(ChatColor.GREEN + "/mail info [index]");
        }
        if (p.hasPermission("Mail.delete")) {
            al.add(ChatColor.GREEN + "/mail delete <index>");
        }
        for (String s : al) {
            p.sendMessage(s);
        }
        p.sendMessage(ChatColor.GOLD + "====== Page 1 of 1 ======");
    }

    public void onDisable() {
        Town.save(db, prefix);
        Plot.save(db, prefix);
        ZonePlayer.save(db, prefix);
        Town.saveBans(db, prefix);
        Mail.save(db, prefix);
        if (db != null) {
            db.close();
        }

    }
}
