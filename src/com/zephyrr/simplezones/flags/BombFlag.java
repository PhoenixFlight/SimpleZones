package com.zephyrr.simplezones.flags;

import com.zephyrr.simplezones.SimpleZones;

public class BombFlag implements Flag {
    private boolean blocked;
    public BombFlag() {

    }

    public void loadDefaults() {
        blocked = SimpleZones.getPlugConfig().getBoolean("default-flags.explode.blocked");
    }

    public void loadTownSets(String s) {
        blocked = Boolean.parseBoolean(s);
    }

    public void setBlocked(Object obj, boolean tf) {
        blocked = tf;
    }

    public void setAll(boolean tf) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public boolean isBlocked(Object obj) {
        return blocked;
    }

    public String getData() {
        return blocked + "";
    }
}
