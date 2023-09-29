package pl.edu.icm.trurl.io.store;

import pl.edu.icm.trurl.io.csv.CsvReader;
import pl.edu.icm.trurl.io.csv.CsvWriter;

public class CsvSingleStoreIOProvider implements SingleStoreIOProvider {
    private final SingleStoreReader csvReader = new CsvReader();
    private final SingleStoreWriter csvWriter = new CsvWriter();

    @Override
    public SingleStoreReader getReaderFor(String format) {
        if (format.equals("csv"))
            return csvReader;
        else
            throw new IllegalArgumentException("No reader available for \""+format+"\". Use different provider");
    }

    @Override
    public SingleStoreWriter getWriterFor(String format) {
        if (format.equals("csv"))
            return csvWriter;
        else
            throw new IllegalArgumentException("No writer available for \""+format+"\". Use different provider");
    }
}
