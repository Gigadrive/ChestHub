package eu.thechest.chesthub.vault;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Created by zeryt on 10.04.2017.
 */
public class VaultStorage {
    public static ArrayList<Vault> STORAGE = new ArrayList<Vault>();

    public static Vault getVaultFromLocation(Location loc){
        for(Vault v : STORAGE){
            if(v.chestLoc.getWorld().getName().equals(loc.getWorld().getName()) && v.chestLoc.getBlockX() == loc.getBlockX() && v.chestLoc.getBlockY() == loc.getBlockY() && v.chestLoc.getBlockZ() == loc.getBlockZ()){
                return v;
            }
        }

        return null;
    }

    public static Vault getVaultByPlayer(Player p){
        for(Vault v : STORAGE){
            if(v.player == p || v.currentOpeners.contains(p)){
                return v;
            }
        }

        return null;
    }
}
