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
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.selector.ChunkInfo;
import pl.edu.icm.trurl.ecs.selector.RandomAccessSelector;
import pl.edu.icm.trurl.ecs.selector.RandomAccessSelector;
import pl.edu.icm.trurl.ecs.util.IteratingSystemBuilder;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.ecs.util.Visit;

import java.util.Collection;
import java.util.Map;
import pl.edu.icm.trurl.ecs.util.Visit;

public class SelectorFromQueryService {

    private final Selectors selectors;
    private final EngineConfiguration engineConfiguration;

    @WithFactory
    public SelectorFromQueryService(Selectors selectors,
                                    EngineConfiguration engineConfiguration) {
        this.selectors = selectors;
        this.engineConfiguration = engineConfiguration;
    }

    public <T> RandomAccessSelector fixedSelectorFromQuery(Query<T> query) {
        ManuallyChunkedSelectorBuilder<T> selectorBuilder = new ManuallyChunkedSelectorBuilder<>();
        EntitySystem entitySystem = IteratingSystemBuilder.iteratingOver(selectors.allEntities())
                .readOnlyEntities()
                .withoutContext()
                .perform(Visit.of(entity -> query.process(entity, selectorBuilder, ChunkInfo.DEFAULT_LABEL)))
                .build();

        engineConfiguration.getEngine().execute(entitySystem);
        return selectorBuilder.build();
    }

    public <T> Map<T, RandomAccessSelector> fixedMultipleSelectorsFromRawQueryInParallel(Map<T, Integer> tagClassifiersWithInitialSizes,
                                                                                         RawQuery<T> query) {
        MapOfManuallyChunkedSelectorsBuilder<T> selectorsBuilder = new MapOfManuallyChunkedSelectorsBuilder<>(tagClassifiersWithInitialSizes);

        selectors.allEntities().chunks()
                .flatMapToInt(Chunk::ids)
                .parallel()
                .forEach(id -> query.process(id, selectorsBuilder, ChunkInfo.DEFAULT_LABEL));
        return selectorsBuilder.build();
    }

    public <T> Map<T, RandomAccessSelector> fixedMultipleSelectorsFromRawQueryInParallel(Collection<T> tagClassifiers,
                                                                                         RawQuery<T> query) {
        return fixedMultipleSelectorsFromRawQueryInParallel(MapOfManuallyChunkedSelectorsBuilder.getDefaultSizes(tagClassifiers),
                query);
    }
}
