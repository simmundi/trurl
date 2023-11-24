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

package pl.edu.icm.trurl.io.orc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.edu.icm.trurl.io.csv.CsvReader;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayAttributeFactory;
import pl.edu.icm.trurl.store.attribute.BooleanAttribute;
import pl.edu.icm.trurl.store.attribute.IntAttribute;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class OrcWriterReaderTest {

    @TempDir
    File tempDir;

    @Test
    public void writeThenRead() throws IOException {
        // given
        File orcFile = new File(tempDir, "data_short.orc");

        Store storeToWrite = new Store(new ArrayAttributeFactory(), 2418);
        configureStore(storeToWrite);

        Store storeToRead = new Store(new ArrayAttributeFactory(), 2418);
        configureStore(storeToRead);

        loadFromCsvResource(storeToWrite, "/store.csv");

        OrcWriter orcWriter = new OrcWriter(new OrcImplementationsService());
        OrcReader orcStoreReader = new OrcReader(new OrcImplementationsService());

        int writeCount = storeToWrite.getCounter().getCount();

        // execute
        orcWriter.write(orcFile, storeToWrite);
        orcStoreReader.read(orcFile, storeToRead);
        int readCount = storeToRead.getCounter().getCount();

        // assert
        assertThat(readCount).isEqualTo(writeCount);
        assertThat(dataFromStore(storeToWrite, writeCount)).isEqualTo(dataFromStore(storeToRead, writeCount));
    }

    private void loadFromCsvResource(Store storeToWrite, String name) throws IOException {
        new CsvReader().read(new File(Objects.requireNonNull(this.getClass().getResource(name)).getFile()), storeToWrite);
    }

    private List<List<String>> dataFromStore(Store store, int rows) {
        return IntStream.range(0, rows).mapToObj(row -> store.attributes().map(a -> a.isEmpty(row) ? null : a.getString(row)).collect(Collectors.toList())).collect(Collectors.toList());
    }

    @Test
    @DisplayName("OrcReader should append to non empty store")
    public void loadToNonEmpty() throws IOException {
        // given
        Store store = new Store(new ArrayAttributeFactory(), 10);
        configureStore(store);
        ((BooleanAttribute) store.get("bools")).setBoolean(store.getCounter().next(), false);
        ((IntAttribute) store.get("ints")).setInt(store.getCounter().next(), 3712);
        int preLoadCount = store.getCounter().getCount();

        OrcReader orcReader = new OrcReader(new OrcImplementationsService());

        File orcFileToRead = new File(Objects.requireNonNull(this.getClass().getResource("/data_short.orc")).getFile());
        final int newRowsCount = 3;

        // execute
        orcReader.read(orcFileToRead, store);

        // assert
        assertThat(store.getCounter().getCount()).isEqualTo(preLoadCount + newRowsCount);
        assertThat(store.get("bools").getString(0)).isEqualTo("false");
        assertThat(store.get("ints").getString(1)).isEqualTo("3712");

        assertLoadedData(store, preLoadCount);
    }

    private static void assertLoadedData(Store store, int preLoadCount) {
        assertThat(store.get("bools").getString(preLoadCount + 0)).isEqualTo("true");
        assertThat(store.get("bools").getString(preLoadCount + 1)).isEqualTo("false");
        assertThat(store.get("bools").getString(preLoadCount + 2)).isEqualTo("false");
        assertThat(store.get("bytes").getString(preLoadCount + 0)).isEqualTo("1");
        assertThat(store.get("bytes").getString(preLoadCount + 1)).isEqualTo("2");
        assertThat(store.get("bytes").getString(preLoadCount + 2)).isEqualTo("3");
        assertThat(store.get("doubles").getString(preLoadCount + 0)).isEqualTo("NaN");
        assertThat(store.get("doubles").getString(preLoadCount + 1)).isEqualTo("11.0");
        assertThat(store.get("doubles").getString(preLoadCount + 2)).isEqualTo("NaN");
        assertThat(store.get("enums").getString(preLoadCount + 0)).isEqualTo("SQUARE");
        assertThat(store.get("enums").getString(preLoadCount + 1)).isEqualTo("SQUARE");
        assertThat(store.get("enums").getString(preLoadCount + 2)).isEqualTo("");
        assertThat(store.get("floats").getString(preLoadCount + 0)).isEqualTo("1.0");
        assertThat(store.get("floats").getString(preLoadCount + 1)).isEqualTo("2.0");
        assertThat(store.get("floats").getString(preLoadCount + 2)).isEqualTo("3.0");
        assertThat(store.get("ints").getString(preLoadCount + 0)).isEqualTo("100");
        assertThat(store.get("ints").getString(preLoadCount + 1)).isEqualTo("101");
        assertThat(store.get("ints").getString(preLoadCount + 2)).isEqualTo(Integer.toString(Integer.MIN_VALUE));
        assertThat(store.get("shorts").getString(preLoadCount + 0)).isEqualTo("1000");
        assertThat(store.get("shorts").getString(preLoadCount + 1)).isEqualTo("1001");
        assertThat(store.get("shorts").getString(preLoadCount + 2)).isEqualTo("1002");
        assertThat(store.get("strings").getString(preLoadCount + 0)).isEqualTo("Call");
        assertThat(store.get("strings").getString(preLoadCount + 1)).isEqualTo("me");
        assertThat(store.get("strings").getString(preLoadCount + 2)).isEqualTo("Ishmael");
    }


    private void configureStore(Store store) {
        store.addBoolean("bools");
        store.addByte("bytes");
        store.addDouble("doubles");
        store.addEnum("enums", Shape.class);
        store.addFloat("floats");
        store.addInt("ints");
        store.addShort("shorts");
        store.addString("strings");
    }
}
