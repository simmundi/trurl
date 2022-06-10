package pl.edu.icm.trurl.store.array;

import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.StoreFactory;

public class ArrayStoreFactory implements StoreFactory {
    private final int defaulCapacity;

    public ArrayStoreFactory(int defaulCapacity) {
        this.defaulCapacity = defaulCapacity;
    }

    public ArrayStoreFactory() {
        this(ArrayStore.DEFAULT_CAPACITY);
    }

    @Override
    public Store create() {
        return new ArrayStore(defaulCapacity);
    }
}
