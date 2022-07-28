package pl.edu.icm.trurl.store.attribute;

import pl.edu.icm.trurl.store.IntSink;

public interface ValueObjectListAttribute extends Attribute {
    int getSize(int row);

    void loadIds(int row, IntSink intSink);

    /**
     * @param size number of ids to save
     * @param firstNewIndex first index to use if size exceeds the biggest row size ever
     * @return returns firstNewIndex if firstNewIndex wasn't used or (the greatest used index + 1)
     */
    int saveIds(int row, int size, int firstNewIndex);
}
