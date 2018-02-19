package eu.thechest.chesthub;

import org.bukkit.Location;

/**
 * Created by zeryt on 25.05.2017.
 */
public class ChestReward {
    public Location loc;
    public int achievement;
    public int coins;

    public ChestReward(Location loc, int achievement, int coins){
        this.loc = loc;
        this.achievement = achievement;
        this.coins = coins;
    }
}
