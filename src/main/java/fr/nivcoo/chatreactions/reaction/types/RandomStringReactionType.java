package fr.nivcoo.chatreactions.reaction.types;

import fr.nivcoo.chatreactions.ChatReactions;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomStringReactionType implements ReactionTypeEntry {

    private final int weight;
    private String randomString;

    public RandomStringReactionType() {
        this.weight = ChatReactions.get().getConfiguration().getInt("reaction_types.types.random");
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public String generateWord() {
        int min = ChatReactions.get().getConfiguration().getInt("reaction_types.random_string.length_min");
        int max = ChatReactions.get().getConfiguration().getInt("reaction_types.random_string.length_max");
        String chars = ChatReactions.get().getConfiguration().getString("reaction_types.random_string.characters");

        if (chars == null || chars.isEmpty()) {
            throw new IllegalStateException("The characters configuration for random strings is empty.");
        }

        int length = ThreadLocalRandom.current().nextInt(min, max + 1);
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }

        randomString = sb.toString();
        return randomString;
    }

    @Override
    public boolean isCorrect(String input) {
        return input.trim().equals(randomString);
    }

    @Override
    public List<String> getDescriptionMessages() {
        return ChatReactions.get().getConfiguration().getStringList("messages.chat.type_messages.random");
    }

    public String expectedAnswer(){ return randomString; }
}
