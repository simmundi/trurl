package pl.edu.icm.trurl.store.attribute;

public class StubAttribute implements Attribute {

    private int capacity = 0;

    @Override
    public void ensureCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public boolean isEmpty(int row) {
        return true;
    }

    @Override
    public void setEmpty(int row) {
        // nothing
    }

    @Override
    public String name() {
        return "STUB";
    }

    @Override
    public String getString(int row) {
        return "";
    }

    @Override
    public void setString(int row, String value) {
        // noop
    }
}
