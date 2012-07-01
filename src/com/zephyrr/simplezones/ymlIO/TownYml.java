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
    
    // Area protection flags
    public boolean bomb;
    public boolean fire;
    public String animals;
    public String blocks;
    public String monsters;
}
