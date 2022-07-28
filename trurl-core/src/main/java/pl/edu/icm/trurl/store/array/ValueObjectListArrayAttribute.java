package pl.edu.icm.trurl.store.array;

import com.google.common.base.Strings;
import pl.edu.icm.trurl.store.IntSink;
import pl.edu.icm.trurl.store.attribute.ValueObjectListAttribute;

import java.util.Arrays;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

public class ValueObjectListArrayAttribute implements ValueObjectListAttribute {
    private final String name;
    private int[][] values;
    private int capacity;
    private final Pattern splitter = Pattern.compile(",");

    public ValueObjectListArrayAttribute(String name, int capacity) {
        this.name = name;
        values = new int[0][0];
        ensureCapacity(capacity);
    }

    @Override
    public void ensureCapacity(int capacity) {
        if (capacity > this.capacity) {
            int target = (int) Math.max(capacity, this.capacity * 1.5);
            this.capacity = target;
            this.values = Arrays.copyOf(values, target);
        }
    }

    @Override
    public boolean isEmpty(int row) {
        return row >= values.length || values[row] == null || values[row].length < 1 || values[row][0] <= 0;
    }

    @Override
    public void setEmpty(int row) {
        if (values[row].length > 0 && values[row][0] > 0) values[row][0] = -values[row][0];
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String getString(int row) {
        int[] ints = values[row];
        if (ints.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(EntityEncoder.encode(ints[0]));
        for (int i = 1; i < ints.length; i++) {
            sb.append(splitter);
            sb.append(EntityEncoder.encode(ints[i]));
        }
        return sb.toString();
    }

    @Override
    public void setString(int row, String value) {
        if (!Strings.isNullOrEmpty(value)) {
            String[] split = splitter.split(value);
            int[] result = values[row];
            if (result == null || result.length != split.length) {
                result = new int[split.length];
                values[row] = result;
            }
            for (int i = 0; i < split.length; i++) {
                result[i] = EntityEncoder.decode(split[i]);
            }
        } else {
            setEmpty(row);
        }
    }

    @Override
    public int getSize(int row) {
        int[] ints = values[row];
        if (ints == null) return -1;
        int i = 0;
        while (i < ints.length && ints[i] > 0) {
            i++;
        }

        return i;
    }

    @Override
    public void loadIds(int row, IntSink intSink) {
        int[] result = values[row];
        if (result != null) {
            for (int i = 0; i < result.length && result[i] >= 0; i++) {
                intSink.setInt(i, result[i]);
            }
        }
    }

    @Override
    public int saveIds(int row, int size, int firstNewIndex) {
        checkArgument(firstNewIndex > 0, "Index should be greater than 0. 0 >= " + firstNewIndex);
        checkArgument(size >= 0, "Size should be greater or equal 0. 0 > " + size);
        int[] ints = values[row];
        int oldLength = ints == null ? 0 : ints.length;
        if (size <= oldLength) {
            for (int i = 0; i < size; i++)
                ints[i] = Math.abs(ints[i]);
            if (size < oldLength) ints[size] = -Math.abs(ints[size]);
            return firstNewIndex;
        } else {
            ints = ints == null ? new int[size] : Arrays.copyOf(ints, (int) Math.max(size, ints.length * 1.5));
            values[row] = ints;
            for (int i = 0; i < oldLength; i++) {
                ints[i] = Math.abs(ints[i]);
            }
            for (int i = 0; i < ints.length - oldLength; i++) {
                ints[oldLength + i] = firstNewIndex + i;
            }
            if (size < ints.length)
                ints[size ] = -Math.abs(ints[size]);
            return Math.abs(ints[ints.length - 1]) + 1;
        }
    }
}
