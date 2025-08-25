package fr.nivcoo.chatreactions;

import fr.nivcoo.chatreactions.actions.PlayerNameUpdateAction;
import fr.nivcoo.chatreactions.cache.CacheManager;
import fr.nivcoo.chatreactions.command.commands.StartCMD;
import fr.nivcoo.chatreactions.command.commands.StopCMD;
import fr.nivcoo.chatreactions.listeners.PlayerListener;
import fr.nivcoo.chatreactions.placeholder.PlaceHolderAPI;
import fr.nivcoo.chatreactions.reaction.ReactionManager;
import fr.nivcoo.chatreactions.actions.ReactionWinAction;
import fr.nivcoo.chatreactions.utils.Database;
import fr.nivcoo.utilsz.commands.CommandManager;
import fr.nivcoo.utilsz.config.Config;
import fr.nivcoo.utilsz.database.DatabaseManager;
import fr.nivcoo.utilsz.database.DatabaseType;
import fr.nivcoo.utilsz.redis.RedisChannelRegistry;
import fr.nivcoo.utilsz.redis.RedisManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ChatReactions extends JavaPlugin {

    private static ChatReactions INSTANCE;
    private Config config;
    private ReactionManager reactionManager;
    private Database database;
    private CacheManager cacheManager;
    private DatabaseManager dbManager;

    private RedisManager redisManager;
    private RedisChannelRegistry redisChannel;

    @Override
    public void onEnable() {
        INSTANCE = this;

        // Config
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }
        config = new Config(configFile);


        setupDatabase();

        if (config.getBoolean("redis.enabled")) {
            redisManager = new RedisManager(
                    this,
                    config.getString("redis.host"),
                    config.getInt("redis.port"),
                    config.getString("redis.username"),
                    config.getString("redis.password")
            );

            redisChannel = redisManager.createRegistry("chatreactions-update");

            redisChannel.register(ReactionWinAction.class);
            redisChannel.register(PlayerNameUpdateAction.class);

            getLogger().info("Redis enabled and connected to " + config.getString("redis.host") + ":" + config.getInt("redis.port"));
        } else {
            getLogger().info("Redis disabled.");
        }

        loadCacheManager();
        reactionManager = new ReactionManager();
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceHolderAPI().register();
        }

        CommandManager commandManager = new CommandManager(this, config, "chatreactions", "chatreactions.commands");
        commandManager.addCommand(new StartCMD());
        commandManager.addCommand(new StopCMD());
    }

    @Override
    public void onDisable() {
        getReactionManager().disablePlugin();
        if (reactionManager != null) reactionManager.disablePlugin();
        if (dbManager != null) dbManager.closeConnection();
        if (redisManager != null) redisManager.close();
    }

    private void setupDatabase() {
        String type = config.getString("database.type", "sqlite").toLowerCase();
        String dbName = config.getString("database.mysql.database", "invest");

        DatabaseType dbType = switch (type) {
            case "mysql" -> DatabaseType.MYSQL;
            case "mariadb" -> DatabaseType.MARIADB;
            default -> DatabaseType.SQLITE;
        };

        String sqlitePath = new File(getDataFolder(), config.getString("database.sqlite.path", "invest.db")).getPath();

        dbManager = new DatabaseManager(
                dbType,
                config.getString("database.mysql.host"),
                config.getInt("database.mysql.port"),
                dbName,
                config.getString("database.mysql.username"),
                config.getString("database.mysql.password"),
                sqlitePath
        );

        database = new Database(dbManager);
        database.initDB();
    }

    public void loadCacheManager() {
        cacheManager = new CacheManager();
        Bukkit.getPluginManager().registerEvents(cacheManager, this);
    }

    public Config getConfiguration() {
        return config;
    }

    public ReactionManager getReactionManager() {
        return reactionManager;
    }

    public Database getDatabase() {
        return database;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public static ChatReactions get() {
        return INSTANCE;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public RedisChannelRegistry getRedisChannel() {
        return redisChannel;
    }

    public boolean isRedisEnabled() {
        return redisManager != null;
    }
}
