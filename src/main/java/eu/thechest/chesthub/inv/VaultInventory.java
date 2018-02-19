package eu.thechest.chesthub.inv;

import eu.thechest.chestapi.items.ItemCategory;
import eu.thechest.chestapi.items.ItemUtil;
import eu.thechest.chestapi.items.VaultItem;
import eu.thechest.chestapi.server.GameType;
import eu.thechest.chestapi.server.ServerSettingsManager;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.util.StringUtils;
import eu.thechest.chesthub.ChestHub;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Created by zeryt on 27.03.2017.
 */
public class VaultInventory implements Listener {
    public static ArrayList<String> COOLDOWN = new ArrayList<String>();

    public static void openFor(Player p, boolean mainMenu, ItemCategory category, int page){
        Inventory inv = null;
        ChestUser u = ChestUser.getUser(p);

        if(mainMenu){
            inv = Bukkit.createInventory(null,9*3,"[CV] Inventory");

            for(ItemCategory c : ItemCategory.values()){
                inv.addItem(c.toItemStack(u.getCurrentLanguage()));
            }

            inv.setItem(inv.getSize()-1, ItemUtil.namedItem(Material.BARRIER, org.bukkit.ChatColor.DARK_RED + u.getTranslatedMessage("Close"), null));
        } else {
            inv = Bukkit.createInventory(null, 9*6, "[CV] " + category.getName() + " | " + page);

            ArrayList<VaultItem> items = new ArrayList<VaultItem>();
            int sizePerPage = 36;

            for(VaultItem i : u.getUnlockedItems()){
                if(i != null && i.getCategory() == category) items.add(i);
            }

            int total = items.size();

            /*Collections.sort(items, new Comparator<LobbyShopItem>() {
                public int compare(LobbyShopItem p1, LobbyShopItem p2) {
                    return p1.getCost() - p2.getCost();
                }
            });*/

            for(VaultItem i : items.stream().skip((page-1) * sizePerPage).limit(sizePerPage).collect(Collectors.toCollection(ArrayList::new))){
                ItemStack iStack = i.getItem();
                ItemMeta m = iStack.getItemMeta();
                ArrayList<String> lore = new ArrayList<String>();

                if(i.getCategory() == ItemCategory.FAME_TITLE){
                    m.setDisplayName(i.getFameTitleColor() + i.getName() + " " + ChatColor.DARK_GRAY + "#" + i.getID());
                } else {
                    m.setDisplayName(i.getRarity().getColor() + i.getName() + " " + ChatColor.DARK_GRAY + "#" + i.getID());
                }

                lore.add(i.getCategory().getColor() + u.getTranslatedMessage(i.getCategory().getNameSingular()));

                if(i.getDescription() != null && !i.getDescription().isEmpty()){
                    lore.add("   ");

                    for(String s : StringUtils.getWordWrapLore(u.getTranslatedMessage(i.getDescription()))){
                        lore.add(ChatColor.GRAY + s);
                    }
                }

                lore.add(" ");
                lore.add(i.getRarity().getColor() + u.getTranslatedMessage(i.getRarity().getName() + " Item"));

                //if(u.getActiveItems().contains(i)){
                if(u.isActive(i)){
                    lore.add(ChatColor.GOLD.toString() + ChatColor.MAGIC.toString() + "abc" + ChatColor.RESET.toString() + ChatColor.GREEN + " " + u.getTranslatedMessage("Currently active!") + " " + ChatColor.GOLD.toString() + ChatColor.MAGIC.toString() + "abc");
                } else {
                    lore.add(ChatColor.YELLOW + u.getTranslatedMessage("Click to activate this item"));
                }

                m.setLore(lore);
                iStack.setItemMeta(m);

                if(u.isActive(i)){
                    inv.addItem(ItemUtil.addGlow(ItemUtil.setUnbreakable(ItemUtil.hideFlags(iStack),true)));
                } else {
                    inv.addItem(ItemUtil.setUnbreakable(ItemUtil.hideFlags(iStack),true));
                }
            }

            inv.setItem(36,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
            inv.setItem(37,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
            inv.setItem(38,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
            inv.setItem(39,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
            inv.setItem(40,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
            inv.setItem(41,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
            inv.setItem(42,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
            inv.setItem(43,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
            inv.setItem(44,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));

            inv.setItem(45,ItemUtil.namedItem(Material.REDSTONE_BLOCK, ChatColor.RED.toString() + ChatColor.BOLD.toString() + u.getTranslatedMessage("Disable current item"), null));

            double d = (((double)total)/((double)sizePerPage));
            int maxPages = ((Double)d).intValue();
            if(maxPages < d) maxPages++;

            if(page != 1) inv.setItem(47,ItemUtil.namedItem(Material.ARROW, org.bukkit.ChatColor.GOLD + "<< " + org.bukkit.ChatColor.AQUA + u.getTranslatedMessage("Previous page"), null));
            inv.setItem(49, ItemUtil.namedItem(Material.BARRIER, org.bukkit.ChatColor.DARK_RED + u.getTranslatedMessage("Close"), null));
            if(maxPages > page) inv.setItem(51,ItemUtil.namedItem(Material.ARROW, org.bukkit.ChatColor.AQUA + u.getTranslatedMessage("Next page") + org.bukkit.ChatColor.GOLD + " >>", null));
        }

        if(inv != null) p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(e.getWhoClicked() instanceof Player){
            Player p = (Player)e.getWhoClicked();
            ChestUser u = ChestUser.getUser(p);
            Inventory inv = e.getInventory();
            int slot = e.getRawSlot();

            if(inv.getName().startsWith("[CV]")){
                if(inv.getName().equals("[CV] Inventory")){
                    e.setCancelled(true);

                    if(slot >= 0 && slot <= (inv.getSize()-1)){
                        if(e.getCurrentItem() != null && e.getCurrentItem().getItemMeta() != null && e.getCurrentItem().getItemMeta().getDisplayName() != null){
                            ItemCategory c = null;

                            for(ItemCategory cat : ItemCategory.values()){
                                if(cat.toItemStack(u.getCurrentLanguage()).getItemMeta().getDisplayName().equals(e.getCurrentItem().getItemMeta().getDisplayName())){
                                    c = cat;
                                }
                            }

                            if(c != null){
                                openFor(p,false,c,1);
                            } else {
                                p.closeInventory();
                            }
                        }
                    }
                } else {
                    String c = null;
                    c = inv.getName().replace("[CV] ","");
                    String[] sp = c.split("|");
                    c = "";

                    for(String s : sp){
                        if(s.equals("|")) break;
                        c = c + s;
                    }

                    c = c.trim();

                    ItemCategory cat = null;

                    for(ItemCategory ca : ItemCategory.values()){
                        if(ca.getName().equals(c)) cat = ca;
                    }

                    if(cat != null){
                        if(e.getCurrentItem() != null && e.getCurrentItem().getType() != null && e.getCurrentItem().getType() == Material.STAINED_GLASS_PANE) return;

                        String[] aa = inv.getName().split("|");
                        int currentPage = Integer.parseInt(aa[aa.length-1].trim());

                        if(slot >= 0 && slot <= 35){
                            if(e.getCurrentItem() != null && e.getCurrentItem().getItemMeta() != null && e.getCurrentItem().getItemMeta().getDisplayName() != null){
                                String dis = e.getCurrentItem().getItemMeta().getDisplayName();
                                String[] aaa = dis.split("#");
                                String a = aaa[aaa.length-1];

                                if(StringUtils.isValidInteger(a)){
                                    int itemID = Integer.parseInt(a);
                                    VaultItem item = VaultItem.getItem(itemID);

                                    if(COOLDOWN.contains(p.getName())) return;

                                    if(!u.getActiveItems().contains(item)){
                                        u.setActiveItem(item);

                                        p.playSound(p.getEyeLocation(), Sound.NOTE_BASS,1f,3f);
                                        p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Item activated!") + " " + ChatColor.YELLOW + "(" + item.getName() + ")");

                                        openFor(p,false,cat,currentPage);

                                        if(item.getCategory() == ItemCategory.LOBBY_HAT) ChestHub.getInstance().giveLobbyHelmet(p);
                                        if(item.getCategory() == ItemCategory.PETS) ChestHub.getInstance().updatePet(p);
                                        if(item.getCategory() == ItemCategory.FAME_TITLE) u.updateFameTitleAboveHead();
                                        if(item.getCategory() == ItemCategory.GADGET) ChestHub.getInstance().giveLobbyItems(p);

                                        if(ChestHub.GADGET_COOLDOWN.containsKey(p.getName())){
                                            Bukkit.getScheduler().cancelTask(ChestHub.GADGET_COOLDOWN.get(p.getName()));
                                            ChestHub.GADGET_COOLDOWN.remove(p.getName());
                                        }

                                        COOLDOWN.add(p.getName());
                                        new BukkitRunnable(){
                                            @Override
                                            public void run() {
                                                COOLDOWN.remove(p.getName());
                                            }
                                        }.runTaskLater(ChestHub.getInstance(), 2*20);
                                    } else {
                                        u.disableItem(item.getCategory());

                                        p.playSound(p.getEyeLocation(), Sound.NOTE_BASS,1f,0.5f);
                                        p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Item disabled!"));

                                        openFor(p,false,cat,currentPage);

                                        if(item.getCategory() == ItemCategory.LOBBY_HAT) ChestHub.getInstance().giveLobbyHelmet(p);
                                        if(item.getCategory() == ItemCategory.PETS) ChestHub.getInstance().updatePet(p);
                                        if(item.getCategory() == ItemCategory.FAME_TITLE) u.updateFameTitleAboveHead();
                                        if(item.getCategory() == ItemCategory.GADGET) ChestHub.getInstance().giveLobbyItems(p);

                                        if(ChestHub.GADGET_COOLDOWN.containsKey(p.getName())){
                                            Bukkit.getScheduler().cancelTask(ChestHub.GADGET_COOLDOWN.get(p.getName()));
                                            ChestHub.GADGET_COOLDOWN.remove(p.getName());
                                        }

                                        COOLDOWN.add(p.getName());
                                        new BukkitRunnable(){
                                            @Override
                                            public void run() {
                                                COOLDOWN.remove(p.getName());
                                            }
                                        }.runTaskLater(ChestHub.getInstance(), 2*20);
                                    }
                                }
                            }
                        } else {
                            if(e.getCurrentItem() != null && e.getCurrentItem().getItemMeta() != null && e.getCurrentItem().getItemMeta().getDisplayName() != null){
                                if(slot == 47){
                                    //int currentPage = Integer.parseInt(inv.getName().replace("SG Shop | ",""));

                                    openFor(p,false,cat,currentPage-1);
                                } else if(slot == 49){
                                    openFor(p,true,null,0);
                                } else if(slot == 51){
                                    openFor(p,false,cat,currentPage+1);
                                } else if(slot == 45){
                                    if(COOLDOWN.contains(p.getName())) return;
                                    boolean co = false;

                                    VaultItem oldItem = u.getActiveItem(cat);
                                    if(oldItem != null){
                                        u.disableItem(cat);
                                        co = true;
                                    }

                                    p.playSound(p.getEyeLocation(), Sound.NOTE_BASS,1f,0.5f);
                                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Item disabled!"));

                                    openFor(p,false,cat,currentPage);

                                    if(cat == ItemCategory.LOBBY_HAT) ChestHub.getInstance().giveLobbyHelmet(p);
                                    if(cat == ItemCategory.PETS) ChestHub.getInstance().updatePet(p);
                                    if(cat == ItemCategory.GADGET) ChestHub.getInstance().giveLobbyItems(p);
                                    if(cat == ItemCategory.FAME_TITLE) u.updateFameTitleAboveHead();

                                    if(ChestHub.GADGET_COOLDOWN.containsKey(p.getName())){
                                        Bukkit.getScheduler().cancelTask(ChestHub.GADGET_COOLDOWN.get(p.getName()));
                                        ChestHub.GADGET_COOLDOWN.remove(p.getName());
                                    }

                                    if(co){
                                        COOLDOWN.add(p.getName());
                                        new BukkitRunnable(){
                                            @Override
                                            public void run() {
                                                COOLDOWN.remove(p.getName());
                                            }
                                        }.runTaskLater(ChestHub.getInstance(), 2*20);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
