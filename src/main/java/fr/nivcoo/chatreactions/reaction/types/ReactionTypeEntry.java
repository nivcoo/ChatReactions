package fr.nivcoo.chatreactions.reaction.types;

import java.util.List;

public interface ReactionTypeEntry {

    int getWeight();
    String generateWord();
    boolean isCorrect(String input);
    List<String> getDescriptionMessages();
}
