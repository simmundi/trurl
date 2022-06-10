package pl.edu.icm.trurl.util;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class EnumTable<R extends Enum, C extends Enum, V> {
    private final Object[] values;
    private final int rows;
    private final int cols;

    public EnumTable(Class<R> rowClass, Class<C> columnClass) {
        rows = rowClass.getEnumConstants().length;
        cols = columnClass.getEnumConstants().length;
        values = new Object[rows * cols];
    }

    public void put(R row, C col, V value) {
        values[row.ordinal() * cols + col.ordinal()] = value;
    }

    public V get(R row, C col) {
        return (V) values[row.ordinal() * cols + col.ordinal()];
    }

    public Stream<V> stream() {
        return IntStream.range(0, values.length)
                .mapToObj(idx -> (V)values[idx])
                .filter(v -> v != null);
    }
}
