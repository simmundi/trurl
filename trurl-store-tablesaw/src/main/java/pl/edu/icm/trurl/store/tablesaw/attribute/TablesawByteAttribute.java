package pl.edu.icm.trurl.store.tablesaw.attribute;

import pl.edu.icm.trurl.store.attribute.ByteAttribute;
import tech.tablesaw.api.ShortColumn;

public class TablesawByteAttribute extends TablesawAttribute<ShortColumn> implements ByteAttribute {

    public TablesawByteAttribute(String name) {
        super(ShortColumn.create(name));
    }

    public TablesawByteAttribute(String name, int initialSize) {
        super(ShortColumn.create(name, initialSize));
    }

    public byte getByte(int row) {
        return (byte) column().getShort(row);
    }

    public void setByte(int row, byte value) {
        column().set(row, value);
    }

    @Override
    String getNotBlankString(int row) {
        return Byte.toString(getByte(row));
    }

    @Override
    void setNotBlankString(int row, String value) {
        setByte(row, Byte.parseByte(value));
    }

}
