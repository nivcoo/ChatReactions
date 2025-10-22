package fr.nivcoo.chatreactions.actions.rpc;

public record CheckAnswerRes(boolean accepted, String topLine, int place, double seconds, String winSound) {}