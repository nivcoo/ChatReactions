package fr.nivcoo.chatreactions.actions;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.utilsz.redis.RedisAction;
import fr.nivcoo.utilsz.redis.RedisSerializable;

@RedisAction("reaction-stop")
public record ReactionStopAction(String finalMessage) implements RedisSerializable {

    @Override
    public void execute() {
        var plugin = ChatReactions.get();

        plugin.getReactionManager().clearShadow();

        plugin.getDisplay().showStop(finalMessage);
        plugin.getLogger().info("[ChatReactions] Reaction stopped and broadcasted.");
    }
}
