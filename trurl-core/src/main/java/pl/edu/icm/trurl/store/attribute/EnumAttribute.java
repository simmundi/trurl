package pl.edu.icm.trurl.store.attribute;

public interface EnumAttribute<E extends Enum<E>> extends Attribute {
    E getEnum(int row);
    void setEnum(int row, E value);
    byte getOrdinal(int row);
    void setOrdinal(int row, byte value);
    E[] values();
}
