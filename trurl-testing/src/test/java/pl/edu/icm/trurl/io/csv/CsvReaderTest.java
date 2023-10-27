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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayAttributeFactory;
import pl.edu.icm.trurl.store.attribute.Attribute;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class CsvReaderTest {

    private enum Letter {
        A, B, C, D
    }

    @Test
    @DisplayName("Should load all data from a csv file, no matter how the columns are ordered")
    public void load() throws IOException {
        // given
        CsvReader csvReader = new CsvReader();
        Store store = new Store(new ArrayAttributeFactory(), 1000);
        store.addInt("age");
        store.addEnum("letter", Letter.class);
        // todo uncomment
        // store.addEntityList("entities");
        store.addByte("bytes");
        store.addFloat("number");
        store.addString("name");
        store.addBoolean("bool");
        // todo uncomment
        // store.addEntity("entity");
        store.addShort("short");

        // execute
        csvReader.read(new File(Objects.requireNonNull(CsvReaderTest.class.getResource("/data1.csv")).getFile()), store);

        // assert
        Attribute namesAttribute = store.get("name");
        Attribute lettersAttribute = store.get("letter");
        assertThat(namesAttribute.getString(0)).isEqualTo("Jan");
        assertThat(namesAttribute.getString(1)).isEqualTo("Filip");
        assertThat(namesAttribute.getString(2)).isEqualTo("Adam");
        assertThat(lettersAttribute.getString(0)).isEqualTo("A");
        assertThat(lettersAttribute.getString(1)).isEqualTo("B");
        assertThat(lettersAttribute.getString(2)).isEqualTo("C");
    }

}
