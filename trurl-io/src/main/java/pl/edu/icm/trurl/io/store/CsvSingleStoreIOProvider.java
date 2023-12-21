package pl.edu.icm.trurl.io.store;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.io.csv.CsvReader;
import pl.edu.icm.trurl.io.csv.CsvWriter;

public class CsvSingleStoreIOProvider implements SingleStoreIOProvider {
    private final CsvReader csvReader;
    private final CsvWriter csvWriter;

    @WithFactory
    public CsvSingleStoreIOProvider(CsvReader csvReader, CsvWriter csvWriter) {
        this.csvReader = csvReader;
        this.csvWriter = csvWriter;
    }

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
