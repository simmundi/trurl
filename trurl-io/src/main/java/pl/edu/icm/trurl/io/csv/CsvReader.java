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

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.io.ReaderProvider;
import pl.edu.icm.trurl.io.parser.Parser;
import pl.edu.icm.trurl.io.store.SingleStoreReader;
import pl.edu.icm.trurl.store.StoreAccess;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.attribute.StubAttribute;

import java.io.Reader;
import java.util.*;

public class CsvReader implements SingleStoreReader {

    private final ReaderProvider readerProvider;

    private final Attribute SKIP = new StubAttribute();

    @WithFactory
    public CsvReader(ReaderProvider readerProvider) {
        this.readerProvider = readerProvider;
    }


    @Override
    public void read(String file, StoreAccess store) {
        load(readerProvider.readerForFile(file), store, Collections.emptyMap());
    }

    public void load(Reader reader, StoreAccess store, String... columns) {
        load(reader, store, Collections.emptyMap(), columns);
    }


    private void load(Reader reader, StoreAccess store, Map<String, String> mappings, String... columns) {
        Parser parser = new Parser(reader);

        List<String> header = new ArrayList<>();
        parser.nextCsvRow(header);
        parser.nextLine();
        Attribute[] attributes = new Attribute[header.size()];

        final int columnCount = header.size();
        for (int i = 0; i < columnCount; i++) {
            String attributeName = mappings.getOrDefault(header.get(i), header.get(i));
            attributes[i] = Optional.<Attribute>ofNullable(store.get(attributeName)).orElse(SKIP);
        }

        // TODO: there is assumption here that the CSV file begins with the same
        // row as the store, i.e. an empty store is used to load a full CSV dump.
        // It is possible that the CSV file was created by saving only a part of the
        // store or that the store wasn't empty; in those cases we need to offset
        // values of references and joins.
        List<String> line = new ArrayList<>();
        while (parser.hasMore()) {
            parser.nextCsvRow(line);
            parser.nextLine();
            int next = store.getCounter().next();
            for (int i = 0; i < columnCount; i++) {
                attributes[i].setString(next, line.get(i));
            }
        }
    }
}

