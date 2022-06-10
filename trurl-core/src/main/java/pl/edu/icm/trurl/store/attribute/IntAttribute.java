package pl.edu.icm.trurl.store.attribute;

public interface IntAttribute extends Attribute {
    int getInt(int row);
    void setInt(int row, int value);
}
