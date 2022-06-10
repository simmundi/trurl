package pl.edu.icm.trurl.store.array;

import pl.edu.icm.trurl.store.AbstractStoreTest;

class ArrayStoreTest extends AbstractStoreTest<ArrayStore> {
    @Override
    protected ArrayStore createStore() {
        return new ArrayStore();
    }
}
