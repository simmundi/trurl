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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.dao.Dao;
import pl.edu.icm.trurl.exampledata.Stats;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NoCacheSessionTest {

    public static final int ID = 123;
    public static final int OWNER_ID = 77;

    @Mock
    Engine engine;

    @Mock
    Dao statsDao;

    @Mock
    ComponentAccessor componentAccessor;

    @Mock
    DaoManager daoManager;

    @BeforeEach
    void before() {
        lenient().when(engine.getDaoManager()).thenReturn(daoManager);
        lenient().when(daoManager.indexToMapper(0)).thenReturn(statsDao);
        lenient().when(daoManager.componentCount()).thenReturn(1);
    }

    @Test
    @DisplayName("Should create attached entities in NORMAL mode")
    void getEntity() {
        // given
        NoCacheSession noCacheSession = new NoCacheSession(engine, 10, NoCacheSession.Mode.NORMAL, 1);

        // execute
        DetachedEntity entity1 = noCacheSession.getEntity(ID);
        DetachedEntity entity2 = noCacheSession.getEntity(ID);
        DetachedEntity entity3 = noCacheSession.getEntity(ID + 1);

        // assert
        assertThat(entity1).isSameAs(entity2);
        assertThat(entity1).isNotSameAs(entity3);
        verify(daoManager, times(2)).componentCount();
    }

    @Test
    @DisplayName("Should create detached entities in DETACHED mode")
    void getEntity__detached() {
        // given
        NoCacheSession noCacheSession = new NoCacheSession(engine, 10, NoCacheSession.Mode.DETACHED_ENTITIES, OWNER_ID);
        // execute
        DetachedEntity entity1 = noCacheSession.getEntity(ID);
        DetachedEntity entity2 = noCacheSession.getEntity(ID);

        // assert
        assertThat(entity1).isNotSameAs(entity2);
        verify(daoManager, times(2)).componentCount();
    }

    @Test
    @DisplayName("Should create stub entities in STUB mode")
    void getEntity__stubs() {
        // given
        NoCacheSession noCacheSession = new NoCacheSession(engine, 10, NoCacheSession.Mode.STUB_ENTITIES, OWNER_ID);
        // execute
        DetachedEntity entity1 = noCacheSession.getEntity(ID);
        DetachedEntity entity2 = noCacheSession.getEntity(ID);

        // assert
        assertThat(entity1).isNotSameAs(entity2);
        verify(daoManager, never()).componentCount();
    }

    @Test
    @DisplayName("Should persist entities using dao")
    void persist() {
        // given
        NoCacheSession noCacheSession = new NoCacheSession(engine, 10, NoCacheSession.Mode.NORMAL, OWNER_ID);
        DetachedEntity entity1 = noCacheSession.getEntity(ID);
        DetachedEntity entity2 = noCacheSession.getEntity(ID + 1);
        entity1.add(new Stats());
        entity2.add(new Stats());

        // execute
        noCacheSession.close();

        // assert
        verify(statsDao, times(2)).save(eq(noCacheSession), any(), anyInt());
    }

    @Test
    @DisplayName("Should return the engine")
    void getEngine() {
        // given
        NoCacheSession noCacheSession = new NoCacheSession(engine, 10, NoCacheSession.Mode.NORMAL, OWNER_ID);

        // execute
        Engine engine = noCacheSession.getEngine();

        // assert
        assertThat(engine).isSameAs(this.engine);
    }
}