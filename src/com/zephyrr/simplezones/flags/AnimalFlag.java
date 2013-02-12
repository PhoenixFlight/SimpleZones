package com.zephyrr.simplezones.flags;

import com.zephyrr.simplezones.SimpleZones;
import java.util.HashMap;
import org.bukkit.entity.EntityType;

/**
 *
 * @author Phoenix
 */
public class AnimalFlag implements Flag {
    public static enum AniIDs {
        ANI1(EntityType.CHICKEN),
        ANI2(EntityType.COW),
        ANI3(EntityType.IRON_GOLEM),
        ANI4(EntityType.MUSHROOM_COW),
        ANI5(EntityType.PIG),
        ANI6(EntityType.SHEEP),
        ANI7(EntityType.SNOWMAN),
        ANI8(EntityType.VILLAGER),
        ANI9(EntityType.WOLF),
        ANI10(EntityType.OCELOT);
        public EntityType type;
        AniIDs(EntityType type) {
            this.type = type;
        }
    }
    private HashMap<EntityType, Boolean> blockList;

    public AnimalFlag() {
        blockList = new HashMap<EntityType, Boolean>();
        loadDefaults();
    }

    @Override
	public void loadDefaults() {
        blockList.put(EntityType.CHICKEN, SimpleZones.getPlugConfig().getBoolean("default-flags.animals.chicken"));
        blockList.put(EntityType.COW, SimpleZones.getPlugConfig().getBoolean("default-flags.animals.cow"));
        blockList.put(EntityType.IRON_GOLEM, SimpleZones.getPlugConfig().getBoolean("default-flags.animals.irongolem"));
        blockList.put(EntityType.MUSHROOM_COW, SimpleZones.getPlugConfig().getBoolean("default-flags.animals.mooshroom"));
        blockList.put(EntityType.PIG, SimpleZones.getPlugConfig().getBoolean("default-flags.animals.pig"));
        blockList.put(EntityType.SHEEP, SimpleZones.getPlugConfig().getBoolean("default-flags.animals.sheep"));
        blockList.put(EntityType.SNOWMAN, SimpleZones.getPlugConfig().getBoolean("default-flags.animals.snowman"));
        blockList.put(EntityType.VILLAGER, SimpleZones.getPlugConfig().getBoolean("default-flags.animals.villager"));
        blockList.put(EntityType.WOLF, SimpleZones.getPlugConfig().getBoolean("default-flags.animals.wolf"));
    }

    @Override
	public void loadTownSets(String s) {
        String[] data = s.split(",");
        int i = 0;
        for (EntityType et : blockList.keySet()) {
            blockList.put(et, Boolean.parseBoolean(data[i++]));
        }
    }

    @Override
	public void setBlocked(Object obj, boolean tf) {
        if (!(obj instanceof EntityType)) {
            return;
        }
        blockList.put((EntityType) obj, tf);
    }

    @Override
	public void setAll(boolean tf) {
        for(EntityType et : blockList.keySet())
            blockList.put(et, tf);
    }

    @Override
	public boolean isBlocked(Object obj) {
        return obj instanceof EntityType && blockList.containsKey(obj) && blockList.get(obj);
    }

    @Override
	public String getData() {
        String data = "";
        for (Boolean b : blockList.values()) {
            data += b + ",";
        }
        if (!data.isEmpty()) {
            data = data.substring(0, data.length() - 1);
        }
        return data;
    }
}
