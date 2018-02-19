package eu.thechest.chesthub.inv;

import eu.thechest.chestapi.items.ItemUtil;
import eu.thechest.chestapi.server.ServerSettingsManager;
import eu.thechest.chestapi.user.ChestUser;
import net.minecraft.server.v1_8_R3.AttributeInstance;
import net.minecraft.server.v1_8_R3.AttributeModifier;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Created by zeryt on 24.06.2017.
 */
public class PremiumMenu implements Listener {
    public static void openFor(Player p){
        ChestUser u = ChestUser.getUser(p);
        Inventory inv = Bukkit.createInventory(null,9*3,"Premium Menu");

        inv.setItem(12, ItemUtil.namedItem(Material.SADDLE, ChatColor.AQUA + u.getTranslatedMessage("Summon horse"),null));
        inv.setItem(14, ItemUtil.namedItem(Material.BARRIER, net.md_5.bungee.api.ChatColor.DARK_RED + u.getTranslatedMessage("Close"), null));

        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if (e.getWhoClicked() instanceof Player) {
            Player p = (Player) e.getWhoClicked();
            ChestUser u = ChestUser.getUser(p);
            Inventory inv = e.getInventory();

            if(inv.getName().equals("Premium Menu")){
                e.setCancelled(true);

                if(e.getRawSlot() == 12){
                    if(!u.isVanished()){
                        p.closeInventory();

                        Horse h = (Horse)p.getWorld().spawnEntity(p.getLocation(), EntityType.HORSE);

                        h.getInventory().setSaddle(new ItemStack(Material.SADDLE));
                        h.setVariant(Horse.Variant.HORSE);
                        h.setColor(Horse.Color.BROWN);
                        h.setStyle(Horse.Style.NONE);
                        h.setCarryingChest(true);
                        h.setAdult();
                        h.setCustomName(u.getRank().getColor() + p.getName() + ChatColor.GREEN + "'s Horse");
                        h.setJumpStrength(0.8);
                        h.setPassenger(p);

                        EntityInsentient nmsEntity = (EntityInsentient)((CraftLivingEntity)h).getHandle();
                        AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
                        AttributeModifier m = new AttributeModifier(UUID.randomUUID(),"asd",1.1d,1);
                        attributes.b(m);
                        attributes.a(m);

                        u.achieve(26);
                    } else {
                        p.closeInventory();
                        p.playSound(p.getEyeLocation(), Sound.NOTE_BASS,1f,0.5f);
                        p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + net.md_5.bungee.api.ChatColor.RED + u.getTranslatedMessage("You can't do that in vanish mode."));
                    }
                } else if(e.getRawSlot() == 14) {
                    MyProfile.openFor(p);
                }
            }
        }
    }
}
