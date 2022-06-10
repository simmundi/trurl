package pl.edu.icm.trurl.store.attribute;

public interface ByteAttribute extends Attribute {
    byte getByte(int row);
    void setByte(int row, byte value);
}
