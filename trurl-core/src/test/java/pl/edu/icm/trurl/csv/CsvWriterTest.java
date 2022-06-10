package pl.edu.icm.trurl.csv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.util.Filesystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsvWriterTest {

    @Mock
    Filesystem filesystem;
    @Mock
    Attribute columnA;
    @Mock
    Attribute columnB;
    @Mock
    Attribute columnC;
    @Mock
    Store store;
    @Captor
    ArgumentCaptor<File> file;

    ByteArrayOutputStream results;

    @InjectMocks
    CsvWriter csvWriter;


    @BeforeEach
    void before() throws FileNotFoundException {
        results =  new ByteArrayOutputStream();
        when(columnA.name()).thenReturn("a");
        when(columnB.name()).thenReturn("b");
        when(columnC.name()).thenReturn("c");
        when(store.attributes()).thenReturn(Stream.of(columnA, columnB, columnC));
        when(filesystem.openForWriting(file.capture())).thenReturn(results);
    }

    @Test
    @DisplayName("Should create correct file and output correct header")
    void writeCsv__header() throws IOException {
        // execute
        csvWriter.writeCsv("a/b/test.csv", store);

        // assert
        assertThat(file.getValue().toString()).isEqualTo("a/b/test.csv");
        assertThat(results.toString()).isEqualTo("a,b,c\n");
    }

    @Test
    @DisplayName("Should create a file with a couple correct rows")
    void writeCsv__proper_rows() throws IOException {
        // given
        when(store.getCount()).thenReturn(3);
        setMockValues(columnA, "one", null, "three");
        setMockValues(columnB, null, "test", "four");
        setMockValues(columnC, null, null, "five");

        // execute
        csvWriter.writeCsv("a/b/test.csv", store);

        // assert
        assertThat(results.toString()).isEqualTo("a,b,c\none,,\n,test,\nthree,four,five\n");
    }

    @Test
    @DisplayName("Should correctly quote text with commas, newlines or quotation marks")
    void writeCsv__excape_quotes_newlines_and_commas() throws IOException {
        // given
        when(store.getCount()).thenReturn(1);
        setMockValues(columnA, "one,two,three");
        setMockValues(columnB, "Marcin \"Duddie\" Dudar");
        setMockValues(columnC, "Jack\nand\nJill");

        // execute
        csvWriter.writeCsv("a/b/test.csv", store);

        // assert
        assertThat(results.toString()).isEqualTo("a,b,c\n\"one,two,three\",\"Marcin \"\"Duddie\"\" Dudar\",\"Jack\\nand\\nJill\"\n");
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
