package pl.edu.icm.trurl.util;

public interface StatusListener {
    void done(long millis, String text, String comment);
    void problem(String text, int count);
}
