package com.zephyrr.simplezones.flags;

import com.zephyrr.simplezones.SimpleZones;
import java.util.HashMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

/**
 *
 * @author Phoenix
 */
public class MonsterFlag implements Flag {
    private HashMap<EntityType, Boolean> blockMap;

    public MonsterFlag() {
        blockMap = new HashMap<EntityType, Boolean>();
        loadDefaults();
    }

    public void loadDefaults() {
        FileConfiguration fc = SimpleZones.getPlugConfig();
        blockMap.put(EntityType.CAVE_SPIDER, fc.getBoolean("default-flags.monsters.cave_spider"));
        blockMap.put(EntityType.CREEPER, fc.getBoolean("default-flags.monsters.creeper"));
        blockMap.put(EntityType.ENDER_DRAGON, fc.getBoolean("default-flags.monsters.ender_dragon"));
        blockMap.put(EntityType.ENDERMAN, fc.getBoolean("default-flags.monsters.enderman"));
        blockMap.put(EntityType.GHAST, fc.getBoolean("default-flags.monsters.ghast"));
        blockMap.put(EntityType.GIANT, fc.getBoolean("default-flags.monsters.giant"));
        blockMap.put(EntityType.MAGMA_CUBE, fc.getBoolean("default-flags.monsters.magma_cube"));
        blockMap.put(EntityType.PIG_ZOMBIE, fc.getBoolean("default-flags.monsters.pig_zombie"));
        blockMap.put(EntityType.SILVERFISH, fc.getBoolean("default-flags.monsters.silverfish"));
        blockMap.put(EntityType.SKELETON, fc.getBoolean("default-flags.monsters.skeleton"));
        blockMap.put(EntityType.SLIME, fc.getBoolean("default-flags.monsters.slime"));
        blockMap.put(EntityType.SPIDER, fc.getBoolean("default-flags.monsters.spider"));
        blockMap.put(EntityType.SQUID, fc.getBoolean("default-flags.monsters.squid"));
        blockMap.put(EntityType.ZOMBIE, fc.getBoolean("default-flags.monsters.zombie"));
    }

    public void loadTownSets(String s) {
        String[] data = s.split(",");
        int i = 0;
        for(EntityType et : blockMap.keySet())
            blockMap.put(et, Boolean.parseBoolean(data[i++]));
    }

    public String getData() {
        String data = "";
        for(Boolean b : blockMap.values())
            data += b + ",";
        if(!data.isEmpty())
            data = data.substring(0, data.length() - 1);
        return data;
    }

    public void setBlocked(Object obj, boolean tf) {
        if(!(obj instanceof EntityType))
            return;
        blockMap.put((EntityType)obj, tf);
    }

    public boolean isBlocked(Object obj) {
        return obj instanceof EntityType && blockMap.containsKey(obj) && blockMap.get(obj);
    }

    public void setAll(boolean tf) {
        for(EntityType et : blockMap.keySet())
            blockMap.put(et, tf);
    }
}
