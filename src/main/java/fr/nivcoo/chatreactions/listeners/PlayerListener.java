package fr.nivcoo.chatreactions.listeners;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.chatreactions.actions.rpc.CheckAnswerEndpoint;
import fr.nivcoo.chatreactions.actions.rpc.CheckAnswerRes;
import fr.nivcoo.chatreactions.reaction.Reaction;
import fr.nivcoo.utilsz.redis.RedisManager;
import fr.nivcoo.utilsz.redis.bus.RedisBus;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.concurrent.TimeUnit;

public class PlayerListener implements Listener {

    private final boolean managerRole;

    public PlayerListener(boolean managerRole) {
        this.managerRole = managerRole;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncChatEvent e) {
        var plugin = ChatReactions.get();
        var current = plugin.getReactionManager().getCurrentReaction();
        if (current == null) return;

        Player p = e.getPlayer();
        var msg = PlainTextComponentSerializer.plainText().serialize(e.message());
        if (!current.isCorrect(msg)) return;

        if (managerRole) {
            if (current.tryAcceptLocal(p.getUniqueId(), msg)) e.setCancelled(true);
            return;
        }

        var bus = plugin.getBus();
        var ep = new CheckAnswerEndpoint(plugin.getConfiguration().getString("server.name"),
                p.getUniqueId(), msg, System.currentTimeMillis());
        try {
            var res = bus.call(ep, CheckAnswerRes.class).get(250, java.util.concurrent.TimeUnit.MILLISECONDS);
            if (res != null && res.accepted()) e.setCancelled(true);
        } catch (Exception ignored) {

        }
    }

}
