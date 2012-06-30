package com.zephyrr.simplezones.flags;

import org.bukkit.entity.EntityType;

/**
 *
 * @author Phoenix
 */
public class FlagSet {
    private Flag[] flags;
    public FlagSet() {
        flags = new Flag[5];
        flags[0] = new MonsterFlag();
    }
    public boolean isBlocked(Object obj) {
        if(obj instanceof EntityType) {
            return flags[0].isBlocked(obj);
        }
        return false;
    }
    public boolean setFlag(String s) {
        if(s.length() < 2)
            return false;
        if(s.charAt(0) != '+' && s.charAt(0) != '-')
            return false;
        switch(s.charAt(1)) {
            case 'f':
                break;
            case 'e':
                break;
            case 'b':
                break;
            case 'a':
                break;
            case 'm':
                if(s.length() == 2)
                    flags[0].setAll(s.charAt(0) == '+');
                break;
            default: return false;
        }
        return true;
    }
}
