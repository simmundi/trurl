package pl.edu.icm.trurl.io.store;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.io.ReaderProvider;
import pl.edu.icm.trurl.io.WriterProvider;
import pl.edu.icm.trurl.io.csv.CsvReader;
import pl.edu.icm.trurl.io.csv.CsvWriter;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayAttributeFactory;
import pl.edu.icm.trurl.store.array.ByteArrayAttribute;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreIOTest {
    @TempDir
    Path tempDir;
    @Mock
    Store mockedStore;
    @Mock
    SingleStoreIOProvider singleStoreIOProvider;
    ReaderProvider readerProvider = new ReaderProvider();
    WriterProvider writerProvider = new WriterProvider();



    @Test
    void writeShouldThrow() {
        StoreIO storeIO = new StoreIO(singleStoreIOProvider, readerProvider, writerProvider);
        assertThrows(IllegalArgumentException.class, () -> storeIO.writeStoreToFiles(new File("notExtendedAtAll").getAbsolutePath(), "baseName", mockedStore, "csv"));
        assertThrows(IllegalArgumentException.class, () -> storeIO.writeStoreToFiles(new File("notExtendedProperly.txt").getAbsolutePath(), "baseName", mockedStore, "csv"));
    }

    @Test
    void writeStoreToFiles() throws IOException {
        // given
        when(singleStoreIOProvider.getWriterFor("csv")).thenReturn(new CsvWriter(new WriterProvider()));

        StoreIO storeIO = new StoreIO(singleStoreIOProvider, readerProvider, writerProvider);
        Path filePath = tempDir.resolve("test.properties");
        File file = filePath.toFile();
        Store store = new Store(new ArrayAttributeFactory(), 10);
        store.addByte("byte_column");
        ByteArrayAttribute byteColumn = store.get("byte_column");
        byteColumn.setByte(1, (byte) 8);
        store.addSubstore("oranges");
        Store substore = store.getSubstore("oranges");
        substore.addSubstore("peel");
        substore.getSubstore("peel");

        // execute
        storeIO.writeStoreToFiles(file.getAbsolutePath(), "testBase", store, "csv");
        try (Stream<Path> stream = Files.list(tempDir)) {
            assertThat(stream
                    .map(Path::getFileName).map(Path::toString).collect(Collectors.toList()))
                    .containsExactlyInAnyOrder("test.properties", "testBase.csv", "testBase.oranges.csv", "testBase.oranges.peel.csv");
        }
    }

    @Test
    @Disabled("Store changes")
    void readStoreFromFiles() throws IOException {
        // given
        when(singleStoreIOProvider.getReaderFor("csv")).thenReturn(new CsvReader(new ReaderProvider()));
        StoreIO storeIO = new StoreIO(singleStoreIOProvider, readerProvider, writerProvider);
        Path filePath = Paths.get(Objects.requireNonNull(this.getClass().getResource("/StoreIO/test.properties")).getPath());
        File file = filePath.toFile();
        Store store = new Store(new ArrayAttributeFactory(), 10);
        store.addByte("byte_column");
        store.addSubstore("oranges");
        Store substore = store.getSubstore("oranges");
        substore.addInt("oranges");
        substore.addSubstore("peel");
        Store peel = substore.getSubstore("peel");
        peel.addByte("peel");
        peel.addByte("color");
        // execute

        storeIO.readStoreFromFiles(file.getAbsolutePath(), store);
        // assert
        assertThat(store.getCounter().getCount()).isEqualTo(5);
    }
}