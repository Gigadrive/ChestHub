package eu.thechest.chesthub.inv;

import eu.thechest.chestapi.items.ItemUtil;
import eu.thechest.chestapi.server.GameType;
import eu.thechest.chestapi.server.ServerSettingsManager;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.user.GlobalParty;
import eu.thechest.chestapi.user.Rank;
import eu.thechest.chestapi.util.PlayerUtilities;
import eu.thechest.chesthub.ChallengerRequest;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class ChallengerMenu implements Listener {
    public static void openFor(Player p, Player p2){
        ChestUser u = ChestUser.getUser(p);
        Inventory inv = Bukkit.createInventory(null,9, "[CM] " + p2.getName());

        inv.setItem(3, ItemUtil.hideFlags(ItemUtil.namedItem(Material.IRON_SWORD, ChatColor.DARK_RED + "Survival Games: " + ChatColor.YELLOW + "Duels",new String[]{ChatColor.YELLOW + u.getTranslatedMessage("Click to challenge!")})));
        inv.setItem(5, ItemUtil.hideFlags(ItemUtil.namedItem(Material.SNOW_BALL, ChatColor.AQUA + "SoccerMC",new String[]{ChatColor.YELLOW + u.getTranslatedMessage("Click to challenge!")})));

        p.openInventory(inv);
    }

    public static boolean isPartyCompatibleSoccer(Player p, Player p2){
        ChestUser u = ChestUser.getUser(p);
        ChestUser u2 = ChestUser.getUser(p2);

        GlobalParty party1 = GlobalParty.getParty(p);
        GlobalParty party2 = GlobalParty.getParty(p2);

        boolean compatible;

        if(party1 != null && party2 != null){
            if(party1.leader.toString().equals(p.getUniqueId().toString()) && party2.leader.toString().equals(p2.getUniqueId().toString())){
                if(party1.members.size() == party2.members.size() && (party1.members.size() <= 4)){
                    compatible = true;
                } else {
                    compatible = false;
                }
            } else {
                compatible = false;
            }
        } else {
            compatible = false;
        }

        return compatible;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(e.getWhoClicked() instanceof Player){
            Player p = (Player)e.getWhoClicked();
            ChestUser u = ChestUser.getUser(p);
            Inventory inv = e.getInventory();
            int slot = e.getRawSlot();

            if(inv.getName().startsWith("[CM] ")){
                e.setCancelled(true);

                String s = inv.getName().replace("[CM] ","");

                if(s.length() > 0){
                    Player p2 = Bukkit.getPlayer(s);

                    if(p2 != null){
                        ChestUser u2 = ChestUser.getUser(p2);

                        if(e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()){
                            if(ChallengerRequest.getRequest(p,p2) == null){
                                if(ChallengerRequest.getRequest(p2,p) == null){
                                    if(u.hasPermission(Rank.PRO_PLUS) || (!u.hasPermission(Rank.PRO_PLUS) && PlayerUtilities.getFriendsFromUUID(p.getUniqueId()).contains(p2.getUniqueId().toString()))){
                                        if(u.hasPermission(Rank.SR_MOD) || u2.allowsChallengerRequests()){
                                            GameType g = null;

                                            if(slot == 3){
                                                g = GameType.SG_DUELS;
                                            } else if(slot == 5){
                                                g = GameType.SOCCER;
                                            }

                                            if(g != null){
                                                /*if(g == GameType.SOCCER && !u.hasPermission(Rank.VIP)){
                                                    p.closeInventory();
                                                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Soccer is currently in maintenance."));

                                                    return;
                                                }*/

                                                if(g == GameType.SG_DUELS && ((GlobalParty.getParty(p) != null))){
                                                    p.closeInventory();
                                                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Please leave your party."));

                                                    return;
                                                }

                                                if(g == GameType.SOCCER && (GlobalParty.getParty(p) != null || GlobalParty.getParty(p2) != null) && !isPartyCompatibleSoccer(p,p2)){
                                                    p.closeInventory();
                                                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("In order to create a party soccer match, your parties have to be of the same size and have a maximum of 4 players in them."));
                                                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Additionally, the party leaders have to challenge each other."));

                                                    return;
                                                }

                                                ChallengerRequest r = new ChallengerRequest(p,p2,g);
                                                ChallengerRequest.STORAGE.add(r);

                                                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Your request has been sent."));
                                                if(u2.hasPermission(Rank.VIP)){
                                                    p2.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u2.getTranslatedMessage("%p has challenged you in %g.").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.GREEN).replace("%g",g.getColor() + g.getName() + ChatColor.GREEN));
                                                } else {
                                                    p2.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u2.getTranslatedMessage("%p has challenged you in %g.").replace("%p",p.getDisplayName() + ChatColor.GREEN).replace("%g",g.getColor() + g.getName() + ChatColor.GREEN));
                                                }
                                                p2.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u2.getTranslatedMessage("Use your challenger to accept their request."));
                                                p2.playSound(p2.getEyeLocation(), Sound.NOTE_PLING,1f,1f);
                                                p.closeInventory();
                                            }
                                        } else {
                                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("This player ignores challenger requests."));
                                        }
                                    } else {
                                        p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You aren't friends with that player."));
                                        p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Purchase %r at %l to be able to do this.").replace("%r",Rank.PRO_PLUS.getColor() + Rank.PRO_PLUS.getName()).replace("%l", net.md_5.bungee.api.ChatColor.YELLOW + "https://store.thechest.eu" + net.md_5.bungee.api.ChatColor.RED));
                                    }
                                } else {
                                    p.closeInventory();
                                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You already have a request from that player."));
                                }
                            } else {
                                p.closeInventory();
                                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You've already sent a challenge request to that player."));
                            }
                        }
                    } else {
                        p.closeInventory();
                        p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("That player is not online."));
                    }
                }
            }
        }
    }
}
