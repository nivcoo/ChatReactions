package fr.nivcoo.chatreactions.actions;

import fr.nivcoo.utilsz.redis.RedisAction;
import fr.nivcoo.utilsz.redis.RedisSerializable;

@RedisAction("reaction-cancel")
public record ReactionCancelAction() implements RedisSerializable {
    @Override public void execute() {  }
}
