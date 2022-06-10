package pl.edu.icm.trurl.store.attribute;

public interface DoubleAttribute extends Attribute {
    double getDouble(int row);
    void setDouble(int row, double value);
}
