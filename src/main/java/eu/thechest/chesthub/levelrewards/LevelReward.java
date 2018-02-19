package eu.thechest.chesthub.levelrewards;

import eu.thechest.chestapi.items.VaultItem;

import java.util.ArrayList;

/**
 * Created by zeryt on 25.06.2017.
 */
public class LevelReward {
    public static final ArrayList<LevelReward> REWARDS = new ArrayList<LevelReward>();
    private static boolean init = false;

    public static LevelReward getReward(int level){
        LevelReward r = null;

        for(LevelReward re : REWARDS){
            if(re.level == level) r = re;
        }

        return r;
    }

    public static void init(){
        if(!init){
            LevelReward r = null;

            /*r = new LevelReward(1);
            r.coins = 100;
            REWARDS.add(r);*/

            r = new LevelReward(2);
            r.coins = 200;
            REWARDS.add(r);

            r = new LevelReward(3);
            r.coins = 300;
            r.vaultShards = 15;
            REWARDS.add(r);

            r = new LevelReward(4);
            r.coins = 400;
            r.randomChests = 2;
            REWARDS.add(r);

            r = new LevelReward(5);
            r.coins = 500;
            r.vaultShards = 50;
            REWARDS.add(r);

            r = new LevelReward(6);
            r.coins = 600;
            r.vaultShards = 50;
            REWARDS.add(r);

            r = new LevelReward(7);
            r.coins = 700;
            r.randomChests = 4;
            REWARDS.add(r);

            r = new LevelReward(8);
            r.coins = 800;
            REWARDS.add(r);

            r = new LevelReward(9);
            r.coins = 900;
            r.vaultShards = 120;
            REWARDS.add(r);

            r = new LevelReward(10);
            r.coins = 1000;
            r.vaultShards = 50;
            r.randomChests = 10;
            r.keys = 1;
            REWARDS.add(r);

            init = true;
        }
    }

    public int level = 1;

    public int coins = 0;
    public int randomChests = 0;
    public int keys = 0;
    public int vaultShards = 0;
    public VaultItem[] items;

    public LevelReward(int level){ this.level = level; }
}
