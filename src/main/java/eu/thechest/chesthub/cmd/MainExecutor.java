package eu.thechest.chesthub.cmd;

import com.dsh105.echopet.libs.captainbern.protocol.PacketType;
import eu.thechest.chestapi.ChestAPI;
import eu.thechest.chestapi.items.ItemCategory;
import eu.thechest.chestapi.items.VaultItem;
import eu.thechest.chestapi.server.ServerSettingsManager;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.user.Rank;
import eu.thechest.chestapi.util.StringUtils;
import eu.thechest.chesthub.ChestHub;
import eu.thechest.chesthub.LocationManager;
import eu.thechest.chesthub.inv.LevelRewardsMenu;
import eu.thechest.chesthub.inv.ResetTokensMenu;
import eu.thechest.chesthub.inv.StatisticsMenu;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zeryt on 11.02.2017.
 */
public class MainExecutor implements CommandExecutor {
    public static ArrayList<String> PETNAME_COOLDOWN = new ArrayList<String>();
    public static ArrayList<String> STATS_COOLDOWN = new ArrayList<String>();
    public static HashMap<Player,String> TOPLIST_CACHE = new HashMap<Player,String>();

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("setlocation")){
            if(sender instanceof Player){
                Player p = (Player)sender;
                ChestUser u = ChestUser.getUser(p);

                if(u.hasPermission(Rank.ADMIN)){
                    if(args.length == 0){
                        p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + "/" + label + " <Location ID> [here]");
                    } else {
                        if(ChestHub.SETLOCATION.containsKey(p)){
                            ChestHub.SETLOCATION.remove(p);
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Operation aborted."));
                        } else {
                            String id = args[0];
                            boolean here = false;
                            if(args.length >= 2 && args[1].equalsIgnoreCase("here")) here = true;

                            if(here){
                                LocationManager.setLocation(id,p.getLocation(),p.getUniqueId());
                                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Location defined."));
                            } else {
                                ChestHub.SETLOCATION.put(p,id);
                                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Please right-click a block to define the location."));
                                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Use /setlocation again to leave binding mode."));
                            }
                        }
                    }
                } else {
                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You aren't permitted to execute this command."));
                }
            } else {
                sender.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + "You must be a player to do this!");
            }
        }

        if(cmd.getName().equalsIgnoreCase("levelrewards")){
            if(sender instanceof Player){
                Player p = (Player)sender;
                ChestUser u = ChestUser.getUser(p);

                LevelRewardsMenu.openFor(p);
            } else {
                sender.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + "You must be a player to do this!");
            }
        }

        if(cmd.getName().equalsIgnoreCase("resettokens")){
            if(sender instanceof Player){
                Player p = (Player)sender;
                ChestUser u = ChestUser.getUser(p);

                ResetTokensMenu.openFor(p);
            } else {
                sender.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + "You must be a player to do this!");
            }
        }

        if(cmd.getName().equalsIgnoreCase("stats")){
            if(sender instanceof Player){
                Player p = (Player)sender;
                ChestUser u = ChestUser.getUser(p);

                if(args.length == 0){
                    p.performCommand("stats " + p.getName());
                } else {
                    if(STATS_COOLDOWN.contains(p.getName())){
                        p.closeInventory();
                        p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Please wait a little bit before executing this command again."));
                    } else {
                        StatisticsMenu.openFor(p,args[0]);
                        STATS_COOLDOWN.add(p.getName());

                        Bukkit.getScheduler().scheduleSyncDelayedTask(ChestHub.getInstance(),new Runnable(){
                            @Override
                            public void run() {
                                STATS_COOLDOWN.remove(p.getName());
                            }
                        },5*10);
                    }
                }
            } else {
                sender.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + "You must be a player to do this!");
            }
        }

        if(cmd.getName().equalsIgnoreCase("petname")){
            if(sender instanceof Player){
                ChestAPI.async(() -> {
                    Player p = (Player)sender;
                    ChestUser u = ChestUser.getUser(p);
                    VaultItem pet = u.getActiveItem(ItemCategory.PETS);

                    if(u.hasPermission(Rank.PRO_PLUS)){
                        if(PETNAME_COOLDOWN.contains(p.getName())){
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Please wait a little bit before executing this command again."));
                        } else {
                            if(pet != null){
                                if(args.length == 0){
                                    ChestHub.resetPetName(p,pet);
                                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Your pet's name has been cleared."));
                                    ChestHub.getInstance().updatePet(p);
                                    PETNAME_COOLDOWN.add(p.getName());
                                    new BukkitRunnable(){
                                        @Override
                                        public void run() {
                                            PETNAME_COOLDOWN.remove(p.getName());
                                        }
                                    }.runTaskLater(ChestHub.getInstance(),5*20);
                                } else {
                                    StringBuilder sb = new StringBuilder("");
                                    for (int i = 0; i < args.length; i++) {
                                        sb.append(args[i]).append(" ");
                                    }
                                    String s = StringUtils.limitString(sb.toString().trim(),20);

                                    ChestHub.setPetName(p,pet,s);
                                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Your pet's name has been updated."));
                                    ChestHub.getInstance().updatePet(p);
                                    PETNAME_COOLDOWN.add(p.getName());
                                    new BukkitRunnable(){
                                        @Override
                                        public void run() {
                                            PETNAME_COOLDOWN.remove(p.getName());
                                        }
                                    }.runTaskLater(ChestHub.getInstance(),5*20);
                                }
                            } else {
                                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You have to spawn a pet in order to name it!"));
                            }
                        }
                    } else {
                        p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You aren't permitted to execute this command."));
                    }
                });
            } else {
                sender.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + "You must be a player to do this!");
            }
        }

        return false;
    }
}
