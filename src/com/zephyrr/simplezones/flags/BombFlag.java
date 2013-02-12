package com.zephyrr.simplezones.flags;

import com.zephyrr.simplezones.SimpleZones;

public class BombFlag implements Flag {
    private boolean blocked;
    public BombFlag() {
        loadDefaults();
    }

    @Override
	public void loadDefaults() {
        blocked = SimpleZones.getPlugConfig().getBoolean("default-flags.explode.blocked");
    }

    @Override
	public void loadTownSets(String s) {
        blocked = Boolean.parseBoolean(s);
    }

    @Override
	public void setBlocked(Object obj, boolean tf) {
        blocked = tf;
    }

    @Override
	public void setAll(boolean tf) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
	public boolean isBlocked(Object obj) {
        return blocked;
    }

    @Override
	public String getData() {
        return blocked + "";
    }
}
