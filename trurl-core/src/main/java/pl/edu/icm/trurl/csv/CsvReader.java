package pl.edu.icm.trurl.csv;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento2.annotation.WithFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.StoreFactory;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.attribute.StubAttribute;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class CsvReader {

    private final Logger logger = LoggerFactory.getLogger(CsvReader.class);
    private final Attribute SKIP = new StubAttribute();

    @WithFactory
    public CsvReader() {
    }

    public <T> Mapper<T> load(InputStream stream, StoreFactory storeFactory, Class<T> model, String... columns) {
        Store store = storeFactory.create();
        Mapper<T> mapper = Mappers.create(model);
        mapper.configureStore(store);
        int count = load(stream, store, columns);
        mapper.attachStore(store);
        mapper.setCount(count);
        return mapper;
    }


    public int load(InputStream stream, Store store, String... columns) {
        return load(stream, store, Collections.emptyMap(), columns);
    }


    public int load(InputStream stream, Store store, Map<String, String> mappings, String... columns) {
        CsvParserSettings settings = new CsvParserSettings();
        settings.detectFormatAutomatically(',', '\t');
        CsvParser csvParser = new CsvParser(settings);
        csvParser.beginParsing(new BufferedInputStream(stream, 1024 * 1024));
        String[] header = columns.length > 0 ? columns : csvParser.parseNext();
        Attribute[] attributes = new Attribute[header.length];

        final int columnCount = header.length;
        for (int i = 0; i < columnCount; i++) {
            String attributeName = mappings.getOrDefault(header[i], header[i]);
            attributes[i] = Optional.<Attribute>ofNullable(store.get(attributeName)).orElse(SKIP);
        }

        int rowCount = 0;
        while (true) {
            String[] line = csvParser.parseNext();
            if (line == null) {
                break;
            }
            try {
                for (int i = 0; i < columnCount; i++) {
                    attributes[i].ensureCapacity(rowCount + 1);
                    attributes[i].setString(rowCount, line[i]);
                }
                rowCount++;
            } catch (RuntimeException re) {
                logger.info("wrong format in csv file: "
                        + re.getMessage()
                        .replace('\n', ' ')
                        .replace('\r', ' '));
            }
        }
        store.fireUnderlyingDataChanged(0, rowCount);
        return rowCount;
    }
}

