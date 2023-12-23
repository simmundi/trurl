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

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.index.RandomAccessIndex;
import pl.edu.icm.trurl.ecs.util.ManuallyChunkedArrayIndex;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builder for building deterministic ManuallyChunkedSelectors with out-of-order data and in parallel.
 * <p>
 * The chunks will be ordered in natural order of the labels, the ids will be ordered in ascending order.
 * <p>
 * Adding the same (id, label) pair multiple times is not an error and the pair will only be added once.
 * <p>
 * Adding the same id with different labels will cause the same id to appear in multiple different chunks.
 */
public class ManuallyChunkedSelectorBuilder<T> implements Query.Result<T> {

    private final ConcurrentHashMap<String, ConcurrentHashMap<Integer, Boolean>> data = new ConcurrentHashMap<>();

    /**
     * Adds an entity to a chunk identified by the label.
     *
     * @param entity - must not be null
     * @param tag    - must not be null
     */
    @Override
    public void add(Entity entity, String tag, T tagClassifier) {
        data.computeIfAbsent(tag, unused -> new ConcurrentHashMap<>())
                .put((int) entity.getId(), Boolean.TRUE);
    }

    public RandomAccessIndex build() {
        int size = data.values().stream().mapToInt(ConcurrentHashMap::size).sum();

        ManuallyChunkedArrayIndex selector = new ManuallyChunkedArrayIndex(size, data.entrySet().size());
        data.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(chunkData -> {
            chunkData.getValue().keySet().stream().sorted().forEach(selector::add);
            selector.endChunk(chunkData.getKey());
        });
        return selector;
    }

}
