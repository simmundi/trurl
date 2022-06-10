package pl.edu.icm.trurl.store.attribute;

import pl.edu.icm.trurl.store.IntSink;
import pl.edu.icm.trurl.store.IntSource;

public interface IntListAttribute extends Attribute {

    int getSize(int row);

    void loadInts(int row, IntSink ints);

    void saveInts(int row, int size, IntSource ints);

    boolean isEqual(int row, int size, IntSource ints);
}
