package eu.thechest.chesthub.inv;

import eu.thechest.chestapi.ChestAPI;
import eu.thechest.chestapi.game.GamePerk;
import eu.thechest.chestapi.items.ItemUtil;
import eu.thechest.chestapi.server.GameType;
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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Created by zeryt on 10.07.2017.
 */
public class GamePerkMenu implements Listener {
    public static void openFor(Player p, GameType gameType){
        openFor(p,gameType,1);
    }

    public static void openFor(Player p, GameType gameType, int page){
        ChestUser u = ChestUser.getUser(p);

        int total = 0;
        int sizePerPage = 0;

        InventoryMenuBuilder inv = new InventoryMenuBuilder().withSize(ChestAPI.MAX_INVENTORY_SIZE);
        inv.withTitle(gameType.getName() + " Shop");

        ArrayList<GamePerk> perks = new ArrayList<GamePerk>();
        for(GamePerk perk : GamePerk.STORAGE.values()) if(perk.getGamemode() == gameType) perks.add(perk);

        sizePerPage = 36;
        total = perks.size();

        int slot = 0;

        for(GamePerk perk : perks.stream().skip((page-1) * sizePerPage).limit(sizePerPage).collect(Collectors.toCollection(ArrayList::new))){
            ItemStack i = perk.getIcon();
            ItemMeta iM = i.getItemMeta();

            iM.setDisplayName(ChatColor.AQUA.toString() + ChatColor.BOLD.toString() + u.getTranslatedMessage(perk.getName()));
            ArrayList<String> iL = new ArrayList<String>();
            for(String s : StringUtils.getWordWrapLore(u.getTranslatedMessage(perk.getDescription()))){
                iL.add(ChatColor.GRAY + s);
            }
            iL.add("  ");
            iL.add(ChatColor.GRAY + u.getTranslatedMessage("Price") + ": " + ChatColor.GOLD + perk.getPrice() + " " + u.getTranslatedMessage("Coins"));
            if(perk.getRequiredRank() != null){
                if(!u.hasPermission(perk.getRequiredRank())){
                    iL.add(ChatColor.RED + u.getTranslatedMessage("Required Rank: %r.").replace("%r",perk.getRequiredRank().getColor() + perk.getRequiredRank().getName()));
                } else {
                    iL.add(ChatColor.GRAY + u.getTranslatedMessage("Required Rank: %r.").replace("%r",perk.getRequiredRank().getColor() + perk.getRequiredRank().getName()));
                }
            }

            if(u.hasGamePerk(perk)){
                iL.add(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + u.getTranslatedMessage("You already have this perk."));
            } else {
                iL.add(ChatColor.YELLOW + u.getTranslatedMessage("Click to buy this perk!"));
            }

            iM.setLore(iL);
            i.setItemMeta(iM);

            if(u.hasGamePerk(perk)){
                inv.withItem(slot,ItemUtil.hideFlags(i));
            } else {
                inv.withItem(slot,ItemUtil.hideFlags(i), ((player, clickType, itemStack) -> { openConfirmation(p,perk); }), ClickType.LEFT);
            }

            slot++;
        }

        inv.withItem(36, ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.withItem(37,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.withItem(38,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.withItem(39,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.withItem(40,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.withItem(41,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.withItem(42,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.withItem(43,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.withItem(44,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));

        double d = (((double)total)/((double)sizePerPage));
        int maxPages = ((Double)d).intValue();
        if(maxPages < d) maxPages++;

        if(page != 1) inv.withItem(47,ItemUtil.namedItem(Material.ARROW, org.bukkit.ChatColor.GOLD + "<< " + org.bukkit.ChatColor.AQUA + u.getTranslatedMessage("Previous page"), null),((player, clickType, itemStack) -> { openFor(p,gameType,page-1); }), ClickType.LEFT);
        inv.withItem(49, ItemUtil.namedItem(Material.BARRIER, org.bukkit.ChatColor.DARK_RED + u.getTranslatedMessage("Close"), null), ((player, clickType, itemStack) -> { p.closeInventory(); }), ClickType.LEFT);
        if(maxPages > page) inv.withItem(51,ItemUtil.namedItem(Material.ARROW, org.bukkit.ChatColor.AQUA + u.getTranslatedMessage("Next page") + org.bukkit.ChatColor.GOLD + " >>", null),((player, clickType, itemStack) -> { openFor(p,gameType,page+1); }), ClickType.LEFT);

        inv.show(p);
    }

    public static void openConfirmation(Player p, GamePerk perk){
        ChestUser u = ChestUser.getUser(p);

        InventoryMenuBuilder inv = new InventoryMenuBuilder().withSize(9*3);
        inv.withTitle(u.getTranslatedMessage("Are you sure?"));

        inv.withItem(11,ItemUtil.namedItem(Material.STAINED_CLAY,ChatColor.GREEN + u.getTranslatedMessage("Buy this perk"),null,5), ((player, clickType, itemStack) -> {
            if(!u.hasGamePerk(perk)){
                if(u.getCoins() >= perk.getPrice()){
                    if(perk.getRequiredRank() == null || (perk.getRequiredRank() != null && u.hasPermission(perk.getRequiredRank()))){
                        u.giveGamePerk(perk);
                        u.reduceCoins(perk.getPrice());
                        p.playSound(p.getEyeLocation(), Sound.LEVEL_UP,1f,2f);
                        p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("You have successfully bought the perk %p.").replace("%p",ChatColor.YELLOW + u.getTranslatedMessage(perk.getName()) + ChatColor.GREEN));
                        openFor(p,perk.getGamemode());
                    } else {
                        p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("This perk requires %r to buy.").replace("%r",perk.getRequiredRank().getColor() + perk.getRequiredRank().getName() + ChatColor.RED));
                    }
                } else {
                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You don't have enough coins."));
                }
            } else {
                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You already own this perk."));
            }
        }), ClickType.LEFT);

        inv.withItem(15,ItemUtil.namedItem(Material.STAINED_CLAY,ChatColor.RED + u.getTranslatedMessage("DON'T Buy this perk"),null,14), ((player, clickType, itemStack) -> {
            openFor(p,perk.getGamemode());
        }), ClickType.LEFT);

        inv.show(p);
    }
}
