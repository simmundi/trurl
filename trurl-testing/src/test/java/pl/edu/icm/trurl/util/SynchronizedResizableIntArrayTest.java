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

package pl.edu.icm.trurl.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unimi.dsi.fastutil.ints.IntArrays.shuffle;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SynchronizedResizableIntArrayTest {
    @Test
    @DisplayName("should resize properly")
    void add_resize() {
        //given
        SynchronizedResizableIntArray synchronizedResizableIntArray = new SynchronizedResizableIntArray(10);
        //execute
        IntStream.range(0, 20).parallel().forEach(synchronizedResizableIntArray::add);
        //assert
        assertEquals(synchronizedResizableIntArray.getCurrentSize(), 20);
    }

    @Test
    @DisplayName("should add correct values in parallel")
    void add_correct_values() {
        //given
        SynchronizedResizableIntArray synchronizedResizableIntArray = new SynchronizedResizableIntArray(10);
        int[] someArray = IntStream.range(0, 20_000).toArray();
        //execute
        Arrays.stream(shuffle(someArray, new Random(123)))
                .parallel().forEach(synchronizedResizableIntArray::add);
        //assert
        assertThat(synchronizedResizableIntArray.stream().toArray()).containsExactlyInAnyOrder(someArray);
    }

    @Test
    @DisplayName("should return size of filled part of array")
    void getCurrentSize() {
        //given
        SynchronizedResizableIntArray synchronizedResizableIntArray = new SynchronizedResizableIntArray(10);
        synchronizedResizableIntArray.add(1234);
        //execute
        int size = synchronizedResizableIntArray.getCurrentSize();
        //assert
        assertEquals(size, 1);
    }

    @Test
    void stream() {
        //given
        SynchronizedResizableIntArray synchronizedResizableIntArray = new SynchronizedResizableIntArray(10);
        //execute
        Stream.of(1, 2, 6, 5, 3).forEach(synchronizedResizableIntArray::add);
        //assert
        assertThat(synchronizedResizableIntArray.stream().toArray()).containsExactlyInAnyOrder(1, 2, 6, 5, 3);
    }
}