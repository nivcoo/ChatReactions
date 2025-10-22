package fr.nivcoo.chatreactions.actions;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.utilsz.redis.RedisAction;
import fr.nivcoo.utilsz.redis.RedisSerializable;

import java.util.UUID;

@RedisAction("reaction-topline")
public record ReactionTopLineAction(int place, UUID player, double seconds, String line, String winSound) implements RedisSerializable {
    @Override public void execute() {
        ChatReactions.get().getDisplay().showTopLine(line, player, winSound);
    }
}