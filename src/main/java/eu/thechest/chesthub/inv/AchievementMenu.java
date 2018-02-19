package eu.thechest.chesthub.inv;

import eu.thechest.chestapi.ChestAPI;
import eu.thechest.chestapi.achievement.Achievement;
import eu.thechest.chestapi.items.ItemUtil;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
 * Created by zeryt on 26.02.2017.
 */
public class AchievementMenu implements Listener {
    public static void openMainMenu(Player p){
        ChestUser u = ChestUser.getUser(p);

        InventoryMenuBuilder inv = new InventoryMenuBuilder().withSize(27);
        inv.withTitle(u.getTranslatedMessage("Achievements"));

        inv.withItem(0,ItemUtil.hideFlags(ItemUtil.namedItem(Material.DIAMOND,ChatColor.GRAY + u.getTranslatedMessage("General"),null)), ((player, clickType, itemStack) -> openFor(p,1,"GENERAL")), ClickType.LEFT);
        inv.withItem(1,ItemUtil.hideFlags(ItemUtil.namedItem(Material.SLIME_BALL,ChatColor.GRAY + u.getTranslatedMessage("Hub"),null)), ((player, clickType, itemStack) -> openFor(p,1,"LOBBY")), ClickType.LEFT);
        inv.withItem(2,ItemUtil.hideFlags(ItemUtil.namedItem(Material.CHEST,ChatColor.DARK_RED + u.getTranslatedMessage("Survival Games"),null)), ((player, clickType, itemStack) -> openFor(p,1,"SURVIVAL_GAMES")), ClickType.LEFT);
        inv.withItem(3,ItemUtil.hideFlags(ItemUtil.namedItem(Material.RECORD_7,ChatColor.BLUE + u.getTranslatedMessage("Musical Guess"),null)), ((player, clickType, itemStack) -> openFor(p,1,"MUSICALGUESS")), ClickType.LEFT);
        inv.withItem(4,ItemUtil.hideFlags(ItemUtil.namedItem(Material.DIAMOND_SWORD,ChatColor.YELLOW + u.getTranslatedMessage("KitPvP"),null)), ((player, clickType, itemStack) -> openFor(p,1,"KITPVP")), ClickType.LEFT);
        inv.withItem(5,ItemUtil.hideFlags(ItemUtil.namedItem(Material.BOOK_AND_QUILL,ChatColor.DARK_PURPLE + u.getTranslatedMessage("Build & Guess"),null)), ((player, clickType, itemStack) -> openFor(p,1,"BUILD_GUESS")), ClickType.LEFT);
        inv.withItem(6,ItemUtil.hideFlags(ItemUtil.namedItem(Material.SNOW_BALL,ChatColor.AQUA + u.getTranslatedMessage("Soccer"),null)), ((player, clickType, itemStack) -> openFor(p,1,"SOCCERMC")), ClickType.LEFT);
        inv.withItem(7,ItemUtil.hideFlags(ItemUtil.namedItem(Material.DIAMOND_CHESTPLATE,ChatColor.DARK_AQUA + u.getTranslatedMessage("Death Match"),null)), ((player, clickType, itemStack) -> openFor(p,1,"DEATHMATCH")), ClickType.LEFT);
        inv.withItem(8,ItemUtil.hideFlags(ItemUtil.namedItem(Material.ENDER_CHEST,ChatColor.DARK_RED + u.getTranslatedMessage("Survival Games") + ": " + ChatColor.YELLOW + u.getTranslatedMessage("Duels"),null)), ((player, clickType, itemStack) -> openFor(p,1,"SGDUELS")), ClickType.LEFT);
        inv.withItem(9,ItemUtil.hideFlags(ItemUtil.namedItem(Material.DIAMOND_HOE,ChatColor.LIGHT_PURPLE + u.getTranslatedMessage("Tobiko"),null)), ((player, clickType, itemStack) -> openFor(p,1,"TOBIKO")), ClickType.LEFT);

        inv.withItem(26,ItemUtil.namedItem(Material.BARRIER, ChatColor.DARK_RED + u.getTranslatedMessage("Close"), null), ((player, clickType, itemStack) -> MyProfile.openFor(p)), ClickType.LEFT);

        /*Inventory inv = Bukkit.createInventory(null,9,"[AM] MainMenu");

        inv.addItem(ItemUtil.hideFlags(ItemUtil.namedItem(Material.DIAMOND,ChatColor.GRAY + u.getTranslatedMessage("General / Hub"),null)));
        inv.addItem(ItemUtil.hideFlags(ItemUtil.namedItem(Material.CHEST,ChatColor.DARK_RED + "Survival Games",null)));
        inv.addItem(ItemUtil.hideFlags(ItemUtil.namedItem(Material.RECORD_7,ChatColor.BLUE + "Musical Guess",null)));
        inv.addItem(ItemUtil.hideFlags(ItemUtil.namedItem(Material.DIAMOND_SWORD,ChatColor.YELLOW + "KitPvP",null)));
        inv.addItem(ItemUtil.hideFlags(ItemUtil.namedItem(Material.BOOK_AND_QUILL,ChatColor.DARK_PURPLE + "Build & Guess",null)));
        inv.addItem(ItemUtil.hideFlags(ItemUtil.namedItem(Material.SNOW_BALL,ChatColor.AQUA + "SoccerMC",null)));
        inv.addItem(ItemUtil.hideFlags(ItemUtil.namedItem(Material.DIAMOND_CHESTPLATE,ChatColor.DARK_AQUA + "DeathMatch",null)));

        inv.setItem(8,ItemUtil.namedItem(Material.BARRIER, ChatColor.DARK_RED + u.getTranslatedMessage("Close"), null));

        p.openInventory(inv);*/

        inv.show(p);
    }

    public static void openFor(Player p, int page, String category){
        ChestUser u = ChestUser.getUser(p);

        ItemStack pl = ItemUtil.namedItem(Material.STAINED_GLASS_PANE, " ", null, 15);
        ItemStack prev = ItemUtil.namedItem(Material.ARROW, ChatColor.GOLD + "<< " + ChatColor.AQUA + u.getTranslatedMessage("Previous page"), null);
        ItemStack next = ItemUtil.namedItem(Material.ARROW, ChatColor.AQUA + u.getTranslatedMessage("Next page") + ChatColor.GOLD + " >>", null);
        ItemStack close = ItemUtil.namedItem(Material.BARRIER, ChatColor.DARK_RED + u.getTranslatedMessage("Close"), null);

        ArrayList<Achievement> achievements = new ArrayList<Achievement>();
        for(Achievement a : Achievement.getAchievements()){
            if(a.getCategory().equals(category)) achievements.add(a);
        }

        int sizePerPage = 36;
        int total = achievements.size();

        double d = (((double)total)/((double)sizePerPage));
        int maxPages = ((Double)d).intValue();
        if(maxPages < d) maxPages++;

        InventoryMenuBuilder inv = new InventoryMenuBuilder().withSize(ChestAPI.MAX_INVENTORY_SIZE);
        inv.withTitle(u.getTranslatedMessage("Achievements") + " (" + page + "/" + maxPages + ")");

        int slot = 0;

        for(Achievement a : achievements.stream().skip((page-1) * sizePerPage).limit(sizePerPage).collect(Collectors.toCollection(ArrayList::new))){
            if(!u.hasAchieved(a)){
                ItemStack i = new ItemStack(Material.INK_SACK);
                i.setDurability((short)8);
                ItemMeta m = i.getItemMeta();
                m.setDisplayName(ChatColor.DARK_GRAY + u.getTranslatedMessage(a.getTitle()));
                ArrayList<String> lore = new ArrayList<String>();
                for(String dd : StringUtils.getWordWrapLore(u.getTranslatedMessage(a.getDescription()))){
                    lore.add(ChatColor.GRAY + dd);
                }
                lore.add(" ");
                lore.add(ChatColor.YELLOW + u.getTranslatedMessage("Category"));
                String cat = a.getCategory();
                if(cat.equals("GENERAL")){
                    cat = ChatColor.GRAY + u.getTranslatedMessage("General");
                } else if(cat.equals("LOBBY")){
                    cat = ChatColor.GRAY + u.getTranslatedMessage("Hub");
                } else if(cat.equals("SURVIVAL_GAMES")){
                    cat = ChatColor.DARK_RED + "Survival Games";
                } else if(cat.equals("KITPVP")){
                    cat = ChatColor.YELLOW + "KitPvP";
                } else if(cat.equals("DEATHMATCH")){
                    cat = ChatColor.DARK_AQUA + "Deathmatch";
                } else if(cat.equals("MUSICALGUESS")){
                    cat = ChatColor.BLUE + "Musical Guess";
                } else if(cat.equals("BUILD_GUESS")){
                    cat = ChatColor.DARK_PURPLE + "Build & Guess";
                } else if(cat.equals("SOCCER")){
                    cat = ChatColor.AQUA + "SoccerMC";
                } else if(cat.equals("SGDUELS")){
                    cat = ChatColor.DARK_RED + "Survival Games: " + ChatColor.YELLOW + "Duels";
                } else if(cat.equals("TOBIKO")){
                    cat = ChatColor.LIGHT_PURPLE + "Tobiko";
                }
                lore.add(ChatColor.GREEN + "> " + cat);
                m.setLore(lore);
                i.setItemMeta(m);
                inv.withItem(slot,i);
            } else {
                ItemStack i = new ItemStack(Material.INK_SACK);
                i.setDurability((short)10);
                ItemMeta m = i.getItemMeta();
                m.setDisplayName(ChatColor.GREEN + u.getTranslatedMessage(a.getTitle()));
                ArrayList<String> lore = new ArrayList<String>();
                for(String dd : StringUtils.getWordWrapLore(u.getTranslatedMessage(a.getDescription()))){
                    lore.add(ChatColor.GRAY + dd);
                }
                lore.add(" ");
                lore.add(ChatColor.YELLOW + u.getTranslatedMessage("Category"));
                String cat = a.getCategory();
                if(cat.equals("GENERAL")){
                    cat = ChatColor.GRAY + u.getTranslatedMessage("General");
                } else if(cat.equals("LOBBY")){
                    cat = ChatColor.GRAY + u.getTranslatedMessage("Hub");
                } else if(cat.equals("SURVIVAL_GAMES")){
                    cat = ChatColor.DARK_RED + "Survival Games";
                } else if(cat.equals("KITPVP")){
                    cat = ChatColor.YELLOW + "KitPvP";
                } else if(cat.equals("DEATHMATCH")){
                    cat = ChatColor.DARK_AQUA + "Deathmatch";
                } else if(cat.equals("MUSICALGUESS")){
                    cat = ChatColor.BLUE + "Musical Guess";
                } else if(cat.equals("BUILD_GUESS")){
                    cat = ChatColor.DARK_PURPLE + "Build & Guess";
                } else if(cat.equals("SOCCER")){
                    cat = ChatColor.AQUA + "SoccerMC";
                } else if(cat.equals("SGDUELS")){
                    cat = ChatColor.DARK_RED + "Survival Games: " + ChatColor.YELLOW + "Duels";
                } else if(cat.equals("TOBIKO")){
                    cat = ChatColor.LIGHT_PURPLE + "Tobiko";
                }
                lore.add(ChatColor.GREEN + "> " + cat);
                m.setLore(lore);
                i.setItemMeta(m);
                inv.withItem(slot,i);
            }

            slot++;
        }

        inv.withItem(36, pl);
        inv.withItem(37, pl);
        inv.withItem(38, pl);
        inv.withItem(39, pl);
        inv.withItem(40, pl);
        inv.withItem(41, pl);
        inv.withItem(42, pl);
        inv.withItem(43, pl);
        inv.withItem(44, pl);

        if(page != 1) inv.withItem(47,prev, ((player, clickType, itemStack) -> openFor(p,page-1,category)), ClickType.LEFT);
        inv.withItem(49,close, ((player, clickType, itemStack) -> openMainMenu(p)), ClickType.LEFT);
        if(maxPages > page) inv.withItem(51,next, ((player, clickType, itemStack) -> openFor(p,page-1,category)), ClickType.LEFT);

        inv.show(p);

        /*Inventory inv = Bukkit.createInventory(null,54,"[AM] " + category + " | " + page);

        for(Achievement a : achievements.stream().skip((page-1) * sizePerPage).limit(sizePerPage).collect(Collectors.toCollection(ArrayList::new))){
            if(!u.hasAchieved(a)){
                ItemStack i = new ItemStack(Material.INK_SACK);
                i.setDurability((short)8);
                ItemMeta m = i.getItemMeta();
                m.setDisplayName(ChatColor.DARK_GRAY + u.getTranslatedMessage(a.getTitle()));
                ArrayList<String> lore = new ArrayList<String>();
                for(String d : StringUtils.getWordWrapLore(u.getTranslatedMessage(a.getDescription()))){
                    lore.add(ChatColor.GRAY + d);
                }
                lore.add(" ");
                lore.add(ChatColor.YELLOW + u.getTranslatedMessage("Category"));
                String cat = a.getCategory();
                if(cat.equals("GENERAL")){
                    cat = ChatColor.GRAY + u.getTranslatedMessage("General / Hub");
                } else if(cat.equals("SURVIVAL_GAMES")){
                    cat = ChatColor.DARK_RED + "Survival Games";
                } else if(cat.equals("KITPVP")){
                    cat = ChatColor.YELLOW + "KitPvP";
                } else if(cat.equals("DEATHMATCH")){
                    cat = ChatColor.DARK_AQUA + "Deathmatch";
                } else if(cat.equals("MUSICALGUESS")){
                    cat = ChatColor.BLUE + "Musical Guess";
                } else if(cat.equals("BUILD_GUESS")){
                    cat = ChatColor.DARK_PURPLE + "Build & Guess";
                } else if(cat.equals("SOCCER")){
                    cat = ChatColor.AQUA + "SoccerMC";
                }
                lore.add(ChatColor.GREEN + "> " + cat);
                m.setLore(lore);
                i.setItemMeta(m);
                inv.addItem(i);
            } else {
                ItemStack i = new ItemStack(Material.INK_SACK);
                i.setDurability((short)10);
                ItemMeta m = i.getItemMeta();
                m.setDisplayName(ChatColor.GREEN + u.getTranslatedMessage(a.getTitle()));
                ArrayList<String> lore = new ArrayList<String>();
                for(String d : StringUtils.getWordWrapLore(u.getTranslatedMessage(a.getDescription()))){
                    lore.add(ChatColor.GRAY + d);
                }
                lore.add(" ");
                lore.add(ChatColor.YELLOW + u.getTranslatedMessage("Category"));
                String cat = a.getCategory();
                if(cat.equals("GENERAL")){
                    cat = ChatColor.GRAY + u.getTranslatedMessage("General / Hub");
                } else if(cat.equals("SURVIVAL_GAMES")){
                    cat = ChatColor.DARK_RED + "Survival Games";
                } else if(cat.equals("KITPVP")){
                    cat = ChatColor.YELLOW + "KitPvP";
                } else if(cat.equals("DEATHMATCH")){
                    cat = ChatColor.DARK_AQUA + "Deathmatch";
                } else if(cat.equals("MUSICALGUESS")){
                    cat = ChatColor.BLUE + "Musical Guess";
                } else if(cat.equals("BUILD_GUESS")){
                    cat = ChatColor.DARK_PURPLE + "Build & Guess";
                } else if(cat.equals("SOCCER")){
                    cat = ChatColor.AQUA + "SoccerMC";
                }
                lore.add(ChatColor.GREEN + "> " + cat);
                m.setLore(lore);
                i.setItemMeta(m);
                inv.addItem(i);
            }
        }

        inv.setItem(36, pl);
        inv.setItem(37, pl);
        inv.setItem(38, pl);
        inv.setItem(39, pl);
        inv.setItem(40, pl);
        inv.setItem(41, pl);
        inv.setItem(42, pl);
        inv.setItem(43, pl);
        inv.setItem(44, pl);

        if(page != 1) inv.setItem(47,prev);
        inv.setItem(49,close);
        if(maxPages > page) inv.setItem(51,next);

        p.openInventory(inv);*/
    }

    /*@EventHandler
    public void onClick(InventoryClickEvent e){
        if (e.getWhoClicked() instanceof Player) {
            Player p = (Player) e.getWhoClicked();
            ChestUser u = ChestUser.getUser(p);
            Inventory inv = e.getInventory();
            int slot = e.getRawSlot();

            if (inv.getName().startsWith("[AM]")) {
                String[] s = inv.getName().split(" ");
                String cat = s[1];

                if(cat.equals("MainMenu")){
                    if(slot == 0){
                        openFor(p,1,"GENERAL");
                    } else if(slot == 1){
                        openFor(p,1,"SURVIVAL_GAMES");
                    } else if(slot == 2){
                        openFor(p,1,"MUSICALGUESS");
                    } else if(slot == 3){
                        openFor(p,1,"KITPVP");
                    } else if(slot == 4){
                        openFor(p,1,"BUILD_GUESS");
                    } else if(slot == 5){
                        openFor(p,1,"SOCCERMC");
                    } else if(slot == 6){
                        openFor(p,1,"DEATHMATCH");
                    } else if(slot == 8){
                        MyProfile.openFor(p);
                    }
                } else {
                    int page = Integer.parseInt(e.getInventory().getName().replace("[AM] " + cat + " | ", ""));

                    if(slot == 47){
                        openFor(p,page-1,cat);
                    } else if(slot == 49){
                        openMainMenu(p);
                    } else if(slot == 51){
                        openFor(p,page+1,cat);
                    }
                }
            }
        }
    }*/
}
