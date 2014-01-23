package com.ksanur.lightbikes;

import com.ksanur.lightbikes.bikes.Bike;
import com.ksanur.lightbikes.bikes.Direction;
import mondocommand.CallInfo;
import mondocommand.MondoCommand;
import mondocommand.dynamic.Sub;
import org.apache.commons.lang.StringUtils;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * User: bobacadodl
 * Date: 1/20/14
 * Time: 3:26 PM
 */
public class LightBikes extends JavaPlugin {
    private static LightBikes instance;

    public void onEnable() {
        instance = this;

        MondoCommand base = new MondoCommand();
        base.autoRegisterFrom(this);
        getCommand("lightbikes").setExecutor(base);

        //getServer().getPluginManager().registerEvents(this,this);

        //add custom entities
        for (BikeType bikeType : BikeType.values()) {
            BikeNMS.addCustomEntity(bikeType.getBikeClass(), bikeType.getName(), bikeType.getId());
        }
    }

    @Sub(name = "ridebike", usage = "<name>", description = "Get a bike",
            permission = "lightbikes.ridebike", minArgs = 1)
    public void test(CallInfo call) {
        String query = normalize(call.getArg(0));

        BikeType closest = null;
        int closestDistance = Integer.MAX_VALUE;
        for (BikeType bikeType : BikeType.values()) {
            int distance = StringUtils.getLevenshteinDistance(query, normalize(bikeType.getName()));
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = bikeType;
            }
        }
        net.minecraft.server.v1_7_R1.Entity bike = BikeNMS.spawnBike(BikeType.PIG, call.getPlayer().getLocation());
        if (bike instanceof Bike) {
            ((Bike) bike).setDirection(Direction.fromYaw(call.getPlayer().getLocation().getYaw()));
        }
        bike.getBukkitEntity().setPassenger(call.getPlayer());
        call.reply("{GREEN}Created the bike!");
    }

    public String normalize(String s) {
        return s.toLowerCase().replace("_", "");
    }

    public static LightBikes getInstance() {
        return instance;
    }
}
