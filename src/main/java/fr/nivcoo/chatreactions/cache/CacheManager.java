package fr.nivcoo.chatreactions.cache;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.chatreactions.actions.ReactionWinAction;
import fr.nivcoo.chatreactions.utils.Database;

import java.util.*;

import fr.nivcoo.edenplayers.EdenPlayers;
import fr.nivcoo.edenplayers.api.AEdenPlayers;
import fr.nivcoo.edenplayers.api.model.PlayerProfile;
import org.bukkit.event.Listener;

import java.util.stream.Collectors;

public class CacheManager implements Listener {

    private final ChatReactions plugin;
    private final Database database;

    private Map<UUID, Integer> rankingCache = new LinkedHashMap<>();

    public CacheManager() {
        this.plugin = ChatReactions.get();
        this.database = plugin.getDatabase();

        loadFullRanking();
    }


    public String resolvePlayerName(UUID uuid) {
        AEdenPlayers api = EdenPlayers.get();
        if (api != null) {
            Optional<PlayerProfile> optProfile = api.getProfileCached(uuid);
            if (optProfile.isPresent()) {
                String name = optProfile.get().getUsername();
                if (name != null && !name.isBlank()) {
                    return name;
                }
            }
        }

        return uuid.toString();
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
            plugin.getBus().publish(new ReactionWinAction(uuid, newCount));
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
}
