package pl.edu.icm.trurl.store.attribute;

public interface BooleanAttribute extends Attribute {
    boolean getBoolean(int row);
    void setBoolean(int row, boolean value);
}
