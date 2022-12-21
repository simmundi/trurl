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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.SessionFactory;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.selector.ChunkInfo;
import pl.edu.icm.trurl.ecs.selector.Selector;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntityIteratorTest {

    @Mock
    Selector selector;

    @Mock
    Systems.IdxProcessor<String> processor;

    @Mock
    SessionFactory sessionFactory;
    @Mock
    SessionFactory configuredSessionFactory;
    @Mock
    Session session;

    @Test
    @DisplayName("Should create a session for each chunk")
    void forEach() {
        // given
        when(selector.chunks()).thenReturn(Stream.of(
                new Chunk(ChunkInfo.of(1, 123), IntStream.range(0, 5)),
                new Chunk(ChunkInfo.of(2, 123), IntStream.range(100, 115)),
                new Chunk(ChunkInfo.of(3, 123), IntStream.range(200, 225)),
                new Chunk(ChunkInfo.of(4, 123), IntStream.range(300, 350))
        ));
        when(selector.estimatedChunkSize()).thenReturn(1);
        when(sessionFactory.withModeAndCount(Session.Mode.NORMAL, 8))
                .thenReturn(configuredSessionFactory);
        when(configuredSessionFactory.create(anyInt())).thenReturn(session);

        EntitySystem iterator = EntityIterator
                .select(selector)
                .forEach(chunk -> chunk.getChunkInfo().getChunkId() + "x", processor);

        // execute
        iterator.execute(sessionFactory);

        // assert
        verify(configuredSessionFactory, times(4)).create(anyInt());
        verify(processor, times(5)).process(eq("1x"), eq(session), anyInt());
        verify(processor, times(15)).process(eq("2x"), eq(session), anyInt());
        verify(processor, times(25)).process(eq("3x"), eq(session), anyInt());
        verify(processor, times(50)).process(eq("4x"), eq(session), anyInt());
    }

    @Test
    @DisplayName("Should create a session for each chunk in parallel")
    void forEach_parallel() {
        // given
        when(selector.chunks()).thenReturn(IntStream.range(0, 1000).mapToObj(id ->
                new Chunk(ChunkInfo.of(1, 100), IntStream.range(id * 100, id * 100 + 40))));
        when(sessionFactory.withModeAndCount(any(), anyInt())).thenReturn(configuredSessionFactory);
        when(configuredSessionFactory.create(anyInt())).thenReturn(session);

        EntitySystem iterator = EntityIterator
                .select(selector)
                .parallel()
                .forEach(chunk -> "x", processor);

        // execute
        iterator.execute(sessionFactory);

        // assert
        verify(configuredSessionFactory, times(1000)).create(anyInt());
        verify(processor, times(40000)).process(any(), any(), anyInt());
    }

}
