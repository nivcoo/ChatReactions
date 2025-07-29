package fr.nivcoo.chatreactions.reaction.types;

import fr.nivcoo.chatreactions.ChatReactions;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ReversedWordReactionType implements ReactionTypeEntry {

    private final int weight;
    private String originalWord;

    public ReversedWordReactionType() {
        this.weight = ChatReactions.get().getConfiguration().getInt("reaction_types.types.reversed");
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public String generateWord() {
        List<String> words = ChatReactions.get().getReactionManager().getWords();

        if (words.isEmpty()) {
            throw new IllegalStateException("Le fichier de mots est vide !");
        }

        this.originalWord = words.get(ThreadLocalRandom.current().nextInt(words.size()));
        return new StringBuilder(originalWord).reverse().toString();
    }

    @Override
    public boolean isCorrect(String input) {
        return input.trim().equalsIgnoreCase(originalWord);
    }

    @Override
    public List<String> getDescriptionMessages() {
        return ChatReactions.get().getConfiguration().getStringList("messages.chat.type_messages.reversed");
    }
}
