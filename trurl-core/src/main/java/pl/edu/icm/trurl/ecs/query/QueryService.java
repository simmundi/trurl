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

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.index.Chunk;
import pl.edu.icm.trurl.ecs.index.ChunkInfo;
import pl.edu.icm.trurl.ecs.index.RandomAccessIndex;
import pl.edu.icm.trurl.ecs.util.Action;
import pl.edu.icm.trurl.ecs.util.Indices;
import pl.edu.icm.trurl.ecs.util.IteratingSystemBuilder;

import java.util.Collection;
import java.util.Map;

public class QueryService {

    private final Indices indices;
    private final EngineConfiguration engineConfiguration;

    @WithFactory
    public QueryService(Indices indices,
                        EngineConfiguration engineConfiguration) {
        this.indices = indices;
        this.engineConfiguration = engineConfiguration;
    }

    public <T> RandomAccessIndex fixedSelectorFromQuery(Query<T> query) {
        ManuallyChunkedSelectorBuilder<T> selectorBuilder = new ManuallyChunkedSelectorBuilder<>();
        EntitySystem entitySystem = IteratingSystemBuilder.iteratingOver(indices.allEntities())
                .withoutContext()
                .perform(Action.of(entity -> query.process(entity, selectorBuilder, ChunkInfo.DEFAULT_LABEL)))
                .build();

        engineConfiguration.getEngine().execute(entitySystem);
        return selectorBuilder.build();
    }

    public <T> Map<T, RandomAccessIndex> fixedMultipleSelectorsFromRawQueryInParallel(Map<T, Integer> tagClassifiersWithInitialSizes,
                                                                                      RawQuery<T> query) {
        MapOfManuallyChunkedSelectorsBuilder<T> selectorsBuilder = new MapOfManuallyChunkedSelectorsBuilder<>(tagClassifiersWithInitialSizes);

        indices.allEntities().chunks()
                .flatMapToInt(Chunk::ids)
                .parallel()
                .forEach(id -> query.process(id, selectorsBuilder, ChunkInfo.DEFAULT_LABEL));
        return selectorsBuilder.build();
    }

    public <T> Map<T, RandomAccessIndex> fixedMultipleSelectorsFromRawQueryInParallel(Collection<T> tagClassifiers,
                                                                                      RawQuery<T> query) {
        return fixedMultipleSelectorsFromRawQueryInParallel(MapOfManuallyChunkedSelectorsBuilder.getDefaultSizes(tagClassifiers),
                query);
    }
}
