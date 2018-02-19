package eu.thechest.chesthub.inv;

import com.gmail.filoghost.holographicdisplays.object.NamedHologram;
import com.gmail.filoghost.holographicdisplays.object.NamedHologramManager;
import eu.thechest.chestapi.items.*;
import eu.thechest.chestapi.lang.TranslatedHoloLine;
import eu.thechest.chestapi.lang.TranslatedHologram;
import eu.thechest.chestapi.server.ServerSettingsManager;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.user.ScoreboardType;
import eu.thechest.chestapi.util.ChatIcons;
import eu.thechest.chestapi.util.ParticleEffect;
import eu.thechest.chestapi.util.StringUtils;
import eu.thechest.chesthub.ChestHub;
import eu.thechest.chesthub.vault.Vault;
import eu.thechest.chesthub.vault.VaultStorage;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.TileEntityChest;
import net.minecraft.server.v1_8_R3.TileEntityEnderChest;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by zeryt on 10.04.2017.
 */
public class VaultMenu implements Listener {
    public static void openFor(Player p, Vault v){
        openFor(p,1,v);
    }

    public static void openFor(Player p, int page, Vault vault){
        if(page > 0){
            ChestUser u = ChestUser.getUser(p);
            Inventory inv = Bukkit.createInventory(null,9*6, "Chest Vault | Page " + page);
            if(!vault.currentOpeners.contains(p)) vault.currentOpeners.add(p);
            new BukkitRunnable(){
                @Override
                public void run() {
                    if(!vault.currentOpeners.contains(p)) vault.currentOpeners.add(p);
                }
            }.runTaskLater(ChestHub.getInstance(),1L);

            inv.setItem(36,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
            inv.setItem(37,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
            inv.setItem(38,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
            inv.setItem(39,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
            inv.setItem(40,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
            inv.setItem(41,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
            inv.setItem(42,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
            inv.setItem(43,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
            inv.setItem(44,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));

            ArrayList<UnlockedChest> chests = new ArrayList<UnlockedChest>();
            int sizePerPage = 36;

            for(UnlockedChest chest : u.getUnlockedChests().values()){
                chests.add(chest);
            }

            int total = chests.size();

            Collections.sort(chests, new Comparator<UnlockedChest>() {
                public int compare(UnlockedChest p1, UnlockedChest p2) {
                    return p2.id - p1.id;
                }
            });

            if(total > 0){
                for(UnlockedChest v : chests.stream().skip((page-1) * sizePerPage).limit(sizePerPage).collect(Collectors.toCollection(ArrayList::new))){
                    ItemStack i = v.iconToItemStack();
                    ItemMeta iM = i.getItemMeta();

                    iM.setDisplayName(ChatColor.AQUA.toString() + ChatColor.BOLD.toString() + u.getTranslatedMessage(v.name));
                    ArrayList<String> lore = new ArrayList<String>();
                    lore.add(" ");
                    lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + u.getTranslatedMessage("Possible contents:"));

                    List<Integer> possible = Arrays.asList(v.items.toArray(new Integer[]{}));
                    Collections.sort(possible, new Comparator<Integer>() {
                        public int compare(Integer p1, Integer p2) {
                            return VaultItem.getItem(p1).getRarity().getChanceAmount() - VaultItem.getItem(p2).getRarity().getChanceAmount();
                        }
                    });

                    for(int item : possible){
                        if(VaultItem.getItem(item) != null){
                            if(u.hasUnlocked(item)){
                                lore.add(ChatColor.DARK_GREEN.toString() + ChatColor.BOLD.toString() + ChatIcons.CHECK_MARK + " " + VaultItem.getItem(item).getRarity().getColor() + u.getTranslatedMessage(VaultItem.getItem(item).getRarity().getName() + " " + VaultItem.getItem(item).getName() + " " + VaultItem.getItem(item).getCategory().getNameSingular()));
                            } else {
                                lore.add(ChatColor.DARK_RED.toString() + ChatColor.BOLD.toString() + ChatIcons.X + " " + VaultItem.getItem(item).getRarity().getColor() + u.getTranslatedMessage(VaultItem.getItem(item).getRarity().getName() + " " + VaultItem.getItem(item).getName() + " " + VaultItem.getItem(item).getCategory().getNameSingular()));
                            }
                        }
                    }

                    lore.add("  ");
                    lore.add(ChatColor.GOLD + u.getTranslatedMessage("Click to open!"));
                    lore.add(ChatColor.RED + "[" + u.getTranslatedMessage("Costs 1 key") + "]");
                    lore.add(ChatColor.DARK_GRAY.toString() + "ID: #" + v.id);

                    iM.setLore(lore);
                    i.setItemMeta(iM);
                    inv.addItem(i);
                }
            } else {
                inv.addItem(ItemUtil.namedItem(Material.STONE, ChatColor.RED.toString() + ChatColor.BOLD.toString() + u.getTranslatedMessage("You don't have any chests!"), new String[]{ChatColor.GRAY + u.getTranslatedMessage("You can get more chests by playing games!")}));
            }

            double d = (((double)total)/((double)sizePerPage));
            int maxPages = ((Double)d).intValue();
            if(maxPages < d) maxPages++;

            if(page != 1) inv.setItem(48,ItemUtil.namedItem(Material.ARROW, org.bukkit.ChatColor.GOLD + "<< " + org.bukkit.ChatColor.AQUA + u.getTranslatedMessage("Previous page"), null));
            inv.setItem(49, ItemUtil.namedItem(Material.BARRIER, org.bukkit.ChatColor.DARK_RED + u.getTranslatedMessage("Close"), null));
            if(maxPages > page) inv.setItem(50,ItemUtil.namedItem(Material.ARROW, org.bukkit.ChatColor.AQUA + u.getTranslatedMessage("Next page") + org.bukkit.ChatColor.GOLD + " >>", null));

            inv.setItem(45,ItemUtil.namedItem(Material.ENDER_CHEST, ChatColor.AQUA + u.getTranslatedMessage("Total chests") + ":" + ChatColor.YELLOW + " " + u.getUnlockedChests().size(), new String[]{ChatColor.AQUA + u.getTranslatedMessage("Total shards") + ":" + ChatColor.YELLOW + " " + u.getVaultShards(),ChatColor.AQUA + u.getTranslatedMessage("Total chests opened") + ": " + ChatColor.YELLOW.toString() + u.getChestsOpened()}));
            inv.setItem(46,ItemUtil.namedItem(Material.PRISMARINE_SHARD,ChatColor.AQUA + u.getTranslatedMessage("Open Shop"),null));
            inv.setItem(53,ItemUtil.namedItem(Material.TRIPWIRE_HOOK,ChatColor.AQUA + u.getTranslatedMessage("Keys") + ":" + ChatColor.YELLOW + " " + u.getKeys(), new String[]{" ",ChatColor.GRAY + u.getTranslatedMessage("Buy more on %s!").replace("%s",ChatColor.RED.toString() + ChatColor.BOLD.toString() + "thechest.eu/store" + ChatColor.GRAY)}));

            p.openInventory(inv);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(e.getWhoClicked() instanceof Player){
            Player p = (Player)e.getWhoClicked();
            ChestUser u = ChestUser.getUser(p);
            Inventory inv = e.getInventory();
            int slot = e.getRawSlot();

            if(inv.getName().startsWith("Chest Vault")){
                Vault v = VaultStorage.getVaultByPlayer(p);

                if(v != null){
                    e.setCancelled(true);

                    String a = inv.getName().replace("Chest Vault | Page ","");
                    if(StringUtils.isValidInteger(a)){
                        int page = Integer.parseInt(a);

                        if(e.getCurrentItem() != null && e.getCurrentItem().getItemMeta() != null && e.getCurrentItem().getItemMeta().getDisplayName() != null){
                            if(slot >= 0 && slot <= 35){
                                if(e.getCurrentItem().getItemMeta().getLore() != null &&e.getCurrentItem().getItemMeta().getLore().size() > 0){
                                    String z = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getLore().get(e.getCurrentItem().getItemMeta().getLore().size()-1)).replace("ID: #","");
                                    if(StringUtils.isValidInteger(z)){
                                        int id = Integer.parseInt(z);

                                        boolean b = true;

                                        for(Vault vault : VaultStorage.STORAGE){
                                            if(vault.player == p) b = false;
                                        }

                                        p.closeInventory();

                                        if(u.isVanished()){
                                            p.playSound(p.getEyeLocation(), Sound.NOTE_BASS,1f,0.5f);
                                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + net.md_5.bungee.api.ChatColor.RED + u.getTranslatedMessage("You can't do that in vanish mode."));
                                            return;
                                        }

                                        v.currentOpeners.remove(p);
                                        if(b){
                                            if(v.player == null && v.opening == false){
                                                if(u.getKeys() > 0){
                                                    ArrayList<VaultItem> items = new ArrayList<VaultItem>();
                                                    UnlockedChest chest = u.getUnlockedChests().get(id);
                                                    for(int i : chest.items){
                                                        for(int j = 0; j < VaultItem.getItem(i).getRarity().getChanceAmount(); j++){
                                                            items.add(VaultItem.getItem(i));
                                                        }
                                                    }

                                                    Collections.shuffle(items);
                                                    VaultItem item = items.get(0);
                                                    int shards = 0;

                                                    if(u.hasUnlocked(item)){
                                                        if(item.getRarity() == ItemRarity.COMMON){
                                                            shards = 12;
                                                        } else if(item.getRarity() == ItemRarity.RARE){
                                                            shards = 45;
                                                        } else if(item.getRarity() == ItemRarity.EPIC){
                                                            shards = 180;
                                                        } else if(item.getRarity() == ItemRarity.LEGENDARY){
                                                            shards = 402;
                                                        } else if(item.getRarity() == ItemRarity.MYTHIC){
                                                            shards = 1112;
                                                        }

                                                        u.addVaultShards(shards);
                                                    } else {
                                                        u.unlockItem(item);
                                                    }

                                                    u.removeChest(id);
                                                    u.reduceKeys(1);
                                                    u.addChestsOpened(1);

                                                    v.player = p;
                                                    v.opening = true;

                                                    Location holoLocation = v.holo.getLocation();

                                                    v.holo.unregister();

                                                    Location loc = v.chestLoc.clone();
                                                    loc.setX(holoLocation.getX());
                                                    loc.setZ(holoLocation.getZ());
                                                    loc.setYaw(v.animationYaw);
                                                    v.animationStand = (ArmorStand)loc.getWorld().spawnEntity(loc.clone().add(0,-2,0), EntityType.ARMOR_STAND);

                                                    v.animationStand.setGravity(false);
                                                    v.animationStand.setVisible(false);
                                                    v.animationStand.setSmall(false);
                                                    v.animationStand.getEquipment().setHelmet(e.getCurrentItem());
                                                    //v.animationStand.setCustomName(ChatColor.GRAY + "Opening " + p.getDisplayName() + ChatColor.GRAY + "'s chest!");
                                                    v.animationStand.setCustomName(ChatColor.AQUA.toString() + ChatColor.BOLD.toString() + chest.name);
                                                    v.animationStand.setCustomNameVisible(true);
                                                    v.animationStand.setBasePlate(false);

                                                    net.minecraft.server.v1_8_R3.World world = ((CraftWorld) v.chestLoc.getWorld()).getHandle();
                                                    BlockPosition position = new BlockPosition(v.chestLoc.getBlockX(), v.chestLoc.getBlockY(), v.chestLoc.getBlockZ());
                                                    TileEntityEnderChest tileChest = (TileEntityEnderChest) world.getTileEntity(position);
                                                    world.playBlockAction(position, tileChest.w(), 1, 1);

                                                    double i = 0;

                                                    for(int r = 0; r < 28; r++){
                                                        final int f = r;
                                                        final double g = i;
                                                        new BukkitRunnable(){
                                                            @Override
                                                            public void run() {
                                                                v.animationStand.teleport(loc.clone().add(0,g,0));
                                                            }
                                                        }.runTaskLater(ChestHub.getInstance(), 2*r);

                                                        i += 0.125;
                                                    }

                                                    for(int r = 0; r <= 12; r++){
                                                        final int y = r;
                                                        new BukkitRunnable(){
                                                            @Override
                                                            public void run() {
                                                                if(v.animationStand != null){
                                                                    Location z = v.animationStand.getLocation();
                                                                    //p.sendMessage(String.valueOf(y));
                                                                    z.getWorld().playSound(z,Sound.NOTE_PLING,1f,1f);
                                                                }
                                                            }
                                                        }.runTaskLater(ChestHub.getInstance(), (long)Math.pow(r,2));
                                                    }

                                                    for(int r = 0; r < 30; r++){
                                                        new BukkitRunnable(){
                                                            @Override
                                                            public void run() {
                                                                Location a = v.chestLoc.clone();
                                                                a.setX(holoLocation.getX());
                                                                a.setZ(holoLocation.getZ());

                                                                Location center = a.add(0,3,0);
                                                                double radius = 1;
                                                                int amount = 15;
                                                                World world = center.getWorld();
                                                                double increment = (2 * Math.PI) / amount;
                                                                ArrayList<Location> locations = new ArrayList<Location>();
                                                                for(int i = 0;i < amount; i++) {
                                                                    double angle = i * increment;
                                                                    double x = center.getX() + (radius * Math.cos(angle));
                                                                    double z = center.getZ() + (radius * Math.sin(angle));
                                                                    locations.add(new Location(world, x, center.getY(), z));
                                                                }

                                                                for(Location loc : locations){
                                                                    ParticleEffect.FIREWORKS_SPARK.display(0f,0f,0f,0f,amount,loc,30);
                                                                }
                                                            }
                                                        }.runTaskLater(ChestHub.getInstance(), 2+(2*r));
                                                    }

                                                    new BukkitRunnable(){
                                                        @Override
                                                        public void run() {
                                                            //Location a = v.chestLoc.clone().add(0,5,0);
                                                            Location a = v.animationStand.getEyeLocation().clone().add(0,0.75,0);
                                                            /*a.setX(holoLocation.getX());
                                                            a.setZ(holoLocation.getZ());*/
                                                            ParticleEffect.ENCHANTMENT_TABLE.display(0f,0f,0f,1f,150,a,30);
                                                            ParticleEffect.ENCHANTMENT_TABLE.display(0f,0f,0f,1f,150,a,30);

                                                            a.getWorld().playSound(a,Sound.FIREWORK_TWINKLE,1f,1f);
                                                        }
                                                    }.runTaskLater(ChestHub.getInstance(), 5*20);

                                                    new BukkitRunnable(){
                                                        @Override
                                                        public void run() {
                                                            //Location a = v.chestLoc.clone().add(0,4,0);
                                                            Location a = v.animationStand.getEyeLocation().clone().add(0,0.75,0);
                                                            /*a.setX(holoLocation.getX());
                                                            a.setZ(holoLocation.getZ());*/

                                                            ParticleEffect.EXPLOSION_LARGE.display(0f,0f,0f,0f,10,a,30);
                                                            a.getWorld().playSound(a,Sound.EXPLODE,1f,1f);
                                                        }
                                                    }.runTaskLater(ChestHub.getInstance(), 8*20);

                                                    final int s = shards;
                                                    new BukkitRunnable(){
                                                        @Override
                                                        public void run() {
                                                            Location nHolo = holoLocation.add(0,0.5,0);
                                                            if(s > 0){
                                                                v.holo = new TranslatedHologram(
                                                                        new TranslatedHoloLine[]{
                                                                                new TranslatedHoloLine(item.getItem()),
                                                                                new TranslatedHoloLine(item.getRarity().getColor() + item.getName() + " " + item.getCategory().getNameSingular()),
                                                                                new TranslatedHoloLine(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "DUPLICATE!")
                                                                        },nHolo);
                                                            } else {
                                                                v.holo = new TranslatedHologram(
                                                                        new TranslatedHoloLine[]{
                                                                                new TranslatedHoloLine(item.getItem()),
                                                                                new TranslatedHoloLine(item.getRarity().getColor() + item.getName() + " " + item.getCategory().getNameSingular())
                                                                        },nHolo);
                                                            }

                                                            p.sendMessage(ChatColor.AQUA + StringUtils.LINE_SEPERATOR);
                                                            u.sendCenteredMessage(ChatColor.AQUA.toString() + ChatColor.BOLD.toString() + u.getTranslatedMessage("Chest Vault"),true);
                                                            p.sendMessage(" ");
                                                            u.sendCenteredMessage(item.getRarity().getColor() + item.getName() + " " + item.getCategory().getNameSingular(),true);
                                                            if(s > 0){
                                                                u.sendCenteredMessage(ChatColor.RED.toString() + ChatColor.BOLD.toString() + u.getTranslatedMessage("Duplicate").toUpperCase() + "! " + ChatColor.DARK_AQUA + "(" + ChatColor.GREEN + "+" + ChatColor.DARK_AQUA + s + " " + u.getTranslatedMessage("Vault Shards") + ")",true);
                                                            } else {
                                                                u.sendCenteredMessage(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + u.getTranslatedMessage("NEW ITEM!"),true);
                                                            }
                                                            p.sendMessage(" ");
                                                            u.sendCenteredMessage(item.getRarity().getColor() + u.getTranslatedMessage(item.getRarity().getName() + " Item"),true);

                                                            /*if(item.getCategory() == ItemRarity.SG_ARROWTRAIL){
                                                                u.sendCenteredMessage(ChatColor.AQUA + "Survival Games " + u.getTranslatedMessage("Arrow Trail"));
                                                            } else if(item.getCategory() == ItemRarity.SG_KILLEFFECT){
                                                                u.sendCenteredMessage(ChatColor.RED + "Survival Games " + u.getTranslatedMessage("Kill Effect"));
                                                            } else if(item.getCategory() == ItemRarity.SG_VICTORYEFFECT){
                                                                u.sendCenteredMessage(ChatColor.YELLOW + "Survival Games " + u.getTranslatedMessage("Victory Effect"));
                                                            } else if(item.getCategory() == ItemRarity.SGDUELS_VICTORYEFFECT){
                                                                u.sendCenteredMessage(ChatColor.YELLOW + "SG:Duels " + u.getTranslatedMessage("Victory Effect"));
                                                            } else if(item.getCategory() == ItemRarity.SGDUELS_ARROWTRAIL){
                                                                u.sendCenteredMessage(ChatColor.AQUA + "SG:Duels " + u.getTranslatedMessage("Arrow Trail"));
                                                            } else if(item.getCategory() == ItemRarity.KPVP_KILLEFFECT){
                                                                u.sendCenteredMessage(ChatColor.RED + "KitPvP " + u.getTranslatedMessage("Kill Effect"));
                                                            } else if(item.getCategory() == ItemRarity.KPVP_ARROWTRAIL){
                                                                u.sendCenteredMessage(ChatColor.AQUA + "KitPvP " + u.getTranslatedMessage("Arrow Trail"));
                                                            }*/
                                                            u.sendCenteredMessage(item.getCategory().getColor() + item.getCategory().getNameSingular(),true);

                                                            p.sendMessage(" ");
                                                            p.sendMessage(ChatColor.AQUA + StringUtils.LINE_SEPERATOR);

                                                            u.updateScoreboard(ScoreboardType.LOBBY);

                                                            if(s > 0){
                                                                loc.getWorld().playSound(loc,Sound.NOTE_BASS,1f,0.5f);
                                                            } else {
                                                                if(item.getRarity() == ItemRarity.COMMON){
                                                                    loc.getWorld().playSound(loc,Sound.LEVEL_UP,1f,2f);
                                                                } else if(item.getRarity() == ItemRarity.RARE){
                                                                    loc.getWorld().playSound(loc,Sound.LEVEL_UP,1f,2f);
                                                                } else if(item.getRarity() == ItemRarity.EPIC){
                                                                    loc.getWorld().playSound(loc,Sound.LEVEL_UP,1f,1.5f);
                                                                } else if(item.getRarity() == ItemRarity.LEGENDARY){
                                                                    loc.getWorld().playSound(loc,Sound.LEVEL_UP,1f,1f);

                                                                    Firework fw = (Firework) p.getWorld().spawnEntity(loc, EntityType.FIREWORK);
                                                                    FireworkMeta fwm = fw.getFireworkMeta();

                                                                    FireworkEffect effect = FireworkEffect.builder().flicker(true).withColor(Color.ORANGE).withFade(Color.ORANGE).with(FireworkEffect.Type.STAR).trail(true).build();
                                                                    fwm.addEffect(effect);
                                                                    fwm.setPower(1);
                                                                    fw.setFireworkMeta(fwm);
                                                                } else if(item.getRarity() == ItemRarity.MYTHIC){
                                                                    loc.getWorld().playSound(loc,Sound.LEVEL_UP,1f,0.25f);

                                                                    Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
                                                                    FireworkMeta fwm = fw.getFireworkMeta();

                                                                    FireworkEffect effect = FireworkEffect.builder().flicker(true).withColor(Color.PURPLE).withFade(Color.PURPLE).with(FireworkEffect.Type.STAR).trail(true).build();
                                                                    fwm.addEffect(effect);
                                                                    fwm.setPower(1);
                                                                    fw.setFireworkMeta(fwm);
                                                                } else if(item.getRarity() == ItemRarity.LIMITED){
                                                                    loc.getWorld().playSound(loc,Sound.LEVEL_UP,1f,2f);
                                                                }
                                                            }
                                                        }
                                                    }.runTaskLater(ChestHub.getInstance(), 10*20);

                                                    new BukkitRunnable(){
                                                        @Override
                                                        public void run() {
                                                            v.animationStand.remove();
                                                            v.animationStand = null;

                                                            /*h.clearLines();

                                                            h.appendTextLine(ChatColor.AQUA + "Chest Vault");
                                                            h.appendTextLine(ChatColor.GOLD.toString() + ChatColor.BOLD + "CLICK TO OPEN");

                                                            h.teleport(holoLocation);*/

                                                            if(v.holo != null) v.holo.unregister();
                                                            v.spawnHolo();

                                                            v.player = null;
                                                            v.opening = false;

                                                            net.minecraft.server.v1_8_R3.World world = ((CraftWorld) v.chestLoc.getWorld()).getHandle();
                                                            BlockPosition position = new BlockPosition(v.chestLoc.getBlockX(), v.chestLoc.getBlockY(), v.chestLoc.getBlockZ());
                                                            TileEntityEnderChest tileChest = (TileEntityEnderChest) world.getTileEntity(position);
                                                            if(tileChest != null) world.playBlockAction(position, tileChest.w(), 1, 0);
                                                        }
                                                    }.runTaskLater(ChestHub.getInstance(), 13*20);
                                                } else {
                                                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You don't have enough keys to open this chest."));
                                                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You can buy keys at %l.").replace("%l",ChatColor.YELLOW + "https://thechest.eu/store" + ChatColor.RED));
                                                }
                                            } else {
                                                p.closeInventory();
                                                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("This vault is already in use."));
                                                p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,0.5f);
                                            }
                                        } else {
                                            p.closeInventory();
                                            p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You are already opening a chest."));
                                            p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,0.5f);
                                        }
                                    }
                                }
                            } else {
                                if(slot == 48){
                                    Vault vault = VaultStorage.getVaultByPlayer(p);
                                    //p.closeInventory();
                                    openFor(p,page-1,vault);
                                } else if(slot == 49){
                                    p.closeInventory();
                                } else if(slot == 50){
                                    Vault vault = VaultStorage.getVaultByPlayer(p);
                                    //p.closeInventory();
                                    openFor(p,page+1,vault);
                                } else if(slot == 46){
                                    VaultShardMenu.openFor(p);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        if(e.getPlayer() instanceof Player){
            Player p = (Player)e.getPlayer();
            ChestUser u = ChestUser.getUser(p);
            Inventory inv = e.getInventory();

            if(inv.getName().startsWith("Chest Vault")){
                Vault v = VaultStorage.getVaultByPlayer(p);

                if(v != null){
                    v.currentOpeners.remove(p);
                }
            }
        }
    }
}
