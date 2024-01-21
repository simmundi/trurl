package pl.edu.icm.trurl.io.orc;

import pl.edu.icm.trurl.io.ReaderProvider;
import pl.edu.icm.trurl.io.WriterProvider;
import pl.edu.icm.trurl.io.csv.CsvReader;
import pl.edu.icm.trurl.io.csv.CsvWriter;
import pl.edu.icm.trurl.io.orc.OrcReader;
import pl.edu.icm.trurl.io.orc.OrcWriter;
import pl.edu.icm.trurl.io.store.SingleStoreIOProvider;
import pl.edu.icm.trurl.io.store.SingleStoreReader;
import pl.edu.icm.trurl.io.store.SingleStoreWriter;

import java.util.Map;

public class MultiFormatSingleStoreIOProvider implements SingleStoreIOProvider {
    private final Map<String, SingleStoreWriter> writers = Map.of(
            "csv", new CsvWriter(new WriterProvider()),
            "orc", new OrcWriter()
    );
    private final Map<String, SingleStoreReader> readers = Map.of(
            "csv", new CsvReader(new ReaderProvider()),
            "orc", new OrcReader()
    );

    @Override
    public SingleStoreReader getReaderFor(String format) {
        SingleStoreReader reader = readers.get(format);
        if (reader == null) {
            throw new IllegalArgumentException("No reader available for \"" + format + "\". Use different provider");
        }
        return reader;
    }

    @Override
    public SingleStoreWriter getWriterFor(String format) {
        SingleStoreWriter writer = writers.get(format);
        if (writer == null) {
            throw new IllegalArgumentException("No writer available for \"" + format + "\". Use different provider");
        }
        return writer;
    }
}
