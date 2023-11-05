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
package pl.edu.icm.trurl.ecs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayAttributeFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EngineTest {
    public static final int INITIAL_CAPACITY = 100;
    public static final int CAPACITY_HEADROOM = 50;
    @Mock
    Store store;
    @Mock
    MapperSet mapperSet;
    @Mock
    Session session;
    @Mock
    SessionFactory sessionFactory;
    @Mock
    EntitySystem system;
    @Mock
    Mapper mapperA;
    @Mock
    Mapper mapperB;
    @Mock
    Counter counterA;

    @BeforeEach
    void before() {
        lenient().when(store.getCounter()).thenReturn(counterA);
        lenient().when(counterA.getCount()).thenReturn(300);
        lenient().when(mapperSet.streamMappers()).thenAnswer(params -> Stream.of(
                mapperA,
                mapperB
        ));
        lenient().when(sessionFactory.create()).thenReturn(session);
    }

    @Test
    void construct() {
        // execute
        new Engine(INITIAL_CAPACITY, CAPACITY_HEADROOM, mapperSet, false, new ArrayAttributeFactory());

        // assert
        verify(mapperA).configureStore(any());
        verify(mapperA).attachStore(any());
        verify(mapperB).configureStore(any());
        verify(mapperB).attachStore(any());
    }

    @Test
    void execute() {
        // given
        Engine engine = new Engine(store, CAPACITY_HEADROOM, mapperSet, false);

        // execute
        engine.execute(system);

        // assert
        verify(system, times(1)).execute(any());
    }

    @Test
    void streamDetached() {
        // given
        Engine engine = new Engine(store, CAPACITY_HEADROOM, mapperSet, false);

        // execute
        List<Integer> result = engine.streamDetached().map(Entity::getId).collect(Collectors.toList());

        // assertThat
        assertThat(result).isEqualTo(IntStream.range(0, 300).boxed().collect(Collectors.toList()));
    }

    @Test
    void getRootStore() {
        // given
        Engine engine = new Engine(store, CAPACITY_HEADROOM, mapperSet, false);

        // execute
        Store result = engine.getRootStore();

        // assert
        assertThat(result).isSameAs(store);
    }

    @Test
    void getMapperSet() {
        // given
        Engine engine = new Engine(store, CAPACITY_HEADROOM, mapperSet, false);

        // execute
        MapperSet result = engine.getMapperSet();

        // assert
        assertThat(result).isSameAs(mapperSet);
    }

    @Test
    void getCount() {
        // given
        Engine engine = new Engine(store, CAPACITY_HEADROOM, mapperSet, false);

        // execute
        int count = engine.getCount();

        // assert
        assertThat(count).isEqualTo(300);
    }

    @Test
    void nextId() {
        // given
        Engine engine = new Engine(store, CAPACITY_HEADROOM, mapperSet, false);

        // execute
        int nextId = engine.nextId();
        int nextNextId = engine.nextId();

        // assert
        verify(counterA, times(2)).next();
    }
}