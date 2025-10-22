package fr.nivcoo.chatreactions.actions;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.utilsz.redis.RedisAction;
import fr.nivcoo.utilsz.redis.RedisSerializable;

import java.util.List;

@RedisAction("reaction-start")
public record ReactionStartAction(String display, String answer, List<String> typeLines, String startSound)
        implements RedisSerializable {

    @Override public void execute() {
        var plugin = ChatReactions.get();

        plugin.getReactionManager().installShadow(answer);

        plugin.getDisplay().showStart(display, typeLines, startSound);

        plugin.getLogger().info("[ChatReactions] Reaction start broadcasted (display='" + display + "')");
    }
}
