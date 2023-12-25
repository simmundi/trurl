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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class MapOfManuallyChunkedSelectorsBuilder<T> implements RawQuery.Result<T> {
    private final Map<T, ManuallyChunkedRawIndexBuilder<?>> selectorBuilders;

    public MapOfManuallyChunkedSelectorsBuilder(Map<T, Integer> tagClassifiersWithInitialSizes) {
        Map<T, ManuallyChunkedRawIndexBuilder<?>> map = new HashMap<>();
        tagClassifiersWithInitialSizes.forEach(
                (k, v) -> map.computeIfAbsent(k, (unused) -> new ManuallyChunkedRawIndexBuilder<>(v))
        );
        selectorBuilders = Collections.unmodifiableMap(map);
    }

    public static <K> Map<K, Integer> getDefaultSizes(Collection<K> tagClassifiers) {
        return tagClassifiers.stream().collect(toMap(
                tagClassifier -> tagClassifier,
                u -> ManuallyChunkedRawIndexBuilder.DEFAULT_INITIAL_SIZE
        ));
    }

    @Override
    public void add(int entityId, String tag) {
        throw new UnsupportedOperationException("Use add(entityId, tag, tagClassifier)");
    }

    @Override
    public void add(int entityId, String tag, T tagClassifier) {
        selectorBuilders.get(tagClassifier).add(entityId, tag);
    }

    public Map<T, RandomAccessIndex> build() {
        return selectorBuilders.entrySet().stream().collect(toMap(Map.Entry::getKey, entry -> entry.getValue().build()));
    }
}
