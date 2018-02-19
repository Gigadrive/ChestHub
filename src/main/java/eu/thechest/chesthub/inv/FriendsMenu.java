package eu.thechest.chesthub.inv;

import com.dsh105.echopet.compat.api.util.inventory.InventoryMenu;
import eu.thechest.chestapi.ChestAPI;
import eu.thechest.chestapi.items.ItemUtil;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.user.Rank;
import eu.thechest.chestapi.util.CrewTagData;
import eu.thechest.chestapi.util.PlayerUtilities;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;
import org.inventivetalent.menubuilder.inventory.ItemListener;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

public class FriendsMenu {
    public static void openFor(Player p){
        openFor(p,1);
    }

    public static void openFor(Player p, int page){
        ChestUser u = ChestUser.getUser(p);
        int sizePerPage = 36;

        InventoryMenuBuilder inv = new InventoryMenuBuilder().withSize(ChestAPI.MAX_INVENTORY_SIZE);
        inv.withTitle(u.getTranslatedMessage("Friends"));
        ArrayList<String> friends = new ArrayList<String>();
        friends.addAll(PlayerUtilities.getFriendsFromUUID(p.getUniqueId()));
        int total = friends.size();

        if(total > 0){
            int slot = 0;

            for(String friend : friends.stream().skip((page-1) * sizePerPage).limit(sizePerPage).collect(Collectors.toCollection(ArrayList::new))){
                UUID uuid = UUID.fromString(friend);
                String name = PlayerUtilities.getNameFromUUID(uuid);
                Rank rank = PlayerUtilities.getRankFromUUID(uuid);
                CrewTagData tagData = PlayerUtilities.getCrewTagFromUUID(uuid);

                ItemStack item = new ItemStack(Material.SKULL_ITEM,1,(short)3);
                ItemMeta itemMeta = item.getItemMeta();
                if(tagData != null && tagData.tag != null){
                    itemMeta.setDisplayName(rank.getColor() + name + " " + ChatColor.GRAY + "[" + ChatColor.YELLOW + tagData.tag + ChatColor.GRAY + "]");
                } else {
                    itemMeta.setDisplayName(rank.getColor() + name);
                }
                ArrayList<String> itemLore = new ArrayList<String>();
                itemMeta.setLore(itemLore);
                item.setItemMeta(itemMeta);

                inv.withItem(slot, item, ((player, clickType, itemStack) -> player.sendMessage(uuid.toString())), ClickType.LEFT);
                slot++;
            }
        }

        inv.withItem(36, ItemUtil.namedItem(Material.STAINED_GLASS_PANE, " ", null, 15));
        inv.withItem(37, ItemUtil.namedItem(Material.STAINED_GLASS_PANE, " ", null, 15));
        inv.withItem(38, ItemUtil.namedItem(Material.STAINED_GLASS_PANE, " ", null, 15));
        inv.withItem(39, ItemUtil.namedItem(Material.STAINED_GLASS_PANE, " ", null, 15));
        inv.withItem(40, ItemUtil.namedItem(Material.STAINED_GLASS_PANE, " ", null, 15));
        inv.withItem(41, ItemUtil.namedItem(Material.STAINED_GLASS_PANE, " ", null, 15));
        inv.withItem(42, ItemUtil.namedItem(Material.STAINED_GLASS_PANE, " ", null, 15));
        inv.withItem(43, ItemUtil.namedItem(Material.STAINED_GLASS_PANE, " ", null, 15));
        inv.withItem(44, ItemUtil.namedItem(Material.STAINED_GLASS_PANE, " ", null, 15));

        double d = (((double)total)/((double)sizePerPage));
        int maxPages = ((Double)d).intValue();
        if(maxPages < d) maxPages++;

        if(page != 1) inv.withItem(47,ItemUtil.namedItem(Material.ARROW, org.bukkit.ChatColor.GOLD + "<< " + org.bukkit.ChatColor.AQUA + u.getTranslatedMessage("Previous page"), null), ((player, clickType, itemStack) -> FriendsMenu.openFor(player,page-1)), ClickType.LEFT);
        inv.withItem(49, ItemUtil.namedItem(Material.BARRIER, org.bukkit.ChatColor.DARK_RED + u.getTranslatedMessage("Close"), null), (player, clickType, itemStack) -> MyProfile.openFor(player), ClickType.LEFT);
        if(maxPages > page) inv.withItem(51,ItemUtil.namedItem(Material.ARROW, org.bukkit.ChatColor.AQUA + u.getTranslatedMessage("Next page") + org.bukkit.ChatColor.GOLD + " >>", null), (player, clickType, itemStack) -> FriendsMenu.openFor(player,page+1), ClickType.LEFT);

        inv.show(p);
    }
}
