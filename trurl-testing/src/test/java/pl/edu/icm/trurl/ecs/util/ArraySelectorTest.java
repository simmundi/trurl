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

package pl.edu.icm.trurl.ecs.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.selector.Chunk;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ArraySelectorTest {

    @Test
    void estimatedChunkSize() {
        //given
        ArraySelector defaultArraySelector = new ArraySelector();
        ArraySelector customArraySelector = new ArraySelector(10, 17);
        //execute
        int defaultEstimatedChunkSize = defaultArraySelector.estimatedChunkSize();
        int customEstimatedChunkSize = customArraySelector.estimatedChunkSize();
        //assert
        assertThat(defaultEstimatedChunkSize).isEqualTo(1024);
        assertThat(customEstimatedChunkSize).isEqualTo(17);
    }

    @Test
    void shuffle() {
        //given
        Random random = new Random(123);
        int[] array = IntStream.range(0, 10000).toArray();
        ArraySelector arraySelector = new ArraySelector(array);
        //execute
        arraySelector.shuffle(random);
        //assert
        assertThat(arraySelector.chunks().flatMapToInt(Chunk::ids).toArray()).containsExactlyInAnyOrder(array);
    }

    @Test
    void getIdByIndex() {
        //given
        ArraySelector arraySelector = new ArraySelector();
        arraySelector.add(10);
        arraySelector.add(12);
        //execute
        int v0 = arraySelector.getIdByIndex(0);
        int v1 = arraySelector.getIdByIndex(1);
        Executable shouldThrow = () -> arraySelector.getIdByIndex(2);
        //assert
        assertThat(v0).isEqualTo(10);
        assertThat(v1).isEqualTo(12);
        assertThrows(IndexOutOfBoundsException.class, shouldThrow);
    }

    @Test
    void add() {
        //given
        ArraySelector arraySelector = new ArraySelector();
        //execute
        arraySelector.add(24);
        arraySelector.add(261);
        arraySelector.add(61);
        arraySelector.add(2137);
        //assert
        assertThat(IntStream.range(0, 4).map(arraySelector::getIdByIndex).toArray()).containsExactly(24, 261, 61, 2137);
    }

    @Test
    void addAll() {
        //given
        ArraySelector arraySelector = new ArraySelector();
        arraySelector.add(10);
        //execute
        arraySelector.addAll(IntStream.range(11, 30).toArray());
        //assert
        assertThat(IntStream.range(0, 20).map(arraySelector::getIdByIndex).toArray()).containsExactly(IntStream.range(10, 30).toArray());
    }

    @Test
    void getCount() {
        //given
        ArraySelector arraySelector = new ArraySelector(16_000);
        IntStream.range(0, 15_123).parallel().forEach(arraySelector::add);
        //execute
        int count = arraySelector.getCount();
        //assert
        assertThat(count).isEqualTo(15_123);
    }

    @Test
    void chunks() {
        //given
        ArraySelector arraySelector = new ArraySelector(2048, 1024);
        int[] ids = IntStream.range(0, 2047).toArray();
        arraySelector.addAll(ids);
        Supplier<Stream<Chunk>> chunkStreamSupplier = arraySelector::chunks;
        //execute
        List<Chunk> chunkList = chunkStreamSupplier.get().collect(Collectors.toList());
        //assert
        assertThat(chunkList.get(0).ids().count()).isEqualTo(1024);
        assertThat(chunkList.get(1).ids().count()).isEqualTo(1023);
        assertThat(chunkStreamSupplier.get().flatMapToInt(Chunk::ids).toArray()).containsExactlyInAnyOrder(ids);
    }
}