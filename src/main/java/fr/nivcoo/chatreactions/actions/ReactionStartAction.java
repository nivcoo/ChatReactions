package fr.nivcoo.chatreactions.actions;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.utilsz.redis.RedisAction;
import fr.nivcoo.utilsz.redis.RedisSerializable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

@RedisAction("reaction-start")
public record ReactionStartAction(String display, String answer, List<String> typeLines, String startSound)
        implements RedisSerializable {

    @Override public void execute() {
        var plugin = ChatReactions.get();

        plugin.getReactionManager().installShadow(answer);

        String messageText = plugin.getReactionManager().formatStartMessage(typeLines);
        String hoverText = String.join("\n", plugin.getConfiguration()
                .getStringList("messages.chat.start_messages.hover")).replace("{0}", display);

        Component hover = LegacyComponentSerializer.legacySection().deserialize(hoverText);
        Component msg = LegacyComponentSerializer.legacySection().deserialize(messageText)
                .hoverEvent(HoverEvent.showText(hover));

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(msg);
            try { p.playSound(p.getLocation(), Sound.valueOf(startSound), 0.4f, 1.7f); } catch (IllegalArgumentException ignored) {}
        }

        plugin.getLogger().info("[ChatReactions] Reaction start broadcasted (display='" + display + "')");
    }
}
