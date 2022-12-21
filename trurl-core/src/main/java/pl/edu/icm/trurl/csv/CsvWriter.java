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

package pl.edu.icm.trurl.csv;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.DefaultWorkDir;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.attribute.Attribute;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CsvWriter {
    private final WorkDir workDir;
    private final Pattern NEEDS_ESCAPE = Pattern.compile("[,\\n\\r\"]");

    @WithFactory
    public CsvWriter() {
        this(new DefaultWorkDir());
    }

    public CsvWriter(WorkDir workDir) {
        this.workDir = workDir;
    }

    public void writeCsv(String outputPath, Store store) throws IOException {
        writeCsv(outputPath, store, 0, store.getCount());
    }

    public void writeCsv(String outputPath, Store store, int fromInclusive, int toExclusive) throws IOException {
        try (
                OutputStream outputStream = this.workDir.openForWriting(new File(outputPath));
                OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                BufferedWriter bufferedWriter = new BufferedWriter(streamWriter, 1024 * 1024 * 128)
        ) {
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
