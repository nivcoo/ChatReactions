// fr/nivcoo/chatreactions/ui/ReactionDisplay.java
package fr.nivcoo.chatreactions.ui;

import fr.nivcoo.chatreactions.ChatReactions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public final class ReactionDisplay {
    private final ChatReactions plugin;

    public ReactionDisplay(ChatReactions plugin) {
        this.plugin = plugin;
    }

    public void showStart(String displayToken, List<String> typeLines, String startSound) {
        String messageText = plugin.getReactionManager().formatStartMessage(typeLines);
        String hoverText = String.join("\n",
                        plugin.getConfiguration().getStringList("messages.chat.start_messages.hover"))
                .replace("{0}", displayToken);

        Component hover = LegacyComponentSerializer.legacySection().deserialize(hoverText);
        Component msg = LegacyComponentSerializer.legacySection().deserialize(messageText)
                .hoverEvent(HoverEvent.showText(hover));

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(msg);
            try {
                p.playSound(p.getLocation(), Sound.valueOf(startSound), 0.4f, 1.7f);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void showTopLine(String formattedLine, UUID player, String winSound) {
        Component c = LegacyComponentSerializer.legacySection().deserialize(formattedLine);
        Bukkit.getServer().sendMessage(c);
        Player p = Bukkit.getPlayer(player);
        if (p != null) {
            try {
                p.playSound(p.getLocation(), Sound.valueOf(winSound), 0.4f, 1.7f);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void showStop(String finalMessage) {
        if (finalMessage == null || finalMessage.isBlank()) return;
        Component msg = LegacyComponentSerializer.legacySection().deserialize(finalMessage);
        Bukkit.getServer().sendMessage(msg);
    }
}
