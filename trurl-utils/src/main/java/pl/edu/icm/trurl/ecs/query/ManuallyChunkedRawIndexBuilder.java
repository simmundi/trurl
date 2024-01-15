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

import pl.edu.icm.trurl.ecs.index.RandomAccessIndex;
import pl.edu.icm.trurl.ecs.util.ManuallyChunkedArrayIndex;
import pl.edu.icm.trurl.util.SynchronizedResizableIntArray;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builder for building deterministic ManuallyChunkedIndexes with out-of-order data and in parallel.
 * <p>
 * The chunks will be ordered in natural order of the labels.
 * <p>
 * Adding the same (id, label) pair multiple times will cause adding the same id multiple times.
 * <p>
 * Adding the same id with different labels will cause the same id to appear in multiple different chunks.
 */
public class ManuallyChunkedRawIndexBuilder<T> implements RawQuery.Result<T> {
    public final static int DEFAULT_INITIAL_SIZE = 1024;
    private final int initialSize;
    private final ConcurrentHashMap<String, SynchronizedResizableIntArray> data = new ConcurrentHashMap<>();

    public ManuallyChunkedRawIndexBuilder(int initialSize) {
        this.initialSize = initialSize;
    }

    public ManuallyChunkedRawIndexBuilder() {
        this(DEFAULT_INITIAL_SIZE);
    }

    /**
     * Adds an entity to a chunk identified by the label.
     *
     * @param entityId - entity id
     * @param tag      - must not be null
     */
    @Override
    public void add(int entityId, String tag, T tagClassifier) {
        data.computeIfAbsent(tag, unused -> new SynchronizedResizableIntArray(this.initialSize))
                .add(entityId);
    }

    public RandomAccessIndex build() {
        int size = data.values().stream().mapToInt(SynchronizedResizableIntArray::getCurrentSize).sum();

        ManuallyChunkedArrayIndex index = new ManuallyChunkedArrayIndex(size, data.entrySet().size());
        data.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(chunkData -> {
            chunkData.getValue().stream().forEach(index::add);
            index.endChunk(chunkData.getKey());
        });
        return index;
    }

}
