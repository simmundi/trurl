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
import pl.edu.icm.trurl.ecs.NoCacheSession;
import pl.edu.icm.trurl.ecs.SessionFactory;
import pl.edu.icm.trurl.ecs.dao.LifecycleEvent;
import pl.edu.icm.trurl.ecs.index.Chunk;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IteratingSystemBuilderTest {


    private Function<Chunk, String> contextFactory = chunk -> "#" + chunk.getChunkInfo().getChunkId();
    @Mock
    private Action<String> action1;
    @Mock
    private Action<String> action2;
    @Mock
    SessionFactory sessionFactory;
    @Mock
    NoCacheSession noCacheSession;

    @Test
    @DisplayName("Should create a system running in parallel")
    void create() {
        // given
        RangeIndex rangeIndex = new RangeIndex(0, 1000, 10);
        when(sessionFactory.withModeAndCount(any(), anyInt())).thenReturn(sessionFactory);
        when(sessionFactory.createOrGet(anyInt())).thenReturn(noCacheSession);

        // execute
        EntitySystem system = IteratingSystemBuilder.iteratingOverInParallel(rangeIndex)
                .persistingAll()
                .withContext(contextFactory)
                .perform(action1)
                .build();
        system.execute(sessionFactory);

        // assert
        verify(sessionFactory).withModeAndCount(NoCacheSession.Mode.NORMAL, 80);
        verify(sessionFactory).lifecycleEvent(LifecycleEvent.PRE_PARALLEL_ITERATION);
        verify(sessionFactory, times(100)).createOrGet(anyInt());
        verify(action1, times(1000)).perform(anyString(), eq(noCacheSession), anyInt());
        verify(action2, times(1000)).perform(anyString(), eq(noCacheSession), anyInt());
    }

    @Test
    @DisplayName("Should create a system running in sequence")
    void create__sequential() {
        // given
        RangeIndex rangeSelector = new RangeIndex(0, 1000, 10);
        when(sessionFactory.withModeAndCount(any(), anyInt())).thenReturn(sessionFactory);
        when(sessionFactory.createOrGet(anyInt())).thenReturn(noCacheSession);

        // execute
        EntitySystem system = IteratingSystemBuilder.iteratingOver(rangeSelector)
                .withoutPersisting()
                .withContext(contextFactory)
                .perform(action1)
                .build();
        system.execute(sessionFactory);

        // assert
        verify(sessionFactory).withModeAndCount(NoCacheSession.Mode.DETACHED_ENTITIES, 80);
        verify(sessionFactory, never()).lifecycleEvent(any());
        verify(sessionFactory, times(100)).createOrGet(anyInt());
        verify(action1, times(1000)).perform(anyString(), eq(noCacheSession), anyInt());
    }

}