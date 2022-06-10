package pl.edu.icm.trurl.store.tablesaw.attribute;

import pl.edu.icm.trurl.store.attribute.FloatAttribute;
import tech.tablesaw.api.FloatColumn;

public class TablesawFloatAttribute extends TablesawAttribute<FloatColumn> implements FloatAttribute {
    public TablesawFloatAttribute(String name) {
        super(FloatColumn.create(name));
    }

    public TablesawFloatAttribute(String name, int initialSize) {
        super(FloatColumn.create(name, initialSize));
    }

    public float getFloat(int row) {
        return column().getFloat(row);
    }

    public void setFloat(int row, float value) {
        column().set(row, value);
    }

    @Override
    void setNotBlankString(int row, String value) {
        column().set(row, Float.parseFloat(value));
    }

    @Override
    String getNotBlankString(int row) {
        return Float.toString(column().getFloat(row));
    }
}
