package fr.nivcoo.chatreactions.actions;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.utilsz.redis.RedisAction;
import fr.nivcoo.utilsz.redis.RedisSerializable;
import org.bukkit.Bukkit;

import java.util.UUID;

@RedisAction("player_name_update")
public record PlayerNameUpdateAction(UUID uuid, String name) implements RedisSerializable {
    @Override public void execute() {
        if (name == null || name.isBlank()) return;
        Bukkit.getScheduler().runTask(ChatReactions.get(), () ->
                ChatReactions.get().getCacheManager().cacheName(uuid, name)
        );
    }
}
