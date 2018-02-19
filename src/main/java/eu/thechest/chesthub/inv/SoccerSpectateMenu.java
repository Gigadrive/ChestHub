package eu.thechest.chesthub.inv;

import de.dytanic.cloudnet.api.CloudNetAPI;
import eu.thechest.chestapi.ChestAPI;
import eu.thechest.chestapi.items.ItemUtil;
import eu.thechest.chestapi.maps.Map;
import eu.thechest.chestapi.mysql.MySQLManager;
import eu.thechest.chestapi.server.ServerSettingsManager;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.util.PlayerUtilities;
import eu.thechest.chesthub.ChestHub;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

public class SoccerSpectateMenu {
    private static ArrayList<Game> GAMES;
    public static ArrayList<Game> getGames(){
        if(GAMES == null){
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `soccer_upcomingGames` WHERE `team1` IS NOT NULL AND `team2` IS NOT NULL AND `status` IS NOT NULL AND `status` != ? AND `status` != ? AND `status` != ? AND `server` IS NOT NULL AND `map` != ? ORDER BY `time`");
                ps.setString(1,"PREPARING");
                ps.setString(2,"ENDING");
                ps.setString(3,"CANCELLED");
                ps.setInt(4,0);
                ResultSet rs = ps.executeQuery();

                if(rs.first()){
                    GAMES = new ArrayList<Game>();
                    rs.beforeFirst();

                    while(rs.next()){
                        ArrayList<UUID> team1 = new ArrayList<UUID>();
                        ArrayList<UUID> team2 = new ArrayList<UUID>();

                        for(String s : rs.getString("team1").split(",")) team1.add(UUID.fromString(s));
                        for(String s : rs.getString("team2").split(",")) team2.add(UUID.fromString(s));

                        GAMES.add(new Game(rs.getInt("id"),team1,team2,rs.getString("status"),rs.getString("server"),rs.getInt("map")));
                    }
                } else {
                    GAMES = new ArrayList<Game>();
                }

                MySQLManager.getInstance().closeResources(rs,ps);

                new BukkitRunnable(){
                    @Override
                    public void run() {
                        GAMES = null;
                    }
                }.runTaskLater(ChestHub.getInstance(),10*20);
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        return GAMES;
    }

    public static void openFor(Player p){
        openFor(p,1);
    }

    public static void openFor(Player p, int page){
        ChestUser u = ChestUser.getUser(p);
        InventoryMenuBuilder inv = new InventoryMenuBuilder(ChestAPI.MAX_INVENTORY_SIZE);
        inv.withTitle(u.getTranslatedMessage("Select a game"));

        ArrayList<Game> games = getGames();
        int sizePerPage = 36;
        int total = games.size();

        if(total > 0){
            int slot = 0;

            for(Game g : games.stream().skip((page-1) * sizePerPage).limit(sizePerPage).collect(Collectors.toCollection(ArrayList::new))){
                ItemStack item = new ItemStack(Material.EYE_OF_ENDER);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName(ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "SoccerGame-" + g.id);
                ArrayList<String> itemLore = new ArrayList<String>();
                itemLore.add(ChatColor.AQUA + u.getTranslatedMessage("Map") + ": " + ChatColor.WHITE + g.map.getName());
                itemLore.add("  ");
                for(UUID uuid : g.team1) itemLore.add(ChatColor.RED + PlayerUtilities.getNameFromUUID(uuid));
                itemLore.add(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "---" + ChatColor.GREEN + " vs " + ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "---");
                for(UUID uuid : g.team2) itemLore.add(ChatColor.BLUE + PlayerUtilities.getNameFromUUID(uuid));
                itemLore.add(" ");
                itemLore.add(ChatColor.YELLOW + u.getTranslatedMessage("Click to spectate!"));

                itemMeta.setLore(itemLore);
                item.setItemMeta(itemMeta);

                inv.withItem(slot, item, ((player, clickType, itemStack) -> {
                    String server = g.server;
                    if(CloudNetAPI.getInstance().getServerInfo(server) != null){
                        p.closeInventory();

                        ChestAPI.async(() -> {
                            try {
                                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("DELETE FROM `soccer_spectate` WHERE `uuid` = ?");
                                ps.setString(1,p.getUniqueId().toString());
                                ps.executeUpdate();
                                ps.close();

                                ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `soccer_spectate` (`uuid`,`game`) VALUES(?,?);");
                                ps.setString(1,p.getUniqueId().toString());
                                ps.setInt(2,g.id);
                                ps.executeUpdate();
                                ps.close();

                                u.connect(g.server);
                            } catch(Exception e){
                                e.printStackTrace();
                                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("An error occurred."));
                            }
                        });
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

        if(page != 1) inv.withItem(47,ItemUtil.namedItem(Material.ARROW, org.bukkit.ChatColor.GOLD + "<< " + org.bukkit.ChatColor.AQUA + u.getTranslatedMessage("Previous page"), null), ((player, clickType, itemStack) -> openFor(player,page-1)), ClickType.LEFT);
        inv.withItem(49, ItemUtil.namedItem(Material.BARRIER, org.bukkit.ChatColor.DARK_RED + u.getTranslatedMessage("Close"), null), (player, clickType, itemStack) -> p.closeInventory(), ClickType.LEFT);
        if(maxPages > page) inv.withItem(51,ItemUtil.namedItem(Material.ARROW, org.bukkit.ChatColor.AQUA + u.getTranslatedMessage("Next page") + org.bukkit.ChatColor.GOLD + " >>", null), (player, clickType, itemStack) -> openFor(player,page+1), ClickType.LEFT);

        inv.show(p);
    }

    public static class Game {
        public int id;
        public ArrayList<UUID> team1;
        public ArrayList<UUID> team2;
        public String status;
        public String server;
        public Map map;

        public Game(int id, ArrayList<UUID> team1, ArrayList<UUID> team2, String status, String server, int map){
            this.id = id;
            this.team1 = team1;
            this.team2 = team2;
            this.status = status;
            this.server = server;
            this.map = Map.getMap(map);
        }
    }
}
