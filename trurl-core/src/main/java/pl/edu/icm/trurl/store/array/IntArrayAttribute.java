package pl.edu.icm.trurl.store.array;

import com.google.common.base.Strings;
import pl.edu.icm.trurl.store.attribute.IntAttribute;

import java.util.Arrays;

final public class IntArrayAttribute implements IntAttribute {
    private String name;
    private int capacity;
    private int[] values;
    private final static int NULL = Integer.MIN_VALUE;

    public IntArrayAttribute(String name, int capacity) {
        this.name = name;
        values = new int[0];
        ensureCapacity(capacity);
    }

    @Override
    public void ensureCapacity(int capacity) {
        if (capacity > this.capacity) {
            int target = (int) Math.max(capacity, this.capacity * 1.5);
            this.capacity = target;
            int[] bigger = Arrays.copyOf(values, target);
            Arrays.fill(bigger, values.length, target, NULL);
            this.values = bigger;
        }
    }
    @Override
    public boolean isEmpty(int row) {
        return row >= values.length || values[row] == NULL;
    }

    @Override
    public void setEmpty(int row) {
        values[row] = NULL;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String getString(int row) {
        return Integer.toString(getInt(row));
    }

    @Override
    public void setString(int row, String value) {
        setInt(row, Strings.isNullOrEmpty(value) ? Integer.MIN_VALUE : Integer.parseInt(value));
    }

    @Override
    public int getInt(int row) {
        return values[row];
    }

    @Override
    public void setInt(int row, int value) {
        values[row] = value;
    }
}
