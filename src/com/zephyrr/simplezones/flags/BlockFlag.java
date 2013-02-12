package com.zephyrr.simplezones.flags;

import com.zephyrr.simplezones.SimpleZones;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Material;

/**
 *
 * @author Phoenix
 */
public class BlockFlag implements Flag {

    private HashMap<Material, Boolean> blockedBlocks;
    private HashSet<Material> unblockable;
    public BlockFlag() {
        blockedBlocks = new HashMap<Material, Boolean>();
        unblockable = new HashSet<Material>();
        loadDefaults();
        loadFinals();
        loadBans();
    }

    private void loadBans() {
        String[] dataIn = SimpleZones.getPlugConfig().getString("default-flags.blocks.unblockable").split(",");
        int[] data = new int[dataIn.length];
        for(int i = 0; i < dataIn.length; i++) {
            try {
                data[i] = Integer.parseInt(dataIn[i]);
                if(data[i] < 0 || data[i] > 136)
                    data[i] = -1;
            } catch(NumberFormatException ex) {
                data[i] = -1;
            }
        }
        for(int i = 0; i < data.length; i++)
            if(data[i] != -1)
                unblockable.add(Material.getMaterial(data[i]));
    }

    private void loadFinals() {
        String[] dataIn = SimpleZones.getPlugConfig().getString("default-flags.blocks.final").split(",");
        int[] data = new int[dataIn.length];
        for(int i = 0; i < dataIn.length; i++) {
            try {
                data[i] = Integer.parseInt(dataIn[i]);
                if(data[i] < 0 || data[i] > 136)
                    data[i] = -1;
            } catch(NumberFormatException ex) {
                data[i] = -1;
            }
        }
        for(int i = 0; i < data.length; i++)
            if(data[i] != -1)
                blockedBlocks.put(Material.getMaterial(data[i]), true);
    }

    @Override
	public void loadDefaults() {
        String[] dataIn = SimpleZones.getPlugConfig().getString("default-flags.blocks.simple-default").split(",");
        int[] data = new int[dataIn.length];
        for(int i = 0; i < dataIn.length; i++) {
            try {
                data[i] = Integer.parseInt(dataIn[i]);
                if(data[i] < 0 || data[i] > 136)
                    data[i] = -1;
            } catch(NumberFormatException ex) {
                data[i] = -1;
            }
        }
        for(int i = 0; i < data.length; i++)
            if(data[i] != -1)
                blockedBlocks.put(Material.getMaterial(data[i]), false);
    }

    @Override
	public void loadTownSets(String s) {
        String[] data = s.split(",");
        for(String d : data)
            blockedBlocks.put(Material.getMaterial(Integer.parseInt(d)), false);
    }

    @Override
	public void setBlocked(Object obj, boolean tf) {
        Material mat = Material.getMaterial((Integer)obj);
        if(unblockable.contains(mat))
            return;
        if(blockedBlocks.containsKey(mat) && blockedBlocks.get(mat))
            return;
        if(tf)
            blockedBlocks.put(mat, false);
        else blockedBlocks.remove(mat);
    }

    @Override
	public void setAll(boolean tf) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
	public boolean isBlocked(Object obj) {
        return obj instanceof Material && blockedBlocks.containsKey(obj);
    }

    @Override
	public String getData() {
        String s = "";
        for(Material m : blockedBlocks.keySet())
            if(!blockedBlocks.get(m))
                s += m.getId() + ",";
        if(!s.isEmpty())
            return s.substring(0, s.length() - 1);
        return "";
    }
}
