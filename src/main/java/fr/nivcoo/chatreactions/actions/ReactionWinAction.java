package fr.nivcoo.chatreactions.actions;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.utilsz.redis.RedisAction;
import fr.nivcoo.utilsz.redis.RedisSerializable;
import org.bukkit.Bukkit;

import java.util.UUID;

@RedisAction("reaction-win")
public record ReactionWinAction(UUID uuid, int count) implements RedisSerializable {

    @Override
    public void execute() {
        Bukkit.getScheduler().runTask(ChatReactions.get(), () -> {
            ChatReactions plugin = ChatReactions.get();
            plugin.getCacheManager().redisUpdatePlayerScore(uuid, count);
            plugin.getLogger().info("Update with Redis : " + uuid + " -> " + count);
        });
    }
}
