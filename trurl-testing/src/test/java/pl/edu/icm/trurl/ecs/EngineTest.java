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
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.dao.Dao;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayAttributeFactory;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EngineTest {
    public static final int INITIAL_CAPACITY = 100;
    public static final int CAPACITY_HEADROOM = 50;
    public static final int SESSION_CACHE_CAPACITY = 25000;
    @Mock
    Store store;
    @Mock
    DaoManager daoManager;
    @Mock
    Session session;
    @Mock
    SessionFactory sessionFactory;
    @Mock
    EntitySystem system;
    @Mock
    Dao daoA;
    @Mock
    Dao daoB;
    @Mock
    Counter counterA;

    @BeforeEach
    void before() {
        lenient().when(store.getCounter()).thenReturn(counterA);
        lenient().when(counterA.getCount()).thenReturn(300);
        lenient().when(daoManager.allDaos()).thenAnswer(params -> List.of(
                daoA,
                daoB
        ));
        lenient().when(sessionFactory.createOrGet()).thenReturn(session);
    }

    @Test
    void construct() {
        // execute
        new Engine(INITIAL_CAPACITY, CAPACITY_HEADROOM, daoManager, new ArrayAttributeFactory(), SESSION_CACHE_CAPACITY);

        // assert
        verify(daoA).configureStore(any());
        verify(daoA).attachStore(any());
        verify(daoB).configureStore(any());
        verify(daoB).attachStore(any());
    }

    @Test
    void execute() {
        // given
        Engine engine = new Engine(store, CAPACITY_HEADROOM, daoManager, SESSION_CACHE_CAPACITY);

        // execute
        engine.execute(system);

        // assert
        verify(system, times(1)).execute(any());
    }

    @Test
    void getRootStore() {
        // given
        Engine engine = new Engine(store, CAPACITY_HEADROOM, daoManager, SESSION_CACHE_CAPACITY);

        // execute
        Store result = engine.getRootStore();

        // assert
        assertThat(result).isSameAs(store);
    }

    @Test
    void getMapperSet() {
        // given
        Engine engine = new Engine(store, CAPACITY_HEADROOM, daoManager, SESSION_CACHE_CAPACITY);

        // execute
        DaoManager result = engine.getDaoManager();

        // assert
        assertThat(result).isSameAs(daoManager);
    }

    @Test
    void getCount() {
        // given
        Engine engine = new Engine(store, CAPACITY_HEADROOM, daoManager, SESSION_CACHE_CAPACITY);

        // execute
        int count = engine.getCount();

        // assert
        assertThat(count).isEqualTo(300);
    }

    @Test
    void nextId() {
        // given
        Engine engine = new Engine(store, CAPACITY_HEADROOM, daoManager, SESSION_CACHE_CAPACITY);

        // execute
        int nextId = engine.allocateNextId();
        int nextNextId = engine.allocateNextId();

        // assert
        verify(counterA, times(2)).next();
    }
}