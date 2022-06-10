package pl.edu.icm.trurl.store.attribute;

public interface Attribute {
    void ensureCapacity(int capacity);
    boolean isEmpty(int row);
    void setEmpty(int row);
    String name();
    String getString(int row);
    void setString(int row, String value);
}
