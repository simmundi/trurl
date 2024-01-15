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

package pl.edu.icm.trurl.store.array;

import org.assertj.core.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

class IntListArrayAttributeTest {

    private final IntListArrayAttribute attribute = new IntListArrayAttribute("abcde", 100);

    @Test
    @DisplayName("Should ensure capacity")
    void ensureCapacity() {
        // given
        int outsideDefaultCapacity = 1456;
        Assertions.assertThatThrownBy(() -> attribute.setEmpty(outsideDefaultCapacity))
                .isInstanceOf(IndexOutOfBoundsException.class);

        // execute
        attribute.ensureCapacity(outsideDefaultCapacity + 1);

        // assert (should not throw)
        attribute.setEmpty(outsideDefaultCapacity);

    }

    @Test
    @DisplayName("Should be empty outside of capacity")
    void isEmpty() {
        // given
        int outsideDefaultCapacity = 1456;

        // execute
        boolean empty = attribute.isEmpty(outsideDefaultCapacity);

        // assert
        assertThat(empty).isTrue();
    }

    @Test
    @DisplayName("Should become empty")
    void setEmpty() {
        // given
        int row = 34;
        attribute.setString(row, "1,2,3");
        assertThat(attribute.isEmpty(row)).isFalse();

        // execute
        attribute.setEmpty(row);

        // assert
        assertThat(attribute.isEmpty(row)).isTrue();
    }

    @Test
    @DisplayName("Should return correct name")
    void name() {
        // execute & assert
        assertThat(attribute.name()).isEqualTo("abcde");
    }

    @Test
    @DisplayName("Should build a correct 36-encoded string")
    void getString() {
        // given
        attribute.saveInts(10, 4, x -> x + 10);

        // execute
        String result = attribute.getString(10);

        // assert
        assertThat(result).isEqualTo("a,b,c,d");
    }

    @Test
    @DisplayName("Should set array of ints as a base36-encoded string")
    void setString() {
        // given
        attribute.setString(10, "a,b,c,d");
        int[] results = new int[4];

        // execute
        attribute.loadInts(10, (idx, value) -> results[idx] = value);

        // assert
        assertThat(results).containsExactly(10, 11, 12, 13);
    }

    @Test
    @DisplayName("Should return correct size")
    void getSize() {
        // given
        attribute.setString(43, "a,b,c,d,de,43,586z");

        // execute
        int size = attribute.getSize(43);

        // assert
        assertThat(size).isEqualTo(7);
    }

    @Test
    @DisplayName("Should save and load ints")
    void loadInts() {
        // given
        int[] given = {6,4,3,5,7,52,54,56,678,45,36,512,423,423,43,563,42,312,12,32,453,3412,2};
        int[] result = new int[given.length];
        attribute.saveInts(3, given.length, idx -> given[idx]);

        // execute
        attribute.loadInts(3, (idx, value) -> result[idx] = value);

        // assert
        assertThat(result).isEqualTo(given);
    }

    @Test
    @DisplayName("Should detect that an array is identical to the intSource")
    void isEqual() {
        // given
        int[] given = {6,4,3,5,7,52,54,56,678,45,36,512,423,423,43,563,42,312,12,32,453,3412,2};
        int[] toCompare = Arrays.copyOf(given, given.length);
        attribute.saveInts(3, given.length, idx -> given[idx]);

        // execute
        boolean equal = attribute.isEqual(3, toCompare.length, (idx) -> toCompare[idx]);

        // assert
        assertThat(equal).isTrue();
    }

    @Test
    @DisplayName("Should detect that an array of different size must be different")
    void isEqual__different_size() {
        // given
        int[] given = {6,4,3,5,7,52,54,56,678,45,36,512,423,423,43,563,42,312,12,32,453,3412,2};
        int[] toCompare = Arrays.copyOf(given, given.length);
        attribute.saveInts(3, given.length, idx -> given[idx]);

        // execute
        boolean equal = attribute.isEqual(3, toCompare.length - 1, (idx) -> toCompare[idx]);

        // assert
        assertThat(equal).isFalse();
    }

    @Test
    @DisplayName("Should detect that an array with same size but one element different is not equal")
    void isEqual__different_element() {
        // given
        int[] given = {45,36,512,423,423,12,32,453,3412,2};
        int[] toCompare = Arrays.copyOf(given, given.length);
        toCompare[toCompare.length - 1] = 3;
        attribute.saveInts(3, given.length, idx -> given[idx]);

        // execute
        boolean equal = attribute.isEqual(3, toCompare.length - 1, (idx) -> toCompare[idx]);

        // assert
        assertThat(equal).isFalse();
    }
}
