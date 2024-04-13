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

package pl.edu.icm.trurl.util.query;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.EngineBuilder;
import pl.edu.icm.trurl.ecs.Step;
import pl.edu.icm.trurl.ecs.index.Chunk;
import pl.edu.icm.trurl.ecs.index.ChunkInfo;
import pl.edu.icm.trurl.ecs.index.RandomAccessIndex;
import pl.edu.icm.trurl.ecs.query.ManuallyChunkedIndexBuilder;
import pl.edu.icm.trurl.ecs.query.Query;
import pl.edu.icm.trurl.ecs.query.RawQuery;
import pl.edu.icm.trurl.ecs.util.ContextualAction;
import pl.edu.icm.trurl.ecs.util.Indexes;
import pl.edu.icm.trurl.ecs.util.IteratingStepBuilder;

import java.util.Collection;
import java.util.Map;

public class QueryService {

    private final Indexes indexes;
    private final EngineBuilder engineBuilder;

    @WithFactory
    public QueryService(Indexes indexes,
                        EngineBuilder engineBuilder) {
        this.indexes = indexes;
        this.engineBuilder = engineBuilder;
    }

    public <T> RandomAccessIndex fixedIndexFromQuery(Query<T> query) {
        ManuallyChunkedIndexBuilder<T> indexBuilder = new ManuallyChunkedIndexBuilder<>();
        Step step = IteratingStepBuilder.iteratingOver(indexes.allEntities())
                .withoutContext()
                .perform(ContextualAction.of(entity -> query.process(entity, indexBuilder, ChunkInfo.DEFAULT_LABEL)))
                .build();

        engineBuilder.getEngine().execute(step);
        return indexBuilder.build();
    }

    public <T> Map<T, RandomAccessIndex> fixedMultipleIndexesFromRawQueryInParallel(Map<T, Integer> tagClassifiersWithInitialSizes,
                                                                                    RawQuery<T> query) {
        MapOfManuallyChunkedIndexesBuilder<T> indexesBuilder = new MapOfManuallyChunkedIndexesBuilder<>(tagClassifiersWithInitialSizes);

        indexes.allEntities().chunks()
                .flatMapToInt(Chunk::ids)
                .parallel()
                .forEach(id -> query.process(id, indexesBuilder, ChunkInfo.DEFAULT_LABEL));
        return indexesBuilder.build();
    }

    public <T> Map<T, RandomAccessIndex> fixedMultipleIndexesFromRawQueryInParallel(Collection<T> tagClassifiers,
                                                                                    RawQuery<T> query) {
        return fixedMultipleIndexesFromRawQueryInParallel(MapOfManuallyChunkedIndexesBuilder.getDefaultSizes(tagClassifiers),
                query);
    }
}
