package pl.edu.icm.trurl.util;

import com.google.common.base.Preconditions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Status {
    private final String text;
    private final int skip;
    private int counter;
    private long startTime;
    public static StatusListener statusListener;
    public final ConcurrentHashMap<String, Integer> problems = new ConcurrentHashMap<>();

    public Status(String text, int skip) {
        this.text = text;
        this.skip = skip;
        Preconditions.checkState(skip > 0, "skip must be 1 or more");
        startTime = System.currentTimeMillis();
        System.out.print(text + ": ");
    }

    public void tick() {
        counter++;
        while (counter > skip) {
            counter -= skip;
            System.out.print("#");
        }
    }

    public void done() {
        done(null);
    }

    public void problem(String problem) {
        problems.compute(problem, (k, v) -> v == null ? 1 : v + 1);
    }

    public void done(String comment, Object... args) {
        double now = System.currentTimeMillis();
        double duration = now - startTime;
        System.out.print(String.format(" [OK] (in %f seconds)", duration / 1000.0));
        if (comment != null) {
            System.out.print(" -> " + String.format(comment, args));
        }
        System.out.println();
        if (statusListener != null) {
            statusListener.done((long) duration, text, comment != null ? String.format(comment, args) : null);
        }
        for (Map.Entry<String, Integer> problem : problems.entrySet()) {
            if (statusListener != null) {
                statusListener.problem(problem.getKey(), problem.getValue());
            }
            System.out.println(String.format(" [PROBLEM] %s (%d time(s))", problem.getKey(), problem.getValue()));
        }
    }

    public static Status of(String message) {
        return new Status(message, 100000);
    }

    public static Status of(String message, int skip) {
        return new Status(message, skip);
    }
}
