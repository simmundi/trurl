package pl.edu.icm.trurl.io.store;

import pl.edu.icm.trurl.store.StoreInspector;

import java.io.File;
import java.io.IOException;

public interface SingleStoreReader {
    void read(File file, StoreInspector store) throws IOException;
}
