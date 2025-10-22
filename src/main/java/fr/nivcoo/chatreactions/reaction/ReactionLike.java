package fr.nivcoo.chatreactions.reaction;

import java.util.UUID;

public interface ReactionLike {
    void start();
    void stop();
    boolean isCorrect(String input);
    boolean tryAcceptLocal(UUID player, String input);
}

