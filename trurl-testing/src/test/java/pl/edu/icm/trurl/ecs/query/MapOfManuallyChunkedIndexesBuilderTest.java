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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.ecs.index.RandomAccessIndex;

import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pl.edu.icm.trurl.ecs.query.MapOfManuallyChunkedSelectorsBuilder.getDefaultSizes;

class MapOfManuallyChunkedIndexesBuilderTest {
    private enum TestTags {
        TAG1, TAG2
    }

    @Test
    @DisplayName("adding without tag is not implemented")
    void addThrows() {
        //given
        MapOfManuallyChunkedSelectorsBuilder<TestTags> builder =
                new MapOfManuallyChunkedSelectorsBuilder<>(getDefaultSizes(asList(TestTags.values())));
        //execute & assert
        assertThrows(UnsupportedOperationException.class, () -> builder.add(10, "test"));
    }

    @Test
    void add() {
        //given
        MapOfManuallyChunkedSelectorsBuilder<TestTags> builder =
                new MapOfManuallyChunkedSelectorsBuilder<>(getDefaultSizes(asList(TestTags.values())));

        //execute
        builder.add(10, "chunk1", TestTags.TAG1);
        builder.add(10, "chunk1", TestTags.TAG2);
        builder.add(2137, "chunk2", TestTags.TAG2);
        builder.add(15123, "chunk2", TestTags.TAG2);
        Map<TestTags, RandomAccessIndex> built = builder.build();
        Map<String, int[]> unpackedSelector1 = built.get(TestTags.TAG1).chunks()
                .collect(toMap(
                        chunk -> chunk.getChunkInfo().getLabel(),
                        chunk -> chunk.ids().toArray()));
        Map<String, int[]> unpackedSelector2 = built.get(TestTags.TAG2).chunks()
                .collect(toMap(
                        chunk -> chunk.getChunkInfo().getLabel(),
                        chunk -> chunk.ids().toArray()));

        //assert
        assertThat(unpackedSelector1.entrySet().size()).isEqualTo(1);
        assertThat(unpackedSelector1.get("chunk1"))
                .containsExactly(10);
        assertThat(unpackedSelector2.entrySet().size()).isEqualTo(2);
        assertThat(unpackedSelector2.get("chunk1"))
                .containsExactly(10);
        assertThat(unpackedSelector2.get("chunk2"))
                .containsExactlyInAnyOrder(2137, 15123);
    }
}