package com.ksanur.lightbikes;

import com.ksanur.lightbikes.nms.bikes.Bike;
import com.ksanur.lightbikes.nms.bikes.Direction;
import com.ksanur.lightbikes.util.SerializableLocation;
import lib.PatPeter.SQLibrary.Database;
import lib.PatPeter.SQLibrary.MySQL;
import lib.PatPeter.SQLibrary.SQLite;
import mondocommand.CallInfo;
import mondocommand.MondoCommand;
import mondocommand.dynamic.Sub;
import org.apache.commons.lang.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.HashMap;

/**
 * User: bobacadodl
 * Date: 1/20/14
 * Time: 3:26 PM
 */
public class LightBikes extends JavaPlugin {
    private static LightBikes instance;
    private final static String NAME = "LightBikes";

    private static Database sql;
    private static DatabaseType databaseType = DatabaseType.SQLite;

    private static HashMap<Player, LBPlayer> players = new HashMap<Player, LBPlayer>();
    private static HashMap<String, LBArena> arenas = new HashMap<String, LBArena>();

    public void onEnable() {
        instance = this;

        //add custom entities
        for (BikeType bikeType : BikeType.values()) {
            BikeNMS.registerBike(bikeType);
        }

        //register glass panes
        BikeNMS.hookBlocks();

        ConfigurationSerialization.registerClass(SerializableLocation.class, "Location");
        ConfigurationSerialization.registerClass(LBArena.class, "LBGame");
        //loadConfig();

        MondoCommand base = new MondoCommand();
        base.autoRegisterFrom(this);
        getCommand("lightbikes").setExecutor(base);

    }

    @Sub(name = "ridebike", usage = "<name>", description = "Get a bike",
            permission = "lightbikes.ridebike", minArgs = 1)
    public void test(CallInfo call) {
        String query = call.getArg(0);

        BikeType closest = null;
        int closestDistance = Integer.MAX_VALUE;
        for (BikeType bikeType : BikeType.values()) {
            int distance = StringUtils.getLevenshteinDistance(query, bikeType.toString().toLowerCase());
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = bikeType;
            }
        }

        Bike bike = BikeNMS.spawnBike(closest, call.getPlayer().getLocation());
        call.reply("" + call.getPlayer().getLocation().getYaw());
        call.reply(Direction.fromYaw(call.getPlayer().getLocation().getYaw()).toString());
        bike.setDirection(Direction.fromYaw(call.getPlayer().getLocation().getYaw()));
        bike.getBukkitEntity().setPassenger(call.getPlayer());
        call.reply("{GREEN}Spawned bike!");
    }

    public static LBArena getArena(String name) {
        return arenas.get(name);
    }

    public static void addArena(LBArena arena) {
        arenas.put(arena.getName(), arena);
    }

    public static LBPlayer getLBPlayer(Player player) {
        return players.get(player);
    }

    public static LBOfflinePlayer getOfflineLBPlayer(OfflinePlayer player) {
        return new LBOfflinePlayer(player);
    }

    public static Database getSQL() {
        return sql;
    }

    public static DatabaseType getDatabaseType() {
        return databaseType;
    }

    public static LightBikes getInstance() {
        return instance;
    }

    public void loadConfig() {
        saveDefaultConfig();

        //LOAD SQL DATABASE
        String prefix = "[" + NAME + "] ";
        String hostname = "localhost";
        int port = 3306;
        String database = NAME;
        String username = "username";
        String password = "password";

        String directory = this.getDataFolder().getAbsolutePath();

        if (getConfig().isString("database.type")) {
            String databaseTypeString = getConfig().getString("database");
            if (DatabaseType.isDatabaseType(databaseTypeString)) {
                databaseType = DatabaseType.getDatabaseType(databaseTypeString);
            } else {
                getLogger().severe("Database \"" + databaseTypeString + "\" is not a valid database type!");
                getLogger().severe("Defaulting to SQLite instead...");
            }
        }

        if (getConfig().isString("database.hostname")) {
            hostname = getConfig().getString("database.hostname");
        }
        if (getConfig().isInt("database.port")) {
            port = getConfig().getInt("database.port");
        }
        if (getConfig().isString("database.database")) {
            database = getConfig().getString("database.database");
        }
        if (getConfig().isString("database.username")) {
            username = getConfig().getString("database.username");
        }
        if (getConfig().isString("database.password")) {
            password = getConfig().getString("database.password");
        }

        switch (databaseType) {
            case MySQL:
                sql = new MySQL(
                        getLogger(),
                        prefix,
                        hostname,
                        3306,
                        database,
                        username,
                        password);
                break;
            case SQLite:
                sql = new SQLite(
                        getLogger(),
                        prefix,
                        directory,
                        database,
                        ".sqlite");
                break;
        }

        getLogger().info("Attempting to establish connection to database...");
        //if it didnt open
        if (!sql.open()) {
            getLogger().severe("Could not connect to database! Defaulting to SQLite instead!");
            //if not sqlite try sqlite instead
            if (!(sql instanceof SQLite)) {
                sql = new SQLite(
                        getLogger(),
                        prefix,
                        directory,
                        database);
                databaseType = DatabaseType.SQLite;
                getLogger().info("Attempting to establish connection to database...");
                if (!sql.open()) {
                    getLogger().severe("Could not connect to SQLite either! Disabling plugin... :(");
                    getServer().getPluginManager().disablePlugin(this);
                    return;
                } else {
                    getLogger().info("Database connection successful!");
                }
            } else {
                getLogger().severe("Could not connect to SQLite! Disabling plugin... :(");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

        } else {
            getLogger().info("Database connection successful!");
        }
        //TODO read sql data in separate thread
        //create default tables
        switch (databaseType) {
            case MySQL:
            case SQLite:
                try {
                    sql.prepare("CREATE TABLE IF NOT EXISTS `players` (" +
                            "`uuid`   int PRIMARY KEY NOT NULL," +
                            "`player` varchar(20)     NOT NULL," +
                            "`online` bool            NOT NULL," +
                            "`wins`   int             NOT NULL," +
                            "`losses` int             NOT NULL);").executeQuery();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;

            //Read arenas

        }
    }
}
