package pl.edu.icm.trurl.io.store;

import pl.edu.icm.trurl.store.StoreInspector;

import java.io.File;
import java.io.IOException;

public interface SingleStoreReader {
    void read(String file, StoreInspector store) throws IOException;
}
