package pl.edu.icm.trurl.store.tablesaw.attribute;

import pl.edu.icm.trurl.store.attribute.DoubleAttribute;
import tech.tablesaw.api.DoubleColumn;

public class TablesawDoubleAttribute extends TablesawAttribute<DoubleColumn> implements DoubleAttribute {
    public TablesawDoubleAttribute(String name) {
        super(DoubleColumn.create(name));
    }

    public TablesawDoubleAttribute(String name, int initialSize) {
        super(DoubleColumn.create(name, initialSize));
    }

    public double getDouble(int row) {
        return column().getDouble(row);
    }
    public void setDouble(int row, double value) {
        column().set(row, value);
    }

    @Override
    void setNotBlankString(int row, String value) {
        column().set(row, Double.parseDouble(value));
    }

    @Override
    String getNotBlankString(int row) {
        return Double.toString(row);
    }
}
