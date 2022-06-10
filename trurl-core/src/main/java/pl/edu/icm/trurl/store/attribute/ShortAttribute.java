package pl.edu.icm.trurl.store.attribute;

public interface ShortAttribute extends Attribute {
    short getShort(int row);
    void setShort(int row, short value);
}
