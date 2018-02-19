package eu.thechest.chesthub.inv;

import eu.thechest.chestapi.ChestAPI;
import eu.thechest.chestapi.items.ItemUtil;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.user.Rank;
import eu.thechest.chestapi.util.StringUtils;
import eu.thechest.chesthub.levelrewards.LevelReward;
import net.minecraft.server.v1_8_R3.AttributeInstance;
import net.minecraft.server.v1_8_R3.AttributeModifier;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHorse;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by zeryt on 18.02.2017.
 */
public class MyProfile {
    public static void openFor(Player p){
        ChestUser u = ChestUser.getUser(p);
        InventoryMenuBuilder inv = new InventoryMenuBuilder(9*5);
        inv.withTitle(u.getTranslatedMessage("My Profile"));

        ItemStack pl = new ItemStack(Material.SKULL_ITEM);
        pl.setDurability((short)3);
        SkullMeta m = (SkullMeta)pl.getItemMeta();

        m.setDisplayName(u.getRank().getColor() + p.getName());

        m.setOwner(p.getName());

        long hours = u.getPlaytime()/60/60;

        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GREEN + u.getTranslatedMessage("Coins") + ": " + ChatColor.WHITE + u.getCoins());
        lore.add(ChatColor.GREEN + u.getTranslatedMessage("Rank") + ": " + u.getRank().getColor() + u.getRank().getName());
        lore.add(ChatColor.GREEN + u.getTranslatedMessage("First join") + ": " + ChatColor.WHITE + u.getFirstJoin().toGMTString());
        if(hours == 1){
            lore.add(ChatColor.GREEN + u.getTranslatedMessage("Playtime") + ": " + ChatColor.WHITE + hours + " " + u.getTranslatedMessage("hour"));
        } else {
            lore.add(ChatColor.GREEN + u.getTranslatedMessage("Playtime") + ": " + ChatColor.WHITE + hours + " " + u.getTranslatedMessage("hours"));
        }
        lore.add(ChatColor.GREEN + u.getTranslatedMessage("Level") + ": " + ChatColor.WHITE + u.getLevel());
        m.setLore(lore);
        pl.setItemMeta(m);

        ItemStack language = ItemUtil.namedItem(Material.PAPER, ChatColor.YELLOW + "Language", StringUtils.getWordWrapLore("Change your language for the entire server to get a better playing experience!",ChatColor.GRAY).toArray(new String[]{}));
        ItemStack settings = ItemUtil.namedItem(Material.DIODE, ChatColor.YELLOW + u.getTranslatedMessage("Settings"), StringUtils.getWordWrapLore(u.getTranslatedMessage("Manage your settings to turn off certain features of the server."),ChatColor.GRAY).toArray(new String[]{}));
        ItemStack achievements = ItemUtil.namedItem(Material.DIAMOND, ChatColor.YELLOW + u.getTranslatedMessage("Achievements"), StringUtils.getWordWrapLore(u.getTranslatedMessage("View your unlocked and locked achievements. You can find new achievements in our lobby and in every game mode. Achievements also give you a lot of exp and coins."),ChatColor.GRAY).toArray(new String[]{}));
        ItemStack friends = ItemUtil.namedItem(Material.BOOK_AND_QUILL, ChatColor.YELLOW + u.getTranslatedMessage("Friends"), StringUtils.getWordWrapLore(u.getTranslatedMessage("Manage your friends."),ChatColor.GRAY).toArray(new String[]{}));
        ItemStack statistics = ItemUtil.namedItem(Material.GOLD_BLOCK, ChatColor.YELLOW + u.getTranslatedMessage("Statistics"), StringUtils.getWordWrapLore(u.getTranslatedMessage("View your statistics for every game mode."),ChatColor.GRAY).toArray(new String[]{}));
        ItemStack premium = ItemUtil.namedItem(Material.GOLDEN_APPLE, ChatColor.YELLOW + u.getTranslatedMessage("Premium Menu"), StringUtils.getWordWrapLore(u.getTranslatedMessage("Special options for premium members. Visit &bthechest.eu/store &7for more information on premium ranks."),ChatColor.GRAY).toArray(new String[]{}));
        ItemStack party = ItemUtil.namedItem(Material.FIREWORK, ChatColor.YELLOW + u.getTranslatedMessage("Party"), StringUtils.getWordWrapLore(u.getTranslatedMessage("Manage your party and it's members. Parties are a nice way to play with your friends. You can create a new party with &b/party create&7."),ChatColor.GRAY).toArray(new String[]{}));
        ItemStack crew = ItemUtil.namedItem(Material.PAINTING, ChatColor.YELLOW + u.getTranslatedMessage("Crew"), StringUtils.getWordWrapLore(u.getTranslatedMessage("Change your crew's name, tag or manage it's members. You can create a new crew with &b/crew create&7."),ChatColor.GRAY).toArray(new String[]{}));
        ItemStack level = ItemUtil.namedItem(Material.EXP_BOTTLE, ChatColor.YELLOW + u.getTranslatedMessage("Level Rewards"), StringUtils.getWordWrapLore(u.getTranslatedMessage("Claim rewards for every level you reach. You can get experience and level up by playing games, earning achievements and much more."),ChatColor.GRAY).toArray(new String[]{}));
        ItemStack soon = ItemUtil.namedItem(Material.BARRIER,ChatColor.RED + u.getTranslatedMessage("Coming soon"),null);
        ItemStack placeholder = ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15);

        //for(int i = 0; i < inv.getSize(); i++) inv.setItem(i,placeholder);

        inv.withItem(2,premium,((player, clickType, itemStack) -> {
            if(u.hasPermission(Rank.PRO_PLUS)){
                PremiumMenu.openFor(p);
            } else {

            }
        }), ClickType.LEFT);

        inv.withItem(3,party,((player, clickType, itemStack) -> {
            p.closeInventory();
            ChestAPI.executeBungeeCommand(p.getName(),"party info");
        }), ClickType.LEFT);

        inv.withItem(5,friends,((player, clickType, itemStack) -> {
            p.closeInventory();
            ChestAPI.executeBungeeCommand(p.getName(),"friends list");
        }), ClickType.LEFT);

        inv.withItem(6,achievements,((player, clickType, itemStack) -> {
            AchievementMenu.openMainMenu(p);
        }), ClickType.LEFT);

        inv.withItem(11,crew,((player, clickType, itemStack) -> {
            p.closeInventory();
            ChestAPI.executeBungeeCommand(p.getName(),"crew info");
        }), ClickType.LEFT);

        inv.withItem(15,level,((player, clickType, itemStack) -> {
            LevelRewardsMenu.openFor(p);
        }), ClickType.LEFT);

        inv.withItem(22,pl);

        inv.withItem(29,statistics,((player, clickType, itemStack) -> {
            p.performCommand("stats " + p.getName());
        }), ClickType.LEFT);

        inv.withItem(33,soon);

        inv.withItem(38,language,((player, clickType, itemStack) -> {
            LanguageMenu.openFor(p);
        }), ClickType.LEFT);

        inv.withItem(39,settings,((player, clickType, itemStack) -> {
            SettingsMenu.openFor(p);
        }), ClickType.LEFT);

        inv.withItem(41,soon);

        inv.withItem(42,soon);

        inv.show(p);
    }

    /*@EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player p = (Player) e.getWhoClicked();
            ChestUser u = ChestUser.getUser(p);
            Inventory inv = e.getInventory();

            if (inv.getName().equals("My Profile")) {
                e.setCancelled(true);
                if(e.getRawSlot() == 2){
                    // PREMIUM
                    if(u.hasPermission(Rank.PRO_PLUS)) PremiumMenu.openFor(p);
                } else if(e.getRawSlot() == 3){
                    // PARTY
                    p.closeInventory();
                    ChestAPI.executeBungeeCommand(p.getName(),"party info");
                } else if(e.getRawSlot() == 5){
                    // FRIENDS

                    /*if(u.hasPermission(Rank.VIP)){
                        FriendsMenu.openFor(p);
                    } else {
                        p.closeInventory();
                        ChestAPI.executeBungeeCommand(p.getName(),"friends list");
                    }
                    p.closeInventory();
                    ChestAPI.executeBungeeCommand(p.getName(),"friends list");
                } else if(e.getRawSlot() == 6){
                    // ACHIEVEMENTS
                    AchievementMenu.openMainMenu(p);
                } else if(e.getRawSlot() == 11){
                    // CREW
                    p.closeInventory();
                    ChestAPI.executeBungeeCommand(p.getName(),"crew info");
                } else if(e.getRawSlot() == 15){
                    // LEVEL REWARDS
                    LevelRewardsMenu.openFor(p);
                } else if(e.getRawSlot() == 29){
                    // STATISTICS
                    p.performCommand("stats " + p.getName());
                } else if(e.getRawSlot() == 33){
                    // SOON
                } else if(e.getRawSlot() == 38){
                    // LANGUAGE
                    LanguageMenu.openFor(p);
                } else if(e.getRawSlot() == 39){
                    // SETTINGS
                    SettingsMenu.openFor(p);
                } else if(e.getRawSlot() == 41){
                    // SOON
                } else if(e.getRawSlot() == 42){
                    // SOON
                }

                /*if(e.getRawSlot() == 3){
                    // LANGUAGE

                    LanguageMenu.openFor(p);
                } else if(e.getRawSlot() == 4){
                    // SETTINGS

                    SettingsMenu.openFor(p);
                } else if(e.getRawSlot() == 5){
                    // ACHIEVEMENTS

                    AchievementMenu.openFor(p,1);
                } else if(e.getRawSlot() == 6){
                    // FRIENDS

                    p.closeInventory();
                    ChestAPI.executeBungeeCommand(p.getName(),"friends list");
                } else if(e.getRawSlot() == 7){
                    // STATISTICS

                    StatisticsMenu.openFor(p);
                } else if(e.getRawSlot() == 8){
                    // HORSE

                    if(u.hasPermission(Rank.PRO_PLUS)){
                        p.closeInventory();

                        Horse h = (Horse)p.getWorld().spawnEntity(p.getLocation(), EntityType.HORSE);

                        h.getInventory().setSaddle(new ItemStack(Material.SADDLE));
                        h.setVariant(Horse.Variant.HORSE);
                        h.setColor(Horse.Color.BROWN);
                        h.setStyle(Horse.Style.NONE);
                        h.setCarryingChest(true);
                        h.setAdult();
                        h.setCustomName(u.getRank().getColor() + p.getName() + ChatColor.GREEN + "'s Horse");
                        h.setJumpStrength(0.8);
                        h.setPassenger(p);

                        EntityInsentient nmsEntity = (EntityInsentient)((CraftLivingEntity)h).getHandle();
                        AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
                        AttributeModifier m = new AttributeModifier(UUID.randomUUID(),"asd",1.1d,1);
                        attributes.b(m);
                        attributes.a(m);
                    }
                }
            }
        }
    }*/
}
