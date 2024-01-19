package pl.edu.icm.trurl.io.store;

import pl.edu.icm.trurl.store.StoreAccess;

import java.io.IOException;

public interface SingleStoreReader {
    void read(String file, StoreAccess store) throws IOException;
}
