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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.Counter;
import pl.edu.icm.trurl.io.WriterProvider;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.attribute.Attribute;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsvWriterTest {

    @TempDir
    Path tempDir;
    @Mock
    Attribute columnA;
    @Mock
    Attribute columnB;
    @Mock
    Attribute columnC;
    @Mock
    Store store;
    @Mock
    Counter counter;
    @Spy
    WriterProvider writerProvider = new WriterProvider();

    @InjectMocks
    CsvWriter csvWriter;


    @BeforeEach
    void before() {
        when(columnA.name()).thenReturn("a");
        when(columnB.name()).thenReturn("b");
        when(columnC.name()).thenReturn("c");
        when(store.getAllAttributes()).thenReturn(List.of(columnA, columnB, columnC));
    }

    @Test
    @Disabled("Store changes")
    @DisplayName("Should create correct file and output correct header")
    void writeCsv__header() throws IOException {
        // execute
        File outputPath = new File(tempDir.toFile(), "test.csv");
        csvWriter.write(outputPath.getAbsolutePath(), store);

        // assert
        try (Stream<String> stream = Files.lines(outputPath.toPath())) {
            Optional<String> header = stream.findFirst();
            assertThat(header.isPresent()).isTrue();
            assertThat(header.get()).isEqualTo("a,b,c");
        }
    }

    @Test
    @DisplayName("Should create a file with a couple correct rows")
    void writeCsv__proper_rows() throws IOException {
        // given
        File outputPath = new File(tempDir.toFile(), "test.csv");
        when(store.getCounter()).thenReturn(counter);
        when(counter.getCount()).thenReturn(4);
        setMockValues(columnA, "one", null, "three", null);
        setMockValues(columnB, null, "test", "four", null);
        setMockValues(columnC, null, null, "five", null);

        // execute
        csvWriter.write(outputPath.getAbsolutePath(), store);

        // assert
        try (Stream<String> stringStream = Files.lines(outputPath.toPath())) {
            assertThat(stringStream.collect(Collectors.joining("\n"))).isEqualTo("a,b,c\none,,\n,test,\nthree,four,five\n,,");
        }
    }

    @Test
    @DisplayName("Should correctly quote text with commas, newlines or quotation marks")
    void writeCsv__excape_quotes_newlines_and_commas() throws IOException {
        // given
        File outputPath = new File(tempDir.toFile(), "test.csv");
        when(store.getCounter()).thenReturn(counter);
        when(counter.getCount()).thenReturn(1);
        setMockValues(columnA, "one,two,three");
        setMockValues(columnB, "Marcin \"Duddie\" Dudar");
        setMockValues(columnC, "Jack\nand\nJill");

        // execute
        csvWriter.write(outputPath.getAbsolutePath(), store);

        // assert
        try (Stream<String> stream = Files.lines(outputPath.toPath())) {
            String data = stream.collect(Collectors.joining("\n"));
            assertThat(data).isEqualTo("a,b,c\n\"one,two,three\",\"Marcin \"\"Duddie\"\" Dudar\",\"Jack\\nand\\nJill\"");
        }
    }

    private void setMockValues(Attribute attribute, String... values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                when(attribute.isEmpty(i)).thenReturn(true);
            } else {
                when(attribute.isEmpty(i)).thenReturn(false);
                when(attribute.getString(i)).thenReturn(values[i]);
            }
        }
    }
}
