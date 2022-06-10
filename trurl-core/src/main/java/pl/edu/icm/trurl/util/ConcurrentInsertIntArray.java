package pl.edu.icm.trurl.util;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkPositionIndex;

public class ConcurrentInsertIntArray {
    public static final int defaultMaxSize = 40_000_000;

    private final AtomicInteger counter = new AtomicInteger(0);
    private final int maxSize;
    private final int[] table;

    public ConcurrentInsertIntArray(int maxSize) {
        this.maxSize = maxSize;
        this.table = new int[maxSize];
    }


    public void add(int value) {
        try {
            table[counter.getAndIncrement()] = value;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Maximum table size exceeded");
        }
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getCurrentSize() {
        return counter.get();
    }

    public boolean contains(int id) {
        for (int i = 0; i < counter.get(); i++) {
            if (id == table[i]) {
                return true;
            }
        }
        return false;
    }

    public IntStream stream() {
        return Arrays.stream(this.table, 0, counter.get());
    }

    public int[] elements() {
        return table;
    }

    public int getInt(int index) {
        checkPositionIndex(index, getCurrentSize() - 1, "index out of bounds");
        return table[index];
    }
}
