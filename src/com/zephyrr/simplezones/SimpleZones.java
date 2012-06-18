package com.zephyrr.simplezones;

import java.io.File;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sqlibrary.*;
import com.zephyrr.simplezones.listeners.*;

/**
 *
 * @author Phoenix
 */
public class SimpleZones extends JavaPlugin {

    private static Server serv;

    public static Player getPlayer(String name) {
        return serv.getPlayer(name);
    }
    public static World getWorld(String name) {
        return serv.getWorld(name);
    }
    public static World getDefaultWorld() {
        return serv.getWorlds().get(0);
    }

    private Database db;
    private String prefix = "SZ_";

    public void onEnable() {
        SimpleZones.serv = getServer();
        // TODO: Change this to use the config file.
        db = new MySQL(getLogger(),
                prefix,
                "localhost",
                "3306",
                "simplezones",
                "simplezones",
                "simplezones");
        db.open();
        firstRun();
        Town.fill(db, prefix);
        Plot.fill(db, prefix);
        ZonePlayer.fill(db, prefix);
        Town.fillBans(db, prefix);
        Mail.fill(db, prefix);

        for(Player p : getServer().getOnlinePlayers()) {
            ZonePlayer.registerUser(p);
            ZonePlayer.findUser(p.getName()).setPlayer(p);
        }

        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    private boolean firstRun() {
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
                    + "PRIMARY KEY (M_Id)"
                    + ")");
        }

        if (!new File("plugins/SimpleZones/config.yml").exists()) {
            saveDefaultConfig();
        }

        return true;
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
                    if (args.length == 2) 
                        return false;
                    return zoneSender.plotAddMember(args[2]);
                } else if (args[1].equalsIgnoreCase("removemember") && sender.hasPermission("Zone.plot.removemember")) {
                    if (args.length == 2) 
                        return false;
                    return zoneSender.plotRemoveMember(args[2]);
                } else if (args[1].equalsIgnoreCase("create") && sender.hasPermission("Zone.plot.create")) {
                    return zoneSender.plotCreate(db, prefix);
                }
                return false;
            }
            if (args[0].equalsIgnoreCase("define") && sender.hasPermission("Zone.define")) {
                return zoneSender.define();
            } else if (args[0].equalsIgnoreCase("create") && sender.hasPermission("Zone.create")) {
                if (args.length == 1) 
                    return false;
                return zoneSender.create(args[1], db, prefix);
            } else if (args[0].equalsIgnoreCase("setowner") && sender.hasPermission("Zone.setowner")) {
                if (args.length == 1) 
                    return false;
                return zoneSender.setOwner(args[1]);
            } else if (args[0].equalsIgnoreCase("invite") && sender.hasPermission("Zone.invite")) {
                if (args.length == 1) 
                    return false;
                return zoneSender.invite(args[1]);
            } else if (args[0].equalsIgnoreCase("ban") && sender.hasPermission("Zone.ban")) {
                if (args.length == 1) 
                    return false;
                return zoneSender.ban(args[1]);
            } else if (args[0].equalsIgnoreCase("unban") && sender.hasPermission("Zone.unban")) {
                if (args.length == 1)
                    return false;
                return zoneSender.unban(args[1]);
            } else if (args[0].equalsIgnoreCase("setwarp") && sender.hasPermission("Zone.setwarp")) {
                return zoneSender.setWarp();
            } else if (args[0].equalsIgnoreCase("delete") && sender.hasPermission("Zone.delete")) {
                return zoneSender.delete();
            } else if (args[0].equalsIgnoreCase("list") && sender.hasPermission("Zone.list")) {
                listTowns(send);
                return true;
            } else if (args[0].equalsIgnoreCase("members") && sender.hasPermission("Zone.members")) {
                if (args.length == 1) 
                    return false;
                listMembers(send, args[1]);
                return true;
            } else if (args[0].equalsIgnoreCase("warp") && sender.hasPermission("Zone.warp")) {
                if (args.length == 1) 
                    return false;
                warp(send, args[1]);
                return true;
            } else if (args[0].equalsIgnoreCase("join") && sender.hasPermission("Zone.join")) {
                if (args.length == 1)
                    return false;
                return zoneSender.join(args[1]);
            } else if (args[0].equalsIgnoreCase("leave") && sender.hasPermission("Zone.leave")) {
                return zoneSender.quit();
            } else if (args[0].equalsIgnoreCase("help") && sender.hasPermission("Zone.leave")) {
                showTownHelp(send, args);
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("mail")) {
            if (args[0].equalsIgnoreCase("send") && sender.hasPermission("Mail.send")) {
                if (args.length < 3) 
                    return false;
                String to = args[1];
                if(ZonePlayer.findUser(to) == null) {
                    send.sendMessage(ChatColor.RED + "[SimpleZones] There is no player named " + to);
                    return true;
                }
                String msg = "";
                for(int i = 2; i < args.length; i++)
                    msg += args[i] + " ";
                ZonePlayer.findUser(to).sendMail(new Mail(msg, false, zoneSender));
                return true;
            } else if (args[0].equalsIgnoreCase("info") && sender.hasPermission("Mail.info")) {
                if (args.length == 1) {
                    zoneSender.getMailInfo();
                    return true;
                }
                try {
                    zoneSender.getMailInfo(Integer.parseInt(args[1]));
                } catch(NumberFormatException ex) {}
                return true;
            } else if (args[0].equalsIgnoreCase("read") && sender.hasPermission("Mail.read")) {
                if (args.length == 1) 
                    return false;
                try {
                    zoneSender.readMail(Integer.parseInt(args[1]));
                } catch(NumberFormatException ex) {}
                return true;
            } else if (args[0].equalsIgnoreCase("help") && sender.hasPermission("Mail.help")) {
                showMailHelp(send);
                return true;
            } else if(args[0].equalsIgnoreCase("delete") && sender.hasPermission("Mail.delete")) {
                try {
                    zoneSender.deleteMail(Integer.parseInt(args[1]));
                } catch(NumberFormatException ex) {}
                return true;
            }
        }
        return false;
    }

    private void listTowns(Player p) {
        p.sendMessage(ChatColor.GOLD + "[SimpleZones] Town List:");
        for(String s : Town.getTownList().keySet())
            p.sendMessage(ChatColor.GOLD + s);
    }

    private void listMembers(Player p, String s) {
        if(Town.getTown(s) == null)
            p.sendMessage(ChatColor.RED + "[SimpleZones] " + s + " does not exist.");
        else {
            p.sendMessage(ChatColor.GOLD + "[SimpleZone] Members of " + s + ":");
            p.sendMessage(ChatColor.GOLD + "Owner: " + Town.getTown(s).getOwner());
            for(String name : Town.getTown(s).getMembers())
                p.sendMessage(ChatColor.GOLD + name);
        }
    }

    private void warp(Player p, String s) {
        if(Town.getTown(s) == null)
            p.sendMessage(ChatColor.RED + "[SimpleZones] " + s + " does not exist.");
        else p.teleport(Town.getTown(s).getWarp());
    }

    private void showTownHelp(Player p, String[] args) {
        int page = 1;
        if(args.length != 1)
            try {
                page = Integer.parseInt(args[1]);
            } catch(NumberFormatException ex) {}
        switch(page) {
            case 1:
                if(p.hasPermission("Zone.define")) {
                    p.sendMessage(ChatColor.GREEN + "/zone define");
                    p.sendMessage(ChatColor.GOLD + "Starts the cuboid selection process to define a town.");
                }
                if(p.hasPermission("Zone.create")) {
                    p.sendMessage(ChatColor.GREEN + "/zone create <name>");
                    p.sendMessage(ChatColor.GOLD + "Creates a town from the selection.");
                }
                if(p.hasPermission("Zone.setowner")) {
                    p.sendMessage(ChatColor.GREEN + "/zone setowner <player>");
                    p.sendMessage(ChatColor.GOLD + "Transfers ownership to <player>. You must own the town.");
                }
                if(p.hasPermission("Zone.invite")) {
                    p.sendMessage(ChatColor.GREEN + "/zone invite <player>");
                    p.sendMessage(ChatColor.GOLD + "Adds <player> to your town.");
                }
                if(p.hasPermission("Zone.ban")) {
                    p.sendMessage(ChatColor.GREEN + "/zone ban <player>");
                    p.sendMessage(ChatColor.GOLD + "Bans <player> from your town.  They are kicked, cannot ask to join, and cannot enter the town anymore.");
                }
                if(p.hasPermission("Zone.delete")) {
                    p.sendMessage(ChatColor.GREEN + "/zone delete");
                    p.sendMessage(ChatColor.GOLD + "Permanently deletes your town.");
                }
                p.sendMessage(ChatColor.GOLD + "======== Page 1 of 3 ========");
                break;
            case 2:
                if(p.hasPermission("Zone.unban")) {
                    p.sendMessage(ChatColor.GREEN + "/zone unban <player>");
                    p.sendMessage(ChatColor.GOLD + "Unbans <player> from your town.");
                }
                if(p.hasPermission("Zone.setwarp")) {
                    p.sendMessage(ChatColor.GREEN + "/zone setwarp");
                    p.sendMessage(ChatColor.GOLD + "Sets the warp point for your town to your location.");
                }
                if(p.hasPermission("Zone.plot.define")) {
                    p.sendMessage(ChatColor.GREEN + "/zone plot define");
                    p.sendMessage(ChatColor.GOLD + "Starts the cuboid selection process to define a plot.");
                }
                if(p.hasPermission("Zone.plot.create")) {
                    p.sendMessage(ChatColor.GREEN + "/zone plot create");
                    p.sendMessage(ChatColor.GOLD + "Creates a plot from the given selection.");
                }
                if(p.hasPermission("Zone.plot.addmember")) {
                    p.sendMessage(ChatColor.GREEN + "/zone plot addmember <player>");
                    p.sendMessage(ChatColor.GOLD + "Allows <player to build in the plot you're standing in.");
                }
                if(p.hasPermission("Zone.plot.removemember")) {
                    p.sendMessage(ChatColor.GREEN + "/zone plot removemember <player>");
                    p.sendMessage(ChatColor.GOLD + "Disallows <player> to build in the plot you're standing in.");
                }
                p.sendMessage(ChatColor.GOLD + "======== Page 2 of 3 ========");
                break;
            case 3:
                if(p.hasPermission("Zone.list")) {
                    p.sendMessage(ChatColor.GREEN + "/zone list");
                    p.sendMessage(ChatColor.GOLD + "Lists all towns");
                }
                if(p.hasPermission("Zone.members")) {
                    p.sendMessage(ChatColor.GREEN + "/zone members <town>");
                    p.sendMessage(ChatColor.GOLD + "Lists the members in <town>");
                }
                if(p.hasPermission("Zone.warp")) {
                    p.sendMessage(ChatColor.GREEN + "/zone warp <town>");
                    p.sendMessage(ChatColor.GOLD + "Warps you to <town>");
                }
                if(p.hasPermission("Zone.join")) {
                    p.sendMessage(ChatColor.GREEN + "/zone join <town>");
                    p.sendMessage(ChatColor.GOLD + "Notifies the owner of <town> that you would like to join.");
                }
                if(p.hasPermission("Zone.leave")) {
                    p.sendMessage(ChatColor.GREEN + "/zone leave");
                    p.sendMessage(ChatColor.GOLD + "Leaves the current town.");
                }
                p.sendMessage(ChatColor.GOLD + "======== Page 3 of 3 ========");
        }
    }

    private void showMailHelp(Player p) {
        if(p.hasPermission("Mail.send"))
            p.sendMessage(ChatColor.GREEN + "/mail send <player> <message>");
        if(p.hasPermission("Mail.read"))
            p.sendMessage(ChatColor.GREEN + "/mail read <index>");
        if(p.hasPermission("Mail.info"))
            p.sendMessage(ChatColor.GREEN + "/mail info [index]");
        if(p.hasPermission("Mail.delete"))
            p.sendMessage(ChatColor.GREEN + "/mail delete <index>");
    }

    public void onDisable() {
        Town.save(db, prefix);
        Plot.save(db, prefix);
        ZonePlayer.save(db, prefix);
        Town.saveBans(db, prefix);
        Mail.save(db, prefix);
        db.close();
    }
}
