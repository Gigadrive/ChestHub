package eu.thechest.chesthub.inv;

import eu.thechest.chestapi.ChestAPI;
import eu.thechest.chestapi.items.ItemUtil;
import eu.thechest.chestapi.mysql.MySQLManager;
import eu.thechest.chestapi.server.ServerSettingsManager;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.util.PlayerUtilities;
import javafx.print.PageLayout;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

/**
 * Created by zeryt on 19.02.2017.
 */
public class StatisticsMenu implements Listener {

    public static void openFor(Player p, String playerName){
        playerName = playerName.trim();
        ChestUser u = ChestUser.getUser(p);
        UUID uuid = PlayerUtilities.getUUIDFromName(playerName);

        if(uuid != null){
            Inventory inv = Bukkit.createInventory(null,9,"[SM] " + playerName);

            inv.setItem(0,ItemUtil.hideFlags(ItemUtil.namedItem(Material.CHEST,ChatColor.DARK_RED + "Survival Games", new String[]{ChatColor.GRAY + u.getTranslatedMessage("Click to load")})));
            //inv.setItem(1,ItemUtil.hideFlags(ItemUtil.namedItem(Material.DIAMOND_CHESTPLATE,ChatColor.GRAY + "Minecraft " + ChatColor.DARK_AQUA + "Deathmatch", new String[]{ChatColor.GRAY + u.getTranslatedMessage("Click to load")})));
            inv.setItem(1,ItemUtil.hideFlags(ItemUtil.namedItem(Material.DIAMOND_SWORD,ChatColor.YELLOW + "KitPvP", new String[]{ChatColor.GRAY + u.getTranslatedMessage("Click to load")})));
            inv.setItem(2,ItemUtil.hideFlags(ItemUtil.namedItem(Material.RECORD_7,ChatColor.BLUE + "Musical Guess", new String[]{ChatColor.GRAY + u.getTranslatedMessage("Click to load")})));
            inv.setItem(3,ItemUtil.hideFlags(ItemUtil.namedItem(Material.BOOK_AND_QUILL,ChatColor.DARK_PURPLE + "Build & Guess", new String[]{ChatColor.GRAY + u.getTranslatedMessage("Click to load")})));
            inv.setItem(4,ItemUtil.hideFlags(ItemUtil.namedItem(Material.SNOW_BALL,ChatColor.AQUA + "SoccerMC", new String[]{ChatColor.GRAY + u.getTranslatedMessage("Click to load")})));
            inv.setItem(5,ItemUtil.hideFlags(ItemUtil.namedItem(Material.DIAMOND_HOE,ChatColor.LIGHT_PURPLE + "Tobiko", new String[]{ChatColor.GRAY + u.getTranslatedMessage("Click to load")})));
            inv.setItem(6,ItemUtil.hideFlags(ItemUtil.namedItem(Material.ENDER_CHEST,ChatColor.DARK_RED + "Survival Games: " + ChatColor.YELLOW + "Duels", new String[]{ChatColor.GRAY + u.getTranslatedMessage("Click to load")})));

            inv.setItem(8, ItemUtil.namedItem(Material.BARRIER, ChatColor.DARK_RED + u.getTranslatedMessage("Close"), null));

            p.openInventory(inv);
        } else {
            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Unknown UUID."));
            p.closeInventory();
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if (e.getWhoClicked() instanceof Player) {
            Player p = (Player) e.getWhoClicked();
            ChestUser u = ChestUser.getUser(p);
            Inventory inv = e.getInventory();
            int slot = e.getRawSlot();

            if (inv.getName().startsWith("[SM]")) {
                String playerName = inv.getName().replace("[SM] ","").trim();
                if(playerName != null && !playerName.isEmpty()){
                    UUID uuid = PlayerUtilities.getUUIDFromName(playerName);

                    if(uuid != null){
                        if(slot == 0){
                            // SURVIVAL GAMES

                            if(e.getCurrentItem().getItemMeta().getLore().contains(ChatColor.GRAY + u.getTranslatedMessage("Click to load"))){
                                ItemStack i = e.getCurrentItem();
                                ItemMeta iM = i.getItemMeta();
                                List<String> lore = iM.getLore();
                                lore.clear();

                                int points = 100;
                                int kills = 0;
                                int deaths = 0;
                                double kd = 0;
                                int playedGames = 0;
                                int victories = 0;
                                int losses = 0;
                                double wl = 0;

                                try {
                                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `sg_stats` WHERE `uuid` = ?");
                                    ps.setString(1,uuid.toString());
                                    ResultSet rs = ps.executeQuery();

                                    if(rs.first()){
                                        points = rs.getInt("points");
                                        kills = rs.getInt("kills");
                                        deaths = rs.getInt("deaths");
                                        kd = ChestAPI.calculateKD(kills,deaths);
                                        playedGames = rs.getInt("playedGames");
                                        victories = rs.getInt("victories");
                                        losses = playedGames-victories;
                                        wl = ChestAPI.calculateWL(victories,losses);
                                    }

                                    MySQLManager.getInstance().closeResources(rs,ps);
                                } catch(Exception e1){
                                    e1.printStackTrace();
                                }

                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Points") + ": " + ChatColor.WHITE + points);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Kills") + ": " + ChatColor.WHITE + kills);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Deaths") + ": " + ChatColor.WHITE + deaths);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("K/D ratio") + ": " + ChatColor.WHITE + kd);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Played games") + ": " + ChatColor.WHITE + playedGames);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Victories") + ": " + ChatColor.WHITE + victories);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("W/L ratio") + ": " + ChatColor.WHITE + wl);

                                iM.setLore(lore);
                                i.setItemMeta(iM);
                                inv.setItem(slot,i);
                            }
                        } /*else if(slot == 1){
                    // DEATHMATCH

                    if(e.getCurrentItem().getItemMeta().getLore().contains(ChatColor.GRAY + u.getTranslatedMessage("Click to load"))){
                        ItemStack i = e.getCurrentItem();
                        ItemMeta iM = i.getItemMeta();
                        List<String> lore = iM.getLore();
                        lore.clear();

                        try {
                            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `dm_stats` WHERE `uuid` = ?");
                            ps.setString(1,uuid.toString());
                            ResultSet rs = ps.executeQuery();

                            if(rs.first()){
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Points") + ": " + ChatColor.WHITE + rs.getInt("points"));
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Kills") + ": " + ChatColor.WHITE + rs.getInt("kills"));
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Deaths") + ": " + ChatColor.WHITE + rs.getInt("kills"));
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Played games") + ": " + ChatColor.WHITE + rs.getInt("playedGames"));
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Victories") + ": " + ChatColor.WHITE + rs.getInt("victories"));
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("First game") + ": " + ChatColor.WHITE + rs.getTimestamp("firstJoin").toGMTString());
                            } else {
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Points") + ": " + ChatColor.WHITE + 100);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Kills") + ": " + ChatColor.WHITE + 0);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Deaths") + ": " + ChatColor.WHITE + 0);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Played games") + ": " + ChatColor.WHITE + 0);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Victories") + ": " + ChatColor.WHITE + 0);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("First game") + ": " + ChatColor.WHITE + "-");
                            }

                            MySQLManager.getInstance().closeResources(rs,ps);
                        } catch(Exception e1){
                            e1.printStackTrace();
                        }

                        iM.setLore(lore);
                        i.setItemMeta(iM);
                        inv.setItem(slot,i);
                    }
                } */else if(slot == 1){
                            // KITPVP

                            if(e.getCurrentItem().getItemMeta().getLore().contains(ChatColor.GRAY + u.getTranslatedMessage("Click to load"))){
                                ItemStack i = e.getCurrentItem();
                                ItemMeta iM = i.getItemMeta();
                                List<String> lore = iM.getLore();
                                lore.clear();

                                int points = 100;
                                int kills = 0;
                                int deaths = 0;
                                double kd = 0;

                                try {
                                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `kpvp_stats` WHERE `uuid` = ?");
                                    ps.setString(1,uuid.toString());
                                    ResultSet rs = ps.executeQuery();

                                    if(rs.first()){
                                        points = rs.getInt("points");
                                        kills = rs.getInt("kills");
                                        deaths = rs.getInt("deaths");
                                        kd = ChestAPI.calculateKD(kills,deaths);
                                    }

                                    MySQLManager.getInstance().closeResources(rs,ps);
                                } catch(Exception e1){
                                    e1.printStackTrace();
                                }

                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Points") + ": " + ChatColor.WHITE + points);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Kills") + ": " + ChatColor.WHITE + kills);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Deaths") + ": " + ChatColor.WHITE + deaths);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("K/D ratio") + ": " + ChatColor.WHITE + kd);

                                iM.setLore(lore);
                                i.setItemMeta(iM);
                                inv.setItem(slot,i);
                            }
                        } else if(slot == 2){
                            // MUSICAL GUESS

                            if(e.getCurrentItem().getItemMeta().getLore().contains(ChatColor.GRAY + u.getTranslatedMessage("Click to load"))){
                                ItemStack i = e.getCurrentItem();
                                ItemMeta iM = i.getItemMeta();
                                List<String> lore = iM.getLore();
                                lore.clear();

                                int points = 100;
                                int playedGames = 0;
                                int victories = 0;
                                int losses = 0;
                                double wl = 0;

                                try {
                                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `mg_stats` WHERE `uuid` = ?");
                                    ps.setString(1,uuid.toString());
                                    ResultSet rs = ps.executeQuery();

                                    if(rs.first()){
                                        points = rs.getInt("points");
                                        playedGames = rs.getInt("playedGames");
                                        victories = rs.getInt("victories");
                                        losses = playedGames-victories;
                                        wl = ChestAPI.calculateWL(victories,losses);
                                    }

                                    MySQLManager.getInstance().closeResources(rs,ps);
                                } catch(Exception e1){
                                    e1.printStackTrace();
                                }

                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Points") + ": " + ChatColor.WHITE + points);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Played games") + ": " + ChatColor.WHITE + playedGames);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Victories") + ": " + ChatColor.WHITE + victories);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("W/L ratio") + ": " + ChatColor.WHITE + wl);

                                iM.setLore(lore);
                                i.setItemMeta(iM);
                                inv.setItem(slot,i);
                            }
                        } else if(slot == 3){
                            // BUILD AND GUESS

                            if(e.getCurrentItem().getItemMeta().getLore().contains(ChatColor.GRAY + u.getTranslatedMessage("Click to load"))){
                                ItemStack i = e.getCurrentItem();
                                ItemMeta iM = i.getItemMeta();
                                List<String> lore = iM.getLore();
                                lore.clear();

                                int points = 100;
                                int playedGames = 0;
                                int victories = 0;
                                int losses = 0;
                                double wl = 0;
                                int guessedWords = 0;

                                try {
                                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `bg_stats` WHERE `uuid` = ?");
                                    ps.setString(1,uuid.toString());
                                    ResultSet rs = ps.executeQuery();

                                    if(rs.first()){
                                        points = rs.getInt("points");
                                        playedGames = rs.getInt("playedGames");
                                        victories = rs.getInt("victories");
                                        losses = playedGames-victories;
                                        wl = ChestAPI.calculateWL(victories,losses);
                                        guessedWords = rs.getInt("guessedWords");
                                    }

                                    MySQLManager.getInstance().closeResources(rs,ps);
                                } catch(Exception e1){
                                    e1.printStackTrace();
                                }

                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Points") + ": " + ChatColor.WHITE + points);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Played games") + ": " + ChatColor.WHITE + playedGames);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Victories") + ": " + ChatColor.WHITE + victories);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("W/L ratio") + ": " + ChatColor.WHITE + wl);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Guessed words") + ": " + ChatColor.WHITE + guessedWords);

                                iM.setLore(lore);
                                i.setItemMeta(iM);
                                inv.setItem(slot,i);
                            }
                        } else if(slot == 4){
                            // SOCCER

                            if(e.getCurrentItem().getItemMeta().getLore().contains(ChatColor.GRAY + u.getTranslatedMessage("Click to load"))){
                                ItemStack i = e.getCurrentItem();
                                ItemMeta iM = i.getItemMeta();
                                List<String> lore = iM.getLore();
                                lore.clear();

                                int points = 100;
                                int playedGames = 0;
                                int victories = 0;
                                int losses = 0;
                                double wl = 0;
                                int goals = 0;

                                try {
                                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `soccer_stats` WHERE `uuid` = ?");
                                    ps.setString(1,uuid.toString());
                                    ResultSet rs = ps.executeQuery();

                                    if(rs.first()){
                                        points = rs.getInt("points");
                                        playedGames = rs.getInt("playedGames");
                                        victories = rs.getInt("victories");
                                        losses = playedGames-victories;
                                        wl = ChestAPI.calculateWL(victories,losses);
                                        goals = rs.getInt("goals");
                                    }

                                    MySQLManager.getInstance().closeResources(rs,ps);
                                } catch(Exception e1){
                                    e1.printStackTrace();
                                }

                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Points") + ": " + ChatColor.WHITE + points);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Played games") + ": " + ChatColor.WHITE + playedGames);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Victories") + ": " + ChatColor.WHITE + victories);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("W/L ratio") + ": " + ChatColor.WHITE + wl);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Goals") + ": " + ChatColor.WHITE + goals);

                                iM.setLore(lore);
                                i.setItemMeta(iM);
                                inv.setItem(slot,i);
                            }
                        } else if(slot == 5){
                            // TOBIKO

                            if(e.getCurrentItem().getItemMeta().getLore().contains(ChatColor.GRAY + u.getTranslatedMessage("Click to load"))){
                                ItemStack i = e.getCurrentItem();
                                ItemMeta iM = i.getItemMeta();
                                List<String> lore = iM.getLore();
                                lore.clear();

                                int points = 100;
                                int hits = 0;
                                int finalHits = 0;
                                int kills = 0;
                                int playedGames = 0;
                                int victories = 0;
                                int losses = 0;
                                double wl = 0;

                                try {
                                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `tk_stats` WHERE `uuid` = ?");
                                    ps.setString(1,uuid.toString());
                                    ResultSet rs = ps.executeQuery();

                                    if(rs.first()){
                                        points = rs.getInt("points");
                                        hits = rs.getInt("hits");
                                        finalHits = rs.getInt("finalHits");
                                        kills = rs.getInt("kills");
                                        playedGames = rs.getInt("playedGames");
                                        victories = rs.getInt("victories");
                                        losses = playedGames-victories;
                                        wl = ChestAPI.calculateWL(victories,losses);
                                    }

                                    MySQLManager.getInstance().closeResources(rs,ps);
                                } catch(Exception e1){
                                    e1.printStackTrace();
                                }

                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Points") + ": " + ChatColor.WHITE + points);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Hits") + ": " + ChatColor.WHITE + hits);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Final Hits") + ": " + ChatColor.WHITE + finalHits);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Kills") + ": " + ChatColor.WHITE + kills);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Played games") + ": " + ChatColor.WHITE + playedGames);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Victories") + ": " + ChatColor.WHITE + victories);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("W/L ratio") + ": " + ChatColor.WHITE + wl);

                                iM.setLore(lore);
                                i.setItemMeta(iM);
                                inv.setItem(slot,i);
                            }
                        } else if(slot == 6){
                            // SG DUELS

                            if(e.getCurrentItem().getItemMeta().getLore().contains(ChatColor.GRAY + u.getTranslatedMessage("Click to load"))){
                                ItemStack i = e.getCurrentItem();
                                ItemMeta iM = i.getItemMeta();
                                List<String> lore = iM.getLore();
                                lore.clear();

                                int elo = 1000;
                                int kills = 0;
                                int deaths = 0;
                                double kd = 0;
                                int playedGames = 0;
                                int victories = 0;
                                int losses = 0;
                                double wl = 0;
                                int openedChests = 0;

                                try {
                                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `sgduels_stats` WHERE `uuid` = ?");
                                    ps.setString(1,uuid.toString());
                                    ResultSet rs = ps.executeQuery();

                                    if(rs.first()){
                                        elo = rs.getInt("elo");
                                        kills = rs.getInt("kills");
                                        deaths = rs.getInt("deaths");
                                        playedGames = rs.getInt("playedGames");
                                        victories = rs.getInt("victories");
                                        losses = playedGames-victories;
                                        ChestAPI.calculateWL(victories,losses);
                                        openedChests = rs.getInt("chestsOpened");
                                    }

                                    MySQLManager.getInstance().closeResources(rs,ps);
                                } catch(Exception e1){
                                    e1.printStackTrace();
                                }

                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Elo") + ": " + ChatColor.WHITE + elo);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Kills") + ": " + ChatColor.WHITE + kills);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Deaths") + ": " + ChatColor.WHITE + deaths);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("K/D ratio") + ": " + ChatColor.WHITE + kd);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Played Games") + ": " + ChatColor.WHITE + playedGames);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Victories") + ": " + ChatColor.WHITE + victories);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("W/L ratio") + ": " + ChatColor.WHITE + wl);
                                lore.add(ChatColor.GRAY + u.getTranslatedMessage("Opened Chests") + ": " + ChatColor.WHITE + openedChests);

                                iM.setLore(lore);
                                i.setItemMeta(iM);
                                inv.setItem(slot,i);
                            }
                        } else if(slot == 8){
                            // CLOSE

                            if(playerName.equalsIgnoreCase(p.getName())){
                                MyProfile.openFor(p);
                            } else {
                                p.closeInventory();
                            }
                        }
                    }
                }
            }
        }
    }

}
