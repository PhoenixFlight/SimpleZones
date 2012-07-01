package com.zephyrr.simplezones.flags;

import com.zephyrr.simplezones.SimpleZones;

/**
 *
 * @author Phoenix
 */
public class FireFlag implements Flag {

    private boolean burn;

    public FireFlag() {
        loadDefaults();
    }

    public void loadDefaults() {
        burn = SimpleZones.getPlugConfig().getBoolean("default-flags.fire.burn");
    }

    public void loadTownSets(String s) {
        burn = Boolean.parseBoolean(s);
    }

    public void setBlocked(Object obj, boolean tf) {
        burn = tf;
    }

    public void setAll(boolean tf) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public boolean isBlocked(Object obj) {
        return burn;
    }

    public String getData() {
        return "" + burn;
    }
}
