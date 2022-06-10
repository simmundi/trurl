package pl.edu.icm.trurl.store.array;

import com.google.common.base.Strings;
import pl.edu.icm.trurl.store.attribute.StringAttribute;

import java.util.ArrayList;
import java.util.Collections;

final public class StringArrayAttribute implements StringAttribute {
    private final String name;
    private final ArrayList<String> strings;
    private int capacity;

    public StringArrayAttribute(String name, int capacity) {
        this.name = name;
        this.strings = new ArrayList<>(capacity);
        ensureCapacity(capacity);
    }

    @Override
    public void ensureCapacity(int capacity) {
        if (capacity > this.capacity) {
            int target = (int) Math.max(capacity, this.capacity * 1.5);
            this.capacity = target;
            this.strings.ensureCapacity(target);
            this.strings.addAll(Collections.nCopies(target - strings.size(), null));
        }
    }

    @Override
    public boolean isEmpty(int row) {
        return row >= strings.size() || Strings.isNullOrEmpty(strings.get(row));
    }

    @Override
    public void setEmpty(int row) {
        strings.set(row, null);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String getString(int row) {
        String value = strings.get(row);
        return Strings.isNullOrEmpty(value) ? "" : value;
    }

    @Override
    public void setString(int row, String value) {
        strings.set(row, value);
    }

}
