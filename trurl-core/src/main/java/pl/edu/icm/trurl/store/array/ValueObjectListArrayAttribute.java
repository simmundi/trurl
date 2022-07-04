package pl.edu.icm.trurl.store.array;

import pl.edu.icm.trurl.store.IntSink;
import pl.edu.icm.trurl.store.IntSource;
import pl.edu.icm.trurl.store.attribute.ValueObjectListAttribute;

public final class ValueObjectListArrayAttribute implements ValueObjectListAttribute {

    private final IntListArrayAttributeWithPadding wrappedAttribute;

    public ValueObjectListArrayAttribute(IntListArrayAttributeWithPadding wrappedAttribute) {
        this.wrappedAttribute = wrappedAttribute;
    }

    @Override
    public void ensureCapacity(int capacity) {
        wrappedAttribute.ensureCapacity(capacity);
    }

    @Override
    public boolean isEmpty(int row) {
        return wrappedAttribute.isEmpty(row);
    }

    @Override
    public void setEmpty(int row) {
        wrappedAttribute.setEmpty(row);
    }

    @Override
    public String name() {
        return wrappedAttribute.name();
    }

    @Override
    public String getString(int row) {
        return wrappedAttribute.getString(row);
    }

    @Override
    public void setString(int row, String value) {
        wrappedAttribute.setString(row, value);
    }

    @Override
    public int getSize(int row) {
        return wrappedAttribute.getSize(row);
    }

    @Override
    public void loadIds(int row, IntSink ids) {
        wrappedAttribute.loadInts(row, ids);
    }

    @Override
    public void saveIds(int row, int size, IntSource ids) {
        wrappedAttribute.saveInts(row, size, ids);
    }

    @Override
    public boolean isEqual(int row, int size, IntSource ids) {
        return wrappedAttribute.isEqual(row, size, ids);
    }
}
