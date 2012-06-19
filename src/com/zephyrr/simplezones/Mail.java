package com.zephyrr.simplezones;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import org.bukkit.ChatColor;
import org.yaml.snakeyaml.Yaml;
import sqlibrary.Database;
import com.zephyrr.simplezones.ymlIO.MailYml;
import java.io.FileWriter;
import java.util.ArrayList;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

/**
 *
 * @author Phoenix
 */
public class Mail {
    public static void save(Database db, String prefix) {
        if(db == null) {
            saveYML();
            return;
        }
        int id = 0;
        db.wipeTable(prefix + "mail");
        for(ZonePlayer zone : ZonePlayer.getPMap().values()) {
            for(Mail m : zone.getMailList()) {
                int senderID = m.from.getID();
                int toID = zone.getID();
                String contents = m.message;
                boolean read = m.read;
                String query = "INSERT INTO " + prefix + "mail VALUES("
                        + id + ","
                        + senderID + ","
                        + toID + ","
                        + "'" + contents + "',"
                        + read
                        + ")";
                db.query(query);
                id++;
            }
        }
    }

    public static void fill(Database db, String prefix) {
        if(db == null) {
            fillYML();
            return;
        }
        Collection<ZonePlayer> collection = ZonePlayer.getPMap().values();
        for(ZonePlayer z : collection) {
            try {
                ResultSet rs = db.query("SELECT * FROM " + prefix + "mail WHERE RecipientID=" + z.getID());
                while(rs.next()) {
                    int sender = rs.getInt("SenderID");
                    String contents = rs.getString("Contents");
                    boolean read = rs.getBoolean("Unread");
                    ZonePlayer send = null;
                    for(ZonePlayer zone : collection)
                        if(zone.getID() == sender) {
                            send = zone;
                            break;
                        }
                    Mail mail = new Mail(contents, read, send);
                    z.sendMail(mail);
                }
            } catch(SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void saveYML() {
        try {
            File out = new File("plugins/SimpleZones/mail.yml");
            if(!out.exists())
                out.createNewFile();
            ArrayList<MailYml> al = new ArrayList<MailYml>();
            for(ZonePlayer zp : ZonePlayer.getPMap().values()) {
                for(Mail m : zp.getMailList()) {
                    MailYml myml = new MailYml();
                    myml.contents = m.message;
                    myml.receiver = zp.getID();
                    myml.sender = m.from.getID();
                    myml.unread = m.read;
                    al.add(myml);
                }
            }
            new Yaml().dumpAll(al.iterator(), new FileWriter(out));
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void fillYML() {
        File in = new File("plugins/SimpleZones/mail.yml");
        if(!in.exists())
            return;
        try {
            InputStream stream = new FileInputStream(in);
            Yaml yaml = new Yaml(new CustomClassLoaderConstructor(MailYml.class.getClassLoader()));
            for(Object o : yaml.loadAll(stream)) {
                MailYml myml = (MailYml)o;
                String msg = myml.contents;
                boolean read = myml.unread;
                int sender = myml.sender;
                int receiver = myml.receiver;
                ZonePlayer to = null, from = null;
                for(ZonePlayer zp : ZonePlayer.getPMap().values()) {
                    if(zp.getID() == sender) {
                        from = zp;
                    } else if(zp.getID() == receiver) {
                        to = zp;
                    }
                }
                Mail m = new Mail(msg, read, from);
                to.sendMail(m);
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    private String message;
    private boolean read;
    private ZonePlayer from;
    public Mail(String msg, boolean read, ZonePlayer sender) {
        message = msg;
        this.read = read;
        from = sender;
    }
    public String read() {
        read = true;
        String ret = ChatColor.GOLD + from.getName() + " wrote:\n" + message;
        return ret;
    }
    public boolean isUnread() {
        return !read;
    }
    public String getInfo() {
        return ChatColor.GOLD + "Sender: " + from.getName() + "\nRead: " + read;
    }
}
