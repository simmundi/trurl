package pl.edu.icm.trurl.store.array;

import com.google.common.base.Converter;
import com.google.common.base.Enums;
import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import pl.edu.icm.trurl.store.attribute.EnumAttribute;

import java.util.Arrays;
import java.util.Collections;

final public class EnumArrayAttribute<T extends Enum<T>> implements EnumAttribute<T> {
    private final String name;
    private final Converter<String, T> converter;
    private byte[] values;
    private static byte NULL = Byte.MIN_VALUE;
    private T[] instances;
    private int capacity;

    public EnumArrayAttribute(Class<T> enumType, String name, int capacity) {
        this.name = name;
        values = new byte[0];
        converter = Enums.stringConverter(enumType);
        instances = enumType.getEnumConstants();
        ensureCapacity(capacity);
    }

    @Override
    public void ensureCapacity(int capacity) {
        if (capacity > this.capacity) {
            int target = (int) Math.max(capacity, this.capacity * 1.5);
            this.capacity = target;
            byte[] bigger = Arrays.copyOf(values, target);
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
        byte ordinal = values[row];
        return ordinal >= 0 ? instances[ordinal].name() : "";
    }

    @Override
    public void setString(int row, String value) {
        values[row] = Strings.isNullOrEmpty(value) ? Byte.MIN_VALUE : (byte) converter.convert(value).ordinal();
    }

    @Override
    public T getEnum(int row) {
        byte ordinal = values[row];
        return ordinal == Byte.MIN_VALUE ? null : instances[ordinal];
    }

    @Override
    public void setEnum(int row, T value) {
        setOrdinal(row, value != null ? (byte) value.ordinal() : Byte.MIN_VALUE);
    }

    @Override
    public byte getOrdinal(int row) {
        return values[row];
    }

    @Override
    public void setOrdinal(int row, byte value) {
        values[row] = value;
    }

    @Override
    public T[] values() {
        return instances;
    }
}
