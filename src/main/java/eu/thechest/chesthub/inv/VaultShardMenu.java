package eu.thechest.chesthub.inv;

import eu.thechest.chestapi.ChestAPI;
import eu.thechest.chestapi.items.ItemUtil;
import eu.thechest.chestapi.server.ServerSettingsManager;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Created by zeryt on 24.06.2017.
 */
public class VaultShardMenu implements Listener {
    public static void openFor(Player p){
        ChestUser u = ChestUser.getUser(p);
        Inventory inv = Bukkit.createInventory(null,9*6,"Vault Shards");

        inv.setItem(11, ItemUtil.namedItem(Material.ENDER_CHEST, ChatColor.YELLOW + u.getTranslatedMessage("Click to buy"),new String[]{ChatColor.GOLD + u.getTranslatedMessage("1 chest"),ChatColor.YELLOW + u.getTranslatedMessage("Cost: " + ChatColor.AQUA + "50 " + u.getTranslatedMessage("Vault Shards"))}));
        inv.setItem(12, ItemUtil.namedItem(Material.ENDER_CHEST, ChatColor.YELLOW + u.getTranslatedMessage("Click to buy"),new String[]{ChatColor.GOLD + u.getTranslatedMessage("5 chests"),ChatColor.YELLOW + u.getTranslatedMessage("Cost: " + ChatColor.AQUA + "250 " + u.getTranslatedMessage("Vault Shards"))},0,5));
        inv.setItem(13, ItemUtil.namedItem(Material.ENDER_CHEST, ChatColor.YELLOW + u.getTranslatedMessage("Click to buy"),new String[]{ChatColor.GOLD + u.getTranslatedMessage("10 chests"),ChatColor.YELLOW + u.getTranslatedMessage("Cost: " + ChatColor.AQUA + "500 " + u.getTranslatedMessage("Vault Shards"))},0,10));
        inv.setItem(14, ItemUtil.namedItem(Material.ENDER_CHEST, ChatColor.YELLOW + u.getTranslatedMessage("Click to buy"),new String[]{ChatColor.GOLD + u.getTranslatedMessage("20 chests"),ChatColor.YELLOW + u.getTranslatedMessage("Cost: " + ChatColor.AQUA + "1000 " + u.getTranslatedMessage("Vault Shards"))},0,20));
        inv.setItem(15, ItemUtil.namedItem(Material.ENDER_CHEST, ChatColor.YELLOW + u.getTranslatedMessage("Click to buy"),new String[]{ChatColor.GOLD + u.getTranslatedMessage("50 chests"),ChatColor.YELLOW + u.getTranslatedMessage("Cost: " + ChatColor.AQUA + "2500 " + u.getTranslatedMessage("Vault Shards"))},0,50));

        inv.setItem(20, ItemUtil.namedItem(Material.TRIPWIRE_HOOK, ChatColor.YELLOW + u.getTranslatedMessage("Click to buy"),new String[]{ChatColor.GREEN + u.getTranslatedMessage("1 key"),ChatColor.YELLOW + u.getTranslatedMessage("Cost: " + ChatColor.AQUA + "500 " + u.getTranslatedMessage("Vault Shards"))}));
        inv.setItem(21, ItemUtil.namedItem(Material.TRIPWIRE_HOOK, ChatColor.YELLOW + u.getTranslatedMessage("Click to buy"),new String[]{ChatColor.GREEN + u.getTranslatedMessage("5 keys"),ChatColor.YELLOW + u.getTranslatedMessage("Cost: " + ChatColor.AQUA + "2500 " + u.getTranslatedMessage("Vault Shards"))},0,5));
        inv.setItem(22, ItemUtil.namedItem(Material.TRIPWIRE_HOOK, ChatColor.YELLOW + u.getTranslatedMessage("Click to buy"),new String[]{ChatColor.GREEN + u.getTranslatedMessage("10 keys"),ChatColor.YELLOW + u.getTranslatedMessage("Cost: " + ChatColor.AQUA + "5000 " + u.getTranslatedMessage("Vault Shards"))},0,10));
        inv.setItem(23, ItemUtil.namedItem(Material.TRIPWIRE_HOOK, ChatColor.YELLOW + u.getTranslatedMessage("Click to buy"),new String[]{ChatColor.GREEN + u.getTranslatedMessage("20 keys"),ChatColor.YELLOW + u.getTranslatedMessage("Cost: " + ChatColor.AQUA + "10000 " + u.getTranslatedMessage("Vault Shards"))},0,20));
        inv.setItem(24, ItemUtil.namedItem(Material.TRIPWIRE_HOOK, ChatColor.YELLOW + u.getTranslatedMessage("Click to buy"),new String[]{ChatColor.GREEN + u.getTranslatedMessage("50 keys"),ChatColor.YELLOW + u.getTranslatedMessage("Cost: " + ChatColor.AQUA + "25000 " + u.getTranslatedMessage("Vault Shards"))},0,50));

        ItemStack i = new ItemStack(Material.PRISMARINE_SHARD);
        ItemMeta iM = i.getItemMeta();
        iM.setDisplayName(ChatColor.YELLOW + u.getTranslatedMessage("You have") + ": " + ChatColor.AQUA + u.getVaultShards() + " " + u.getTranslatedMessage("Vault Shards"));
        ArrayList<String> iL = new ArrayList<String>();
        iL.add(" ");
        for(String s : StringUtils.getWordWrapLore(ChatColor.RED.toString() + u.getTranslatedMessage("INFO") + ": " + ChatColor.GRAY + u.getTranslatedMessage("You get Vault Shards when you open a chest and get an item that you already have."))) iL.add(ChatColor.GRAY + s);
        iM.setLore(iL);
        i.setItemMeta(iM);
        i = ItemUtil.hideFlags(i);

        inv.setItem(39,i);
        inv.setItem(41, ItemUtil.namedItem(Material.BARRIER, net.md_5.bungee.api.ChatColor.DARK_RED + u.getTranslatedMessage("Close"), null));

        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if (e.getWhoClicked() instanceof Player) {
            Player p = (Player) e.getWhoClicked();
            ChestUser u = ChestUser.getUser(p);
            Inventory inv = e.getInventory();

            if (inv.getName().equals("Vault Shards")) {
                ChestAPI.async(() -> {
                    if(e.getRawSlot() == 11){
                        // 1 CHEST
                        int cost = 50;
                        if(u.getVaultShards() >= cost){
                            p.closeInventory();
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GRAY + u.getTranslatedMessage("Your request is being processed.."));
                            u.giveRandomChest();
                            u.reduceVaultShards(cost);

                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Your chests have been credited."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_PLING,1f,1f);
                        } else {
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You don't have enough Vault Shards."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,0.5f);
                        }
                    } else if(e.getRawSlot() == 12){
                        // 5 CHESTS
                        int cost = 250;
                        if(u.getVaultShards() >= cost){
                            p.closeInventory();
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GRAY + u.getTranslatedMessage("Your request is being processed.."));
                            for(int i = 0; i < 5; i++) u.giveRandomChest();
                            u.reduceVaultShards(cost);

                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Your chests have been credited."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_PLING,1f,1f);
                        } else {
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You don't have enough Vault Shards."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,0.5f);
                        }
                    } else if(e.getRawSlot() == 13){
                        // 10 CHESTS
                        int cost = 500;
                        if(u.getVaultShards() >= cost){
                            p.closeInventory();
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GRAY + u.getTranslatedMessage("Your request is being processed.."));
                            for(int i = 0; i < 10; i++) u.giveRandomChest();
                            u.reduceVaultShards(cost);

                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Your chests have been credited."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_PLING,1f,1f);
                        } else {
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You don't have enough Vault Shards."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,0.5f);
                        }
                    } else if(e.getRawSlot() == 14){
                        // 20 CHESTS
                        int cost = 1000;
                        if(u.getVaultShards() >= cost){
                            p.closeInventory();
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GRAY + u.getTranslatedMessage("Your request is being processed.."));
                            for(int i = 0; i < 20; i++) u.giveRandomChest();
                            u.reduceVaultShards(cost);

                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Your chests have been credited."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_PLING,1f,1f);
                        } else {
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You don't have enough Vault Shards."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,0.5f);
                        }
                    } else if(e.getRawSlot() == 15){
                        // 50 CHESTS
                        int cost = 2500;
                        if(u.getVaultShards() >= cost){
                            p.closeInventory();
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GRAY + u.getTranslatedMessage("Your request is being processed.."));
                            for(int i = 0; i < 50; i++) u.giveRandomChest();
                            u.reduceVaultShards(cost);

                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Your chests have been credited."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_PLING,1f,1f);
                        } else {
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You don't have enough Vault Shards."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,0.5f);
                        }
                    } else if(e.getRawSlot() == 20){
                        // 1 KEY
                        int cost = 500;
                        if(u.getVaultShards() >= cost){
                            p.closeInventory();
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GRAY + u.getTranslatedMessage("Your request is being processed.."));
                            u.addKeys(1);
                            u.reduceVaultShards(cost);

                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Your keys have been credited."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_PLING,1f,1f);
                        } else {
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You don't have enough Vault Shards."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,0.5f);
                        }
                    } else if(e.getRawSlot() == 21){
                        // 5 KEYS
                        int cost = 2500;
                        if(u.getVaultShards() >= cost){
                            p.closeInventory();
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GRAY + u.getTranslatedMessage("Your request is being processed.."));
                            u.addKeys(5);
                            u.reduceVaultShards(cost);

                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Your keys have been credited."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_PLING,1f,1f);
                        } else {
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You don't have enough Vault Shards."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,0.5f);
                        }
                    } else if(e.getRawSlot() == 22){
                        // 10 KEYS
                        int cost = 5000;
                        if(u.getVaultShards() >= cost){
                            p.closeInventory();
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GRAY + u.getTranslatedMessage("Your request is being processed.."));
                            u.addKeys(10);
                            u.reduceVaultShards(cost);

                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Your keys have been credited."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_PLING,1f,1f);
                        } else {
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You don't have enough Vault Shards."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,0.5f);
                        }
                    } else if(e.getRawSlot() == 23){
                        // 20 KEYS
                        int cost = 10000;
                        if(u.getVaultShards() >= cost){
                            p.closeInventory();
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GRAY + u.getTranslatedMessage("Your request is being processed.."));
                            u.addKeys(20);
                            u.reduceVaultShards(cost);

                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Your keys have been credited."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_PLING,1f,1f);
                        } else {
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You don't have enough Vault Shards."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,0.5f);
                        }
                    } else if(e.getRawSlot() == 24){
                        // 50 KEYS
                        int cost = 25000;
                        if(u.getVaultShards() >= cost){
                            p.closeInventory();
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GRAY + u.getTranslatedMessage("Your request is being processed.."));
                            u.addKeys(50);
                            u.reduceVaultShards(cost);

                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Your keys have been credited."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_PLING,1f,1f);
                        } else {
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You don't have enough Vault Shards."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,0.5f);
                        }
                    } else if(e.getRawSlot() == 41){
                        p.closeInventory();
                    }
                });
            }
        }
    }
}
