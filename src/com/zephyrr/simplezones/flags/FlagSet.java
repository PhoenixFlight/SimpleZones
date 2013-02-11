package com.zephyrr.simplezones.flags;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

/**
 *
 * @author Phoenix
 */
public class FlagSet {
    private Flag[] flags;
    public FlagSet() {
        flags = new Flag[6];
        flags[0] = new MonsterFlag();
        flags[1] = new AnimalFlag();
        flags[2] = new BlockFlag();
        flags[3] = new FireFlag();
        flags[4] = new BombFlag();
        flags[5] = new PvpFlag();
    }
    public String getData(char flag) {
        switch(flag) {
            case 'f':
                return flags[3].getData();
            case 'a':
                return flags[1].getData();
            case 'm':
                return flags[0].getData();
            case 'e':
                return flags[4].getData();
            case 'b':
                return flags[2].getData();
            case 'p':
            	return flags[5].getData();
            default:
                return "";
        }
    }
    public void loadStarts(String a, String b, String c, boolean d, boolean e, boolean f) {
        flags[0].loadTownSets(b);
        flags[1].loadTownSets(a);
        flags[2].loadTownSets(c);
        flags[3].loadTownSets(d + "");
        flags[4].loadTownSets(e + "");
        flags[5].loadTownSets(f + "");
    }
    public boolean isBlocked(Object obj) {
        if(obj instanceof EntityType) {
            return flags[0].isBlocked(obj) || flags[1].isBlocked(obj);
        } else if(obj instanceof Material) {
            return flags[2].isBlocked(((Material)obj));
        } else if(obj instanceof BlockBurnEvent || obj instanceof BlockSpreadEvent) {
            return flags[3].isBlocked(null);
        } else if(obj instanceof ExplosionPrimeEvent) {
            return flags[4].isBlocked(null);
        } else if(obj instanceof EntityDamageByEntityEvent) {
        	return flags[5].isBlocked(obj);
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
                flags[3].setBlocked(null, s.charAt(0) == '+');
                break;
            case 'e':
                flags[4].setBlocked(null, s.charAt(0) == '+');
                break;
            case 'p':
            	flags[5].setBlocked(null, s.charAt(0) == '+');
            	break;
            case 'b':
                if(s.length() == 2)
                    return false;
                else {
                    int id;
                    try {
                        id = Integer.parseInt(s.substring(2));
                    } catch(NumberFormatException ex) {
                        return false;
                    }
                    if(Material.getMaterial(id) == null)
                        return false;
                    flags[2].setBlocked(id, s.charAt(0) == '+');
                }
                break;
            case 'a':
                if(s.length() == 2)
                    flags[1].setAll(s.charAt(0) == '+');
                else {
                    int id;
                    try {
                        id = Integer.parseInt(s.substring(2));
                    } catch(NumberFormatException ex) {
                        return false;
                    }
                    if(id < 1 || id > 10)
                        return false;
                    flags[1].setBlocked(AnimalFlag.AniIDs.valueOf("ANI" + id).type, s.charAt(0) == '+');
                }
                break;
            case 'm':
                if(s.length() == 2)
                    flags[0].setAll(s.charAt(0) == '+');
                else {
                    int id;
                    try {
                        id = Integer.parseInt(s.substring(2));
                    } catch(NumberFormatException ex) {
                        return false;
                    }
                    if(id < 1 || id > 15)
                        return false;
                    flags[0].setBlocked(MonsterFlag.MobIDs.valueOf("mob" + id).type, s.charAt(0) == '+');
                }
                break;
            default: return false;
        }
        return true;
    }
}
