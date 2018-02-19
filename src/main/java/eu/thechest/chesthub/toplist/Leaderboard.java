package eu.thechest.chesthub.toplist;

import eu.thechest.chestapi.ChestAPI;
import eu.thechest.chestapi.lang.TranslatedHoloLine;
import eu.thechest.chestapi.lang.TranslatedHologram;
import eu.thechest.chestapi.mysql.MySQLManager;
import eu.thechest.chestapi.server.GameType;
import eu.thechest.chestapi.user.Rank;
import eu.thechest.chestapi.util.CrewTagData;
import eu.thechest.chestapi.util.PlayerUtilities;
import eu.thechest.chesthub.ChestHub;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.UUID;

@Deprecated
public class Leaderboard {
    public static ArrayList<Leaderboard> STORAGE = new ArrayList<Leaderboard>();

    public static void startUpdateTask(){
        Bukkit.getScheduler().scheduleSyncRepeatingTask(ChestHub.getInstance(), new Runnable(){
            @Override
            public void run() {
                for(Leaderboard l : STORAGE) l.update();
            }
        },0,5*60*20);
    }

    private GameType gameType;
    private String tableName;
    private String points;
    private Location location;
    private TranslatedHologram holo;

    public Leaderboard(GameType gameType, String tableName, String points, Location location){
        this.gameType = gameType;
        this.tableName = tableName;
        this.points = points;
        this.location = location;
    }

    public GameType getGameType() {
        return gameType;
    }

    public String getTableName() {
        return tableName;
    }

    public String getPoints() {
        return points;
    }

    public Location getLocation() {
        return location;
    }

    public TranslatedHologram getHolo() {
        return holo;
    }

    public void update(){
        if(holo != null) holo.unregister();

        ArrayList<TranslatedHoloLine> lines = new ArrayList<TranslatedHoloLine>();

        if(gameType == GameType.SURVIVAL_GAMES){
            lines.add(new TranslatedHoloLine(new ItemStack(Material.CHEST)));
        } else if(gameType == GameType.KITPVP){
            lines.add(new TranslatedHoloLine(new ItemStack(Material.DIAMOND_SWORD)));
        } else if(gameType == GameType.MUSICAL_GUESS){
            lines.add(new TranslatedHoloLine(new ItemStack(Material.RECORD_7)));
        } else if(gameType == GameType.SOCCER){
            lines.add(new TranslatedHoloLine(new ItemStack(Material.SNOW_BALL)));
        } else if(gameType == GameType.BUILD_AND_GUESS){
            lines.add(new TranslatedHoloLine(new ItemStack(Material.BOOK_AND_QUILL)));
        }

        lines.add(new TranslatedHoloLine(gameType.getColor() + gameType.getName() + " Leaderboard"));
        lines.add(new TranslatedHoloLine(""));

        int i = 1;

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT s.uuid,u.username,u.rank,s." + points + " FROM `" + tableName + "` AS s INNER JOIN `users` AS u ON s.uuid = u.uuid ORDER BY s." + points + " DESC LIMIT 10");
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                Rank r = Rank.valueOf(rs.getString("rank"));
                String username = rs.getString("username");
                int points = rs.getInt(this.points);
                UUID uuid = UUID.fromString(rs.getString("uuid"));

                String s = ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + i + ".";
                s = s + " ";
                s = s + r.getColor() + username;

                CrewTagData ct = PlayerUtilities.getCrewTagFromUUID(uuid);
                if(ct != null && ct.tag != null){
                    s = s + " ";
                    s = s + ChatColor.GRAY + "[" + ChatColor.YELLOW;
                    s = s + ct.tag;
                    s = s + ChatColor.GRAY + "]";
                }

                s = s + " " + ChatColor.DARK_GREEN + "-" + " " + ChatColor.GREEN.toString() + points + ChatColor.RESET;

                lines.add(new TranslatedHoloLine(s));
                i++;
            }

            MySQLManager.getInstance().closeResources(rs,ps);

            holo = new TranslatedHologram(lines.toArray(new TranslatedHoloLine[]{}),location);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
