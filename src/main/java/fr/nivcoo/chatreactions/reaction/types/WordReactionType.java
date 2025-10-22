package fr.nivcoo.chatreactions.reaction.types;

import fr.nivcoo.chatreactions.ChatReactions;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WordReactionType implements ReactionTypeEntry {

    private final int weight;
    private String word;

    public WordReactionType() {
        this.weight = ChatReactions.get().getConfiguration().getInt("reaction_types.types.word");
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
        this.word = words.get(ThreadLocalRandom.current().nextInt(words.size()));
        return word;
    }

    @Override
    public boolean isCorrect(String input) {
        return input.trim().equalsIgnoreCase(word);
    }

    @Override
    public List<String> getDescriptionMessages() {
        return ChatReactions.get().getConfiguration().getStringList("messages.chat.type_messages.word");
    }

    public String expectedAnswer(){ return word; }
}
