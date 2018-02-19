package eu.thechest.chesthub.listener;

import eu.thechest.chestapi.ChestAPI;
import eu.thechest.chestapi.achievement.*;
import eu.thechest.chestapi.event.PlayerDataLoadedEvent;
import eu.thechest.chestapi.event.PlayerLocaleChangeEvent;
import eu.thechest.chestapi.event.PlayerToggleVanishEvent;
import eu.thechest.chestapi.items.ItemCategory;
import eu.thechest.chestapi.items.VaultItem;
import eu.thechest.chestapi.mysql.MySQLManager;
import eu.thechest.chestapi.server.*;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.user.GlobalParty;
import eu.thechest.chestapi.user.Rank;
import eu.thechest.chestapi.user.ScoreboardType;
import eu.thechest.chestapi.util.ParticleEffect;
import eu.thechest.chestapi.util.PlayerUtilities;
import eu.thechest.chestapi.util.StringUtils;
import eu.thechest.chesthub.ChallengerRequest;
import eu.thechest.chesthub.ChestHub;
import eu.thechest.chesthub.ChestReward;
import eu.thechest.chesthub.LocationManager;
import eu.thechest.chesthub.cmd.MainExecutor;
import eu.thechest.chesthub.inv.*;
import eu.thechest.chesthub.levelrewards.LevelReward;
import eu.thechest.chesthub.vault.Vault;
import eu.thechest.chesthub.vault.VaultStorage;
import jdk.nashorn.internal.objects.Global;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.*;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.*;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.sql.PreparedStatement;
import java.util.ArrayList;

import static eu.thechest.chesthub.ChestHub.DISALLOWED_BLOCKS;

/**
 * Created by zeryt on 11.02.2017.
 */
public class MainListener implements Listener {
    public static ArrayList<Player> PLATE_CACHE = new ArrayList<Player>();
    public static ArrayList<Player> STATSSIGNCACHE = new ArrayList<Player>();

    private void jumpPad(Player p){
        ChestUser u = ChestUser.getUser(p);
        if(PLATE_CACHE.contains(p)) return;
        PLATE_CACHE.add(p);

        Bukkit.getScheduler().scheduleSyncDelayedTask(ChestHub.getInstance(), new Runnable(){
            public void run(){
                if(PLATE_CACHE.contains(p)) PLATE_CACHE.remove(p);
            }
        }, 2*20);

        if(u.isVanished()){
            p.playSound(p.getEyeLocation(), Sound.NOTE_BASS,1f,0.5f);
            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + net.md_5.bungee.api.ChatColor.RED + u.getTranslatedMessage("You can't do that in vanish mode."));
        } else {
            World w = p.getWorld();
            double x = p.getLocation().getX();
            double y = p.getLocation().getY();
            double z = p.getLocation().getZ();
            p.playEffect(new Location(w, x, y, z), Effect.ENDER_SIGNAL, 10);
            p.playSound(new Location(w, x, y, z), Sound.CLICK, 10.0F, 10.0F);

            p.setVelocity(new Vector(0, 1.5, 0));
            p.playSound(p.getEyeLocation(), Sound.NOTE_BASS, 2f, 1f);

            Bukkit.getScheduler().scheduleSyncDelayedTask(ChestHub.getInstance(), new Runnable(){
                public void run(){
                    Vector v = p.getLocation().getDirection().multiply(8.0D).setY(0.7D);
                    p.setVelocity(v);
                }
            }, 10L);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();

        p.teleport(LocationManager.getLocation("spawn"));
        p.setGameMode(GameMode.ADVENTURE);
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
        e.setJoinMessage(null);
    }

    @EventHandler
    public void onDataLoaded(PlayerDataLoadedEvent e){
        Player p = e.getPlayer();
        ChestUser u = e.getUser();

        ChestHub.getInstance().giveLobbyItems(p);
        ChestHub.getInstance().updatePet(p);

        ChestHub.getInstance().giveLobbyHelmet(p);
        new BukkitRunnable(){
            @Override
            public void run() {
                ChestHub.updateAllStatsSigns(p);
            }
        }.runTaskLater(ChestHub.getInstance(),3*20);

        u.updateLevelBar();
        u.updateScoreboard(ScoreboardType.LOBBY);

        if(u.enabledLobbySpeed() && u.hasPermission(Rank.TITAN)){
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,1,false,false));
        }

        ArrayList<Integer> hats = new ArrayList<Integer>();

        if(u.hasPermission(Rank.PRO)) hats.add(20);
        if(u.hasPermission(Rank.PRO_PLUS)) hats.add(19);
        if(u.hasPermission(Rank.TITAN)) hats.add(18);
        if(u.hasPermission(Rank.VIP)){ hats.add(17); hats.add(114); }
        if(u.hasPermission(Rank.STAFF)) hats.add(16);
        if(u.hasPermission(Rank.BUILD_TEAM)) hats.add(15);
        if(u.hasPermission(Rank.MOD)) hats.add(14);
        if(u.hasPermission(Rank.SR_MOD)) hats.add(13);
        if(u.hasPermission(Rank.CM)) hats.add(12);
        if(u.hasPermission(Rank.ADMIN)) hats.add(11);

        if(hats.size() > 0){
            for(int i : hats){
                VaultItem hat = VaultItem.getItem(i);

                if(hat != null){
                    if(!u.getUnlockedItems().contains(hat)){
                        u.unlockItem(hat);
                    }
                }
            }
        }

        int openReward = 0;
        for(LevelReward l : LevelReward.REWARDS){
            if(l.level <= u.getLevel()){
                if(!u.hasClaimedLevelReward(l.level)){
                    openReward++;
                }
            }
        }

        p.sendMessage("");
        if(openReward == 1){
            p.spigot().sendMessage(new ComponentBuilder(ServerSettingsManager.RUNNING_GAME.getPrefix()).append(u.getTranslatedMessage("You have 1 level reward open! Click here to claim it!")).color(net.md_5.bungee.api.ChatColor.GREEN).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/levelrewards")).create());
        } else if(openReward > 1){
            p.spigot().sendMessage(new ComponentBuilder(ServerSettingsManager.RUNNING_GAME.getPrefix()).append(u.getTranslatedMessage("You have %l level rewards open! Click here to claim it!").replace("%l",String.valueOf(openReward))).color(net.md_5.bungee.api.ChatColor.GREEN).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/levelrewards")).create());
        }

        for(Player all : Bukkit.getOnlinePlayers()){
            if(ChestHub.HIDERS.contains(all)){
                ChestHub.handleHide(all,p);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        ChestUser u = ChestUser.getUser(p);

        if(MainExecutor.TOPLIST_CACHE.containsKey(p)) MainExecutor.TOPLIST_CACHE.remove(p);

        e.setQuitMessage(null);

        if(SettingsMenu.TO_SAVE.contains(p.getName())){
            u.saveSettings();
            SettingsMenu.TO_SAVE.remove(p.getName());
        }

        Vault v = VaultStorage.getVaultByPlayer(p);
        if(v != null){
            if(v.currentOpeners.contains(p)) v.currentOpeners.remove(p);
            if(v.player == p) v.player = null;
        }

        if(VaultInventory.COOLDOWN.contains(p.getName())) VaultInventory.COOLDOWN.remove(p.getName());
        if(MainExecutor.PETNAME_COOLDOWN.contains(p.getName())) MainExecutor.PETNAME_COOLDOWN.remove(p.getName());

        if(PLATE_CACHE.contains(p)) PLATE_CACHE.remove(p);
        if(MainExecutor.STATS_COOLDOWN.contains(p.getName())) MainExecutor.STATS_COOLDOWN.remove(p.getName());

        if(ChestHub.GADGET_COOLDOWN.containsKey(p.getName())){
            Bukkit.getScheduler().cancelTask(ChestHub.GADGET_COOLDOWN.get(p.getName()));
            ChestHub.GADGET_COOLDOWN.remove(p.getName());
        }

        ArrayList<ChallengerRequest> q = new ArrayList<ChallengerRequest>();
        for(ChallengerRequest r : ChallengerRequest.STORAGE) if(r.from == p || r.to == p) q.add(r);
        ChallengerRequest.STORAGE.removeAll(q);

        if(ChestHub.HIDE_COOLDOWN.contains(p)) ChestHub.HIDE_COOLDOWN.remove(p);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e){
        e.setCancelled(true);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(e.getWhoClicked() instanceof Player){
            Player p = (Player)e.getWhoClicked();
            ChestUser u = ChestUser.getUser(p);
            Inventory inv = e.getInventory();

            if(p.getGameMode() == GameMode.CREATIVE && u.hasPermission(Rank.ADMIN)){
                return;
            }

            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e){
        Player p = e.getPlayer();

        ChestAPI.async(() -> {
            Rank r = PlayerUtilities.getRankFromUUID(p.getUniqueId());

            if(ServerUtil.getServerName().startsWith("PremiumLobby") && !(r.getID() >= Rank.PRO.getID())){
                e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                e.setKickMessage(ChatColor.RED + "You need a premium rank to join this lobby!");
                return;
            }

            if(ServerSettingsManager.CURRENT_GAMESTATE == GameState.ENDING){
                e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                e.setKickMessage(ChatColor.RED + "This lobby is restarting.");
                return;
            }
        });
    }

    @EventHandler
    public void onLocaleChange(PlayerLocaleChangeEvent e){
        Player p = e.getPlayer();
        ChestUser u = ChestUser.getUser(p);

        u.updateScoreboard(ScoreboardType.LOBBY);
        ChestHub.getInstance().giveLobbyItems(p);
    }

    @EventHandler
    public void onExit(VehicleExitEvent e){
        if(e.getExited() instanceof Player){
            Player p = (Player)e.getExited();

            if(e.getVehicle() instanceof Horse){
                e.getVehicle().remove();
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e){
        if(e.getEntity().getType() == EntityType.PLAYER){
            if(e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK){
                e.setCancelled(true);
                return;
            }
        } else if(e.getEntity().getType() == EntityType.HORSE){
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onDamageBy(EntityDamageByEntityEvent e){
        e.setCancelled(true);

        if(e.getEntity() instanceof Player && e.getDamager() instanceof Player){
            if(!CitizensAPI.getNPCRegistry().isNPC(e.getEntity()) && !CitizensAPI.getNPCRegistry().isNPC(e.getDamager())){
                Player p = (Player)e.getDamager();
                ChestUser u = ChestUser.getUser(p);
                Player p2 = (Player)e.getEntity();
                ChestUser u2 = ChestUser.getUser(p2);

                if(p.getItemInHand() != null){
                    ItemStack i = p.getItemInHand();

                    if(i.getItemMeta() != null && i.getItemMeta().getDisplayName() != null){
                        String dis = i.getItemMeta().getDisplayName();

                        if(dis.equals(ChatColor.YELLOW + u.getTranslatedMessage("Challenger") + " " + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "(" + u.getTranslatedMessage("Left click on players") + ")")){
                            if(ChallengerRequest.getRequest(p,p2) != null){
                                ChallengerRequest.getRequest(p,p2).retract();
                            } else if(ChallengerRequest.getRequest(p2,p) != null) {
                                ChallengerRequest.getRequest(p2,p).accept();
                            } else {
                                if(u.hasPermission(Rank.PRO_PLUS) || (!u.hasPermission(Rank.PRO_PLUS) && PlayerUtilities.getFriendsFromUUID(p.getUniqueId()).contains(p2.getUniqueId().toString()))){
                                    if(u.hasPermission(Rank.SR_MOD) || u2.allowsChallengerRequests()){
                                        ChallengerMenu.openFor(p,p2);
                                    } else {
                                        p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("This player ignores challenger requests."));
                                    }
                                } else {
                                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You aren't friends with that player."));
                                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Purchase %r at %l to be able to do this.").replace("%r",Rank.PRO_PLUS.getColor() + Rank.PRO_PLUS.getName()).replace("%l", net.md_5.bungee.api.ChatColor.YELLOW + "https://store.thechest.eu" + net.md_5.bungee.api.ChatColor.RED));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onUnload(ChunkUnloadEvent e){
        e.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e){
        Player p = e.getPlayer();

        if(ChestUser.isLoaded(p)){
            ChestUser u = ChestUser.getUser(p);

            if(p.getLocation().getY() < 0){
                p.teleport(LocationManager.getLocation("spawn"));
                return;
            }

            if(e.getFrom().getX() != e.getTo().getX() || e.getFrom().getY() != e.getTo().getY() || e.getFrom().getZ() != e.getTo().getZ()){
                VaultItem trail = u.getActiveItem(ItemCategory.LOBBY_TRAILS);
                if(trail != null){
                    if(!u.isVanished()){
                        // LOBBY TRAILS

                        if(trail.getID() == 101){
                            ParticleEffect.FIREWORKS_SPARK.display(0,0,0,0.0005f,1,e.getTo(),600);
                        } else if(trail.getID() == 103){
                            // HEARTS
                            ParticleEffect.HEART.display(0,0,0,1f,1,e.getTo().clone().add(0,2,0),600);
                        } else if(trail.getID() == 104){
                            // SLIME
                            ParticleEffect.SLIME.display(0,0,0,1f,15,e.getTo().clone().add(0,2,0),600);
                        } else if(trail.getID() == 105){
                            // NOTES
                            ParticleEffect.NOTE.display(new ParticleEffect.NoteColor(StringUtils.randomInteger(0,24)),e.getTo().clone().add(0,2,0),600);
                        } else if(trail.getID() == 106){
                            // FOOT STEPS
                            ParticleEffect.FOOTSTEP.display(0,0,0,1f,1,e.getTo(),600);
                        } else if(trail.getID() == 112){
                            // FOOT STEPS
                            ParticleEffect.FLAME.display(0,0,0,0.001f,1,e.getTo().clone().add(0,2,0),600);
                        }
                    }
                }
            }

            if(p.getLocation().add(0,0,0).getBlock().getType() == Material.GOLD_PLATE || p.getLocation().add(0,-1,0).getBlock().getType() == Material.GOLD_PLATE || p.getLocation().add(0,-2,0).getBlock().getType() == Material.GOLD_PLATE){
                jumpPad(p);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e){
        Player p = e.getPlayer();
        ChestUser u = ChestUser.getUser(p);

        if(p.getGameMode() != GameMode.CREATIVE){
            e.setCancelled(true);
        } else if(!u.hasPermission(Rank.ADMIN)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent e){
        Player p = e.getClicker();
        ChestUser u = ChestUser.getUser(p);
        NPC npc = e.getNPC();

        if(ChestHub.SHOP_NPCS.containsKey(npc.getId())){
            GamePerkMenu.openFor(p,ChestHub.SHOP_NPCS.get(npc.getId()));
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e){
        Player p = e.getPlayer();
        ChestUser u = ChestUser.getUser(p);

        if(p.getGameMode() != GameMode.CREATIVE){
            e.setCancelled(true);
        } else if(!u.hasPermission(Rank.ADMIN)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        Player p = e.getPlayer();
        ChestUser u = ChestUser.getUser(p);

        if(p.getGameMode() != GameMode.CREATIVE){
            e.setCancelled(true);
        } else {
            if(!u.hasPermission(Rank.ADMIN)){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e){
        Player p = e.getPlayer();
        ChestUser u = ChestUser.getUser(p);

        if(e.getRightClicked() instanceof Player){
            Player p2 = (Player)e.getRightClicked();
            ChestUser u2 = ChestUser.getUser(p2);

            if(u.hasPermission(Rank.PRO_PLUS) && u.allowsHeadSeat() && ChestHub.HEADSET_ENABLED){
                Player v = p2;

                while(v.getPassenger() != null && v.getPassenger() instanceof Player){
                    v = (Player)v.getPassenger();
                }

                if(v != p && ChestUser.getUser(v).allowsHeadSeat()){
                    v.setPassenger(p);
                }
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e){
        if(e.getEntity() instanceof Player){
            Player p = (Player)e.getEntity();

            e.setCancelled(true);
            p.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onToggleVanish(PlayerToggleVanishEvent e){
        Player p = e.getPlayer();
        ChestUser u = e.getUser();

        if(u.isVanished()){
            // TOGGLED ON

            ChestHub.getInstance().updatePet(p);
        } else {
            // TOGGLED OFF

            ChestHub.getInstance().updatePet(p);
        }
    }

    private void g(Player p, int cooldown){
        if(cooldown > 0){
            if(ChestHub.GADGET_COOLDOWN.containsKey(p.getName())){
                Bukkit.getScheduler().cancelTask(ChestHub.GADGET_COOLDOWN.get(p.getName()));
                ChestHub.GADGET_COOLDOWN.remove(p.getName());
            }

            int i = Bukkit.getScheduler().scheduleSyncDelayedTask(ChestHub.getInstance(), new Runnable(){
                @Override
                public void run() {
                    ChestHub.GADGET_COOLDOWN.remove(p.getName());
                }
            }, cooldown*20);
            ChestHub.GADGET_COOLDOWN.put(p.getName(),i);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        Player p = e.getPlayer();
        ChestUser u = ChestUser.getUser(p);

        if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
            if(e.getClickedBlock() != null){
                Location loc = e.getClickedBlock().getLocation();

                if(ChestHub.SETLOCATION.containsKey(p)){
                    String id = ChestHub.SETLOCATION.get(p);
                    LocationManager.setLocation(id,loc,p.getUniqueId());
                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Location defined."));
                    ChestHub.SETLOCATION.remove(p);
                    return;
                } else {
                    if(e.getClickedBlock().getType() == Material.SKULL){
                        loc = e.getClickedBlock().getLocation();
                        Skull s = (Skull)e.getClickedBlock().getState();

                        if(LocationManager.isLocation("socialMedia.twitter",e.getClickedBlock().getLocation())){
                            p.spigot().sendMessage(new ComponentBuilder(ServerSettingsManager.RUNNING_GAME.getPrefix()).append(u.getTranslatedMessage("Click to follow us on Twitter!")).color(net.md_5.bungee.api.ChatColor.AQUA).bold(true).event(new ClickEvent(ClickEvent.Action.OPEN_URL,"https://twitter.com/intent/follow?screen_name=TheChestEU")).create());
                        } else if(LocationManager.isLocation("socialMedia.youtube",e.getClickedBlock().getLocation())){
                            p.spigot().sendMessage(new ComponentBuilder(ServerSettingsManager.RUNNING_GAME.getPrefix()).append(u.getTranslatedMessage("Click to subscribe to our YouTube channel!")).color(net.md_5.bungee.api.ChatColor.RED).bold(true).event(new ClickEvent(ClickEvent.Action.OPEN_URL,"https://www.youtube.com/c/TheChestEU?sub_confirmation=1")).create());
                        } else if(LocationManager.isLocation("socialMedia.website",e.getClickedBlock().getLocation())){
                            p.spigot().sendMessage(new ComponentBuilder(ServerSettingsManager.RUNNING_GAME.getPrefix()).append(u.getTranslatedMessage("Click to view our website!")).color(net.md_5.bungee.api.ChatColor.GREEN).bold(true).event(new ClickEvent(ClickEvent.Action.OPEN_URL,"https://thechest.eu")).create());
                        }

                        if(s.getOwner() != null && s.getOwner().equals("Zealock")){
                            ChestReward r = null;

                            for(ChestReward reward : ChestHub.REWARDS){
                                if(reward.loc.getBlockX() == loc.getBlockX() && reward.loc.getBlockY() == loc.getBlockY() && reward.loc.getBlockZ() == loc.getBlockZ() && reward.loc.getWorld().getName().equals(loc.getWorld().getName())){
                                    r = reward;
                                }
                            }

                            if(r != null){
                                if(r.achievement > 0 && r.coins <= 0){
                                    // ACHIEVEMENT ONLY

                                    if(eu.thechest.chestapi.achievement.Achievement.getAchievement(r.achievement) != null){
                                        eu.thechest.chestapi.achievement.Achievement a = eu.thechest.chestapi.achievement.Achievement.getAchievement(r.achievement);

                                        if(!u.hasAchieved(a)){
                                            u.achieve(a);
                                        } else {
                                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You have already claimed this reward."));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK){
            if(p.getItemInHand() != null){
                ItemStack i = p.getItemInHand();

                if(i.getItemMeta() != null && i.getItemMeta().getDisplayName() != null){
                    String dis = i.getItemMeta().getDisplayName();

                    if(p.getInventory().getHeldItemSlot() == 1){
                        VaultItem gadget = u.getActiveItem(ItemCategory.GADGET);
                        int id = gadget.getID();

                        if(gadget != null){
                            e.setCancelled(true);
                            e.setUseInteractedBlock(Event.Result.DENY);
                            e.setUseItemInHand(Event.Result.DENY);

                            if(!u.isVanished()){
                                if(ChestHub.GADGET_COOLDOWN.containsKey(p.getName())){
                                    p.playSound(p.getEyeLocation(), Sound.NOTE_BASS,1f,0.5f);
                                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + net.md_5.bungee.api.ChatColor.RED + u.getTranslatedMessage("Please wait a little while before using this gadget again."));
                                } else {
                                    int cooldown = 0;

                                    if(id == 114){
                                        p.getWorld().playSound(p.getEyeLocation(), Sound.FIREWORK_LAUNCH, 1F, 1F);
                                        //ParticleEffect.FIREWORKS_SPARK.display(null, 1F, 1F, 1F, 1F, 50, p.getLocation().add(0.0, -1.0, 0.0), 50);
                                        ParticleEffect.CLOUD.display(0.05F, 0.05F, 0.05F, 0.05F, 120, p.getLocation().add(0.0, -1.0, 0.0), 900);
                                        p.setVelocity(p.getLocation().getDirection().multiply(1.5).setY(1));
                                    }

                                    g(p,cooldown);
                                }
                            } else {
                                p.playSound(p.getEyeLocation(), Sound.NOTE_BASS,1f,0.5f);
                                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + net.md_5.bungee.api.ChatColor.RED + u.getTranslatedMessage("You can't do that in vanish mode."));
                            }
                        }
                    }

                    if(dis.equals(ChatColor.YELLOW + u.getTranslatedMessage("Game Modes") + " " + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "(" + u.getTranslatedMessage("Right click") + ")")){
                        // GAMEMODES MENU

                        e.setCancelled(true);
                        e.setUseInteractedBlock(Event.Result.DENY);
                        e.setUseItemInHand(Event.Result.DENY);

                        GamemodesMenu.openFor(p);
                    }/* else if(dis.equals(ChatColor.GREEN + u.getTranslatedMessage("Fly Mode") + " " + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "(" + u.getTranslatedMessage("Right click") + ")")){
                        // FLY MODE

                        e.setCancelled(true);
                        e.setUseInteractedBlock(Event.Result.DENY);
                        e.setUseItemInHand(Event.Result.DENY);

                        if(!u.isVanished()){
                            if(u.hasPermission(Rank.VIP)){
                                p.getWorld().playSound(p.getEyeLocation(), Sound.FIREWORK_LAUNCH, 1F, 1F);
                                //ParticleEffect.FIREWORKS_SPARK.display(null, 1F, 1F, 1F, 1F, 50, p.getLocation().add(0.0, -1.0, 0.0), 50);
                                ParticleEffect.CLOUD.display(0.05F, 0.05F, 0.05F, 0.05F, 120, p.getLocation().add(0.0, -1.0, 0.0), 900);
                                p.setVelocity(p.getLocation().getDirection().multiply(1.5).setY(1));
                            }
                        } else {
                            p.playSound(p.getEyeLocation(), Sound.NOTE_BASS,1f,0.5f);
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + net.md_5.bungee.api.ChatColor.RED + u.getTranslatedMessage("You can't do that in vanish mode."));
                        }
                    }*/ else if(dis.equals(ChatColor.YELLOW + u.getTranslatedMessage("Toggle players") + " " + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "(" + u.getTranslatedMessage("Right click") + ")")){
                        // HIDE PLAYERS

                        if(!ChestHub.HIDE_COOLDOWN.contains(p)){
                            u.achieve(24);

                            if(ChestHub.HIDERS.contains(p.getUniqueId())){
                                // IS ON

                                for(Player all : Bukkit.getOnlinePlayers()){
                                    ChestHub.handleShow(p,all);
                                }

                                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("You can now see other players again."));
                                ChestHub.HIDERS.remove(p.getUniqueId());
                            } else {
                                // IS OFF

                                for(Player all : Bukkit.getOnlinePlayers()){
                                    ChestHub.handleHide(p,all);
                                }

                                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("You can no longer see other players."));
                                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("Staff Members, VIPs and friends are still visible."));
                                ChestHub.HIDERS.add(p.getUniqueId());
                            }

                            p.playSound(p.getEyeLocation(),Sound.LEVEL_UP,1f,2f);

                            ChestHub.HIDE_COOLDOWN.add(p);
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    ChestHub.HIDE_COOLDOWN.remove(p);
                                }
                            }.runTaskLater(ChestHub.getInstance(),2*20);
                        } else {
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Please wait a little while before using this item again."));
                        }
                    } else if(dis.equals(ChatColor.YELLOW + u.getTranslatedMessage("My Profile") + " " + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "(" + u.getTranslatedMessage("Right click") + ")")){
                        // MY PROFILE

                        MyProfile.openFor(p);
                    }
                }
            }
        }

        if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
            if(p.getGameMode() == GameMode.CREATIVE && u.hasPermission(Rank.ADMIN)){
                return;
            }

            if(DISALLOWED_BLOCKS.contains(e.getClickedBlock().getType())){
                e.setCancelled(true);
                e.setUseInteractedBlock(Event.Result.DENY);

                return;
            }
        }

        /*if(e.getAction() == Action.PHYSICAL){
            if(e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.GOLD_PLATE){
                jumpPad(p);
            }
        }*/

        if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
            if(e.getClickedBlock() != null){
                if(ChestHub.DISALLOWED_BLOCKS.contains(e.getClickedBlock().getType())){
                    e.setCancelled(true);
                    e.setUseInteractedBlock(Event.Result.DENY);
                    e.setUseItemInHand(Event.Result.DENY);
                    return;
                }

                if(e.getClickedBlock().getType() == Material.SIGN || e.getClickedBlock().getType() == Material.SIGN_POST || e.getClickedBlock().getType() == Material.WALL_SIGN){
                    Sign s = (Sign)e.getClickedBlock().getState();

                    if(!STATSSIGNCACHE.contains(p)){
                        if(ChestAPI.isLocationEqual(e.getClickedBlock().getLocation(),LocationManager.getLocation("statssign.sg.change.classic"))){
                            p.playSound(p.getEyeLocation(),Sound.CLICK,2f,1f);
                            ChestHub.updateStatsSigns(p,GameType.SURVIVAL_GAMES);
                        } else if(ChestAPI.isLocationEqual(e.getClickedBlock().getLocation(),LocationManager.getLocation("statssign.sg.change.duels"))){
                            p.playSound(p.getEyeLocation(),Sound.CLICK,2f,1f);
                            ChestHub.updateStatsSigns(p,GameType.SG_DUELS);
                        } else if(ChestAPI.isLocationEqual(e.getClickedBlock().getLocation(),LocationManager.getLocation("sgduels.joinQueue"))){
                            ChestAPI.executeBungeeCommand("BungeeConsole","queuemanager join SGDuels " + p.getName());
                        } else if(ChestAPI.isLocationEqual(e.getClickedBlock().getLocation(),LocationManager.getLocation("sgduels.leaveQueue"))){
                            ChestAPI.executeBungeeCommand("BungeeConsole","queuemanager leave SGDuels " + p.getName());
                        } else if(ChestAPI.isLocationEqual(e.getClickedBlock().getLocation(),LocationManager.getLocation("soccer.joinQueue.1"))){
                            ChestAPI.executeBungeeCommand("BungeeConsole","queuemanager join SoccerMC1 " + p.getName());
                        } else if(ChestAPI.isLocationEqual(e.getClickedBlock().getLocation(),LocationManager.getLocation("soccer.joinQueue.2"))){
                            ChestAPI.executeBungeeCommand("BungeeConsole","queuemanager join SoccerMC2 " + p.getName());
                        } else if(ChestAPI.isLocationEqual(e.getClickedBlock().getLocation(),LocationManager.getLocation("soccer.joinQueue.3"))){
                            ChestAPI.executeBungeeCommand("BungeeConsole","queuemanager join SoccerMC3 " + p.getName());
                        } else if(ChestAPI.isLocationEqual(e.getClickedBlock().getLocation(),LocationManager.getLocation("soccer.joinQueue.4"))){
                            ChestAPI.executeBungeeCommand("BungeeConsole","queuemanager join SoccerMC4 " + p.getName());
                        } else if(ChestAPI.isLocationEqual(e.getClickedBlock().getLocation(),LocationManager.getLocation("soccer.leaveQueue"))){
                            ChestAPI.executeBungeeCommand("BungeeConsole","queuemanager leave SoccerMC " + p.getName());
                        } else if(ChestAPI.isLocationEqual(e.getClickedBlock().getLocation(),LocationManager.getLocation("soccer.spectate"))){
                            SoccerSpectateMenu.openFor(p);
                        }

                        STATSSIGNCACHE.add(p);
                        new BukkitRunnable(){
                            @Override
                            public void run() {
                                STATSSIGNCACHE.remove(p);
                            }
                        }.runTaskLater(ChestHub.getInstance(),3*20);
                    }
                }

                if(e.getClickedBlock().getType() == Material.ENCHANTMENT_TABLE){
                    e.setCancelled(true);
                    e.setUseInteractedBlock(Event.Result.DENY);
                    e.setUseItemInHand(Event.Result.DENY);

                    VaultInventory.openFor(p,true,null,0);
                }

                if(e.getClickedBlock().getType() == Material.ENDER_CHEST){
                    e.setCancelled(true);
                    e.setUseInteractedBlock(Event.Result.DENY);
                    e.setUseItemInHand(Event.Result.DENY);
                    Vault v = VaultStorage.getVaultFromLocation(e.getClickedBlock().getLocation());

                    if(v != null){
                        if(VaultStorage.getVaultByPlayer(p) == null){
                            //if(!v.currentOpeners.contains(p)) v.currentOpeners.add(p);
                            VaultMenu.openFor(p,v);
                        } else {
                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You are already opening a chest."));
                            p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,0.5f);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e){
        Player p = e.getPlayer();
        ChestUser u = ChestUser.getUser(p);

        if(p.isSneaking()){
            if(ChestHub.HEADSET_ENABLED && p.getPassenger() != null){
                Entity en = p.getPassenger();
                p.eject();
                en.setVelocity(new Vector(0,1.5,0));
            }
        }
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent e){
        if(e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM){
            if(e.getEntity().getType() != EntityType.ARMOR_STAND && e.getEntity().getType() != EntityType.HORSE && e.getEntity().getType() != EntityType.FIREWORK){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent e){
        Player p = e.getPlayer();
        ChestUser u = ChestUser.getUser(p);

        if(!u.hasPermission(Rank.ADMIN)){
            e.setCancelled(true);
        } else {
            if(p.getGameMode() != GameMode.CREATIVE){
                e.setCancelled(true);
            } else {

            }
        }
    }
}
