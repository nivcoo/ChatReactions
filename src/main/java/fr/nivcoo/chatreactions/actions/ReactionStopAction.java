package fr.nivcoo.chatreactions.actions;

import fr.nivcoo.utilsz.redis.RedisAction;
import fr.nivcoo.utilsz.redis.RedisSerializable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;

@RedisAction("reaction-stop")
public record ReactionStopAction(String finalMessage) implements RedisSerializable {
    @Override public void execute() {
        if (finalMessage == null || finalMessage.isBlank()) return;
        Component c = LegacyComponentSerializer.legacySection().deserialize(finalMessage);
        Bukkit.getServer().sendMessage(c);
    }
}