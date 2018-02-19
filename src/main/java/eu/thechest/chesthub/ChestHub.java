package eu.thechest.chesthub;

import com.dsh105.echopet.api.EchoPetAPI;
import com.dsh105.echopet.api.pet.type.*;
import com.dsh105.echopet.compat.api.entity.IPet;
import com.dsh105.echopet.compat.api.entity.PetType;
import com.dsh105.echopet.compat.api.plugin.EchoPet;
import com.gmail.filoghost.holographicdisplays.object.NamedHologram;
import com.gmail.filoghost.holographicdisplays.object.NamedHologramManager;
import eu.thechest.chestapi.ChestAPI;
import eu.thechest.chestapi.game.GameManager;
import eu.thechest.chestapi.items.ItemCategory;
import eu.thechest.chestapi.items.ItemUtil;
import eu.thechest.chestapi.items.VaultItem;
import eu.thechest.chestapi.lang.TranslatedHoloLine;
import eu.thechest.chestapi.lang.TranslatedHologram;
import eu.thechest.chestapi.mysql.MySQLManager;
import eu.thechest.chestapi.server.ChestServer;
import eu.thechest.chestapi.server.GameState;
import eu.thechest.chestapi.server.GameType;
import eu.thechest.chestapi.server.ServerSettingsManager;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.user.Rank;
import eu.thechest.chestapi.util.ParticleEffect;
import eu.thechest.chestapi.util.PlayerUtilities;
import eu.thechest.chestapi.util.StringUtils;
import eu.thechest.chesthub.cmd.MainExecutor;
import eu.thechest.chesthub.inv.*;
import eu.thechest.chesthub.levelrewards.LevelReward;
import eu.thechest.chesthub.listener.MainListener;
import eu.thechest.chesthub.toplist.Leaderboard;
import eu.thechest.chesthub.vault.Vault;
import eu.thechest.chesthub.vault.VaultStorage;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by zeryt on 11.02.2017.
 */
public class ChestHub extends JavaPlugin {
    public static ArrayList<Material> DISALLOWED_BLOCKS = new ArrayList<Material>();
    public static ArrayList<UUID> HIDERS = new ArrayList<UUID>();
    public static ArrayList<Player> HIDE_COOLDOWN = new ArrayList<Player>();
    public static ArrayList<ChestReward> REWARDS = new ArrayList<ChestReward>();
    public static final boolean HEADSET_ENABLED = false;

    public static int RESTART_COUNTDOWN = StringUtils.randomInteger(3,6)*60*60;

    public static Location LOC_VOTEHEAD = null;
    public static Location LOC_VOTESIGN = null;

    public static HashMap<String,Integer> GADGET_COOLDOWN = new HashMap<String,Integer>();
    public static ArrayList<Location> VAULT_INVENTORIES = new ArrayList<Location>();

    public static int NPC_SGDUELS_UNRANKED;
    public static int NPC_SGDUELS_RANKED;

    public static HashMap<Player,String> SETLOCATION = new HashMap<Player,String>();
    public static HashMap<Integer,GameType> SHOP_NPCS = new HashMap<Integer,GameType>();

    public void onEnable(){
        ServerSettingsManager.updateGameState(GameState.JOINABLE);
        ServerSettingsManager.RUNNING_GAME = GameType.NONE;
        ServerSettingsManager.AUTO_OP = true;
        ServerSettingsManager.setMaxPlayers(50);
        ServerSettingsManager.VIP_JOIN = false;
        ServerSettingsManager.ENABLE_NICK = true;
        ServerSettingsManager.SHOW_FAME_TITLE_ABOVE_HEAD = true;
        ServerSettingsManager.PROTECT_ARMORSTANDS = true;
        ServerSettingsManager.SHOW_LEVEL_IN_EXP_BAR = true;

        LocationManager.reload();
        new BukkitRunnable(){
            @Override
            public void run() {
                LocationManager.reload();
            }
        }.runTaskTimer(this,2*60*20,2*60*20);

        MainExecutor exec = new MainExecutor();
        getCommand("setlocation").setExecutor(exec);
        getCommand("petname").setExecutor(exec);
        getCommand("levelrewards").setExecutor(exec);
        getCommand("stats").setExecutor(exec);
        getCommand("resettokens").setExecutor(exec);

        for(World w : Bukkit.getWorlds()){
            prepareWorld(w);
        }

        Bukkit.getPluginManager().registerEvents(new MainListener(),this);
        Bukkit.getPluginManager().registerEvents(new StatisticsMenu(),this);
        Bukkit.getPluginManager().registerEvents(new AchievementMenu(),this);
        Bukkit.getPluginManager().registerEvents(new SettingsMenu(),this);
        Bukkit.getPluginManager().registerEvents(new LanguageMenu(),this);
        Bukkit.getPluginManager().registerEvents(new VaultInventory(),this);
        Bukkit.getPluginManager().registerEvents(new VaultMenu(),this);
        Bukkit.getPluginManager().registerEvents(new PremiumMenu(),this);
        Bukkit.getPluginManager().registerEvents(new VaultShardMenu(),this);
        Bukkit.getPluginManager().registerEvents(new LevelRewardsMenu(),this);
        Bukkit.getPluginManager().registerEvents(new GamePerkMenu(),this);
        Bukkit.getPluginManager().registerEvents(new ResetTokensMenu(),this);
        Bukkit.getPluginManager().registerEvents(new ChallengerMenu(),this);
        Bukkit.getPluginManager().registerEvents(new LobbySelectorMenu(),this);

        instance = this;

        DISALLOWED_BLOCKS.add(Material.BREWING_STAND);
        DISALLOWED_BLOCKS.add(Material.FURNACE);
        DISALLOWED_BLOCKS.add(Material.BURNING_FURNACE);
        DISALLOWED_BLOCKS.add(Material.WORKBENCH);
        DISALLOWED_BLOCKS.add(Material.TRAP_DOOR);
        DISALLOWED_BLOCKS.add(Material.CHEST);
        DISALLOWED_BLOCKS.add(Material.TRAPPED_CHEST);
        DISALLOWED_BLOCKS.add(Material.FENCE_GATE);
        DISALLOWED_BLOCKS.add(Material.SPRUCE_FENCE_GATE);
        DISALLOWED_BLOCKS.add(Material.BIRCH_FENCE_GATE);
        DISALLOWED_BLOCKS.add(Material.JUNGLE_FENCE_GATE);
        DISALLOWED_BLOCKS.add(Material.DARK_OAK_FENCE_GATE);
        DISALLOWED_BLOCKS.add(Material.ACACIA_FENCE_GATE);
        DISALLOWED_BLOCKS.add(Material.DIODE_BLOCK_OFF);
        DISALLOWED_BLOCKS.add(Material.DIODE_BLOCK_ON);
        DISALLOWED_BLOCKS.add(Material.REDSTONE_COMPARATOR_OFF);
        DISALLOWED_BLOCKS.add(Material.REDSTONE_COMPARATOR_ON);
        DISALLOWED_BLOCKS.add(Material.HOPPER);
        DISALLOWED_BLOCKS.add(Material.DROPPER);
        DISALLOWED_BLOCKS.add(Material.DISPENSER);
        DISALLOWED_BLOCKS.add(Material.JUKEBOX);
        DISALLOWED_BLOCKS.add(Material.NOTE_BLOCK);
        DISALLOWED_BLOCKS.add(Material.ANVIL);
        DISALLOWED_BLOCKS.add(Material.DIODE_BLOCK_OFF);
        DISALLOWED_BLOCKS.add(Material.DIODE_BLOCK_ON);
        DISALLOWED_BLOCKS.add(Material.REDSTONE_COMPARATOR_OFF);
        DISALLOWED_BLOCKS.add(Material.REDSTONE_COMPARATOR_ON);
        DISALLOWED_BLOCKS.add(Material.BEACON);

        updateHeadLeaderboards();
        new BukkitRunnable(){
            @Override
            public void run() {
                updateHeadLeaderboards();
            }
        }.runTaskTimer(this,3*60*20,3*60*20);

        /*NamedHologram h = null;

        h = NamedHologramManager.getHologram("sgleaderboard");
        if(h != null){
            Leaderboard.STORAGE.add(new Leaderboard(GameType.SURVIVAL_GAMES,"sg_stats","points",h.getLocation()));
            h.delete();
        }

        h = NamedHologramManager.getHologram("kpvpleaderboard");
        if(h != null){
            Leaderboard.STORAGE.add(new Leaderboard(GameType.KITPVP,"kpvp_stats","points",h.getLocation()));
            h.delete();
        }

        h = NamedHologramManager.getHologram("mgleaderboard");
        if(h != null){
            Leaderboard.STORAGE.add(new Leaderboard(GameType.MUSICAL_GUESS,"mg_stats","points",h.getLocation()));
            h.delete();
        }

        h = NamedHologramManager.getHologram("bgleaderboard");
        if(h != null){
            Leaderboard.STORAGE.add(new Leaderboard(GameType.BUILD_AND_GUESS,"bg_stats","points",h.getLocation()));
            h.delete();
        }

        h = NamedHologramManager.getHologram("soccerleaderboard");
        if(h != null){
            Leaderboard.STORAGE.add(new Leaderboard(GameType.SOCCER,"soccer_stats","points",h.getLocation()));
            h.delete();
        }*/

        for(String id : LocationManager.STORAGE.keySet()){
            Location loc = LocationManager.getLocation(id);

            if(id.startsWith("vaultInventory")){
                new TranslatedHologram(new TranslatedHoloLine[]{new TranslatedHoloLine(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Your Inventory")},ChestAPI.getBlockCenter(loc).add(0,1.5,0));
            }
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(ChestHub.getInstance(), new Runnable() {
            @Override
            public void run() {
                GameType[] gameTypes = new GameType[]{GameType.SURVIVAL_GAMES,GameType.SG_DUELS,GameType.MUSICAL_GUESS,GameType.KITPVP,GameType.TOBIKO,GameType.INFECTION_WARS,GameType.DEATHMATCH};

                for(GameType g : gameTypes){
                    Location loc = LocationManager.getLocation("shopNPC." + g.getAbbreviation().toLowerCase());
                    //NPC npc = CitizensAPI.getNPCRegistry().getById(i);
                    NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.VILLAGER,ChatColor.GREEN.toString());
                    npc.setName("");
                    npc.spawn(loc);
                    npc.getEntity().setCustomName(null);
                    npc.getEntity().setCustomNameVisible(false);
                    ChestAPI.nmsMakeSilent(npc.getEntity());
                    loc = npc.getStoredLocation().clone().add(0,3,0);

                    SHOP_NPCS.put(npc.getId(),g);

                    new TranslatedHologram(new TranslatedHoloLine[]{new TranslatedHoloLine(g.getColor() + g.getName() + " " + ChatColor.GREEN + "Shop"),new TranslatedHoloLine(ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + "CLICK TO OPEN")},loc);
                }

                // SGDUELS NPCS

                /*NPC unranked = CitizensAPI.getNPCRegistry().getById(3);
                NPC ranked = CitizensAPI.getNPCRegistry().getById(4);

                double holoAdd = 3.0;

                if(unranked != null){
                    NPC_SGDUELS_UNRANKED = unranked.getId();
                    unranked.setName(ChatColor.GREEN.toString());

                    new TranslatedHologram(new TranslatedHoloLine[]{new TranslatedHoloLine(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "UNRANKED"),new TranslatedHoloLine(ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + "CLICK TO PLAY")},unranked.getStoredLocation().clone().add(0,holoAdd,0));
                }

                if(ranked != null){
                    NPC_SGDUELS_RANKED = ranked.getId();
                    ranked.setName(ChatColor.GREEN.toString());

                    new TranslatedHologram(new TranslatedHoloLine[]{new TranslatedHoloLine(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "RANKED"),new TranslatedHoloLine(ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + "CLICK TO PLAY")},ranked.getStoredLocation().clone().add(0,holoAdd,0));
                }*/
            }
        },5*20);

        LevelReward.init();
        //Leaderboard.startUpdateTask();

        // INITIALIZE CHEST VAULTS
        VaultStorage.STORAGE.add(new Vault(LocationManager.getLocation("vault1"), 180));
        VaultStorage.STORAGE.add(new Vault(LocationManager.getLocation("vault2"), 90));
        VaultStorage.STORAGE.add(new Vault(LocationManager.getLocation("vault3"), 270));
        VaultStorage.STORAGE.add(new Vault(LocationManager.getLocation("vault4"), 180));
        VaultStorage.STORAGE.add(new Vault(LocationManager.getLocation("vault5"), 0));
        VaultStorage.STORAGE.add(new Vault(LocationManager.getLocation("vault6"), 270));
        VaultStorage.STORAGE.add(new Vault(LocationManager.getLocation("vault7"), 90));
        VaultStorage.STORAGE.add(new Vault(LocationManager.getLocation("vault8"), 0));

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `chestHeads`");
            ResultSet rs = ps.executeQuery();
            rs.beforeFirst();

            while(rs.next()){
                REWARDS.add(new ChestReward(new Location(Bukkit.getWorld(rs.getString("world")), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), 0f, 0f), rs.getInt("achievementToGive"), rs.getInt("coinsToGive")));
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }

        new BukkitRunnable(){
            @Override
            public void run() {
                for(ChestReward r : ChestHub.REWARDS){
                    if(r.achievement > 0 && r.coins <= 0){
                        if(eu.thechest.chestapi.achievement.Achievement.getAchievement(r.achievement) != null){
                            eu.thechest.chestapi.achievement.Achievement a = eu.thechest.chestapi.achievement.Achievement.getAchievement(r.achievement);
                            ArrayList<Player> players = new ArrayList<Player>();

                            if(r.loc != null && r.loc.getWorld() != null){
                                for(Player p : r.loc.getWorld().getPlayers()){
                                    if(ChestUser.isLoaded(p)){
                                        ChestUser u = ChestUser.getUser(p);

                                        if(!u.hasAchieved(a)){
                                            players.add(p);
                                        }
                                    }
                                }

                                if(players.size() > 0)
                                    ParticleEffect.VILLAGER_HAPPY.display(0.5f,0.5f,0.5f,0.05f,15,ChestAPI.getBlockCenter(r.loc).clone().add(0,0.4,0),players);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(this,10,10);

        new BukkitRunnable(){
            @Override
            public void run() {
                RESTART_COUNTDOWN--;

                if(RESTART_COUNTDOWN == 0){
                    cancel();
                    ChestAPI.stopServer();
                } else if(RESTART_COUNTDOWN == 60){
                    for(Player all : Bukkit.getOnlinePlayers()){
                        all.sendMessage(ChatColor.AQUA + ChestUser.getUser(all).getTranslatedMessage("This lobby restarts in 1 minute!"));
                    }
                } else if(RESTART_COUNTDOWN == 1){
                    for(Player all : Bukkit.getOnlinePlayers()){
                        all.sendMessage(ChatColor.AQUA + ChestUser.getUser(all).getTranslatedMessage("This lobby restarts in 1 second!"));
                    }
                } else if(RESTART_COUNTDOWN == 30 || RESTART_COUNTDOWN == 20 || RESTART_COUNTDOWN == 10 || RESTART_COUNTDOWN == 5 || RESTART_COUNTDOWN == 4 || RESTART_COUNTDOWN == 3 || RESTART_COUNTDOWN == 2){
                    for(Player all : Bukkit.getOnlinePlayers()){
                        all.sendMessage(ChatColor.AQUA + ChestUser.getUser(all).getTranslatedMessage("This lobby restarts in %s seconds!").replace("%s",String.valueOf(RESTART_COUNTDOWN)));
                    }
                } else if(RESTART_COUNTDOWN == (60*2) || RESTART_COUNTDOWN == (60*5) || RESTART_COUNTDOWN == (60*10) || RESTART_COUNTDOWN == (60*20) || RESTART_COUNTDOWN == (60*30) || RESTART_COUNTDOWN == (60*60)){
                    for(Player all : Bukkit.getOnlinePlayers()){
                        all.sendMessage(ChatColor.AQUA + ChestUser.getUser(all).getTranslatedMessage("This lobby restarts in %s minutes!").replace("%s",String.valueOf((RESTART_COUNTDOWN)/60)));
                    }
                }
            }
        }.runTaskTimer(this,1L,1*20);

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                try {
                    String user = null;
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `votes` WHERE `uuid` IS NOT NULL ORDER BY `time` DESC LIMIT 1");
                    ResultSet rs = ps.executeQuery();
                    if(rs.first()) user = rs.getString("username");
                    MySQLManager.getInstance().closeResources(rs,ps);

                    if(user == null) return;

                    /*String base = "locations.voteHead.";
                    LOC_VOTEHEAD = new Location(Bukkit.getWorld(getConfig().getString(base + "world")),getConfig().getDouble(base + "x"),getConfig().getDouble(base + "y"),getConfig().getDouble(base + "z"));*/
                    LOC_VOTEHEAD = LocationManager.getLocation("lastVote.head");

                    /*base = "locations.voteSign.";
                    LOC_VOTESIGN = new Location(Bukkit.getWorld(getConfig().getString(base + "world")),getConfig().getDouble(base + "x"),getConfig().getDouble(base + "y"),getConfig().getDouble(base + "z"));*/
                    LOC_VOTESIGN = LocationManager.getLocation("lastVote.sign");

                    Location loc = LOC_VOTESIGN;
                    if(loc.getBlock() == null) return;
                    if(loc.getBlock().getType() == Material.SIGN_POST || loc.getBlock().getType() == Material.WALL_SIGN || loc.getBlock().getType() == Material.SIGN){
                        Sign s = (Sign)loc.getBlock().getState();

                        s.setLine(0,"--*+*--");
                        s.setLine(1,"Latest vote:");
                        s.setLine(2,user);
                        s.setLine(3,"--*+*--");
                        s.update();
                    }

                    loc = LOC_VOTEHEAD;
                    if(loc.getBlock() == null) return;
                    if(loc.getBlock().getType() == Material.SKULL){
                        Skull s = (Skull)loc.getBlock().getState();

                        s.setOwner(user);
                        s.update();
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        },1L,1*60*20);

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                try {
                    String user = null;
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `users` ORDER BY RAND() LIMIT 1");
                    ResultSet rs = ps.executeQuery();
                    if(rs.first()) user = rs.getString("username");
                    MySQLManager.getInstance().closeResources(rs,ps);

                    if(user == null) return;

                    /*String base = "locations.randomUserHead.";
                    LOC_VOTEHEAD = new Location(Bukkit.getWorld(getConfig().getString(base + "world")),getConfig().getDouble(base + "x"),getConfig().getDouble(base + "y"),getConfig().getDouble(base + "z"));*/
                    LOC_VOTEHEAD = LocationManager.getLocation("randomUser.head");

                    /*base = "locations.randomUserSign.";
                    LOC_VOTESIGN = new Location(Bukkit.getWorld(getConfig().getString(base + "world")),getConfig().getDouble(base + "x"),getConfig().getDouble(base + "y"),getConfig().getDouble(base + "z"));*/
                    LOC_VOTESIGN = LocationManager.getLocation("randomUser.sign");

                    Location loc = LOC_VOTESIGN;
                    if(loc.getBlock() == null) return;
                    if(loc.getBlock().getType() == Material.SIGN_POST || loc.getBlock().getType() == Material.WALL_SIGN || loc.getBlock().getType() == Material.SIGN){
                        Sign s = (Sign)loc.getBlock().getState();

                        s.setLine(0,"--*+*--");
                        s.setLine(1,"User spotlight:");
                        s.setLine(2,user);
                        s.setLine(3,"--*+*--");
                        s.update();
                    }

                    loc = LOC_VOTEHEAD;
                    if(loc.getBlock() == null) return;
                    if(loc.getBlock().getType() == Material.SKULL){
                        Skull s = (Skull)loc.getBlock().getState();

                        s.setOwner(user);
                        s.update();
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        },1L,5*60*20);
    }

    public void onDisable(){

    }

    public static void updateAllStatsSigns(Player p){
        updateStatsSigns(p,GameType.SURVIVAL_GAMES);
        //updateStatsSigns(p,GameType.SG_DUELS);
        updateStatsSigns(p,GameType.MUSICAL_GUESS);
        updateStatsSigns(p,GameType.KITPVP);
        //updateStatsSigns(p,GameType.SOCCER);
        updateStatsSigns(p,GameType.INFECTION_WARS);
        updateStatsSigns(p,GameType.BUILD_AND_GUESS);
        //updateStatsSigns(p,GameType.DEATHMATCH);
        updateStatsSigns(p,GameType.TOBIKO);
    }

    public static void updateStatsSigns(Player p, GameType game){
        ChestUser u = ChestUser.getUser(p);
        final Location sign1 = LocationManager.getLocation("statssign." + game.getAbbreviation().toLowerCase() + ".1");
        final Location sign2 = LocationManager.getLocation("statssign." + game.getAbbreviation().toLowerCase() + ".2");
        final Location sign3 = LocationManager.getLocation("statssign." + game.getAbbreviation().toLowerCase() + ".3");
        final Location sign4 = LocationManager.getLocation("statssign." + game.getAbbreviation().toLowerCase() + ".4");
        final Location sign5 = LocationManager.getLocation("statssign." + game.getAbbreviation().toLowerCase() + ".5");
        final Location sign6 = LocationManager.getLocation("statssign." + game.getAbbreviation().toLowerCase() + ".6");
        final Location head = LocationManager.getLocation("statssign." + game.getAbbreviation().toLowerCase() + ".head");

        if(head.getBlock() != null) head.getBlock().setType(Material.AIR); // TODO: update head skin

        ChestAPI.async(() -> {
            try {
                String[] s1 = new String[]{"","","",""};
                String[] s2 = new String[]{"","","",""};
                String[] s3 = new String[]{"","","",""};
                String[] s4 = new String[]{"","","",""};
                String[] s5 = new String[]{"","","",""};
                String[] s6 = new String[]{"","","",""};

                if(game == GameType.SURVIVAL_GAMES){
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `sg_stats` WHERE `uuid` = ?");
                    ps.setString(1,p.getUniqueId().toString());
                    ResultSet rs = ps.executeQuery();

                    int points = 100;
                    int kills = 0;
                    int deaths = 0;
                    double kd = 0;
                    int playedGames = 0;
                    int victories = 0;
                    int losses = 0;
                    double wl = 0;

                    if(rs.first()){
                        points = rs.getInt("points");
                        kills = rs.getInt("kills");
                        deaths = rs.getInt("deaths");
                        kd = ChestAPI.calculateKD(kills,deaths);
                        playedGames = rs.getInt("playedGames");
                        victories = rs.getInt("victories");
                        losses = playedGames-victories;
                        wl = ChestAPI.calculateWL(victories,losses);
                    }

                    s1 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("Points") + ":",String.valueOf(points),""};
                    s2 = new String[]{ChatColor.BOLD + u.getTranslatedMessage("Kills") + ":",String.valueOf(kills),ChatColor.BOLD + u.getTranslatedMessage("Deaths") + ":",String.valueOf(deaths)};
                    s3 = new String[]{ChatColor.BOLD + u.getTranslatedMessage("Victories") + ":",String.valueOf(victories),ChatColor.BOLD + u.getTranslatedMessage("Losses") + ":",String.valueOf(losses)};
                    s5 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("K/D ratio") + ":",String.valueOf(kd),""};
                    s6 = new String[]{ChatColor.BOLD + u.getTranslatedMessage("Played games") + ":",String.valueOf(playedGames),ChatColor.BOLD + u.getTranslatedMessage("W/L ratio") + ":",String.valueOf(wl)};

                    MySQLManager.getInstance().closeResources(rs,ps);
                } else if(game == GameType.SG_DUELS){
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `sgduels_stats` WHERE `uuid` = ?");
                    ps.setString(1,p.getUniqueId().toString());
                    ResultSet rs = ps.executeQuery();

                    int elo = 1000;
                    int kills = 0;
                    int deaths = 0;
                    double kd = 0;
                    int playedGames = 0;
                    int victories = 0;
                    int losses = 0;
                    double wl = 0;

                    if(rs.first()){
                        elo = rs.getInt("elo");
                        kills = rs.getInt("kills");
                        deaths = rs.getInt("deaths");
                        kd = ChestAPI.calculateKD(kills,deaths);
                        playedGames = rs.getInt("playedGames");
                        victories = rs.getInt("victories");
                        losses = playedGames-victories;
                        wl = ChestAPI.calculateWL(victories,losses);
                    }

                    s1 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("Elo") + ":",String.valueOf(elo),""};
                    s2 = new String[]{ChatColor.BOLD + u.getTranslatedMessage("Kills") + ":",String.valueOf(kills),ChatColor.BOLD + u.getTranslatedMessage("Deaths") + ":",String.valueOf(deaths)};
                    s3 = new String[]{ChatColor.BOLD + u.getTranslatedMessage("Victories") + ":",String.valueOf(victories),ChatColor.BOLD + u.getTranslatedMessage("Losses") + ":",String.valueOf(losses)};
                    s5 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("K/D ratio") + ":",String.valueOf(kd),""};
                    s6 = new String[]{ChatColor.BOLD + u.getTranslatedMessage("Played games") + ":",String.valueOf(playedGames),ChatColor.BOLD + u.getTranslatedMessage("W/L ratio") + ":",String.valueOf(wl)};

                    MySQLManager.getInstance().closeResources(rs,ps);
                } else if(game == GameType.MUSICAL_GUESS){
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `mg_stats` WHERE `uuid` = ?");
                    ps.setString(1,p.getUniqueId().toString());
                    ResultSet rs = ps.executeQuery();

                    int points = 0;
                    int playedGames = 0;
                    int victories = 0;
                    int losses = 0;
                    double wl = 0;

                    if(rs.first()){
                        points = rs.getInt("points");
                        playedGames = rs.getInt("playedGames");
                        victories = rs.getInt("victories");
                        losses = playedGames-victories;
                        wl = ChestAPI.calculateWL(victories,losses);
                    }

                    s1 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("Points") + ":",String.valueOf(points),""};
                    s2 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("Played games") + ":",String.valueOf(playedGames),""};
                    s3 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("Losses") + ":",String.valueOf(losses),""};
                    s5 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("Victories") + ":",String.valueOf(victories),""};
                    s6 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("W/L ratio") + ":",String.valueOf(wl),""};

                    MySQLManager.getInstance().closeResources(rs,ps);
                } else if(game == GameType.KITPVP){
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `kpvp_stats` WHERE `uuid` = ?");
                    ps.setString(1,p.getUniqueId().toString());
                    ResultSet rs = ps.executeQuery();

                    int points = 100;
                    int kills = 0;
                    int deaths = 0;
                    int highestStreak = 0;
                    double kd = 0;

                    if(rs.first()){
                        points = rs.getInt("points");
                        kills = rs.getInt("kills");
                        deaths = rs.getInt("deaths");
                        highestStreak = rs.getInt("highestStreak");
                        kd = ChestAPI.calculateKD(kills,deaths);
                    }

                    s1 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("Points") + ":",String.valueOf(points),""};
                    s2 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("Kills") + ":",String.valueOf(kills),""};
                    s3 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("K/D ratio") + ":",String.valueOf(kd),""};
                    s4 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("Highest streak") + ":",String.valueOf(highestStreak),""};
                    s5 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("Deaths") + ":",String.valueOf(deaths),""};

                    MySQLManager.getInstance().closeResources(rs,ps);
                } else if(game == GameType.SOCCER){

                } else if(game == GameType.INFECTION_WARS){

                } else if(game == GameType.BUILD_AND_GUESS){
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `mg_stats` WHERE `uuid` = ?");
                    ps.setString(1,p.getUniqueId().toString());
                    ResultSet rs = ps.executeQuery();

                    int points = 0;
                    int guessedWords = 0;
                    int playedGames = 0;
                    int victories = 0;
                    int losses = 0;
                    double wl = 0;

                    if(rs.first()){
                        points = rs.getInt("points");
                        playedGames = rs.getInt("playedGames");
                        victories = rs.getInt("victories");
                        losses = playedGames-victories;
                        wl = ChestAPI.calculateWL(victories,losses);
                    }

                    s1 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("Points") + ":",String.valueOf(points),""};
                    s2 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("Played games") + ":",String.valueOf(playedGames),""};
                    s3 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("Losses") + ":",String.valueOf(losses),""};
                    s5 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("Victories") + ":",String.valueOf(victories),""};
                    s6 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("W/L ratio") + ":",String.valueOf(wl),""};

                    MySQLManager.getInstance().closeResources(rs,ps);
                } else if(game == GameType.DEATHMATCH){

                } else if(game == GameType.TOBIKO){
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `tk_stats` WHERE `uuid` = ?");
                    ps.setString(1,p.getUniqueId().toString());
                    ResultSet rs = ps.executeQuery();

                    int points = 0;
                    int hits = 0;
                    int finalHits = 0;
                    int kills = 0;
                    int playedGames = 0;
                    int victories = 0;
                    int losses = 0;
                    double wl = 0;

                    if(rs.first()){
                        points = rs.getInt("points");
                        hits = rs.getInt("hits");
                        finalHits = rs.getInt("finalHits");
                        kills = rs.getInt("kills");
                        playedGames = rs.getInt("playedGames");
                        victories = rs.getInt("victories");
                        losses = playedGames-victories;
                        wl = ChestAPI.calculateWL(victories,losses);
                    }

                    s1 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("Points") + ":",String.valueOf(points),""};
                    s2 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("Victories") + ":",String.valueOf(victories),""};
                    s3 = new String[]{ChatColor.BOLD + u.getTranslatedMessage("Hits") + ":",String.valueOf(hits),ChatColor.BOLD + u.getTranslatedMessage("Final hits") + ":",String.valueOf(finalHits)};
                    s4 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("Kills") + ":",String.valueOf(kills),""};
                    s5 = new String[]{"",ChatColor.BOLD + u.getTranslatedMessage("Losses") + ":",String.valueOf(losses),""};
                    s6 = new String[]{ChatColor.BOLD + u.getTranslatedMessage("Played games") + ":",String.valueOf(playedGames),ChatColor.BOLD + u.getTranslatedMessage("W/L rating") + ":",String.valueOf(wl)};

                    MySQLManager.getInstance().closeResources(rs,ps);
                }

                p.sendSignChange(sign1,s1);
                p.sendSignChange(sign2,s2);
                p.sendSignChange(sign3,s3);
                p.sendSignChange(sign4,s4);
                p.sendSignChange(sign5,s5);
                p.sendSignChange(sign6,s6);
            } catch(Exception e){
                e.printStackTrace();
            }
        });
    }

    public static void handleShow(Player p, Player all){
        ChestUser u = ChestUser.getUser(p);

        if(all != p){
            if((ChestUser.getUser(all).isVanished() && u.hasPermission(Rank.MOD)) || (!ChestUser.getUser(all).isVanished())) p.showPlayer(all);
        }
    }

    public static void handleHide(Player p, Player all){
        ChestUser u = ChestUser.getUser(p);

        if(all != p && ((ChestUser.getUser(all).isVanished() && !u.hasPermission(Rank.MOD))||(!ChestUser.getUser(all).isVanished())) && !ChestUser.getUser(all).hasPermission(Rank.VIP) && !PlayerUtilities.getFriendsFromUUID(p.getUniqueId()).contains(all.getUniqueId().toString())){
            p.hidePlayer(all);
        }
    }

    private static ChestHub instance;

    public static ChestHub getInstance(){
        return instance;
    }

    public static String getPetName(Player p, VaultItem pet){
        String name = null;

        if(pet != null && pet.getCategory() == ItemCategory.PETS){
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `petNames` WHERE `uuid` = ? AND `pet` = ?");
                ps.setString(1,p.getUniqueId().toString());
                ps.setInt(2,pet.getID());

                ResultSet rs = ps.executeQuery();
                if(rs.first()){
                    name = rs.getString("name");
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        return name;
    }

    public static void resetPetName(Player p, VaultItem pet){
        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("DELETE FROM `petNames` WHERE `uuid` = ? AND `pet` = ?");
            ps.setString(1,p.getUniqueId().toString());
            ps.setInt(2,pet.getID());
            ps.executeUpdate();
            ps.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void setPetName(Player p, VaultItem pet, String name){
        if(name == null || name.isEmpty()){
            resetPetName(p,pet);
            return;
        }

        try {
            if(getPetName(p,pet) == null){
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `petNames` (`uuid`,`pet`,`name`) VALUES(?,?,?);");
                ps.setString(1,p.getUniqueId().toString());
                ps.setInt(2,pet.getID());
                ps.setString(3,name);
                ps.executeUpdate();
                ps.close();
            } else {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `petNames` SET `name` = ?, `time` = ? WHERE `uuid` = ? AND `pet` = ?");
                ps.setString(1,name);
                ps.setTimestamp(2,new Timestamp(System.currentTimeMillis()));
                ps.setString(3,p.getUniqueId().toString());
                ps.setInt(4,pet.getID());
                ps.executeUpdate();
                ps.close();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void prepareWorld(World w){
        w.setTime(5000);
        w.setGameRuleValue("doMobSpawning","false");
        w.setGameRuleValue("mobGriefing","false");
        w.setGameRuleValue("doDaylightCycle","false");
        w.setDifficulty(Difficulty.EASY);
        w.setStorm(false);
    }

    public Location getLocationFromConfig(String location){
        String world = getConfig().getString("locations."+location+".world");
        double x = getConfig().getDouble("locations."+location+".x");
        double y = getConfig().getDouble("locations."+location+".y");
        double z = getConfig().getDouble("locations."+location+".z");
        float yaw = getConfig().getInt("locations."+location+".yaw");
        float pitch = getConfig().getInt("locations."+location+".pitch");

        return new Location(Bukkit.getWorld(world),x,y,z,yaw,pitch);
    }

    public void giveLobbyItems(Player p){
        /*if(ChestUser.getUser(p).hasPermission(Rank.VIP)){
            lobbyItemsv2(p);
        } else {
            lobbyItemsv1(p);
        }*/
        lobbyItemsv3(p);
    }

    public static void updateHeadLeaderboards(){
        updateLeaderboard(GameType.SURVIVAL_GAMES);
        updateLeaderboard(GameType.SG_DUELS);
        updateLeaderboard(GameType.MUSICAL_GUESS);
        updateLeaderboard(GameType.KITPVP);
        updateLeaderboard(GameType.SOCCER);
        updateLeaderboard(GameType.INFECTION_WARS);
        updateLeaderboard(GameType.BUILD_AND_GUESS);
        //updateLeaderboard(GameType.DEATHMATCH);
        updateLeaderboard(GameType.TOBIKO);
    }

    private static void updateLeaderboard(GameType g){
        u(g,true);
        u(g,false);
    }

    private static void u(GameType type, boolean overall){
        String s = "monthly";
        if(overall) s = "overall";
        Location head1 = LocationManager.getLocation("toplist.head." + type.getAbbreviation().toLowerCase() + "." + s + ".1");
        Location sign1 = LocationManager.getLocation("toplist.sign." + type.getAbbreviation().toLowerCase() + "." + s + ".1");
        Location head2 = LocationManager.getLocation("toplist.head." + type.getAbbreviation().toLowerCase() + "." + s + ".2");
        Location sign2 = LocationManager.getLocation("toplist.sign." + type.getAbbreviation().toLowerCase() + "." + s + ".2");
        Location head3 = LocationManager.getLocation("toplist.head." + type.getAbbreviation().toLowerCase() + "." + s + ".3");
        Location sign3 = LocationManager.getLocation("toplist.sign." + type.getAbbreviation().toLowerCase() + "." + s + ".3");

        String player1 = null;
        String player2 = null;
        String player3 = null;

        if(type == GameType.SURVIVAL_GAMES){
            try {
                PreparedStatement ps;
                if(overall){
                    ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT u.username FROM `sg_stats` AS s INNER JOIN `users` AS u ON u.uuid = s.uuid ORDER BY s.points DESC LIMIT 3");
                } else {
                    ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT u.username FROM `sg_stats` AS s INNER JOIN `users` AS u ON u.uuid = s.uuid ORDER BY s.monthlyPoints DESC LIMIT 3");
                }

                ResultSet rs = ps.executeQuery();
                while(rs.next()){
                    if(player1 == null){
                        player1 = rs.getString("username");
                    } else if(player2 == null){
                        player2 = rs.getString("username");
                    } else if(player3 == null){
                        player3 = rs.getString("username");
                    }
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }
        } else if(type == GameType.SG_DUELS){
            try {
                PreparedStatement ps;
                if(overall){
                    ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT u.username FROM `sgduels_stats` AS s INNER JOIN `users` AS u ON u.uuid = s.uuid ORDER BY s.elo DESC LIMIT 3");
                } else {
                    ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT u.username FROM `sgduels_stats` AS s INNER JOIN `users` AS u ON u.uuid = s.uuid ORDER BY s.monthlyElo DESC LIMIT 3");
                }

                ResultSet rs = ps.executeQuery();
                while(rs.next()){
                    if(player1 == null){
                        player1 = rs.getString("username");
                    } else if(player2 == null){
                        player2 = rs.getString("username");
                    } else if(player3 == null){
                        player3 = rs.getString("username");
                    }
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }
        } else if(type == GameType.MUSICAL_GUESS){
            try {
                PreparedStatement ps;
                if(overall){
                    ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT u.username FROM `mg_stats` AS s INNER JOIN `users` AS u ON u.uuid = s.uuid ORDER BY s.points DESC LIMIT 3");
                } else {
                    ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT u.username FROM `mg_stats` AS s INNER JOIN `users` AS u ON u.uuid = s.uuid ORDER BY s.monthlyPoints DESC LIMIT 3");
                }

                ResultSet rs = ps.executeQuery();
                while(rs.next()){
                    if(player1 == null){
                        player1 = rs.getString("username");
                    } else if(player2 == null){
                        player2 = rs.getString("username");
                    } else if(player3 == null){
                        player3 = rs.getString("username");
                    }
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }
        } else if(type == GameType.KITPVP){
            try {
                PreparedStatement ps;
                if(overall){
                    ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT u.username FROM `kpvp_stats` AS s INNER JOIN `users` AS u ON u.uuid = s.uuid ORDER BY s.points DESC LIMIT 3");
                } else {
                    ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT u.username FROM `kpvp_stats` AS s INNER JOIN `users` AS u ON u.uuid = s.uuid ORDER BY s.monthlyPoints DESC LIMIT 3");
                }

                ResultSet rs = ps.executeQuery();
                while(rs.next()){
                    if(player1 == null){
                        player1 = rs.getString("username");
                    } else if(player2 == null){
                        player2 = rs.getString("username");
                    } else if(player3 == null){
                        player3 = rs.getString("username");
                    }
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }
        } else if(type == GameType.SOCCER){
            try {
                PreparedStatement ps;
                if(overall){
                    ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT u.username FROM `soccer_stats` AS s INNER JOIN `users` AS u ON u.uuid = s.uuid ORDER BY s.points DESC LIMIT 3");
                } else {
                    ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT u.username FROM `soccer_stats` AS s INNER JOIN `users` AS u ON u.uuid = s.uuid ORDER BY s.monthlyPoints DESC LIMIT 3");
                }

                ResultSet rs = ps.executeQuery();
                while(rs.next()){
                    if(player1 == null){
                        player1 = rs.getString("username");
                    } else if(player2 == null){
                        player2 = rs.getString("username");
                    } else if(player3 == null){
                        player3 = rs.getString("username");
                    }
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }
        } else if(type == GameType.INFECTION_WARS){
            try {
                PreparedStatement ps;
                if(overall){
                    ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT u.username FROM `infw_stats` AS s INNER JOIN `users` AS u ON u.uuid = s.uuid ORDER BY s.points DESC LIMIT 3");
                } else {
                    ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT u.username FROM `infw_stats` AS s INNER JOIN `users` AS u ON u.uuid = s.uuid ORDER BY s.monthlyPoints DESC LIMIT 3");
                }

                ResultSet rs = ps.executeQuery();
                while(rs.next()){
                    if(player1 == null){
                        player1 = rs.getString("username");
                    } else if(player2 == null){
                        player2 = rs.getString("username");
                    } else if(player3 == null){
                        player3 = rs.getString("username");
                    }
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }
        } else if(type == GameType.BUILD_AND_GUESS){
            try {
                PreparedStatement ps;
                if(overall){
                    ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT u.username FROM `bg_stats` AS s INNER JOIN `users` AS u ON u.uuid = s.uuid ORDER BY s.points DESC LIMIT 3");
                } else {
                    ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT u.username FROM `bg_stats` AS s INNER JOIN `users` AS u ON u.uuid = s.uuid ORDER BY s.monthlyPoints DESC LIMIT 3");
                }

                ResultSet rs = ps.executeQuery();
                while(rs.next()){
                    if(player1 == null){
                        player1 = rs.getString("username");
                    } else if(player2 == null){
                        player2 = rs.getString("username");
                    } else if(player3 == null){
                        player3 = rs.getString("username");
                    }
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }
        } else if(type == GameType.DEATHMATCH){
            try {
                PreparedStatement ps;
                if(overall){
                    ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT u.username FROM `dm_stats` AS s INNER JOIN `users` AS u ON u.uuid = s.uuid ORDER BY s.points DESC LIMIT 3");
                } else {
                    ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT u.username FROM `dm_stats` AS s INNER JOIN `users` AS u ON u.uuid = s.uuid ORDER BY s.monthlyPoints DESC LIMIT 3");
                }

                ResultSet rs = ps.executeQuery();
                while(rs.next()){
                    if(player1 == null){
                        player1 = rs.getString("username");
                    } else if(player2 == null){
                        player2 = rs.getString("username");
                    } else if(player3 == null){
                        player3 = rs.getString("username");
                    }
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }
        } else if(type == GameType.TOBIKO){
            try {
                PreparedStatement ps;
                if(overall){
                    ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT u.username FROM `tk_stats` AS s INNER JOIN `users` AS u ON u.uuid = s.uuid ORDER BY s.points DESC LIMIT 3");
                } else {
                    ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT u.username FROM `tk_stats` AS s INNER JOIN `users` AS u ON u.uuid = s.uuid ORDER BY s.monthlyPoints DESC LIMIT 3");
                }

                ResultSet rs = ps.executeQuery();
                while(rs.next()){
                    if(player1 == null){
                        player1 = rs.getString("username");
                    } else if(player2 == null){
                        player2 = rs.getString("username");
                    } else if(player3 == null){
                        player3 = rs.getString("username");
                    }
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        if(player1 != null) us(player1,head1,sign1,1);
        if(player2 != null) us(player2,head2,sign2,2);
        if(player3 != null) us(player3,head3,sign3,3);
    }

    private static void us(String player,Location head,Location sign,int count){
        if(head.getBlock() != null && head.getBlock().getType() != null && head.getBlock().getType() == Material.SKULL){
            Skull s = (Skull)head.getBlock().getState();
            s.setOwner(player);
            s.update();
        }

        if(sign.getBlock() != null && sign.getBlock().getType() != null && (sign.getBlock().getType() == Material.SIGN || sign.getBlock().getType() == Material.WALL_SIGN || sign.getBlock().getType() == Material.SIGN_POST)){
            Sign s = (Sign)sign.getBlock().getState();
            s.setLine(0,"--*+*--");
            s.setLine(1,ChatColor.BOLD + "#" + count);
            s.setLine(2,player);
            s.setLine(3,"--*+*--");
            s.update();
        }
    }

    private void lobbyItemsv1(Player p){
        ChestUser u = ChestUser.getUser(p);

        p.getInventory().clear();

        ItemStack profile = new ItemStack(Material.SKULL_ITEM);
        profile.setDurability((short)3);
        SkullMeta profileM = (SkullMeta)profile.getItemMeta();
        profileM.setOwner(p.getName());
        profileM.setDisplayName(ChatColor.RED + u.getTranslatedMessage("My Profile") + " " + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "(" + u.getTranslatedMessage("Right click") + ")");
        profile.setItemMeta(profileM);

        ItemStack gamemode = ItemUtil.namedItem(Material.EMERALD, ChatColor.DARK_GREEN + u.getTranslatedMessage("Gamemodes") + " " + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "(" + u.getTranslatedMessage("Right click") + ")", null);
        ItemStack feather = ItemUtil.namedItem(Material.FEATHER, ChatColor.GREEN + u.getTranslatedMessage("Fly Mode") + " " + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "(" + u.getTranslatedMessage("Right click") + ")", null); // MOVED TO GADGETS
        ItemStack togglePlayers = ItemUtil.namedItem(Material.NETHER_STAR, ChatColor.YELLOW + u.getTranslatedMessage("Toggle players") + " " + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "(" + u.getTranslatedMessage("Right click") + ")", null);
        VaultItem gadget = u.getActiveItem(ItemCategory.GADGET);

        if(gadget != null){
            ItemStack i = gadget.getItem();
            ItemMeta iM = i.getItemMeta();
            iM.setDisplayName(gadget.getRarity().getColor() + u.getTranslatedMessage(gadget.getName()) + " " + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "(" + u.getTranslatedMessage("Right click") + ")");
            i.setItemMeta(iM);

            p.getInventory().setItem(1,gamemode);
            p.getInventory().setItem(3,ItemUtil.hideFlags(i));
            p.getInventory().setItem(5,togglePlayers);
            p.getInventory().setItem(7,profile);
        } else {
            p.getInventory().setItem(0,gamemode);
            p.getInventory().setItem(4,togglePlayers);
            p.getInventory().setItem(8,profile);
        }
    }

    private void lobbyItemsv2(Player p){
        ChestUser u = ChestUser.getUser(p);

        p.getInventory().clear();

        ItemStack profile = new ItemStack(Material.SKULL_ITEM);
        profile.setDurability((short)3);
        SkullMeta profileM = (SkullMeta)profile.getItemMeta();
        profileM.setOwner(p.getName());
        profileM.setDisplayName(ChatColor.RED + u.getTranslatedMessage("My Profile") + " " + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "(" + u.getTranslatedMessage("Right click") + ")");
        profile.setItemMeta(profileM);

        ItemStack gamemode = ItemUtil.namedItem(Material.EMERALD, ChatColor.DARK_GREEN + u.getTranslatedMessage("Gamemodes") + " " + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "(" + u.getTranslatedMessage("Right click") + ")", null);
        ItemStack feather = ItemUtil.namedItem(Material.FEATHER, ChatColor.GREEN + u.getTranslatedMessage("Fly Mode") + " " + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "(" + u.getTranslatedMessage("Right click") + ")", null); // MOVED TO GADGETS
        ItemStack togglePlayers = ItemUtil.namedItem(Material.NETHER_STAR, ChatColor.YELLOW + u.getTranslatedMessage("Toggle players") + " " + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "(" + u.getTranslatedMessage("Right click") + ")", null);
        ItemStack challenger = ItemUtil.namedItem(Material.SHEARS, ChatColor.DARK_AQUA + u.getTranslatedMessage("Challenger") + " " + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "(" + u.getTranslatedMessage("Left click on players") + ")",null);
        VaultItem gadget = u.getActiveItem(ItemCategory.GADGET);

        p.getInventory().addItem(gamemode);
        p.getInventory().addItem(profile);
        p.getInventory().addItem(togglePlayers);
        p.getInventory().addItem(ItemUtil.hideFlags(ItemUtil.setUnbreakable(challenger,true)));

        if(gadget != null){
            ItemStack i = gadget.getItem();
            ItemMeta iM = i.getItemMeta();
            iM.setDisplayName(gadget.getRarity().getColor() + u.getTranslatedMessage(gadget.getName()) + " " + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "(" + u.getTranslatedMessage("Right click") + ")");
            i.setItemMeta(iM);
            p.getInventory().addItem(ItemUtil.hideFlags(i));
        }
    }

    private void lobbyItemsv3(Player p){
        ChestUser u = ChestUser.getUser(p);

        p.getInventory().clear();

        ItemStack profile = new ItemStack(Material.SKULL_ITEM);
        profile.setDurability((short)3);
        SkullMeta profileM = (SkullMeta)profile.getItemMeta();
        profileM.setOwner(p.getName());
        profileM.setDisplayName(ChatColor.YELLOW + u.getTranslatedMessage("My Profile") + " " + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "(" + u.getTranslatedMessage("Right click") + ")");
        profile.setItemMeta(profileM);

        ItemStack gamemode = ItemUtil.namedItem(Material.EMERALD, ChatColor.YELLOW + u.getTranslatedMessage("Game Modes") + " " + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "(" + u.getTranslatedMessage("Right click") + ")", null);
        ItemStack togglePlayers = ItemUtil.namedItem(Material.NETHER_STAR, ChatColor.YELLOW + u.getTranslatedMessage("Toggle players") + " " + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "(" + u.getTranslatedMessage("Right click") + ")", null);
        ItemStack challenger = ItemUtil.namedItem(Material.SHEARS, ChatColor.YELLOW + u.getTranslatedMessage("Challenger") + " " + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "(" + u.getTranslatedMessage("Left click on players") + ")",null);
        VaultItem gadget = u.getActiveItem(ItemCategory.GADGET);

        p.getInventory().setItem(0,challenger);
        if(gadget != null){
            ItemStack i = gadget.getItem();
            ItemMeta iM = i.getItemMeta();
            iM.setDisplayName(gadget.getRarity().getColor() + u.getTranslatedMessage(gadget.getName()) + " " + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "(" + u.getTranslatedMessage("Right click") + ")");
            i.setItemMeta(iM);
            p.getInventory().setItem(1,ItemUtil.hideFlags(i));
        }

        p.getInventory().setItem(4,gamemode);

        p.getInventory().setItem(7,togglePlayers);
        p.getInventory().setItem(8,profile);
    }

    public void updatePet(Player p){
        ChestAPI.sync(() -> {
            ChestUser u = ChestUser.getUser(p);
            VaultItem petItem = u.getActiveItem(ItemCategory.PETS);

            if(EchoPetAPI.getAPI().getPet(p) != null) EchoPetAPI.getAPI().removePet(p,false,false);
            if(petItem == null) return;
            if(u.isVanished()) return;

            String petName = getPetName(p,petItem);

            IPet pet = null;

            if(petItem.getID() == 24){
                pet = EchoPetAPI.getAPI().givePet(p, PetType.SHEEP,false);
                ((SheepPet)pet).setBaby(true);
            } else if(petItem.getID() == 25){
                pet = EchoPetAPI.getAPI().givePet(p, PetType.CHICKEN,false);
                ((ChickenPet)pet).setBaby(true);
            } else if(petItem.getID() == 26){
                pet = EchoPetAPI.getAPI().givePet(p, PetType.CHICKEN,false);
            } else if(petItem.getID() == 27){
                pet = EchoPetAPI.getAPI().givePet(p, PetType.OCELOT,false);
            } else if(petItem.getID() == 28){
                pet = EchoPetAPI.getAPI().givePet(p, PetType.OCELOT,false);
                ((OcelotPet)pet).setBaby(true);
            } else if(petItem.getID() == 29){
                pet = EchoPetAPI.getAPI().givePet(p, PetType.SLIME,false);
                ((SlimePet)pet).setSize(1);
            } else if(petItem.getID() == 30){
                pet = EchoPetAPI.getAPI().givePet(p, PetType.WOLF,false);
            } else if(petItem.getID() == 31){
                pet = EchoPetAPI.getAPI().givePet(p, PetType.WOLF,false);
                ((WolfPet)pet).setBaby(true);
            } else if(petItem.getID() == 32){
                pet = EchoPetAPI.getAPI().givePet(p, PetType.VILLAGER,false);
                ((VillagerPet)pet).setProfession(Villager.Profession.FARMER);
            } else if(petItem.getID() == 33){
                pet = EchoPetAPI.getAPI().givePet(p, PetType.COW,false);
            } else if(petItem.getID() == 34){
                pet = EchoPetAPI.getAPI().givePet(p, PetType.COW,false);
                ((CowPet)pet).setBaby(true);
            } else if(petItem.getID() == 35){
                pet = EchoPetAPI.getAPI().givePet(p, PetType.VILLAGER,false);
                ((VillagerPet)pet).setProfession(Villager.Profession.FARMER);
                ((VillagerPet)pet).setBaby(true);
            } else if(petItem.getID() == 91){
                pet = EchoPetAPI.getAPI().givePet(p, PetType.CREEPER,false);
            } else if(petItem.getID() == 107){
                pet = EchoPetAPI.getAPI().givePet(p, PetType.ZOMBIE,false);
                ((ZombiePet)pet).setBaby(false);
            } else if(petItem.getID() == 108){
                pet = EchoPetAPI.getAPI().givePet(p, PetType.ZOMBIE,false);
                ((ZombiePet)pet).setBaby(true);
            } else if(petItem.getID() == 109){
                pet = EchoPetAPI.getAPI().givePet(p, PetType.SKELETON,false);
            } else if(petItem.getID() == 110){
                pet = EchoPetAPI.getAPI().givePet(p, PetType.VILLAGER,false);
                ((VillagerPet)pet).setProfession(Villager.Profession.PRIEST);
            } else if(petItem.getID() == 111){
                pet = EchoPetAPI.getAPI().givePet(p, PetType.BLAZE,false);
            } else if(petItem.getID() == 113){
                pet = EchoPetAPI.getAPI().givePet(p, PetType.HUMAN,false);
            }

            if(pet != null){
                net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) pet.getCraftPet()).getHandle();
                NBTTagCompound tag = nmsEntity.getNBTTag();
                if (tag == null) {
                    tag = new NBTTagCompound();
                }
                nmsEntity.c(tag);
                tag.setInt("Silent", 1);
                nmsEntity.f(tag);

                if(petName != null){
                    pet.setPetName(petName);
                } else {
                    pet.setPetName(p.getDisplayName() + ChatColor.GRAY + "'s pet");
                }
            }
        });
    }

    public void giveLobbyHelmet(Player p){
        ChestUser u = ChestUser.getUser(p);
        VaultItem hat = u.getActiveItem(ItemCategory.LOBBY_HAT);
        if(hat != null){
            p.getInventory().setHelmet(null);

            if(hat.getID() == 11 || hat.getID() == 12 || hat.getID() == 13 || hat.getID() == 14 || hat.getID() == 15 || hat.getID() == 16 || hat.getID() == 17 || hat.getID() == 19 || hat.getID() == 20){
                Color color = null;

                if(hat.getID() == 20){
                    color = Color.fromRGB(232,169,21);
                } else if(hat.getID() == 19){
                    color = Color.fromRGB(12,85,138);
                } else if(hat.getID() == 17){
                    color = Color.fromRGB(171,15,142);
                } else if(hat.getID() == 16){
                    color = Color.fromRGB(230,226,11);
                } else if(hat.getID() == 15){
                    color = Color.fromRGB(13,46,181);
                } else if(hat.getID() == 14){
                    color = Color.fromRGB(19,214,42);
                } else if(hat.getID() == 13){
                    color = Color.fromRGB(8,115,20);
                } else if(hat.getID() == 12 || hat.getID() == 11){
                    color = Color.fromRGB(255,0,0);
                }

                if(color != null){
                    ItemStack h = new ItemStack(Material.LEATHER_HELMET,1);
                    LeatherArmorMeta lam = (LeatherArmorMeta)h.getItemMeta();
                    lam.setColor(color);
                    h.setItemMeta(lam);
                    p.getInventory().setHelmet(h);
                }
            } else {
                if(hat.getID() == 18){
                    p.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                } else if(hat.getID() == 23){
                    p.getInventory().setHelmet(new ItemStack(Material.GOLD_HELMET));
                } else {
                    if(hat.getSkinURL() != null && !hat.getSkinURL().isEmpty()){
                        ItemStack i = ItemUtil.profiledSkullCustom(hat.getSkinURL());
                        ItemMeta m = i.getItemMeta();
                        m.setDisplayName(hat.getRarity().getColor() + hat.getName());
                        i.setItemMeta(m);

                        p.getInventory().setHelmet(i);
                    }
                }
            }
        } else {
            p.getInventory().setHelmet(null);
        }
    }
}
