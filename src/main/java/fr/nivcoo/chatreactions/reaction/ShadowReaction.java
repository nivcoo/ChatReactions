package fr.nivcoo.chatreactions.reaction;

final class ShadowReaction implements ReactionLike {
    private final String answer;
    private volatile boolean active;

    public ShadowReaction(String answer) {
        this.answer = answer;
    }

    @Override
    public void start() {
        active = true;
    }

    @Override
    public void stop() {
        active = false;
    }

    @Override
    public boolean isCorrect(String input) {
        if (!active || input == null) return false;
        return input.trim().equalsIgnoreCase(answer);
    }
}
