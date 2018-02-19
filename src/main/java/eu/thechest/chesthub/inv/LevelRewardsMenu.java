package eu.thechest.chesthub.inv;

import eu.thechest.chestapi.items.ItemUtil;
import eu.thechest.chestapi.items.VaultItem;
import eu.thechest.chestapi.server.ServerSettingsManager;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.util.StringUtils;
import eu.thechest.chesthub.levelrewards.LevelReward;
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
import java.util.stream.Collectors;

/**
 * Created by zeryt on 25.06.2017.
 */
public class LevelRewardsMenu implements Listener {
    public static void openFor(Player p){
        openFor(p,1);
    }

    public static void openFor(Player p, int page){
        ChestUser u = ChestUser.getUser(p);
        Inventory inv = Bukkit.createInventory(null,9*6,"Level Rewards | Page " + page);
        int sizePerPage = 36;

        inv.setItem(36, ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.setItem(37,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.setItem(38,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.setItem(39,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.setItem(40,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.setItem(41,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.setItem(42,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.setItem(43,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.setItem(44,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));

        ArrayList<LevelReward> rewards = new ArrayList<LevelReward>();
        rewards.addAll(LevelReward.REWARDS);
        int total = rewards.size();

        if(total > 0){
            for(LevelReward reward : rewards.stream().skip((page-1) * sizePerPage).limit(sizePerPage).collect(Collectors.toCollection(ArrayList::new))){
                if(reward.level > u.getLevel()){
                    // PLAYER LEVEL IS TOO LOW
                    inv.addItem(ItemUtil.namedItem(Material.REDSTONE_BLOCK,ChatColor.RED + u.getTranslatedMessage("Level") + " " + ChatColor.YELLOW.toString() + reward.level,new String[]{ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + "???"}));
                } else {
                    if(u.hasClaimedLevelReward(reward.level)){
                        ItemStack i = new ItemStack(Material.COAL_BLOCK);
                        ItemMeta iM = i.getItemMeta();
                        iM.setDisplayName(ChatColor.GRAY + u.getTranslatedMessage("Level") + " " + ChatColor.YELLOW.toString() + reward.level);
                        ArrayList<String> lore = new ArrayList<String>();
                        if(reward.coins > 0) lore.add(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD.toString() + reward.coins + " " + ChatColor.GRAY + u.getTranslatedMessage("Coins"));
                        if(reward.keys > 0) lore.add(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD.toString() + reward.keys + " " + ChatColor.GRAY + u.getTranslatedMessage("Keys"));
                        if(reward.vaultShards > 0) lore.add(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD.toString() + reward.vaultShards + " " + ChatColor.GRAY + u.getTranslatedMessage("Vault Shards"));
                        if(reward.randomChests > 0) lore.add(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD.toString() + reward.randomChests + " " + ChatColor.GRAY + u.getTranslatedMessage("Mystery Chests"));
                        if(reward.items != null && reward.items.length > 0){
                            for(VaultItem item : reward.items){
                                lore.add(ChatColor.DARK_GRAY + "- " + item.getRarity().getColor() + u.getTranslatedMessage(item.getName() + " " + item.getCategory().getNameSingular()));
                            }
                        }
                        iM.setLore(lore);
                        i.setItemMeta(iM);

                        inv.addItem(i);
                    } else {
                        ItemStack i = new ItemStack(Material.GOLD_BLOCK);
                        ItemMeta iM = i.getItemMeta();
                        iM.setDisplayName(ChatColor.GREEN + u.getTranslatedMessage("Level") + " " + ChatColor.YELLOW.toString() + reward.level);
                        ArrayList<String> lore = new ArrayList<String>();
                        if(reward.coins > 0) lore.add(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD.toString() + reward.coins + " " + ChatColor.GREEN + u.getTranslatedMessage("Coins"));
                        if(reward.keys > 0) lore.add(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD.toString() + reward.keys + " " + ChatColor.GREEN + u.getTranslatedMessage("Keys"));
                        if(reward.vaultShards > 0) lore.add(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD.toString() + reward.vaultShards + " " + ChatColor.GREEN + u.getTranslatedMessage("Vault Shards"));
                        if(reward.randomChests > 0) lore.add(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD.toString() + reward.randomChests + " " + ChatColor.GREEN + u.getTranslatedMessage("Mystery Chests"));
                        if(reward.items != null && reward.items.length > 0){
                            for(VaultItem item : reward.items){
                                lore.add(ChatColor.DARK_GRAY + "- " + item.getRarity().getColor() + u.getTranslatedMessage(item.getName() + " " + item.getCategory().getNameSingular()));
                            }
                        }
                        iM.setLore(lore);
                        i.setItemMeta(iM);

                        inv.addItem(i);
                    }
                }
            }
        }

        double d = (((double)total)/((double)sizePerPage));
        int maxPages = ((Double)d).intValue();
        if(maxPages < d) maxPages++;

        if(page != 1) inv.setItem(48,ItemUtil.namedItem(Material.ARROW, org.bukkit.ChatColor.GOLD + "<< " + org.bukkit.ChatColor.AQUA + u.getTranslatedMessage("Previous page"), null));
        inv.setItem(49, ItemUtil.namedItem(Material.BARRIER, org.bukkit.ChatColor.DARK_RED + u.getTranslatedMessage("Close"), null));
        if(maxPages > page) inv.setItem(50,ItemUtil.namedItem(Material.ARROW, org.bukkit.ChatColor.AQUA + u.getTranslatedMessage("Next page") + org.bukkit.ChatColor.GOLD + " >>", null));

        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(e.getWhoClicked() instanceof Player){
            Player p = (Player)e.getWhoClicked();
            ChestUser u = ChestUser.getUser(p);
            Inventory inv = e.getInventory();
            int slot = e.getRawSlot();

            if(inv.getName().startsWith("Level Rewards")){
                e.setCancelled(true);

                String a = inv.getName().replace("Level Rewards | Page ","");
                if(StringUtils.isValidInteger(a)) {
                    int page = Integer.parseInt(a);

                    if (slot >= 0 && slot <= 35) {
                        int level = page*(slot+2);

                        if(level > u.getLevel()){
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Your level is too low for this reward."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,0.5f);
                        } else {
                            if(u.hasClaimedLevelReward(level)){
                                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You have already claimed this reward."));
                            } else {
                                LevelReward r = LevelReward.getReward(level);

                                if(r != null){
                                    if(r.coins > 0) u.addCoins(r.coins);
                                    if(r.keys > 0) u.addKeys(r.keys);
                                    if(r.vaultShards > 0) u.addVaultShards(r.vaultShards);

                                    if(r.randomChests > 0){
                                        for(int i = 0; i < r.randomChests; i++){
                                            u.giveRandomChest();
                                        }
                                    }

                                    if(r.items != null && r.items.length > 0) {
                                        for (VaultItem item : r.items) {
                                            if(!u.hasUnlocked(item)) u.unlockItem(item);
                                        }
                                    }

                                    u.markLevelRewardAsClaimed(level);

                                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("You've successfully claimed your reward for Level %l.").replace("%l",ChatColor.YELLOW.toString() + level + ChatColor.GREEN.toString()));
                                    p.playSound(p.getEyeLocation(),Sound.LEVEL_UP,1f,2f);
                                    openFor(p,page);
                                } else {
                                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("An error occured."));
                                    p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,0.5f);
                                }
                            }
                        }
                    } else {
                        if (slot == 48) {
                            openFor(p, page - 1);
                        } else if (slot == 49) {
                            MyProfile.openFor(p);
                        } else if (slot == 50) {
                            openFor(p, page + 1);
                        }
                    }
                }
            }
        }
    }
}
