package com.zephyrr.simplezones.flags;

/**
 *
 * @author Phoenix
 */
public interface Flag {
    public void loadDefaults();
    public void loadTownSets(String s);
    public void setBlocked(Object obj, boolean tf);
    public void setAll(boolean tf);
    public boolean isBlocked(Object obj);
    public String getData();
}
