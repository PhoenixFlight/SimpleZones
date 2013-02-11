package com.zephyrr.simplezones.flags;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.zephyrr.simplezones.SimpleZones;

public class PvpFlag implements Flag {
	private boolean pvp;
	
	public PvpFlag() {
		loadDefaults();
	}
	
	@Override
	public void loadDefaults() {
		pvp = SimpleZones.getPlugConfig().getBoolean("default-flags.pvp.blocked");
	}

	@Override
	public void loadTownSets(String s) {
		pvp = Boolean.parseBoolean(s);
	}

	@Override
	public void setBlocked(Object obj, boolean tf) {
		pvp = tf;
	}

	@Override
	public void setAll(boolean tf) {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public boolean isBlocked(Object obj) {
		EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)obj;
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			return pvp;
		}
		return false;
	}

	@Override
	public String getData() {
		return "" + pvp;
	}
}
