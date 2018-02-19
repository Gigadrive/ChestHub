package eu.thechest.chesthub;

import de.dytanic.cloudnet.CloudNetwork;
import de.dytanic.cloudnet.api.CloudNetAPI;
import de.dytanic.cloudnet.network.ServerInfo;
import eu.thechest.chestapi.ChestAPI;
import eu.thechest.chestapi.mysql.MySQLManager;
import eu.thechest.chestapi.server.GameType;
import eu.thechest.chestapi.server.ServerSettingsManager;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.user.GlobalParty;
import eu.thechest.chestapi.user.Rank;
import eu.thechest.chestapi.util.PlayerUtilities;
import eu.thechest.chesthub.inv.ChallengerMenu;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class ChallengerRequest {
    public static ArrayList<ChallengerRequest> STORAGE = new ArrayList<ChallengerRequest>();

    public static ChallengerRequest getRequest(Player from, Player to){
        for(ChallengerRequest request : STORAGE){
            if(request.from == from && request.to == to){
                return request;
            }
        }

        return null;
    }

    public Player from;
    public Player to;
    private GameType game;

    public ChallengerRequest(Player from, Player to, GameType game){
        this.from = from;
        this.to = to;
        this.game = game;
    }

    public void accept(){
        Player p = to;
        ChestUser u = ChestUser.getUser(p);

        Player p2 = from;
        ChestUser u2 = ChestUser.getUser(p2);

        if(u.hasPermission(Rank.VIP)){
            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("You have accepted %p's challenge.").replace("%p",u2.getRank().getColor() + p2.getName() + ChatColor.GREEN));
        } else {
            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("You have accepted %p's challenge.").replace("%p",p2.getDisplayName() + ChatColor.GREEN));
        }

        if(u2.hasPermission(Rank.VIP)){
            p2.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u2.getTranslatedMessage("%p has accepted your challenge.").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.GREEN));
        } else {
            p2.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u2.getTranslatedMessage("%p has accepted your challenge.").replace("%p",p.getDisplayName() + ChatColor.GREEN));
        }
        p2.playSound(p2.getEyeLocation(), Sound.NOTE_PLING,1f,1f);

        if(game == GameType.SG_DUELS){
            ChestAPI.async(() -> {
                try {
                    String server = sgDuelsServer();

                    if(server != null && !server.isEmpty()){
                        PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `sgduels_upcomingGames` (`team1`,`team2`,`ranked`) VALUES(?,?,?);");
                        ps.setString(1,p.getUniqueId().toString());
                        ps.setString(2,p2.getUniqueId().toString());
                        ps.setBoolean(3,true);
                        ps.executeUpdate();
                        ps.close();

                        u.connect(server);
                        u2.connect(server);
                    } else {
                        p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Could not find an appropriate game server!"));
                        p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Please try again later."));

                        p2.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u2.getTranslatedMessage("Could not find an appropriate game server!"));
                        p2.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u2.getTranslatedMessage("Please try again later."));
                    }
                } catch(Exception e){
                    e.printStackTrace();

                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("An error occured."));
                    p2.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u2.getTranslatedMessage("An error occured."));
                }
            });
        } else if(game == GameType.SOCCER){
            if((GlobalParty.getParty(p) == null && GlobalParty.getParty(p2) == null) || ChallengerMenu.isPartyCompatibleSoccer(p,p2)){
                String team1 = null;
                String team2 = null;

                if(GlobalParty.getParty(p) == null){
                    team1 = p.getUniqueId().toString();
                } else {
                    for(UUID uuid : GlobalParty.getParty(p).members){
                        if(team1 == null){
                            team1 = uuid.toString();
                        } else {
                            team1 += "," + uuid.toString();
                        }
                    }
                }

                if(GlobalParty.getParty(p2) == null){
                    team2 = p2.getUniqueId().toString();
                } else {
                    for(UUID uuid : GlobalParty.getParty(p2).members){
                        if(team2 == null){
                            team2 = uuid.toString();
                        } else {
                            team2 += "," + uuid.toString();
                        }
                    }
                }

                int minSize = team1.split(",").length;

                final String t1 = team1;
                final String t2 = team2;

                ChestAPI.async(() -> {
                    try {
                        String server = soccerServer(minSize);

                        if(server != null && !server.isEmpty()){
                            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `soccer_upcomingGames` (`team1`,`team2`) VALUES(?,?);");
                            ps.setString(1,t1);
                            ps.setString(2,t2);
                            ps.executeUpdate();
                            ps.close();

                            ArrayList<String> a = new ArrayList<String>();
                            a.addAll(Arrays.asList(t1.split(",")));
                            a.addAll(Arrays.asList(t2.split(",")));

                            for(String s : a) ChestAPI.sendPlayerToServer(PlayerUtilities.getNameFromUUID(UUID.fromString(s)),server);
                        } else {
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Could not find an appropriate game server!"));
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Please try again later."));

                            p2.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u2.getTranslatedMessage("Could not find an appropriate game server!"));
                            p2.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u2.getTranslatedMessage("Please try again later."));
                        }
                    } catch(Exception e){
                        e.printStackTrace();

                        p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("An error occured."));
                        p2.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u2.getTranslatedMessage("An error occured."));
                    }
                });
            } else {
                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("In order to create a party soccer match, your parties have to be of the same size and have a maximum of 4 players in them."));
                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Additionally, the party leaders have to challenge each other."));
            }
        }

        ChallengerRequest.STORAGE.remove(this);
    }

    private String sgDuelsServer() {
        ServerInfo bestServer = ChestAPI.getBestServer("SGDuels",2);
        if(bestServer == null){
            return null;
        } else {
            return bestServer.getName();
        }

        /*String s = null;

        PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT s.name,(s.`players.max`-s.`players.online`) AS `left` FROM `servers` AS s WHERE s.name LIKE 'SGDUELS%' ORDER BY `left` DESC LIMIT 1");
        ResultSet rs = ps.executeQuery();

        if(rs.first()){
            if(rs.getInt("left") >= 2) s = rs.getString("name");
        }

        MySQLManager.getInstance().closeResources(rs,ps);

        return s;*/
    }

    private String soccerServer(int minSize) throws SQLException {
        ServerInfo bestServer = ChestAPI.getBestServer("SoccerMC",minSize);
        if(bestServer == null){
            return null;
        } else {
            return bestServer.getName();
        }

        /*String s = null;

        PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT s.name,(s.`players.max`-s.`players.online`) AS `left` FROM `servers` AS s WHERE s.name LIKE 'SOCCER%' ORDER BY `left` DESC LIMIT 1");
        ResultSet rs = ps.executeQuery();

        if(rs.first()){
            if(rs.getInt("left") >= 2) s = rs.getString("name");
        }

        MySQLManager.getInstance().closeResources(rs,ps);

        return s;*/
    }

    public void retract(){
        Player p = from;
        ChestUser u = ChestUser.getUser(p);

        Player p2 = to;
        ChestUser u2 = ChestUser.getUser(p2);

        p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You have retracted your challenge."));
        if(u2.hasPermission(Rank.VIP)){
            p2.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u2.getTranslatedMessage("%p has retracted their challenge.").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.RED));
        } else {
            p2.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u2.getTranslatedMessage("%p has retracted their challenge.").replace("%p",p.getDisplayName() + ChatColor.RED));
        }

        ChallengerRequest.STORAGE.remove(this);
    }
}
