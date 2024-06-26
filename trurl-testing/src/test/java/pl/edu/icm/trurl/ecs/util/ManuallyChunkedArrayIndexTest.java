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

package pl.edu.icm.trurl.ecs.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.ecs.index.Chunk;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ManuallyChunkedArrayIndexTest {

    ManuallyChunkedArrayIndex index = new ManuallyChunkedArrayIndex();

    @Test
    @DisplayName("Should create a index with ten chunks of different size")
    void create() {
        // given
        int total = 0;

        // execute
        for (int chunk = 0; chunk < 10; chunk++) {
            int count = chunk * 10;
            for (int i = 0; i < count; i++) {
                assertThat(index.getRunningSize()).isEqualTo(i);
                index.add(i);
                total++;
            }
            index.endChunk();
        }

        // assert
        assertThat(index.getCount()).isEqualTo(total);
        assertThat(index.chunks()).hasSize(10);
    }

    @Test
    @DisplayName("Should close the last chunk after call to chunk() even if endChunk not called before")
    void createWithoutClosingLastChunk() {
        // given

        index.add(1);
        index.add(2);
        index.add(3);
        index.endChunk();
        index.add(4);
        index.add(5);
        index.add(6);

        // execute
        Stream<Chunk> chunks = index.chunks();

        // assert
        assertThat(chunks).hasSize(2);
    }
}