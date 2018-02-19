package eu.thechest.chesthub.inv;

import de.dytanic.cloudnet.api.CloudNetAPI;
import de.dytanic.cloudnet.network.ServerInfo;
import de.dytanic.cloudnet.servergroup.ServerState;
import eu.thechest.chestapi.ChestAPI;
import eu.thechest.chestapi.items.ItemUtil;
import eu.thechest.chestapi.server.GameType;
import eu.thechest.chestapi.server.ServerSettingsManager;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.user.Rank;
import eu.thechest.chesthub.ChestHub;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class SpectateMenu {
    private static HashMap<GameType,ArrayList<Game>> GAMES = new HashMap<GameType,ArrayList<Game>>();

    public static ArrayList<Game> getGames(GameType type){
        ArrayList<Game> games = new ArrayList<Game>();

        if(GAMES.containsKey(type)){
            games = GAMES.get(type);
        } else {
            for(ServerInfo info : CloudNetAPI.getInstance().getServers().values()){
                if(info.getGroup().equals(type.getServerGroupName())){
                    if(info.getServerState() == ServerState.INGAME){
                        games.add(new Game(info.getName(),info.getOnlineCount(),info.getMaxPlayers(),info.getMotd(),info.getServerState()));
                    }
                }
            }

            GAMES.put(type,games);

            new BukkitRunnable(){
                @Override
                public void run() {
                    GAMES.remove(type);
                }
            }.runTaskLater(ChestHub.getInstance(),10*20);
        }

        return games;
    }

    public static void openFor(Player p, GameType type){
        openFor(p,type,1);
    }

    public static void openFor(Player p, GameType type, int page){
        if(type == GameType.SOCCER){
            SoccerSpectateMenu.openFor(p,page);
            return;
        }

        ChestUser u = ChestUser.getUser(p);

        InventoryMenuBuilder inv = new InventoryMenuBuilder(ChestAPI.MAX_INVENTORY_SIZE);
        inv.withTitle(u.getTranslatedMessage("Select a game"));

        ArrayList<Game> games = getGames(type);
        int sizePerPage = 36;
        int total = games.size();

        if(total > 0) {
            int slot = 0;

            for (Game g : games.stream().skip((page - 1) * sizePerPage).limit(sizePerPage).collect(Collectors.toCollection(ArrayList::new))) {
                ItemStack item = new ItemStack(Material.EYE_OF_ENDER);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName(ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + g.server);
                ArrayList<String> itemLore = new ArrayList<String>();
                itemLore.add(ChatColor.AQUA + u.getTranslatedMessage("Game") + ": " + ChatColor.WHITE + type.getName());
                itemLore.add(ChatColor.AQUA + u.getTranslatedMessage("Map") + ": " + ChatColor.WHITE + g.map);
                itemLore.add(ChatColor.AQUA + u.getTranslatedMessage("Status") + ": " + ChatColor.RED + g.state.toString());
                itemLore.add(ChatColor.AQUA + u.getTranslatedMessage("Players") + ": " + ChatColor.WHITE + g.online + "/" + g.max);
                itemLore.add(" ");
                itemLore.add(ChatColor.YELLOW + u.getTranslatedMessage("Click to spectate!"));

                itemMeta.setLore(itemLore);
                item.setItemMeta(itemMeta);

                inv.withItem(slot, item, ((player, clickType, itemStack) -> {
                    String server = g.server;
                    if(CloudNetAPI.getInstance().getServerInfo(server) != null) {
                        if(g.online < g.max && !u.hasPermission(Rank.MOD)){
                            p.closeInventory();

                            p.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + u.getTranslatedMessage("Connecting.."));
                            u.connect(g.server);
                        } else {
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("That server is full."));
                        }
                    } else {
                        p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("An error occurred."));
                    }
                }), ClickType.LEFT);

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

        if(page != 1) inv.withItem(47,ItemUtil.namedItem(Material.ARROW, org.bukkit.ChatColor.GOLD + "<< " + org.bukkit.ChatColor.AQUA + u.getTranslatedMessage("Previous page"), null), ((player, clickType, itemStack) -> openFor(player,type, page-1)), ClickType.LEFT);
        inv.withItem(49, ItemUtil.namedItem(Material.BARRIER, org.bukkit.ChatColor.DARK_RED + u.getTranslatedMessage("Close"), null), (player, clickType, itemStack) -> GamemodesMenu.openFor(p), ClickType.LEFT);
        if(maxPages > page) inv.withItem(51,ItemUtil.namedItem(Material.ARROW, org.bukkit.ChatColor.AQUA + u.getTranslatedMessage("Next page") + org.bukkit.ChatColor.GOLD + " >>", null), (player, clickType, itemStack) -> openFor(player,type, page+1), ClickType.LEFT);

        inv.show(p);
    }

    public static class Game {
        public String server;
        public int online;
        public int max;
        public String map;
        public ServerState state;

        public Game(String server, int online, int max, String map, ServerState state){
            this.server = server;
            this.online = online;
            this.max = max;
            this.map = map;
            this.state = state;
        }
    }
}
