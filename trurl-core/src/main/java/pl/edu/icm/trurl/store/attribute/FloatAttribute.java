package pl.edu.icm.trurl.store.attribute;

public interface FloatAttribute extends Attribute {
    float getFloat(int row);
    void setFloat(int row, float value);
}
