package fr.nivcoo.chatreactions.listeners;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.chatreactions.reaction.Reaction;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerListener implements Listener {

    @EventHandler
    public void onAsyncPlayerChat(AsyncChatEvent e) {
        Reaction currentReaction = ChatReactions.get().getReactionManager().getCurrentReaction();
        if (currentReaction == null)
            return;

        Player player = e.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(e.message());

        if (!currentReaction.isCorrect(message))
            return;

        if (currentReaction.addPlayerToTop(player)) {
            e.setCancelled(true);
        }
    }

}
