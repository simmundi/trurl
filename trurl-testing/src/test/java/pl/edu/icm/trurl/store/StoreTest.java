/*
 * Copyright (c) 2022-2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

package pl.edu.icm.trurl.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.store.basic.BasicAttributeFactory;
import pl.edu.icm.trurl.store.attribute.*;

import static org.assertj.core.api.Assertions.assertThat;


class StoreTest {

    Store createStore() {
        return new Store(new BasicAttributeFactory(), 1000);
    }

    @Test
    @DisplayName("Should add substores")
    public void createSubstore() {
        // given
        Store store = createStore();

        // execute
        store.addSubstore("oranges");
        Store substore = store.getSubstore("oranges");
        substore.addSubstore("peel");
        Store subSubstore = substore.getSubstore("peel");

        // assert
        assertThat(substore.getName()).isEqualTo("oranges");
        assertThat(subSubstore.getName()).isEqualTo("oranges.peel");
    }

    @Test
    public void allDescendants() {
        // given
        Store store = createStore();

        // execute
        store.addSubstore("oranges");
        Store substore = store.getSubstore("oranges");
        substore.addSubstore("peel");
        Store subSubstore = substore.getSubstore("peel");

        // assert
        assertThat(store.allDescendants()).containsExactlyInAnyOrder(substore, subSubstore);

    }

    @Test
    @DisplayName("Should add a boolean column")
    public void addBoolean() {
        // given
        Store store = createStore();

        // execute
        store.addBoolean("boolean");
        Attribute attribute = store.get("boolean");

        // assert
        assertThat(attribute).isInstanceOf(BooleanAttribute.class);
    }

    @Test
    @DisplayName("Should add a byte column")
    public void addByte() {
        // given
        Store store = createStore();

        // execute
        store.addByte("byte");
        Attribute attribute = store.get("byte");

        // assert
        assertThat(attribute).isInstanceOf(ByteAttribute.class);
    }

    @Test
    @DisplayName("Should add a double column")
    public void addDouble() {
        // given
        Store store = createStore();

        // execute
        store.addDouble("double");
        Attribute attribute = store.get("double");

        // assert
        assertThat(attribute).isInstanceOf(DoubleAttribute.class);
    }

    @Test
    @DisplayName("Should add an enum column")
    public void addEnum() {
        // given
        Store store = createStore();

        // execute
        store.addEnum("enum", Letters.class);
        Attribute attribute = store.get("enum");

        // assert
        assertThat(attribute).isInstanceOf(EnumAttribute.class);
    }

    @Test
    @DisplayName("Should add a string column")
    public void addString() {
        // given
        Store store = createStore();

        // execute
        store.addString("string");
        Attribute attribute = store.get("string");

        // assert
        assertThat(attribute).isInstanceOf(StringAttribute.class);
    }

    @Test
    @DisplayName("Should add a short column")
    public void addShort() {
        // given
        Store store = createStore();

        // execute
        store.addShort("short");
        Attribute attribute = store.get("short");

        // assert
        assertThat(attribute).isInstanceOf(ShortAttribute.class);
    }

    @Test
    @DisplayName("Should add a boolean column")
    public void addInt() {
        // given
        Store store = createStore();

        // execute
        store.addInt("int");
        Attribute attribute = store.get("int");

        // assert
        assertThat(attribute).isInstanceOf(IntAttribute.class);
    }

    @Test
    @DisplayName("Should add a boolean column")
    public void addFloat() {
        // given
        Store store = createStore();

        // execute
        store.addFloat("float");
        Attribute attribute = store.get("float");

        // assert
        assertThat(attribute).isInstanceOf(FloatAttribute.class);
    }

    private enum Letters {A, B, C}
}
