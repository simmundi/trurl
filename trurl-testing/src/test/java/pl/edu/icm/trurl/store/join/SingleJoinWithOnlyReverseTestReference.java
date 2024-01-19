/*
 * Copyright (c) 2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

package pl.edu.icm.trurl.store.join;

import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.basic.BasicAttributeFactory;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.attribute.IntAttribute;
import pl.edu.icm.trurl.store.attribute.StringAttribute;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SingleJoinWithOnlyReverseTestReference {
    public static final int CAPACITY = 400_000;
    Store store = new Store(new BasicAttributeFactory(), CAPACITY);

    @Test
    void init() {
        // given
        Store target = store.addJoin("image").singleTypedWithReverseOnly();
        store.addString("name");
        target.addString("blob");

        // when
        Attribute blobAttribute = target.get("blob");
        Attribute reverseAttribute = target.get("reverse");
        Join join = store.getJoin("image");

        // assert
        assertThat(blobAttribute).isInstanceOf(StringAttribute.class);
        assertThat(reverseAttribute).isInstanceOf(IntAttribute.class);
        assertThat(join).isInstanceOf(SingleJoinWithReverseOnly.class);
        assertThat(store.getAllAttributes().stream().map(Attribute::name)).containsExactlyInAnyOrder("name");
        assertThat(store.getDataAttributes().stream().map(Attribute::name)).containsExactlyInAnyOrder("name");
        assertThat(target.getAllAttributes().stream().map(Attribute::name)).containsExactlyInAnyOrder("blob", "reverse");
        assertThat(target.getDataAttributes().stream().map(Attribute::name)).containsExactlyInAnyOrder("blob");
    }

    @Test
    void getRow() {
        // given
        Store target = store.addJoin("image").singleTypedWithReverseOnly();
        store.addString("name");
        target.addString("blob");

        Attribute nameAttribute = store.get("name");
        Attribute blobAttribute = target.get("blob");
        Join join = store.getJoin("image");

        // when
        IntStream.range(0, CAPACITY).parallel().forEach(row -> {
            nameAttribute.setString(row, "xxx" + row);
            if (row % 6 == 0) {
                join.setSize(row, 1);
                int targetRow = join.getRow(row, 0);
                blobAttribute.setString(targetRow, "xxx" + row + ".png");
            }
        });

        // assert
        IntStream.range(0, CAPACITY).parallel().forEach(row -> {
            String name = nameAttribute.getString(row);
            if (row % 6 == 0) {
                assertThat(join.getExactSize(row)).isEqualTo(1);
                int targetRow = join.getRow(row, 0);
                String blob = blobAttribute.getString(targetRow);
                assertThat(blob).isEqualTo(name + ".png");
            } else {
                assertThat(join.getExactSize(row)).isEqualTo(0);
            }
        });
    }

    @Test
    void getRow__reverse() {
        // given
        Store target = store.addJoin("image").singleTypedWithReverseOnly();
        target.addInt("idByHand");

        IntAttribute idByHand = target.get("idByHand");
        Join join = store.getJoin("image");

        // when
        IntStream.range(0, CAPACITY).forEach(row -> {
            if (row % 4 == 0) {
                join.setSize(row, 1);
                int targetRow = join.getRow(row, 0);
                idByHand.setInt(targetRow, row);
            } else {
                join.setSize(row, 0);
            }
        });

        // assert
        IntAttribute reverseAttribute = target.get("reverse");
        int count = target.getCounter().getCount();
        assertThat(count).isEqualTo(CAPACITY / 4);
        IntStream.range(0, count).parallel().forEach(row -> {
            int reverse = reverseAttribute.getInt(row);
            assertThat(reverse).isEqualTo(idByHand.getInt(row));
        });
    }
}