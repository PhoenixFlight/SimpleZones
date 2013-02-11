package com.zephyrr.simplezones.ymlIO;

/**
 *
 * @author Phoenix
 */
public class TownYml {
    public int tid;
    public String owner;
    public String name;
    public int lowX, lowZ;
    public int highX, highZ;
    public double warpX, warpY, warpZ;
    public String world;
    public String members;
    public String supers;
    public String entryMessage;
    
    // Area protection flags
    public boolean bomb;
    public boolean fire;
    public boolean pvp;
    public String animals;
    public String blocks;
    public String monsters;
}
