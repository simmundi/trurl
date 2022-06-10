package pl.edu.icm.trurl.store.array;

import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import pl.edu.icm.trurl.store.attribute.FloatAttribute;

import java.util.Arrays;
import java.util.Collections;

final public class FloatArrayAttribute implements FloatAttribute {
    private final String name;
    private int capacity;
    private float[] values;
    private final static float NULL = Float.NaN;

    public FloatArrayAttribute(String name, int capacity) {
        this.name = name;
        this.values = new float[0];
        ensureCapacity(capacity);
    }

    @Override
    public void ensureCapacity(int capacity) {
        if (capacity > this.capacity) {
            int target = (int) Math.max(capacity, this.capacity * 1.5);
            this.capacity = target;
            float[] bigger = Arrays.copyOf(values, target);
            Arrays.fill(bigger, values.length, target, NULL);
            this.values = bigger;
        }
    }

    @Override
    public boolean isEmpty(int row) {
        return row >= values.length || Float.isNaN(values[row]);
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
        return Float.toString(getFloat(row));
    }

    @Override
    public void setString(int row, String value) {
        setFloat(row, Strings.isNullOrEmpty(value) ? Float.MIN_VALUE : Float.parseFloat(value));
    }

    @Override
    public float getFloat(int row) {
        return values[row];
    }

    @Override
    public void setFloat(int row, float value) {
        values[row] = value;
    }
}