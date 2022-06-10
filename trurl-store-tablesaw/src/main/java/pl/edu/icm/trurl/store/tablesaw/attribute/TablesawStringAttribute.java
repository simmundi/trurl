package pl.edu.icm.trurl.store.tablesaw.attribute;

import pl.edu.icm.trurl.store.attribute.StringAttribute;
import tech.tablesaw.api.TextColumn;

public class TablesawStringAttribute extends TablesawAttribute<TextColumn> implements StringAttribute {
    public TablesawStringAttribute(String name) {
        super(TextColumn.create(name));
    }

    public TablesawStringAttribute(String name, int initialSize) {
        super(TextColumn.create(name, initialSize));
    }

    @Override
    String getNotBlankString(int row) {
        return column().getString(row);
    }

    @Override
    void setNotBlankString(int row, String value) {
        column().set(row, value);
    }
}
