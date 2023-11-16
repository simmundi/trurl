package pl.edu.icm.trurl.io.store;

import net.snowyhollows.bento.annotation.ImplementationSwitch;

@ImplementationSwitch(configKey = "trurl.io.single-store-IO-provider",
        cases = {
                @ImplementationSwitch.When(name = "csv-only", implementation = CsvSingleStoreIOProvider.class, useByDefault = true)
        })
public interface SingleStoreIOProvider {
    SingleStoreReader getReaderFor(String format);

    SingleStoreWriter getWriterFor(String format);
}
