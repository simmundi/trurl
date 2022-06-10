package pl.edu.icm.trurl.store.tablesaw.attribute;

import pl.edu.icm.trurl.store.attribute.BooleanAttribute;
import tech.tablesaw.api.BooleanColumn;

public class TablesawBooleanAttribute extends TablesawAttribute<BooleanColumn> implements BooleanAttribute {
    public TablesawBooleanAttribute(String name) {
        super(BooleanColumn.create(name));
    }

    public TablesawBooleanAttribute(String name, int initialSize) {
        super(BooleanColumn.create(name, initialSize));
    }

    public boolean getBoolean(int row) {
        return column().get(row);
    }

    public void setBoolean(int row, boolean value) {
        column().set(row, value);
    }

    @Override
    String getNotBlankString(int row) {
        return Boolean.toString(getBoolean(row));
    }

    @Override
    void setNotBlankString(int row, String value) {
        setBoolean(row, Boolean.parseBoolean(value));
    }

    @Override
    public void setEmpty(int row) {
        column().setMissing(row);
    }

}
