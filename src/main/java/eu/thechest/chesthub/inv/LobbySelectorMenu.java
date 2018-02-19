package eu.thechest.chesthub.inv;

import de.dytanic.cloudnet.api.CloudNetAPI;
import de.dytanic.cloudnet.network.ServerInfo;
import eu.thechest.chestapi.items.ItemUtil;
import eu.thechest.chestapi.server.ServerSettingsManager;
import eu.thechest.chestapi.server.ServerUtil;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.user.Rank;
import eu.thechest.chesthub.ChestHub;
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
import org.bukkit.scheduler.BukkitRunnable;

import javax.persistence.Lob;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

public class LobbySelectorMenu implements Listener {
    private static ArrayList<LobbyData> PREMIUM_LOBBIES = null;
    private static ArrayList<LobbyData> USER_LOBBIES = null;

    public static ArrayList<LobbyData> getLobbies(){
        fetchData();

        ArrayList<LobbyData> a = new ArrayList<LobbyData>();
        a.addAll(PREMIUM_LOBBIES);
        a.addAll(USER_LOBBIES);
        return a;
    }

    public static LobbyData getLobbyData(String serverName){
        for(LobbyData d : getLobbies()){
            if(d.serverName.equalsIgnoreCase(serverName)) return d;
        }

        return null;
    }

    private static void fetchData(){
        if(PREMIUM_LOBBIES == null || USER_LOBBIES == null){
            PREMIUM_LOBBIES = new ArrayList<LobbyData>();
            USER_LOBBIES = new ArrayList<LobbyData>();

            for(ServerInfo info : CloudNetAPI.getInstance().getCloudNetwork().getServers().values()){
                if(info.getGroup().equalsIgnoreCase("PremiumLobby")){
                    PREMIUM_LOBBIES.add(new LobbyData(true,info.getName(),info.getOnlineCount(),info.getMaxPlayers()));
                } else if(info.getGroup().equalsIgnoreCase("Lobby")){
                    USER_LOBBIES.add(new LobbyData(false,info.getName(),info.getOnlineCount(),info.getMaxPlayers()));
                }
            }

            Collections.sort(PREMIUM_LOBBIES, new Comparator<LobbyData>() {
                public int compare(LobbyData p1, LobbyData p2) {
                    return p1.serverName.compareTo(p2.serverName);
                }
            });

            Collections.sort(USER_LOBBIES, new Comparator<LobbyData>() {
                public int compare(LobbyData p1, LobbyData p2) {
                    return p1.serverName.compareTo(p2.serverName);
                }
            });

            new BukkitRunnable(){
                @Override
                public void run() {
                    PREMIUM_LOBBIES = null;
                    USER_LOBBIES = null;
                }
            }.runTaskLater(ChestHub.getInstance(),10*20);
        }
    }

    public static void openFor(Player p){
        openFor(p,1);
    }

    public static void openFor(Player p, int page){
        ChestUser u = ChestUser.getUser(p);
        Inventory inv = Bukkit.createInventory(null,9*6,"Lobby Selector | " + page);

        int sizePerPage = 36;
        int total = getLobbies().size();

        for(LobbyData a : getLobbies().stream().skip((page-1) * sizePerPage).limit(sizePerPage).collect(Collectors.toCollection(ArrayList::new))){
            ItemStack i = new ItemStack(Material.GLOWSTONE_DUST);
            if(!a.premium) i.setType(Material.SULPHUR);
            ItemMeta iM = i.getItemMeta();
            if(a.premium){
                iM.setDisplayName(ChatColor.GOLD + a.serverName);
            } else {
                iM.setDisplayName(ChatColor.AQUA + a.serverName);
            }
            ArrayList<String> iL = new ArrayList<String>();
            iL.add(ChatColor.GRAY + u.getTranslatedMessage("Players") + ": " + a.onlinePlayers + "/" + a.maxPlayers);
            iL.add(" ");
            if(a.serverName.equalsIgnoreCase(ServerUtil.getServerName())){
                iL.add(ChatColor.GREEN + u.getTranslatedMessage("You are here!"));
            } else {
                iL.add(ChatColor.YELLOW + u.getTranslatedMessage("Click to connect!"));
            }
            iM.setLore(iL);
            i.setItemMeta(iM);
            if(a.serverName.equalsIgnoreCase(ServerUtil.getServerName())){
                inv.addItem(ItemUtil.addGlow(i));
            } else {
                inv.addItem(i);
            }
        }

        ItemStack pl = ItemUtil.namedItem(Material.STAINED_GLASS_PANE, " ", null, 15);
        ItemStack prev = ItemUtil.namedItem(Material.ARROW, ChatColor.GOLD + "<< " + ChatColor.AQUA + u.getTranslatedMessage("Previous page"), null);
        ItemStack next = ItemUtil.namedItem(Material.ARROW, ChatColor.AQUA + u.getTranslatedMessage("Next page") + ChatColor.GOLD + " >>", null);
        ItemStack gamemodeMenu = ItemUtil.namedItem(Material.EMERALD, ChatColor.YELLOW + u.getTranslatedMessage("Game Modes"),null);
        ItemStack lobbyMenu = ItemUtil.namedItem(Material.ENDER_PEARL, ChatColor.YELLOW + u.getTranslatedMessage("Lobby Selector"),null);

        inv.setItem(36, pl);
        inv.setItem(37, pl);
        inv.setItem(38, pl);
        inv.setItem(39, pl);
        inv.setItem(40, pl);
        inv.setItem(41, pl);
        inv.setItem(42, pl);
        inv.setItem(43, pl);
        inv.setItem(44, pl);

        inv.setItem(48,gamemodeMenu);
        inv.setItem(50,ItemUtil.addGlow(lobbyMenu));

        double d = (((double)total)/((double)sizePerPage));
        int maxPages = ((Double)d).intValue();
        if(maxPages < d) maxPages++;

        if(page != 1) inv.setItem(46,prev);
        if(maxPages > page) inv.setItem(52,next);

        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(e.getWhoClicked() instanceof Player){
            Player p = (Player)e.getWhoClicked();
            ChestUser u = ChestUser.getUser(p);
            Inventory inv = e.getInventory();
            int slot = e.getRawSlot();

            if(inv.getName().startsWith("Lobby Selector")){
                e.setCancelled(true);

                if(e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()){
                    String[] aa = inv.getName().split("|");
                    int currentPage = Integer.parseInt(aa[aa.length-1].trim());

                    if(slot >= 0 && slot <= 35){
                        String serverName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
                        LobbyData data = getLobbyData(serverName);

                        if(data != null){
                            if(data.premium && !u.hasPermission(Rank.PRO)){
                                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You need a premium rank to join this lobby!"));
                                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Buy a rank on %l!").replace("%l",ChatColor.YELLOW + "https://thechest.eu/store" + ChatColor.RED));
                                return;
                            }

                            if(data.maxPlayers > data.onlinePlayers){
                                u.connect(serverName);
                            } else {
                                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("That lobby is full!"));
                            }
                        }
                    } else {
                        if(slot == 46){
                            openFor(p,currentPage-1);
                        } else if(slot == 52){
                            openFor(p,currentPage+1);
                        } else if(slot == 48){
                            GamemodesMenu.openFor(p);
                        }
                    }
                }
            }
        }
    }

    public static class LobbyData {
        public boolean premium;
        public String serverName;
        public int onlinePlayers;
        public int maxPlayers;

        public LobbyData(boolean premium, String serverName, int onlinePlayers, int maxPlayers){
            this.premium = premium;
            this.serverName = serverName;
            this.onlinePlayers = onlinePlayers;
            this.maxPlayers = maxPlayers;
        }
    }
}
