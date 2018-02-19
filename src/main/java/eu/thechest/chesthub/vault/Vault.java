package eu.thechest.chesthub.vault;

import eu.thechest.chestapi.ChestAPI;
import eu.thechest.chestapi.lang.TranslatedHoloLine;
import eu.thechest.chestapi.lang.TranslatedHologram;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Created by zeryt on 10.04.2017.
 */
public class Vault {
    public Location chestLoc;
    public Location holoLoc;
    public float animationYaw;

    public boolean opening;
    public Player player;
    public ArmorStand animationStand;

    public ArrayList<Player> currentOpeners;

    public TranslatedHologram holo;

    public Vault (Location loc, float animationYaw){
        this.chestLoc = loc;
        Location l = ChestAPI.getBlockCenter(this.chestLoc);
        this.holoLoc = l;
        this.animationYaw = animationYaw;

        this.currentOpeners = new ArrayList<Player>();

        spawnHolo();
    }

    public void spawnHolo(){
        holo = new TranslatedHologram(
                new TranslatedHoloLine[]{
                        new TranslatedHoloLine(ChatColor.AQUA + "Chest Vault"),
                        new TranslatedHoloLine(ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + "CLICK TO OPEN")
                },

                holoLoc.clone().add(0,1.75,0)
        );
    }
}
