package eu.thechest.chesthub.inv;

import de.dytanic.cloudnet.network.ServerInfo;
import de.dytanic.cloudnet.servergroup.ServerState;
import eu.thechest.chestapi.ChestAPI;
import eu.thechest.chestapi.ServerSortMethod;
import eu.thechest.chestapi.items.ItemUtil;
import eu.thechest.chestapi.server.GameType;
import eu.thechest.chestapi.server.ServerSettingsManager;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.user.GlobalParty;
import eu.thechest.chestapi.user.Rank;
import eu.thechest.chestapi.util.StringUtils;
import eu.thechest.chesthub.ChestHub;
import eu.thechest.chesthub.LocationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;

import java.util.ArrayList;

/**
 * Created by zeryt on 11.02.2017.
 */
public class GamemodesMenu {
    private static void teleport(Player p, GameType type){
        ChestUser u = ChestUser.getUser(p);

        if(type == null){
            p.teleport(LocationManager.getLocation("spawn"));
        } else {
            p.teleport(LocationManager.getLocation(type.getAbbreviation().toLowerCase()));
            u.achieve(23);
        }

        p.playSound(p.getEyeLocation(), Sound.ORB_PICKUP,1F,1F);
    }

    private static void a(Player p, String group, boolean allowVIPJoin){
        ChestUser u = ChestUser.getUser(p);

        ServerInfo info = null;

        if(allowVIPJoin && u.hasPermission(Rank.PRO) && ((GlobalParty.getParty(p) == null) || (GlobalParty.getParty(p).members.size() == 1))){
            info = ChestAPI.getBestServer(group,0,ServerState.LOBBY,ServerSortMethod.GET_FULLEST_SERVER);
        } else {
            int minFreeSlots = 1;
            if(GlobalParty.getParty(p) != null) minFreeSlots = GlobalParty.getParty(p).members.size();
            info = ChestAPI.getBestServer(group,minFreeSlots, ServerState.LOBBY, ServerSortMethod.GET_FULLEST_SERVER);
        }

        if(info != null){
            u.connect(info.getName());
        } else {
            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Could not find an appropriate game server!"));
            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Please try again later."));
        }
    }

    private static void quickJoin(Player p, GameType type){
        ChestUser u = ChestUser.getUser(p);

        if(type == GameType.SG_DUELS){
            ChestAPI.executeBungeeCommand("BungeeConsole","queuemanager join SGDuels " + p.getName());
        } else {
            switch(type){
                case SURVIVAL_GAMES:
                    a(p,"SurvivalGames",true);
                    break;
                case MUSICAL_GUESS:
                    a(p,"MusicalGuess",true);
                    break;
                case KITPVP:
                    a(p,"KitPvP",false);
                    break;
                case BUILD_AND_GUESS:
                    a(p,"BuildAndGuess",true);
                    break;
                case TOBIKO:
                    a(p,"Tobiko",true);
                    break;
            }
        }
    }

    private static void handleClick(Player p, ClickType clickType, GameType type){
        if(clickType == ClickType.LEFT){
            p.closeInventory();
            teleport(p,type);
        } else if(clickType == ClickType.RIGHT){
            p.closeInventory();
            quickJoin(p,type);
        } else if(clickType == ClickType.MIDDLE){
            SpectateMenu.openFor(p,type);
        }
    }

    private static ItemStack s(ChestUser u, Material icon, int iconDurability, String name, String category, String text){
        ItemStack sg = new ItemStack(icon);
        sg.setDurability((short)iconDurability);
        ItemMeta sgM = sg.getItemMeta();
        ArrayList<String> sgL = new ArrayList<String>();
        sgM.setDisplayName(name);
        sgL.add(ChatColor.DARK_GRAY + u.getTranslatedMessage(category));
        sgL.add(" ");
        for(String s : StringUtils.getWordWrapLore(u.getTranslatedMessage(text))){
            sgL.add(ChatColor.GRAY + s);
        }
        sgL.add("  ");
        if(icon == Material.SNOW_BALL){
            sgL.add(ChatColor.GREEN + ">" + ChatColor.DARK_GREEN + ">" + ChatColor.GREEN + "> " + ChatColor.YELLOW + u.getTranslatedMessage("Left-click to teleport"));
            sgL.add(ChatColor.GREEN + ">" + ChatColor.DARK_GREEN + ">" + ChatColor.GREEN + "> " + ChatColor.AQUA + u.getTranslatedMessage("Middle-click to spectate"));
        } else {
            sgL.add(ChatColor.GREEN + ">" + ChatColor.DARK_GREEN + ">" + ChatColor.GREEN + "> " + ChatColor.YELLOW + u.getTranslatedMessage("Left-click to teleport"));

            if(icon == Material.CHEST || icon == Material.DIAMOND_HOE){
                sgL.add(ChatColor.GREEN + ">" + ChatColor.DARK_GREEN + ">" + ChatColor.GREEN + "> " + ChatColor.AQUA + u.getTranslatedMessage("Middle-click to spectate"));
            }

            sgL.add(ChatColor.GREEN + ">" + ChatColor.DARK_GREEN + ">" + ChatColor.GREEN + "> " + ChatColor.GOLD + u.getTranslatedMessage("Right-click to quick join"));
        }
        sgM.setLore(sgL);
        sg.setItemMeta(sgM);
        sg = ItemUtil.hideFlags(sg);
        return sg;
    }

    public static void openFor(Player p){
        ChestUser u = ChestUser.getUser(p);
        InventoryMenuBuilder inv = new InventoryMenuBuilder(ChestAPI.MAX_INVENTORY_SIZE);
        inv.withTitle(u.getTranslatedMessage("Game Modes"));

        ItemStack pl = ItemUtil.namedItem(Material.STAINED_GLASS_PANE, " ", null, 15);
        ItemStack gamemodeMenu = ItemUtil.namedItem(Material.EMERALD, ChatColor.YELLOW + u.getTranslatedMessage("Game Modes"),null);
        ItemStack lobbyMenu = ItemUtil.namedItem(Material.ENDER_PEARL, ChatColor.YELLOW + u.getTranslatedMessage("Lobby Selector"),null);

        ItemStack spawn = ItemUtil.namedItem(Material.SLIME_BALL,ChatColor.AQUA.toString() + ChatColor.BOLD.toString() + u.getTranslatedMessage("Spawn"),null);
        ItemStack bg = s(u,Material.BOOK_AND_QUILL,0,ChatColor.DARK_PURPLE + "Build & Guess","Casual Games","Reviving this gamemode back in Minecraft, one player has to build and the others have to guess. The player who gains the most points wins!");
        ItemStack tobiko = s(u,Material.DIAMOND_HOE,0,ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + u.getTranslatedMessage("NEW!") + " " + ChatColor.LIGHT_PURPLE + "Tobiko","Casual Games","One Tobiko against 12 survivors. Can the flying, fire-shooting Tobiko knock the Survivors into the void, or can the Survivors kill the Tobiko first?");
        ItemStack mg = s(u,Material.RECORD_7,0,ChatColor.BLUE + "Musical Guess","Casual Games","Can you guess what song the noteblocks are playing? The player who gains the most points wins!");
        ItemStack sgd = s(u,Material.ENDER_CHEST,0,ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + u.getTranslatedMessage("NEW!") + " " + ChatColor.DARK_RED + "Survival Games: " + ChatColor.YELLOW + "Duels","Competitive","The classic Survival Games in a 1 on 1 battle mode. Climb through the leaderboards in Ranked, or battle your friends in Unranked.");
        ItemStack sg = s(u,Material.CHEST,0,ChatColor.DARK_RED + "Survival Games","Survival","24 players are spawned without anything in an apocalyptic scenario with only one goal: be the last survivor! Use the weapons and items you get from the chest spawned randomly on the map to reach this goal! Only one person can be the winner!");
        ItemStack kpvp = s(u,Material.DIAMOND_SWORD,0,ChatColor.YELLOW + "KitPvP","Competitive","Joining this free-for-all combat mode will bring you a new experience of classic PvP! Select a kit and use abilities like teleporting behind other or jumping in the air and smashing players around.");
        ItemStack soccer = s(u,Material.SNOW_BALL,0,ChatColor.AQUA + "SoccerMC","Competitive","Bringing back Soccer in Minecraft! Play either alone or with your friends.");

        inv.withItem(13,spawn, ((player, clickType, itemStack) -> { teleport(p,null); }), ClickType.LEFT);
        inv.withItem(19,bg, (((player, clickType, itemStack) -> { handleClick(player,clickType,GameType.BUILD_AND_GUESS); })), ClickType.LEFT, ClickType.RIGHT);
        inv.withItem(20,tobiko, (((player, clickType, itemStack) -> { handleClick(player,clickType,GameType.TOBIKO); })), ClickType.LEFT, ClickType.RIGHT, ClickType.MIDDLE);
        inv.withItem(21,mg, (((player, clickType, itemStack) -> { handleClick(player,clickType,GameType.MUSICAL_GUESS); })), ClickType.LEFT, ClickType.RIGHT);
        inv.withItem(22,sgd, (((player, clickType, itemStack) -> { handleClick(player,clickType,GameType.SG_DUELS); })), ClickType.LEFT, ClickType.RIGHT);
        inv.withItem(23,sg, (((player, clickType, itemStack) -> { handleClick(player,clickType,GameType.SURVIVAL_GAMES); })), ClickType.LEFT, ClickType.RIGHT, ClickType.MIDDLE);
        inv.withItem(24,kpvp, (((player, clickType, itemStack) -> { handleClick(player,clickType,GameType.KITPVP); })), ClickType.LEFT, ClickType.RIGHT);
        inv.withItem(25,soccer, (((player, clickType, itemStack) -> { handleClick(p,clickType,GameType.SOCCER); })), ClickType.LEFT, ClickType.MIDDLE);

        inv.withItem(36, pl);
        inv.withItem(37, pl);
        inv.withItem(38, pl);
        inv.withItem(39, pl);
        inv.withItem(40, pl);
        inv.withItem(41, pl);
        inv.withItem(42, pl);
        inv.withItem(43, pl);
        inv.withItem(44, pl);

        inv.withItem(48,ItemUtil.addGlow(gamemodeMenu));
        inv.withItem(50,lobbyMenu, ((player, clickType, itemStack) -> { LobbySelectorMenu.openFor(p); }), ClickType.LEFT);

        inv.show(p);
    }
}
