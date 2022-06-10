package pl.edu.icm.trurl.store.tablesaw.attribute;

import pl.edu.icm.trurl.store.attribute.ShortAttribute;
import tech.tablesaw.api.ShortColumn;

public class TablesawShortAttribute extends TablesawAttribute<ShortColumn> implements ShortAttribute {
    public TablesawShortAttribute(String name) {
        super(ShortColumn.create(name));
    }

    public TablesawShortAttribute(String name, int initialSize) {
        super(ShortColumn.create(name, initialSize));
    }

    public short getShort(int row) {
        return column().getShort(row);
    }
    public void setShort(int row, short value) {
        column().set(row, value);
    }

    @Override
    void setNotBlankString(int row, String value) {
        column().set(row, Short.parseShort(value));
    }

    @Override
    String getNotBlankString(int row) {
        return Short.toString(column().getShort(row));
    }
}
