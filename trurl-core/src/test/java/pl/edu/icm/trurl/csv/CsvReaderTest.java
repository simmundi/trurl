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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.store.array.ArrayAttributeFactory;
import pl.edu.icm.trurl.store.attribute.Attribute;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class CsvReaderTest {

    private enum Letter {
        A, B, C, D;
    }

    @Test
    @DisplayName("Should load all data from a csv file, no matter how the columns are ordered")
    public void load() {
        // given
        CsvReader csvReader = new CsvReader();
        ArrayAttributeFactory store = new ArrayAttributeFactory(1000);
        store.addInt("age");
        store.addStaticCategory("letter", Letter.class);
        store.addEntityList("entities");
        store.addFloat("number");
        store.addString("name");
        store.addBoolean("bool");
        store.addEntity("entity");

        // execute
        csvReader.load(CsvReaderTest.class.getResourceAsStream("/data1.csv"), store);

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

    @Test
    @DisplayName("Should read all data from a csv file with programmatically given header")
    public void load__no_header() {
        // given TODO: fix
//        CsvLoader csvLoader = new CsvLoader();
//        TablesawComponentStore componentStore = new Ta
//        componentStore.addInt("one");
//        componentStore.addEnum("letter", Letter.class);

        // execute
//        csvLoader.load(CsvLoaderTest.class.getResourceAsStream("/data2.csv"), componentStore, "", "letter","","","one"); // TODO: fix

        // assert TODO: fix
//        assertThat(componentStore.asTable("x").rowCount()).isEqualTo(3);
//        assertThat(componentStore.get("one").getString(0)).isEqualTo("1");
//        assertThat(componentStore.get("letter").getString(0)).isEqualTo("A");
    }

    @Test
    @DisplayName("Should read all data from a csv file using a metadata from a component")
    public void load__based_on_component() {
        // given
        CsvReader csvReader = new CsvReader();

        // execute
        InputStream inputStream = CsvReaderTest.class.getResourceAsStream("/data2.csv");
//        var mapper = csvLoader.load( TODO: fix
//                inputStream, Stats.class,
//                "str", "","dex","wis");
//
//        // assert
//        assertThat(Mappers.stream(mapper)).isEqualTo(List.of(
//                new Stats(18, 9, 4),
//                new Stats(17, 10, 3),
//                new Stats(16, 11, 8)
//        ));
    }

}
