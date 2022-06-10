package pl.edu.icm.trurl.io.orc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.edu.icm.trurl.csv.CsvReader;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

class OrcStoreServiceImpl {

    @TempDir
    File tempDir;

    @Test
    public void writeThenRead() throws IOException {
        // given
        String filename = new File(tempDir, "dump.orc").getAbsolutePath();
        ArrayStore storeToWrite = new ArrayStore();
        configureStore(storeToWrite);
        ArrayStore storeToRead = new ArrayStore();
        configureStore(storeToRead);
        loadFromCsvResource(storeToWrite, "/store.csv");
        OrcStoreService orcStoreService = new OrcStoreService(new OrcImplementationsService());
        int writeCount = storeToWrite.getCount();

        // execute
        orcStoreService.write(storeToWrite, filename);
        orcStoreService.read(storeToRead, filename);
        int readCount = storeToRead.getCount();

        // assert
        assertThat(readCount).isEqualTo(writeCount);
        assertThat(dataFromStore(storeToWrite, writeCount))
                .isEqualTo(dataFromStore(storeToRead, writeCount));
    }

    private void loadFromCsvResource(ArrayStore storeToWrite, String name) {
        new CsvReader().load(
                OrcStoreServiceImpl.class.getResourceAsStream(name),
                storeToWrite
        );
    }

    private List<List<String>> dataFromStore(Store store, int rows) {
        return IntStream.range(0, rows).mapToObj(row ->
                store.attributes()
                        .map(a -> a.isEmpty(row) ? null : a.getString(row)).collect(Collectors.toList())
        ).collect(Collectors.toList());
    }

    private void configureStore(ArrayStore store) {
        store.addBoolean("bools");
        store.addByte("bytes");
        store.addDouble("doubles");
        store.addEntity("entities");
        store.addEntityList("entityLists");
        store.addEnum("enums", Shape.class);
        store.addFloat("floats");
        store.addInt("ints");
        store.addShort("shorts");
        store.addString("strings");
    }
}
