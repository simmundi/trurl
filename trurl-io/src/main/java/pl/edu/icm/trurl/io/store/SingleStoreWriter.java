package pl.edu.icm.trurl.io.store;

import net.snowyhollows.bento.annotation.ImplementationSwitch;
import pl.edu.icm.trurl.store.StoreInspector;

import java.io.File;
import java.io.IOException;
@ImplementationSwitch
public interface SingleStoreWriter {
    void write(File file, StoreInspector store) throws IOException;

}
