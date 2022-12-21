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

package pl.edu.icm.trurl.store.tablesaw;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.store.attribute.BooleanAttribute;
import pl.edu.icm.trurl.store.attribute.ByteAttribute;
import pl.edu.icm.trurl.store.attribute.DoubleAttribute;
import pl.edu.icm.trurl.store.attribute.EntityAttribute;
import pl.edu.icm.trurl.store.attribute.EntityListAttribute;
import pl.edu.icm.trurl.store.attribute.EnumAttribute;
import pl.edu.icm.trurl.store.attribute.FloatAttribute;
import pl.edu.icm.trurl.store.attribute.IntAttribute;
import pl.edu.icm.trurl.store.attribute.ShortAttribute;
import pl.edu.icm.trurl.store.attribute.StringAttribute;

import java.util.Optional;

class TablesawStoreTest {

    @Test
    @DisplayName("Should add a boolean column")
    public void addBoolean() {
        // given
        TablesawStore tablesawComponentStore = new TablesawStore();

        // execute
        tablesawComponentStore.addBoolean("boolean");
        var attribute = tablesawComponentStore.get("boolean");

        // assert
        Assertions.assertThat(attribute).isInstanceOf(BooleanAttribute.class);
    }

    @Test
    @DisplayName("Should add a byte column")
    public void addByte() {
        // given
        TablesawStore tablesawComponentStore = new TablesawStore();

        // execute
        tablesawComponentStore.addByte("byte");
        var attribute = tablesawComponentStore.get("byte");

        // assert
        Assertions.assertThat(attribute).isInstanceOf(ByteAttribute.class);
    }

    @Test
    @DisplayName("Should add a double column")
    public void addDouble() {
        // given
        TablesawStore tablesawComponentStore = new TablesawStore();

        // execute
        tablesawComponentStore.addDouble("double");
        var attribute = tablesawComponentStore.get("double");

        // assert
        Assertions.assertThat(attribute).isInstanceOf(DoubleAttribute.class);
    }

    @Test
    @DisplayName("Should add an entity column")
    public void addEntity() {
        // given
        TablesawStore tablesawComponentStore = new TablesawStore();

        // execute
        tablesawComponentStore.addEntity("entity");
        var attribute = tablesawComponentStore.get("entity");

        // assert
        Assertions.assertThat(attribute).isInstanceOf(EntityAttribute.class);
    }

    @Test
    @DisplayName("Should add an entity list column")
    public void addEntityList() {
        // given
        TablesawStore tablesawComponentStore = new TablesawStore();

        // execute
        tablesawComponentStore.addEntityList("entities");
        var attribute = tablesawComponentStore.get("entities");

        // assert
        Assertions.assertThat(attribute).isInstanceOf(EntityListAttribute.class);
    }

    @Test
    @DisplayName("Should add an enum column")
    public void addEnum() {

        // given
        TablesawStore tablesawComponentStore = new TablesawStore();

        // execute
        tablesawComponentStore.addEnum("enum", Letters.class);
        var attribute = tablesawComponentStore.get("enum");

        // assert
        Assertions.assertThat(attribute).isInstanceOf(EnumAttribute.class);
    }

    @Test
    @DisplayName("Should add a string column")
    public void addString() {
        // given
        TablesawStore tablesawComponentStore = new TablesawStore();

        // execute
        tablesawComponentStore.addString("string");
        var attribute = tablesawComponentStore.get("string");

        // assert
        Assertions.assertThat(attribute).isInstanceOf(StringAttribute.class);
    }

    @Test
    @DisplayName("Should add a short column")
    public void addShort() {
        // given
        TablesawStore tablesawComponentStore = new TablesawStore();

        // execute
        tablesawComponentStore.addShort("short");
        var attribute = tablesawComponentStore.get("short");

        // assert
        Assertions.assertThat(attribute).isInstanceOf(ShortAttribute.class);
    }

    @Test
    @DisplayName("Should add a boolean column")
    public void addInt() {
        // given
        TablesawStore tablesawComponentStore = new TablesawStore();

        // execute
        tablesawComponentStore.addInt("int");
        var attribute = tablesawComponentStore.get("int");

        // assert
        Assertions.assertThat(attribute).isInstanceOf(IntAttribute.class);
    }

    @Test
    @DisplayName("Should add a boolean column")
    public void addFloat() {
        // given
        TablesawStore tablesawComponentStore = new TablesawStore();

        // execute
        tablesawComponentStore.addFloat("float");
        var attribute = tablesawComponentStore.get("float");

        // assert
        Assertions.assertThat(attribute).isInstanceOf(FloatAttribute.class);
    }

    @Test
    @DisplayName("Should report count according to last event")
    public void getCountAsNotified() {
        // given
        TablesawStore tablesawComponentStore = new TablesawStore();

        // execute
        tablesawComponentStore.fireUnderlyingDataChanged(0, 2567);

        // assert
        Assertions.assertThat(tablesawComponentStore.getCount()).isEqualTo(2567);
    }

    private enum Letters { A, B, C };

}
