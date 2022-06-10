package pl.edu.icm.trurl.store.tablesaw.attribute;

import com.google.common.base.Converter;
import com.google.common.base.Enums;
import com.google.common.base.Strings;
import pl.edu.icm.trurl.store.attribute.EnumAttribute;
import tech.tablesaw.api.StringColumn;

public class TablesawEnumAttribute<E extends Enum<E>> extends TablesawAttribute<StringColumn> implements EnumAttribute<E> {

    private final Converter<String, E> converter;
    private E[] values;

    public TablesawEnumAttribute(Class<E> enumType, String name) {
        super(StringColumn.create(name));
        converter = Enums.stringConverter(enumType);
        values = enumType.getEnumConstants();
    }

    public TablesawEnumAttribute(Class<E> enumType, String name, int size) {
        super(StringColumn.create(name, size));
        converter = Enums.stringConverter(enumType);
        values = enumType.getEnumConstants();
    }

    public E getEnum(int row) {
        String literal = column().get(row);
        return Strings.isNullOrEmpty(literal) ? null : converter.convert(column().get(row));
    }
    public void setEnum(int row, E value) {
        column().set(row, value == null ? null : value.name());
    }

    @Override
    public byte getOrdinal(int row) {
        return (byte) getEnum(row).ordinal();
    }

    @Override
    public void setOrdinal(int row, byte value) {
        setEnum(row, values[value]);
    }

    @Override
    public E[] values() {
        return values;
    }

    @Override
    void setNotBlankString(int row, String value) {
        column().set(row, value);
    }

    @Override
    String getNotBlankString(int row) {
        return column().getString(row);
    }
}
