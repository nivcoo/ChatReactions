package fr.nivcoo.chatreactions.reaction.types;

import fr.nivcoo.chatreactions.ChatReactions;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MathReactionType implements ReactionTypeEntry {

    private final int weight;
    private String answer;

    public MathReactionType() {
        this.weight = ChatReactions.get().getConfiguration().getInt("reaction_types.types.math");
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public String generateWord() {
        List<String> mathList = ChatReactions.get().getConfiguration().getStringList("reaction_types.math_list");

        if (mathList.isEmpty()) {
            throw new IllegalStateException("The math_list configuration is empty. Please add some equations.");
        }

        String raw = mathList.get(ThreadLocalRandom.current().nextInt(mathList.size()));
        String[] parts = raw.split("=");

        if (parts.length != 2) {
            throw new IllegalStateException("Invalid equation format in math_list: " + raw);
        }

        String equation = parts[0].trim() + " = ?";
        answer = parts[1].trim();

        return equation;
    }

    @Override
    public boolean isCorrect(String input) {
        return input.trim().equalsIgnoreCase(answer);
    }

    @Override
    public List<String> getDescriptionMessages() {
        return ChatReactions.get().getConfiguration().getStringList("messages.chat.type_messages.math");
    }

    @Override
    public String expectedAnswer(){ return answer; }
}
