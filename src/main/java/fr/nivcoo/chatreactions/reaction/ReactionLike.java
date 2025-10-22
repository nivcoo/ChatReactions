package fr.nivcoo.chatreactions.reaction;

import fr.nivcoo.chatreactions.actions.rpc.CheckAnswerRes;

import java.util.UUID;

public interface ReactionLike {
    void start();
    void stop();
    default boolean isCorrect(String input) { return false; }

    default boolean tryAcceptLocal(UUID player, String input) { return false; }

    default CheckAnswerRes tryAccept(UUID player, String input, long atMillis) {
        return new CheckAnswerRes(false, null, 0, 0, null);
    }
}

