package pl.edu.icm.trurl.util;


public class Pair<FIRST, SECOND> {
    public final FIRST first;
    public final SECOND second;

    private Pair(FIRST first, SECOND second) {
        this.first = first;
        this.second = second;
    }

    public static<FIRST, SECOND> Pair<FIRST, SECOND> of(FIRST first, SECOND second) {
        return new Pair<>(first, second);
    }
}
