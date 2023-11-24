/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.trurl.io.csv;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento.annotation.WithFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.icm.trurl.io.store.SingleStoreReader;
import pl.edu.icm.trurl.store.StoreInspector;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.attribute.StubAttribute;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class CsvReader implements SingleStoreReader {
    private final Logger logger = LoggerFactory.getLogger(CsvReader.class);

    private final Attribute SKIP = new StubAttribute();

    @WithFactory
    public CsvReader() {
    }


    @Override
    public void read(File file, StoreInspector store) throws IOException {
        try (InputStream stream = Files.newInputStream(file.toPath())) {
            load(stream, store);
        }
    }

    public void load(InputStream stream, StoreInspector store, String... columns) {
        load(stream, store, Collections.emptyMap(), columns);
    }


    private void load(InputStream stream, StoreInspector store, Map<String, String> mappings, String... columns) {
        int storeCount = store.getCounter().getCount();
        if (storeCount > 0) {
            logger.warn(String.format("Loading from file to non-empty store (store count is: %d). New rows will be appended.", storeCount));
        }
        CsvParserSettings settings = new CsvParserSettings();
        settings.detectFormatAutomatically(',', '\t');
        settings.setSkipEmptyLines(false);
        CsvParser csvParser = new CsvParser(settings);
        csvParser.beginParsing(new BufferedInputStream(stream, 1024 * 1024));
        String[] header = columns.length > 0 ? columns : csvParser.parseNext();
        Attribute[] attributes = new Attribute[header.length];

        final int columnCount = header.length;
        for (int i = 0; i < columnCount; i++) {
            String attributeName = mappings.getOrDefault(header[i], header[i]);
            attributes[i] = Optional.<Attribute>ofNullable(store.get(attributeName)).orElse(SKIP);
        }

        while (true) {
            String[] line = csvParser.parseNext();
            if (line == null) {
                break;
            }
            try {
                int next = store.getCounter().next();
                for (int i = 0; i < columnCount; i++) {
                    attributes[i].setString(next, line[i]);
                }
            } catch (RuntimeException re) {
                logger.info("wrong format in csv file: "
                        + re.getMessage()
                        .replace('\n', ' ')
                        .replace('\r', ' '));
            }
        }
    }
}

