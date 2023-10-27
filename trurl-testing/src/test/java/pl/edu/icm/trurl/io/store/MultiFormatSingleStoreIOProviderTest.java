package pl.edu.icm.trurl.io.store;

import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.io.csv.CsvReader;
import pl.edu.icm.trurl.io.csv.CsvWriter;
import pl.edu.icm.trurl.io.orc.OrcReader;
import pl.edu.icm.trurl.io.orc.OrcWriter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MultiFormatSingleStoreIOProviderTest {

    @Test
    void getReaderForCsvOrc() {
        // given
        MultiFormatSingleStoreIOProvider multiFormatSingleStoreIOProvider = new MultiFormatSingleStoreIOProvider();
        //execute & assert
        assertThat(multiFormatSingleStoreIOProvider.getReaderFor("csv")).isInstanceOf(CsvReader.class);
        assertThat(multiFormatSingleStoreIOProvider.getReaderFor("orc")).isInstanceOf(OrcReader.class);
    }

    @Test
    void getWriterForCsvOrc() {
        // given
        MultiFormatSingleStoreIOProvider multiFormatSingleStoreIOProvider = new MultiFormatSingleStoreIOProvider();
        //execute & assert
        assertThat(multiFormatSingleStoreIOProvider.getWriterFor("csv")).isInstanceOf(CsvWriter.class);
        assertThat(multiFormatSingleStoreIOProvider.getWriterFor("orc")).isInstanceOf(OrcWriter.class);
    }

    @Test
    void shouldThrowOtherwise() {
        // given
        MultiFormatSingleStoreIOProvider multiFormatSingleStoreIOProvider = new MultiFormatSingleStoreIOProvider();
        // execute & assert
        assertThrows(IllegalArgumentException.class, () -> multiFormatSingleStoreIOProvider.getReaderFor("zip"));
        assertThrows(IllegalArgumentException.class, () -> multiFormatSingleStoreIOProvider.getWriterFor("tar.gz"));
    }
}