package fr.nivcoo.chatreactions.actions;

import fr.nivcoo.utilsz.redis.RedisAction;
import fr.nivcoo.utilsz.redis.RedisSerializable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;

@RedisAction("reaction-topline")
public record ReactionTopLineAction(int place, UUID player, double seconds, String line, String winSound) implements RedisSerializable {
    @Override public void execute() {
        Component c = LegacyComponentSerializer.legacySection().deserialize(line);
        Bukkit.getServer().sendMessage(c);
        Player p = Bukkit.getPlayer(player);
        if (p != null) {
            try { p.playSound(p.getLocation(), Sound.valueOf(winSound), 0.4f, 1.7f); } catch (IllegalArgumentException ignored) {}
        }
    }
}