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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.edu.icm.trurl.io.csv.CsvReader;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayAttributeFactory;

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
    @Disabled
    public void writeThenRead() throws IOException {
        // given
        File file = new File(tempDir, "dump.orc");
        Store storeToWrite = new Store(new ArrayAttributeFactory(), 1024);
        configureStore(storeToWrite);
        Store storeToRead = new Store(new ArrayAttributeFactory(), 1024);
        configureStore(storeToRead);
        loadFromCsvResource(storeToWrite, "/store.csv");
        OrcWriter orcWriter = new OrcWriter(new OrcImplementationsService());
        OrcReader orcStoreReader = new OrcReader(new OrcImplementationsService());
        int writeCount = storeToWrite.getCounter().getCount();

        // execute
        orcWriter.write(file,storeToWrite);
        orcStoreReader.read(file,storeToRead);
        int readCount = storeToRead.getCounter().getCount();

        // assert
        assertThat(readCount).isEqualTo(writeCount);
        assertThat(dataFromStore(storeToWrite, writeCount))
                .isEqualTo(dataFromStore(storeToRead, writeCount));
    }

    private void loadFromCsvResource(Store storeToWrite, String name) throws IOException {
        new CsvReader().read(
                new File(Objects.requireNonNull(this.getClass().getResource(name)).getFile()),
                storeToWrite
        );
    }

    private List<List<String>> dataFromStore(Store store, int rows) {
        return IntStream.range(0, rows).mapToObj(row ->
                store.attributes()
                        .map(a -> a.isEmpty(row) ? null : a.getString(row)).collect(Collectors.toList())
        ).collect(Collectors.toList());
    }

    private void configureStore(Store store) {
        store.addBoolean("bools");
        store.addByte("bytes");
        store.addDouble("doubles");
//        todo uncomment
//        store.addEntity("entities");
//        store.addEntityList("entityLists");
        store.addEnum("enums", Shape.class);
        store.addFloat("floats");
        store.addInt("ints");
        store.addShort("shorts");
        store.addString("strings");
    }
}
