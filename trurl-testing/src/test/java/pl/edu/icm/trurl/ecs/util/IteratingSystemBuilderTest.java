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

package pl.edu.icm.trurl.ecs.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.entity.IdentityMapSession;
import pl.edu.icm.trurl.ecs.entity.Session;
import pl.edu.icm.trurl.ecs.entity.SessionFactory;
import pl.edu.icm.trurl.ecs.mapper.LifecycleEvent;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.selector.RangeSelector;
import pl.edu.icm.trurl.ecs.system.IteratingSystemBuilder;
import pl.edu.icm.trurl.ecs.system.Visit;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IteratingSystemBuilderTest {


    private Function<Chunk, String> contextFactory = chunk -> "#" + chunk.getChunkInfo().getChunkId();
    @Mock
    private Visit<String> visit1;
    @Mock
    private Visit<String> visit2;
    @Mock
    SessionFactory sessionFactory;
    @Mock
    Session session;

    @Test
    @DisplayName("Should create a system running in parallel")
    void create() {
        // given
        RangeSelector rangeSelector = new RangeSelector(0, 1000, 10);
        when(sessionFactory.withModeAndCount(any(), anyInt())).thenReturn(sessionFactory);
        when(sessionFactory.create(anyInt())).thenReturn(session);

        // execute
        EntitySystem system = IteratingSystemBuilder.iteratingOverInParallel(rangeSelector)
                .persistingAll()
                .withContext(contextFactory)
                .perform(visit1)
                .andPerform(visit2)
                .build();
        system.execute(sessionFactory);

        // assert
        verify(sessionFactory).withModeAndCount(IdentityMapSession.Mode.NORMAL, 80);
        verify(sessionFactory).lifecycleEvent(LifecycleEvent.PRE_PARALLEL_ITERATION);
        verify(sessionFactory, times(100)).create(anyInt());
        verify(visit1, times(1000)).perform(anyString(), eq(session), anyInt());
        verify(visit2, times(1000)).perform(anyString(), eq(session), anyInt());
    }

    @Test
    @DisplayName("Should create a system running in sequence")
    void create__sequential() {
        // given
        RangeSelector rangeSelector = new RangeSelector(0, 1000, 10);
        when(sessionFactory.withModeAndCount(any(), anyInt())).thenReturn(sessionFactory);
        when(sessionFactory.create(anyInt())).thenReturn(session);

        // execute
        EntitySystem system = IteratingSystemBuilder.iteratingOver(rangeSelector)
                .detachedEntities()
                .withContext(contextFactory)
                .perform(visit1)
                .build();
        system.execute(sessionFactory);

        // assert
        verify(sessionFactory).withModeAndCount(IdentityMapSession.Mode.DETACHED_ENTITIES, 80);
        verify(sessionFactory, never()).lifecycleEvent(any());
        verify(sessionFactory, times(100)).create(anyInt());
        verify(visit1, times(1000)).perform(anyString(), eq(session), anyInt());
    }

}