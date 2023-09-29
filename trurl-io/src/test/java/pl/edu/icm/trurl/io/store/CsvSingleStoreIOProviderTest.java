package pl.edu.icm.trurl.io.store;

import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.io.csv.CsvReader;
import pl.edu.icm.trurl.io.csv.CsvWriter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvSingleStoreIOProviderTest {

    @Test
    void getReaderForCsv() {
        // given
        CsvSingleStoreIOProvider csvSingleStoreIOProvider = new CsvSingleStoreIOProvider();
        //execute & assert
        assertThat(csvSingleStoreIOProvider.getReaderFor("csv")).isInstanceOf(CsvReader.class);
    }

    @Test
    void getWriterForCsv() {
        // given
        CsvSingleStoreIOProvider csvSingleStoreIOProvider = new CsvSingleStoreIOProvider();
        //execute & assert
        assertThat(csvSingleStoreIOProvider.getWriterFor("csv")).isInstanceOf(CsvWriter.class);
    }

    @Test
    void shouldThrowOtherwise() {
        // given
        CsvSingleStoreIOProvider csvSingleStoreIOProvider = new CsvSingleStoreIOProvider();
        // execute & assert
        assertThrows(IllegalArgumentException.class, () -> csvSingleStoreIOProvider.getReaderFor("orc"));
        assertThrows(IllegalArgumentException.class, () -> csvSingleStoreIOProvider.getWriterFor("orc"));
    }
}