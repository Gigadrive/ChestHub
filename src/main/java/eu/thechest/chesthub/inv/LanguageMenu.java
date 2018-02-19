package eu.thechest.chesthub.inv;

import eu.thechest.chestapi.items.ItemUtil;
import eu.thechest.chestapi.lang.Translation;
import eu.thechest.chestapi.server.ServerSettingsManager;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.user.ScoreboardType;
import eu.thechest.chesthub.ChestHub;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * Created by zeryt on 01.03.2017.
 */
public class LanguageMenu implements Listener {
    public static void openFor(Player p){
        ChestUser u = ChestUser.getUser(p);
        Inventory inv = Bukkit.createInventory(null,9,"Language");

        inv.setItem(0, ItemUtil.namedItem(Material.BOOK, ChatColor.YELLOW + "English", null));
        inv.setItem(1, ItemUtil.namedItem(Material.BOOK, ChatColor.YELLOW + "German" + ChatColor.GRAY + " (Deutsch)", null));

        inv.setItem(8, ItemUtil.namedItem(Material.BARRIER, ChatColor.DARK_RED + u.getTranslatedMessage("Close"), null));

        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player p = (Player) e.getWhoClicked();
            ChestUser u = ChestUser.getUser(p);
            Inventory inv = e.getInventory();

            if (inv.getName().equals("Language")) {
                e.setCancelled(true);

                if(e.getRawSlot() == 0){
                    // ENGLISH

                    u.setLanguage(Translation.getLanguage("EN"));
                    p.closeInventory();
                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Your language has been updated."));
                    u.updateScoreboard(ScoreboardType.LOBBY);
                    ChestHub.getInstance().giveLobbyItems(p);
                    u.saveSettings();
                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + org.bukkit.ChatColor.GREEN + u.getTranslatedMessage("Your settings have been saved."));
                    ChestHub.updateAllStatsSigns(p);

                    u.achieve(37);
                } else if(e.getRawSlot() == 1){
                    // GERMAN

                    u.setLanguage(Translation.getLanguage("DE"));
                    p.closeInventory();
                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Your language has been updated."));
                    u.updateScoreboard(ScoreboardType.LOBBY);
                    ChestHub.getInstance().giveLobbyItems(p);
                    u.saveSettings();
                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + org.bukkit.ChatColor.GREEN + u.getTranslatedMessage("Your settings have been saved."));
                    ChestHub.updateAllStatsSigns(p);

                    u.achieve(37);
                } else if(e.getRawSlot() == 8){
                    // CLOSE

                    MyProfile.openFor(p);
                }
            }
        }
    }
}
