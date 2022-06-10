package pl.edu.icm.trurl.store.tablesaw.attribute;

import pl.edu.icm.trurl.store.attribute.IntAttribute;
import tech.tablesaw.api.IntColumn;

public class TablesawIntAttribute extends TablesawAttribute<IntColumn> implements IntAttribute {
    public TablesawIntAttribute(String name) {
        super(IntColumn.create(name));
    }

    public TablesawIntAttribute(String name, int initialSize) {
        super(IntColumn.create(name, initialSize));
    }

    public int getInt(int row) {
        return column().getInt(row);
    }
    public void setInt(int row, int value) {
        column().set(row, value);
    }

    @Override
    void setNotBlankString(int row, String value) {
        column().set(row, Integer.parseInt(value));
    }

    @Override
    String getNotBlankString(int row) {
        return Integer.toString(column().get(row));
    }
}
