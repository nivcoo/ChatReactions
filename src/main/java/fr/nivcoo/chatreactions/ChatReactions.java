package fr.nivcoo.chatreactions;

import fr.nivcoo.chatreactions.cache.CacheManager;
import fr.nivcoo.chatreactions.command.commands.StartCMD;
import fr.nivcoo.chatreactions.command.commands.StopCMD;
import fr.nivcoo.chatreactions.placeholder.PlaceHolderAPI;
import fr.nivcoo.chatreactions.reaction.ReactionManager;
import fr.nivcoo.chatreactions.utils.Database;
import fr.nivcoo.utilsz.commands.CommandManager;
import fr.nivcoo.utilsz.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ChatReactions extends JavaPlugin {

    private static ChatReactions INSTANCE;
    private Config config;
    private ReactionManager reactionManager;
    private Database db;
    private CacheManager cacheManager;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        INSTANCE = this;
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }

        File database = new File(getDataFolder(), "database.db");
        if (!database.exists()) {
            try {
                database.createNewFile();
            } catch (IOException ignored) {
            }
        }
        db = new Database(database.getPath());
        db.initDB();

        config = new Config(new File(getDataFolder() + File.separator + "config.yml"));

        loadCacheManager();

        reactionManager = new ReactionManager();
        Bukkit.getPluginManager().registerEvents(reactionManager, this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceHolderAPI().register();
        }

        commandManager = new CommandManager(this, config, "chatreactions", "chatreactions.commands");
        commandManager.addCommand(new StartCMD());
        commandManager.addCommand(new StopCMD());

    }

    @Override
    public void onDisable() {
        getReactionManager().disablePlugin();
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
        return db;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public static ChatReactions get() {
        return INSTANCE;
    }

}
