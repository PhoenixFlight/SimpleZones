package com.zephyrr.simplezones;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import org.bukkit.ChatColor;
import sqlibrary.Database;

/**
 *
 * @author Phoenix
 */
public class Mail {
    public static void save(Database db, String prefix) {
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
