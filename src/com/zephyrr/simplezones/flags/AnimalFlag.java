package com.zephyrr.simplezones.flags;

import java.util.HashMap;
import org.bukkit.entity.EntityType;

/**
 *
 * @author Phoenix
 */
public class AnimalFlag implements Flag {
    private HashMap<EntityType, Boolean> blockList;
    public AnimalFlag() {
        blockList = new HashMap<EntityType, Boolean>();
        loadDefaults();
    }

    public void loadDefaults() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void loadTownSets(String s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setBlocked(Object obj, boolean tf) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAll(boolean tf) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isBlocked(Object obj) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
