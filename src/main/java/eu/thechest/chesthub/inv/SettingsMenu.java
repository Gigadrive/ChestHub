package eu.thechest.chesthub.inv;

import eu.thechest.chestapi.items.ItemUtil;
import eu.thechest.chestapi.server.ServerSettingsManager;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.user.Rank;
import eu.thechest.chesthub.ChestHub;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

/**
 * Created by zeryt on 26.02.2017.
 */
public class SettingsMenu implements Listener {
    public static ArrayList<String> TO_SAVE = new ArrayList<String>();

    public static Inventory openFor(Player p){
        return openFor(p,true);
    }

    public static Inventory openFor(Player p, boolean open){
        ChestUser u = ChestUser.getUser(p);
        Inventory inv = Bukkit.createInventory(null,9,u.getTranslatedMessage("Settings"));
        ItemStack close = ItemUtil.namedItem(Material.BARRIER, ChatColor.DARK_RED + u.getTranslatedMessage("Close"), null);

        inv.setItem(8,close);

        if(u.allowsFriendRequests()){
            inv.setItem(0, ItemUtil.namedItem(Material.SKULL_ITEM, ChatColor.GOLD + u.getTranslatedMessage("Friend requests"), new String[]{ChatColor.DARK_GREEN + "> " + ChatColor.GREEN + u.getTranslatedMessage("Activated")}, 3));
        } else {
            inv.setItem(0, ItemUtil.namedItem(Material.SKULL_ITEM, ChatColor.GOLD + u.getTranslatedMessage("Friend requests"), new String[]{ChatColor.DARK_RED + "> " + ChatColor.RED + u.getTranslatedMessage("Deactivated")}, 3));
        }

        if(u.allowsPrivateMessages()){
            inv.setItem(1, ItemUtil.namedItem(Material.PAPER, ChatColor.GOLD + u.getTranslatedMessage("Private messages"), new String[]{ChatColor.DARK_GREEN + "> " + ChatColor.GREEN + u.getTranslatedMessage("Activated")}));
        } else {
            inv.setItem(1, ItemUtil.namedItem(Material.PAPER, ChatColor.GOLD + u.getTranslatedMessage("Private messages"), new String[]{ChatColor.DARK_RED + "> " + ChatColor.RED + u.getTranslatedMessage("Deactivated")}));
        }

        if(u.allowsPartyRequests()){
            inv.setItem(2, ItemUtil.namedItem(Material.FIREWORK, ChatColor.GOLD + u.getTranslatedMessage("Party requests"), new String[]{ChatColor.DARK_GREEN + "> " + ChatColor.GREEN + u.getTranslatedMessage("Activated")}));
        } else {
            inv.setItem(2, ItemUtil.namedItem(Material.FIREWORK, ChatColor.GOLD + u.getTranslatedMessage("Party requests"), new String[]{ChatColor.DARK_RED + "> " + ChatColor.RED + u.getTranslatedMessage("Deactivated")}));
        }

        if(u.allowsChallengerRequests()){
            inv.setItem(3, ItemUtil.namedItem(Material.SHEARS, ChatColor.GOLD + u.getTranslatedMessage("Challenger"), new String[]{ChatColor.DARK_GREEN + "> " + ChatColor.GREEN + u.getTranslatedMessage("Activated")}));
        } else {
            inv.setItem(3, ItemUtil.namedItem(Material.SHEARS, ChatColor.GOLD + u.getTranslatedMessage("Challenger"), new String[]{ChatColor.DARK_RED + "> " + ChatColor.RED + u.getTranslatedMessage("Deactivated")}));
        }

        if(u.hasPermission(Rank.TITAN)){
            if(u.enabledLobbySpeed()){
                inv.setItem(4, ItemUtil.namedItem(Material.FEATHER, ChatColor.GOLD + u.getTranslatedMessage("Lobby speed"), new String[]{ChatColor.DARK_GREEN + "> " + ChatColor.GREEN + u.getTranslatedMessage("Activated")}));
            } else {
                inv.setItem(4, ItemUtil.namedItem(Material.FEATHER, ChatColor.GOLD + u.getTranslatedMessage("Lobby speed"), new String[]{ChatColor.DARK_RED + "> " + ChatColor.RED + u.getTranslatedMessage("Deactivated")}));
            }
        }

        /*if(u.hasPermission(Rank.VIP)){
            if(u.enabledAutoNick()){
                inv.setItem(5, ItemUtil.namedItem(Material.NAME_TAG, ChatColor.DARK_PURPLE + u.getTranslatedMessage("Automatic nick"), new String[]{ChatColor.DARK_GREEN + "> " + ChatColor.GREEN + u.getTranslatedMessage("Activated")}));
            } else {
                inv.setItem(5, ItemUtil.namedItem(Material.NAME_TAG, ChatColor.DARK_PURPLE + u.getTranslatedMessage("Automatic nick"), new String[]{ChatColor.DARK_RED + "> " + ChatColor.RED + u.getTranslatedMessage("Deactivated")}));
            }
        }*/

        if(open) p.openInventory(inv);
        return inv;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        if(e.getPlayer() instanceof Player){
            Player p = (Player)e.getPlayer();
            ChestUser u = ChestUser.getUser(p);
            Inventory inv = e.getInventory();

            if(TO_SAVE.contains(p.getName())){
                u.saveSettings();
                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Your settings have been saved."));
                TO_SAVE.remove(p.getName());

                p.playSound(p.getEyeLocation(),Sound.LEVEL_UP,1f,2f);
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(e.getWhoClicked() instanceof Player){
            Player p = (Player)e.getWhoClicked();
            ChestUser u = ChestUser.getUser(p);
            Inventory inv = e.getInventory();
            int slot = e.getRawSlot();

            if(inv.getName().equals(u.getTranslatedMessage("Settings"))){
                if(slot == 0){
                    // FRIEND REQUESTS
                    u.setFriendRequests(!u.allowsFriendRequests());
                    p.playSound(p.getEyeLocation(), Sound.ORB_PICKUP,1f,1f);
                    if(!TO_SAVE.contains(p.getName())) TO_SAVE.add(p.getName());
                    //openFor(p);
                    inv.setContents(openFor(p,false).getContents());
                } else if(slot == 1){
                    // PRIVATE MESSAGES
                    u.setPrivateMessages(!u.allowsPrivateMessages());
                    p.playSound(p.getEyeLocation(), Sound.ORB_PICKUP,1f,1f);
                    if(!TO_SAVE.contains(p.getName())) TO_SAVE.add(p.getName());
                    //openFor(p);
                    inv.setContents(openFor(p,false).getContents());
                } else if(slot == 2){
                    // PARTY REQUESTS
                    u.setPartyRequests(!u.allowsPartyRequests());
                    p.playSound(p.getEyeLocation(), Sound.ORB_PICKUP,1f,1f);
                    if(!TO_SAVE.contains(p.getName())) TO_SAVE.add(p.getName());
                    //openFor(p);
                    inv.setContents(openFor(p,false).getContents());
                } else if(slot == 3){
                    // CHALLENGER
                    u.setChallengerRequests(!u.allowsChallengerRequests());
                    p.playSound(p.getEyeLocation(), Sound.ORB_PICKUP,1f,1f);
                    if(!TO_SAVE.contains(p.getName())) TO_SAVE.add(p.getName());
                    //openFor(p);
                    inv.setContents(openFor(p,false).getContents());
                } else if(slot == 4){
                    // LOBBY SPEED
                    if(u.hasPermission(Rank.TITAN)){
                        u.setLobbySpeed(!u.enabledLobbySpeed());
                        p.playSound(p.getEyeLocation(), Sound.ORB_PICKUP,1f,1f);
                        if(!TO_SAVE.contains(p.getName())) TO_SAVE.add(p.getName());
                        //openFor(p);
                        inv.setContents(openFor(p,false).getContents());

                        for(PotionEffect po : p.getActivePotionEffects()){
                            p.removePotionEffect(po.getType());
                        }

                        if(u.enabledLobbySpeed() && u.hasPermission(Rank.TITAN)){
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,1,false,false));
                        }
                    }
                }/* else if(slot == 5){
                    // AUTO NICK
                    if(u.hasPermission(Rank.VIP)){
                        u.setAutoNick(!u.enabledAutoNick());
                        p.playSound(p.getEyeLocation(), Sound.ORB_PICKUP,1f,1f);
                        if(!TO_SAVE.contains(p.getName())) TO_SAVE.add(p.getName());
                        //openFor(p);
                        inv.setContents(openFor(p,false).getContents());
                    }
                } */else if(slot == 8){
                    // CLOSE
                    MyProfile.openFor(p);
                }
            }
        }
    }
}
