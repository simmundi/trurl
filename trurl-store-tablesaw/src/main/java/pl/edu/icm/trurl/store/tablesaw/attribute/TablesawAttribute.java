package pl.edu.icm.trurl.store.tablesaw.attribute;

import pl.edu.icm.trurl.store.attribute.Attribute;
import tech.tablesaw.columns.Column;

public abstract class TablesawAttribute<E extends Column<?>> implements Attribute {
    private final E column;
    public TablesawAttribute(E column) {
        this.column = column;
    }
    public int capacity() {
        return column.size();
    }
    public void addRows() {
        column.appendMissing();
    }
    public E column() {
        return column;
    }
    public String name() {
        return column.name();
    }

    @Override
    public boolean isEmpty(int row) {
        return row >= column.size() || column.isMissing(row);
    }

    @Override
    public final void setString(int row, String value) {
        if (value == null || value.equals("")) {
            column.setMissing(row);
        } else {
            setNotBlankString(row, value);
        }
    }

    @Override
    public final String getString(int row) {
        return isEmpty(row) ? "" : getNotBlankString(row);
    }

    abstract void setNotBlankString(int row, String value);

    abstract String getNotBlankString(int row);

    @Override
    public void ensureCapacity(int capacity) {
        while (column().size() < capacity) {
            column().appendMissing();
        }
    }

    @Override
    public void setEmpty(int row) {
        column.setMissing(row);
    }
}
