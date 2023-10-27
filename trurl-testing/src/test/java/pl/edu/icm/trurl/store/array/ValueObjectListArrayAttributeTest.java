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
/*
package pl.edu.icm.trurl.store.array;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.stream.IntStream.concat;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ValueObjectListArrayAttributeTest {

    @Test
    void ensureCapacity() {
        //given
        ValueObjectListArrayAttribute valueObjectListArrayAttribute = new ValueObjectListArrayAttribute("test", 0);
        //execute
        valueObjectListArrayAttribute.ensureCapacity(10);
        //assert
        assertThrows(IndexOutOfBoundsException.class,
                () -> valueObjectListArrayAttribute.loadIds(10, (index, value) -> {
                }));

    }

    @Test
    void isEmpty() {
        //given
        ValueObjectListArrayAttribute valueObjectListArrayAttribute = new ValueObjectListArrayAttribute("test", 10);
        //execute
        boolean isEmptyOutOfRange = valueObjectListArrayAttribute.isEmpty(100);
        boolean isEmptyInRange = valueObjectListArrayAttribute.isEmpty(1);
        //assert
        assertThat(isEmptyOutOfRange).isTrue();
        assertThat(isEmptyInRange).isTrue();
    }

    @Test
    void setEmpty() {
        //given
        ValueObjectListArrayAttribute valueObjectListArrayAttribute = new ValueObjectListArrayAttribute("test", 10);
        valueObjectListArrayAttribute.saveIds(0, 10, 1);
        //execute
        boolean notEmpty = !valueObjectListArrayAttribute.isEmpty(0);
        valueObjectListArrayAttribute.setEmpty(0);
        boolean empty = valueObjectListArrayAttribute.isEmpty(0);
        //assert
        assertThat(notEmpty).isTrue();
        assertThat(empty).isTrue();
    }

    @Test
    void name() {
        //given
        ValueObjectListArrayAttribute valueObjectListArrayAttribute = new ValueObjectListArrayAttribute("test", 10);
        //execute
        String name = valueObjectListArrayAttribute.name();
        //assert
        assertThat(name).isEqualTo("test");
    }

    @Test
    void getString() {
        //given
        ValueObjectListArrayAttribute valueObjectListArrayAttribute = new ValueObjectListArrayAttribute("test", 10);
        valueObjectListArrayAttribute.saveIds(0, 14, 1);
        valueObjectListArrayAttribute.saveIds(0, 10, 15);
        //execute
        String stringRow = valueObjectListArrayAttribute.getString(0);
        //assert
        assertThat(stringRow).isEqualTo("1,2,3,4,5,6,7,8,9,a,-b,c,d,e");
    }

    @Test
    void setString() {
        //given
        ValueObjectListArrayAttribute valueObjectListArrayAttribute = new ValueObjectListArrayAttribute("test", 10);
        valueObjectListArrayAttribute.setString(3, "1,2,3,4,5,6,7,8,9,a,-b,c,d,e");
        int[] values = new int[valueObjectListArrayAttribute.getSize(3)];
        //execute
        valueObjectListArrayAttribute.loadIds(3, (index, value) -> values[index] = value);
        String stringRow = valueObjectListArrayAttribute.getString(3);
        //assert
        assertThat(values).containsExactlyInAnyOrder(range(1, 11).toArray());
        assertThat(stringRow).isEqualTo("1,2,3,4,5,6,7,8,9,a,-b,c,d,e");
    }

    @Test
    void getSize() {
        //given
        ValueObjectListArrayAttribute valueObjectListArrayAttribute = new ValueObjectListArrayAttribute("test", 10);
        valueObjectListArrayAttribute.saveIds(1, 14, 1);
        valueObjectListArrayAttribute.setString(3, "1,2,3,4,5,6,7,8,9,a,-b,c,d,e");
        //execute
        int size1 = valueObjectListArrayAttribute.getSize(1);
        int size3 = valueObjectListArrayAttribute.getSize(3);
        //assert
        assertThat(size1).isEqualTo(14);
        assertThat(size3).isEqualTo(10);
    }

    @Test
    void saveIds() {
        //given
        ValueObjectListArrayAttribute valueObjectListArrayAttribute = new ValueObjectListArrayAttribute("test", 10);
        //execute
        valueObjectListArrayAttribute.saveIds(1, 10, 1);
        int[] values1 = new int[valueObjectListArrayAttribute.getSize(1)];
        valueObjectListArrayAttribute.loadIds(1, (index, value) -> values1[index] = value);
        valueObjectListArrayAttribute.saveIds(1, 2, 1);
        int[] values2 = new int[valueObjectListArrayAttribute.getSize(1)];
        valueObjectListArrayAttribute.loadIds(1, (index, value) -> values2[index] = value);
        valueObjectListArrayAttribute.saveIds(1, 13, 15);
        int[] values3 = new int[valueObjectListArrayAttribute.getSize(1)];
        valueObjectListArrayAttribute.loadIds(1, (index, value) -> values3[index] = value);
        //assert
        assertThrows(IllegalArgumentException.class, () -> valueObjectListArrayAttribute.saveIds(4, -1, 4));
        assertThrows(IllegalArgumentException.class, () -> valueObjectListArrayAttribute.saveIds(4, 3, 0));
        assertThat(values1).containsExactlyInAnyOrder(range(1, 11).toArray());
        assertThat(values2).containsExactlyInAnyOrder(range(1, 3).toArray());
        assertThat(values3).containsExactlyInAnyOrder(concat(range(1, 11), range(15, 18)).toArray());
    }

    @Test
    void saveIdsFromIntSource() {
        //given
        int capacity = 10;
        int size = 10;
        ValueObjectListArrayAttribute valueObjectListArrayAttribute = new ValueObjectListArrayAttribute("test", capacity);
        //execute
        valueObjectListArrayAttribute.saveIds(1, size, (i) -> 13 * i + 2);
        int[] values = new int[size];
        valueObjectListArrayAttribute.loadIds(1, (index, value) -> values[index] = value);
        //assert
        assertThat(values).containsExactlyInAnyOrder(2, 15, 28, 41, 54, 67, 80, 93, 106, 119);
    }

    @Test
    void loadIds() {
        //given
        ValueObjectListArrayAttribute valueObjectListArrayAttribute = new ValueObjectListArrayAttribute("test", 10);
        valueObjectListArrayAttribute.saveIds(1, 10, 1);
        valueObjectListArrayAttribute.saveIds(1, 2, 1);
        valueObjectListArrayAttribute.saveIds(1, 13, 15);
        //execute
        int[] values = new int[valueObjectListArrayAttribute.getSize(1)];
        valueObjectListArrayAttribute.loadIds(1, (index, value) -> values[index] = value);
        //assert
        assertThat(values).containsExactlyInAnyOrder(concat(range(1, 11), range(15, 18)).toArray());
    }
}

 */