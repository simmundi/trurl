package pl.edu.icm.trurl.store.array;

import com.google.common.base.Strings;
import pl.edu.icm.trurl.store.attribute.ShortAttribute;

import java.util.Arrays;

final public class ShortArrayAttribute implements ShortAttribute {
    private final String name;
    private int capacity;
    private short[] values;
    private final static short NULL = Short.MIN_VALUE;

    public ShortArrayAttribute(String name, int capacity) {
        this.name = name;
        this.values = new short[0];
        ensureCapacity(capacity);
    }

    @Override
    public void ensureCapacity(int capacity) {
        if (capacity > this.capacity) {
            int target = (int) Math.max(capacity, this.capacity * 1.5);
            this.capacity = target;
            short[] bigger = Arrays.copyOf(values, target);
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
        return Short.toString(getShort(row));
    }

    @Override
    public void setString(int row, String value) {
        setShort(row, Strings.isNullOrEmpty(value) ? Short.MIN_VALUE : Short.parseShort(value));
    }

    @Override
    public short getShort(int row) {
        return values[row];
    }

    @Override
    public void setShort(int row, short value) {
        values[row] = value;
    }
}
