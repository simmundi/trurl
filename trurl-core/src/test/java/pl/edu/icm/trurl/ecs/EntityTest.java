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
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.exampledata.Looks;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntityTest {

    public static final int ID = 123;

    Looks looks = new Looks();

    @Mock
    Mapper mapper;

    @Mock
    MapperSet mapperSet;

    @Mock
    Session session;

    @Mock
    Entity entity;

    @BeforeEach
    void before() {
        lenient().when(mapperSet.classToMapper(Looks.class)).thenReturn(mapper);
        lenient().when(mapperSet.indexToMapper(0)).thenReturn(mapper);
        lenient().when(mapperSet.classToIndex(Looks.class)).thenReturn(0);
        when(mapperSet.componentCount()).thenReturn(1);
        entity = new Entity(mapperSet, session, ID);
    }

    @Test
    void get() {
        // when
        when(mapper.isPresent(ID)).thenReturn(true);
        when(mapper.createAndLoad(session, ID)).thenReturn(looks);

        // execute
        Looks component = entity.get(Looks.class);

        // assert
        assertThat(component).isSameAs(looks);
    }

    @Test
    void get__not_present() {
        // when
        when(mapper.isPresent(ID)).thenReturn(false);

        // execute
        Looks component = entity.get(Looks.class);

        // assert
        verify(mapper, never()).load(any(), any(), anyInt());
        assertThat(component).isNull();
    }

    @Test
    void add() {
        // when
        Looks newLooks = new Looks();

        // execute
        entity.add(newLooks);

        // assert
        verify(mapperSet).classToIndex(Looks.class);
        assertThat(entity.get(Looks.class)).isSameAs(newLooks);
    }

    @Test
    void persist() {
        // when
        Looks newLooks = new Looks();
        entity.add(newLooks);

        // execute
        entity.persist();

        // assert
        verify(mapper).save(session, newLooks, ID);
    }

    @Test
    void getId() {
        // execute
        int id = entity.getId();

        // assert
        assertThat(id).isEqualTo(ID);
    }

    @Test
    void getSession() {
        // execute
        Session session = entity.getSession();

        // assert
        assertThat(session).isEqualTo(session);
    }

    @Test
    void optional() {
        // when
        Looks newLooks = new Looks();
        entity.add(newLooks);

        // execute
        Optional<Looks> result = entity.optional(Looks.class);

        // assert
        assertThat(result).contains(newLooks);
    }

    @Test
    void optional__empty() {
        // execute
        Optional<Looks> result = entity.optional(Looks.class);

        // assert
        assertThat(result).isEmpty();
    }
}
