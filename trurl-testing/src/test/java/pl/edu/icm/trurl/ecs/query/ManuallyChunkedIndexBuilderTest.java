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

package pl.edu.icm.trurl.ecs.query;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.index.Index;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

class ManuallyChunkedIndexBuilderTest {

    @Test
    @DisplayName("Should build correct selector")
    void add() {
        // given
        ManuallyChunkedSelectorBuilder mcsb = new ManuallyChunkedSelectorBuilder();
        int createCount = 10000 * 27 + 1;
        IntStream.range(0, createCount).parallel().forEach(id -> {
            String label = createLabel(id);
            mcsb.add(Entity.stub(id), label);
        });

        // execute
        Index index = mcsb.build();

        // assert
        AtomicInteger counter = new AtomicInteger();
        index.chunks().forEach(chunk -> {
            String label = chunk.getChunkInfo().getLabel();
            chunk.ids().forEach(id -> {
                Assertions.assertThat(createLabel(id)).isEqualTo(label);
                counter.incrementAndGet();
            });
        });
        Assertions.assertThat(counter.get()).isEqualTo(createCount);
    }

    private static String createLabel(int id) {
        return Integer.toString(id % 27);
    }
}