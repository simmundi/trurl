package pl.edu.icm.trurl.store.array;

import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.StoreFactory;

public class ArrayStoreFactory implements StoreFactory {

    @Override
    public Store create(int initialCapacity) {
        return new ArrayStore(initialCapacity);
    }

    @Override
    public int defaultInitialCapacity() {
        return ArrayStore.DEFAULT_INITIAL_CAPACITY;
    }
}
