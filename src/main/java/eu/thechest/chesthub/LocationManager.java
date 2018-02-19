package eu.thechest.chesthub;

import eu.thechest.chestapi.ChestAPI;
import eu.thechest.chestapi.mysql.MySQLManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.UUID;

public class LocationManager {
    public static HashMap<String,Location> STORAGE = new HashMap<String,Location>();

    public static void reload(){
        try {
            STORAGE.clear();

            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `lobby_locations`");
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                if(!STORAGE.containsKey(rs.getString("id"))){
                    String id = rs.getString("id");
                    String worldName = rs.getString("world");
                    World world = Bukkit.getWorld(worldName);
                    double x = rs.getDouble("x");
                    double y = rs.getDouble("y");
                    double z = rs.getDouble("z");
                    float yaw = rs.getFloat("yaw");
                    float pitch = rs.getFloat("pitch");

                    if(world != null){
                        STORAGE.put(id,new Location(world,x,y,z,yaw,pitch));
                    }
                }
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static Location getLocation(String id){
        if(STORAGE.containsKey(id)){
            return STORAGE.get(id);
        } else {
            return new Location(Bukkit.getWorld("ChestHubV3"),0,0,0);
        }
    }

    public static boolean isLocation(String name, Location loc){
        return ChestAPI.isLocationEqual(loc,LocationManager.getLocation(name));
    }

    public static void setLocation(String id, Location loc, UUID addedBy){
        boolean update = false;

        if(STORAGE.containsKey(id)){
            STORAGE.remove(id);
            update = true;
        }

        STORAGE.put(id,loc);

        final boolean u = update;
        ChestAPI.async(() -> {
            try {
                if(u){
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `lobby_locations` SET `world` = ?, `x` = ?, `y` = ?, `z` = ?, `yaw` = ?, `pitch` = ?, `addedBy` = ?, `time` = CURRENT_TIMESTAMP WHERE `id` = ?");
                    ps.setString(1,loc.getWorld().getName());
                    ps.setDouble(2,loc.getX());
                    ps.setDouble(3,loc.getY());
                    ps.setDouble(4,loc.getZ());
                    ps.setFloat(5,loc.getYaw());
                    ps.setFloat(6,loc.getPitch());
                    ps.setString(7,addedBy.toString());
                    ps.setString(8,id);
                    ps.executeUpdate();
                    ps.close();
                } else {
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `lobby_locations` (`id`,`world`,`x`,`y`,`z`,`yaw`,`pitch`,`addedBy`) VALUES(?,?,?,?,?,?,?,?);");
                    ps.setString(1,id);
                    ps.setString(2,loc.getWorld().getName());
                    ps.setDouble(3,loc.getX());
                    ps.setDouble(4,loc.getY());
                    ps.setDouble(5,loc.getZ());
                    ps.setFloat(6,loc.getYaw());
                    ps.setFloat(7,loc.getPitch());
                    ps.setString(8,addedBy.toString());
                    ps.executeUpdate();
                    ps.close();
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        });
    }
}
