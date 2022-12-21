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

import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.selector.ChunkInfo;
import pl.edu.icm.trurl.ecs.selector.Selector;

import java.util.function.IntSupplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RangeSelector implements Selector {
    private final IntSupplier fromInclusive;
    private final IntSupplier toExclusive;
    private final int chunkSize;

    public RangeSelector(int fromInclusive, int toExclusive, int chunkSize) {
        this.fromInclusive = () -> fromInclusive;
        this.toExclusive = () -> toExclusive;
        this.chunkSize = chunkSize;
    }

    public RangeSelector(IntSupplier fromInclusive, IntSupplier toExclusive, int chunkSize) {
        this.fromInclusive = fromInclusive;
        this.toExclusive = toExclusive;
        this.chunkSize = chunkSize;
    }

    public static RangeSelector of(int fromInclusive, int toExclusive, int chunkSize) {
        return new RangeSelector(fromInclusive, toExclusive, chunkSize);
    }

    @Override
    public Stream<Chunk> chunks() {
        int count = toExclusive.getAsInt() - fromInclusive.getAsInt();
        int fromInclusiveAsInt = fromInclusive.getAsInt();

        int fullChunksCount = count / chunkSize;
        int restSize = count % chunkSize;
        int chunkCount = (int) Math.ceil((double) count / chunkSize);

        return IntStream.range(0, chunkCount).mapToObj(chunkId -> {
            int currentChunkSize = chunkId == fullChunksCount ? restSize : chunkSize;
            int start = chunkId * chunkSize + fromInclusiveAsInt;

            return new Chunk(ChunkInfo.of(chunkId, currentChunkSize), IntStream.range(start, start + currentChunkSize));
        });
    }
}
