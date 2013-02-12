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
    public static enum MobIDs {
        mob1(EntityType.CAVE_SPIDER),
        mob2(EntityType.CREEPER),
        mob3(EntityType.ENDER_DRAGON),
        mob4(EntityType.ENDERMAN),
        mob5(EntityType.GHAST),
        mob6(EntityType.GIANT),
        mob7(EntityType.MAGMA_CUBE),
        mob8(EntityType.PIG_ZOMBIE),
        mob9(EntityType.SILVERFISH),
        mob10(EntityType.SKELETON),
        mob11(EntityType.SLIME),
        mob12(EntityType.SPIDER),
        mob13(EntityType.SQUID),
        mob14(EntityType.ZOMBIE),
        mob15(EntityType.BLAZE);
        public EntityType type;
        MobIDs(EntityType type) {
            this.type = type;
        }
    }
    private HashMap<EntityType, Boolean> blockMap;

    public MonsterFlag() {
        blockMap = new HashMap<EntityType, Boolean>();
        loadDefaults();
    }

    @Override
	public void loadDefaults() {
        FileConfiguration fc = SimpleZones.getPlugConfig();
        blockMap.put(EntityType.BLAZE, fc.getBoolean("default-flags.monsters.blaze"));
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

    @Override
	public void loadTownSets(String s) {
        String[] data = s.split(",");
        int i = 0;
        for(EntityType et : blockMap.keySet())
            blockMap.put(et, Boolean.parseBoolean(data[i++]));
    }

    @Override
	public String getData() {
        String data = "";
        for(Boolean b : blockMap.values())
            data += b + ",";
        if(!data.isEmpty())
            data = data.substring(0, data.length() - 1);
        return data;
    }

    @Override
	public void setBlocked(Object obj, boolean tf) {
        if(!(obj instanceof EntityType))
            return;
        blockMap.put((EntityType)obj, tf);
    }

    @Override
	public boolean isBlocked(Object obj) {
        return obj instanceof EntityType && blockMap.containsKey(obj) && blockMap.get(obj);
    }

    @Override
	public void setAll(boolean tf) {
        for(EntityType et : blockMap.keySet())
            blockMap.put(et, tf);
    }
}
