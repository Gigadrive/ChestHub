package eu.thechest.chesthub.inv;

import com.avaje.ebean.config.dbplatform.MySqlBlob;
import eu.thechest.chestapi.items.ItemUtil;
import eu.thechest.chestapi.mysql.MySQLManager;
import eu.thechest.chestapi.server.ServerSettingsManager;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.PreparedStatement;
import java.util.ArrayList;

public class ResetTokensMenu implements Listener {
    public static void openFor(Player p){
        ChestUser u = ChestUser.getUser(p);
        Inventory inv = Bukkit.createInventory(null,9*6,u.getTranslatedMessage("Reset Tokens"));

        inv.setItem(13, ItemUtil.hideFlags(ItemUtil.namedItem(Material.NAME_TAG, ChatColor.YELLOW.toString() + u.getResetTokens() + " " + ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + u.getTranslatedMessage("Reset Tokens"),new String[]{" ",ChatColor.GRAY + u.getTranslatedMessage("Buy more on %s!").replace("%s",ChatColor.RED.toString() + ChatColor.BOLD.toString() + "thechest.eu/store" + ChatColor.GRAY)})));

        inv.setItem(19,b(p,Material.DIAMOND_HOE,0,ChatColor.LIGHT_PURPLE + "Tobiko"));
        inv.setItem(20,b(p,Material.CHEST,0,ChatColor.DARK_RED + "Survival Games"));
        inv.setItem(21,b(p,Material.DIAMOND_SWORD,0,ChatColor.YELLOW + "KitPvP"));
        inv.setItem(22,b(p,Material.RECORD_7,0,ChatColor.BLUE + "Musical Guess"));
        inv.setItem(23,b(p,Material.BOOK_AND_QUILL,0,ChatColor.DARK_PURPLE + "Build & Guess"));
        inv.setItem(24,b(p,Material.SNOW_BALL,0,ChatColor.AQUA + "SoccerMC"));
        inv.setItem(25,b(p,Material.ENDER_CHEST,0,ChatColor.DARK_RED + "Survival Games: " + ChatColor.YELLOW + "Duels"));

        inv.setItem(40,ItemUtil.namedItem(Material.BARRIER, org.bukkit.ChatColor.DARK_RED + u.getTranslatedMessage("Close"), null));

        p.openInventory(inv);
    }

    private static ItemStack b(Player p, Material icon, int durability, String name){
        ChestUser u = ChestUser.getUser(p);
        ItemStack i = new ItemStack(icon);
        i.setDurability((short)durability);

        ItemMeta iM = i.getItemMeta();
        iM.setDisplayName(name);
        ArrayList<String> iL = new ArrayList<String>();
        iL.add(" ");

        for(String s : StringUtils.getWordWrapLore(u.getTranslatedMessage("Click to reset your statistics in %g! This will cost you 1 reset token.").replace("%g",name + ChatColor.GRAY))){
            iL.add(ChatColor.GRAY + s);
        }

        iL.add(" ");

        for(String s : StringUtils.getWordWrapLore(u.getTranslatedMessage("Please note that this change is permanent and can NOT be reverted!"))){
            iL.add(ChatColor.RED.toString() + ChatColor.BOLD.toString() + s);
        }

        iM.setLore(iL);

        i.setItemMeta(iM);

        return ItemUtil.hideFlags(i);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(e.getWhoClicked() instanceof Player){
            Player p = (Player)e.getWhoClicked();
            ChestUser u = ChestUser.getUser(p);
            Inventory inv = e.getInventory();
            int slot = e.getRawSlot();

            if(inv.getName().equals(u.getTranslatedMessage("Reset Tokens"))){
                if(slot == 40){
                    p.closeInventory();
                } else {
                    String tableName = null;

                    if(slot == 19){
                        tableName = "tk_stats";
                    } else if(slot == 20){
                        tableName = "sg_stats";
                    } else if(slot == 21){
                        tableName = "kpvp_stats";
                    } else if(slot == 22){
                        tableName = "mg_stats";
                    } else if(slot == 23){
                        tableName = "bg_stats";
                    } else if(slot == 24){
                        tableName = "soccer_stats";
                    } else if(slot == 25){
                        tableName = "sgduels_stats";
                    }

                    if(tableName != null){
                        p.closeInventory();

                        if(u.getResetTokens() > 0){
                            try {
                                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("DELETE FROM `" + tableName + "` WHERE `uuid` = ?");
                                ps.setString(1,p.getUniqueId().toString());
                                ps.executeUpdate();
                                ps.close();

                                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Your statistics have been reset."));
                                u.reduceResetTokens(1);
                            } catch(Exception e1){
                                e1.printStackTrace();
                                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("An error occured."));
                            }
                        } else {
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You don't have enough reset tokens."));
                        }
                    }
                }
            }
        }
    }
}
