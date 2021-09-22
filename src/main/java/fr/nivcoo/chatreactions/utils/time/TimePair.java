package fr.nivcoo.chatreactions.utils.time;

public class TimePair<U, V> {

    private U first;

    private V second;

    public TimePair(U first, V second) {
        this.first = first;
        this.second = second;
    }

    public U getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }

}
