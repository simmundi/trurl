package pl.edu.icm.trurl.io.store;

import net.snowyhollows.bento.annotation.ImplementationSwitch;
import pl.edu.icm.trurl.store.StoreAccess;

import java.io.IOException;
@ImplementationSwitch
public interface SingleStoreWriter {
    void write(String file, StoreAccess store) throws IOException;

}
