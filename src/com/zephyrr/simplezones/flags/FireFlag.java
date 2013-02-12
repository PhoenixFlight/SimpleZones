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

    @Override
	public void loadDefaults() {
        burn = SimpleZones.getPlugConfig().getBoolean("default-flags.fire.burn");
    }

    @Override
	public void loadTownSets(String s) {
        burn = Boolean.parseBoolean(s);
    }

    @Override
	public void setBlocked(Object obj, boolean tf) {
        burn = tf;
    }

    @Override
	public void setAll(boolean tf) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
	public boolean isBlocked(Object obj) {
        return burn;
    }

    @Override
	public String getData() {
        return "" + burn;
    }
}
