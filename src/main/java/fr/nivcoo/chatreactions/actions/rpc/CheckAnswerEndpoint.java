package fr.nivcoo.chatreactions.actions.rpc;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.utilsz.redis.RedisAction;
import fr.nivcoo.utilsz.redis.rpc.RpcAnnotated;

import java.util.UUID;

@RedisAction(value = "chatreact_check_answer", response = CheckAnswerRes.class, receiveOwnMessages = true)
public record CheckAnswerEndpoint(String serverName, UUID player, String plainMessage, long atMillis) implements RpcAnnotated {
    @Override public CheckAnswerRes handle() {
        ChatReactions pl = ChatReactions.get();
        if (!pl.isManager()) return null;
        var rm = pl.getReactionManager();
        var r = rm.getCurrentReaction();
        if (r == null) return null;
        return r.tryAccept(player, plainMessage, atMillis);
    }
}