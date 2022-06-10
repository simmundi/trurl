package pl.edu.icm.trurl.store.attribute;

public interface StringAttribute extends Attribute {
    String getString(int row);
    void setString(int row, String value);
}
