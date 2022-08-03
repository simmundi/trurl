package pl.edu.icm.trurl.store.tablesaw;

import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.StoreFactory;

public class TablesawStoreFactory implements StoreFactory {
    @Override
    public Store create(int ignoredInitialCapacity) {
        return new TablesawStore();
    }
}
