package fr.nivcoo.chatreactions.cache;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.chatreactions.actions.ReactionWinAction;
import fr.nivcoo.chatreactions.utils.Database;

import java.util.LinkedHashMap;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class CacheManager implements Listener {

    private final ChatReactions plugin;
    private final Database database;

    private Map<UUID, Integer> rankingCache = new LinkedHashMap<>();
    private final Map<UUID, String> nameCache = new HashMap<>();

    public CacheManager() {
        this.plugin = ChatReactions.get();
        this.database = plugin.getDatabase();

        loadFullRanking();
        loadAllNamesFromDatabase();
    }

    public void cacheName(UUID uuid, String name) {
        if (name == null || name.isBlank()) return;
        nameCache.put(uuid, name);
        ChatReactions.get().getDatabase().savePlayerName(uuid, name);
    }

    public String resolvePlayerName(UUID uuid) {
        String c = nameCache.get(uuid);
        if (c != null) return c;

        String db = ChatReactions.get().getDatabase().getPlayerName(uuid);
        if (db != null && !db.isBlank()) {
            nameCache.put(uuid, db);
            return db;
        }

        var online = Bukkit.getPlayer(uuid);
        if (online != null) {
            String n = online.getName();
            cacheName(uuid, n);
            return n;
        }

        String off = Bukkit.getOfflinePlayer(uuid).getName();
        if (off != null && !off.isBlank()) {
            cacheName(uuid, off);
            return off;
        }

        String fb = uuid.toString().substring(0, 8);
        nameCache.put(uuid, fb);
        return fb;
    }

    public void loadFullRanking() {
        rankingCache.clear();
        rankingCache.putAll(database.getAllPlayersScore());
        sortRankingCache();
        plugin.getLogger().info("[ChatReactions] Loaded ranking for " + rankingCache.size() + " players.");
    }


    public void redisUpdatePlayerScore(UUID uuid, int value) {
        rankingCache.put(uuid, value);
        sortRankingCache();
    }

    public void updatePlayerScore(UUID uuid, int value) {
        int newCount = getPlayerScore(uuid) + value;
        database.updatePlayerScore(uuid, newCount);
        rankingCache.put(uuid, newCount);

        if (plugin.isRedisEnabled()) {
            plugin.getRedisChannel().publish(new ReactionWinAction(uuid, newCount));
        }

        sortRankingCache();
    }

    private void sortRankingCache() {
        rankingCache = rankingCache.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public int getPlayerScore(UUID uuid) {
        return rankingCache.getOrDefault(uuid, 0);
    }

    public Map<UUID, Integer> getSortedRanking() {
        return rankingCache;
    }

    public void loadAllNamesFromDatabase() {
        Map<UUID, String> all = ChatReactions.get().getDatabase().getAllPlayerNames();
        for (Map.Entry<UUID, String> entry : all.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isBlank()) {
                nameCache.put(entry.getKey(), entry.getValue());
            }
        }
        ChatReactions.get().getLogger().info("[ChatReactions] Loaded " + all.size() + " player names into cache.");
    }
}
