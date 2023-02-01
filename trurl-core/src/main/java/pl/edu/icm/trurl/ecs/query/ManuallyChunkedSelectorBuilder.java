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
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.ecs.util.ManuallyChunkedArraySelector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Comparator.comparing;

/**
 * Builder for building deterministic ManuallyChunkedSelectors with out-of-order data and in parallel.
 *
 * The chunks will be ordered in natural order of the labels, the ids will be ordered in ascending order.
 *
 * Adding the same (id, label) pair multiple times is not an error and the pair will only be added once.
 *
 * Adding the same id with different labels will cause the same id to appear in multiple different chunks.
 */
public class ManuallyChunkedSelectorBuilder implements Query.Result {

    private final ConcurrentHashMap<String, ConcurrentHashMap<Integer, Boolean>> data = new ConcurrentHashMap<>();

    /**
     * Adds an entity to a chunk identified by the label.
     * @param entity - must not be null
     * @param label - must not be null
     */
    @Override
    public void add(Entity entity, String label) {
        data.computeIfAbsent(label, unused -> new ConcurrentHashMap<>())
                .put(entity.getId(), Boolean.TRUE);
    }

    public Selector build() {
        int size = data.values().stream().mapToInt(ConcurrentHashMap::size).sum();

        ManuallyChunkedArraySelector selector = new ManuallyChunkedArraySelector(size, data.entrySet().size());
        data.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(chunkData -> {
            chunkData.getValue().keySet().stream().sorted().forEach(id -> selector.add(id));
            selector.endChunk(chunkData.getKey());
        });
        return selector;
    }

}
