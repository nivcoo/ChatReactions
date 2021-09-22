package fr.nivcoo.chatreactions.cache;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.chatreactions.utils.Database;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.UUID;

public class CacheManager implements Listener {

    private ChatReactions chatReactions;

    private Database db;

    private HashMap<UUID, Integer> playersClassementCache;

    public CacheManager() {
        chatReactions = ChatReactions.get();
        db = chatReactions.getDatabase();
        playersClassementCache = new HashMap<>();
        getAllPlayersCount();
    }

    public void getAllPlayersCount() {
        playersClassementCache = db.getAllPlayersCount(Bukkit.getServer().getOnlinePlayers());
    }

    public void updatePlayerCount(UUID uuid, int addNumber) {
        int newCount = getPlayerCount(uuid) + addNumber;
        db.updatePlayerCount(uuid, newCount);
        playersClassementCache.put(uuid, newCount);
    }

    public int getPlayerCount(UUID uuid) {
        Integer count = playersClassementCache.get(uuid);
        if (count == null) {
            count = db.getPlayerCount(uuid);
            playersClassementCache.put(uuid, count);
        }
        return count;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        int count = db.getPlayerCount(uuid);
        playersClassementCache.put(uuid, count);

    }

}
