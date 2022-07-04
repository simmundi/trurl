package pl.edu.icm.trurl.store.attribute;

import pl.edu.icm.trurl.store.IntSink;
import pl.edu.icm.trurl.store.IntSource;

public interface ValueObjectListAttribute extends Attribute {
    int getSize(int row);

    void loadIds(int row, IntSink ids);

    void saveIds(int row, int size, IntSource ids);

    boolean isEqual(int row, int size, IntSource ids);
}
