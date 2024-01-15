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
import pl.edu.icm.trurl.io.WriterProvider;
import pl.edu.icm.trurl.io.store.SingleStoreWriter;
import pl.edu.icm.trurl.store.StoreInspector;
import pl.edu.icm.trurl.store.attribute.Attribute;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CsvWriter implements SingleStoreWriter {
    private final Pattern NEEDS_ESCAPE = Pattern.compile("[,\\n\\r\"]");
    private final WriterProvider writerProvider;

    @WithFactory
    public CsvWriter(WriterProvider writerProvider) {
        this.writerProvider = writerProvider;
    }

    @Override
    public void write(String outputPath, StoreInspector store) throws IOException {
        try (Writer bufferedWriter = writerProvider.writerForFile(outputPath, 1024 * 1024 * 128)) {
            write(bufferedWriter, store, 0, store.getCounter().getCount());
        }
    }

    private void write(Writer bufferedWriter, StoreInspector store, int fromInclusive, int toExclusive) throws IOException {
        Attribute[] attributes = store.attributes().collect(Collectors.toList()).toArray(new Attribute[]{});

        for (int i = 0; i < attributes.length; i++) {
            if (i != 0) {
                bufferedWriter.write(',');
            }
            writeCharacters(bufferedWriter, attributes[i].name());
        }

        bufferedWriter.write('\n');

        for (int row = fromInclusive; row < toExclusive; row++) {
            for (int i = 0; i < attributes.length; i++) {
                if (i != 0) {
                    bufferedWriter.write(',');
                }
                if (!attributes[i].isEmpty(row)) {
                    writeCharacters(bufferedWriter, attributes[i].getString(row));
                }
            }
            bufferedWriter.write('\n');
        }
    }


    private void writeCharacters(Writer writer, String chars) throws IOException {
        if (NEEDS_ESCAPE.matcher(chars).find()) {
            writeEscaped(writer, chars);
        } else {
            writer.write(chars);
        }
    }

    private void writeEscaped(Writer writer, String chars) throws IOException {
        int length = chars.length();
        writer.write('"');
        for (int i = 0; i < length; i++) {
            char current = chars.charAt(i);
            switch (current) {
                case '"':
                    writer.write("\"\"");
                    break;
                case '\n':
                case '\r':
                    writer.write("\\n");
                    break;
                default:
                    writer.write(current);
            }
        }
        writer.write('"');
    }
}
