package fr.nivcoo.chatreactions.actions.rpc;

public record CheckAnswerRes(boolean accepted, String topLine, int place, double seconds, String winSound) {

    public static CheckAnswerRes empty() {
        return new CheckAnswerRes(false, null, 0, 0, null);
    }

    public static CheckAnswerRes success(String topLine, int place, double seconds, String winSound) {
        return new CheckAnswerRes(true, topLine, place, seconds, winSound);
    }
}
