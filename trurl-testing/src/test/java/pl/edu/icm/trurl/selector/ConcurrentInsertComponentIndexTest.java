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

package pl.edu.icm.trurl.selector;

import org.assertj.core.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.MapperSet;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.MapperListeners;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.exampledata.Stats;

import java.util.stream.IntStream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConcurrentInsertComponentIndexTest {
    @Mock
    Engine engine;

    @Mock
    MapperSet mapperSet;

    @Mock
    Mapper<Stats> statsMapper;

    @Mock
    MapperListeners<Stats> mapperListeners;

    @Mock
    EngineConfiguration engineConfiguration;

    StatsConcurrentInsertIndex statsBitmapIndex;

    @BeforeEach
    void before() {
        statsBitmapIndex = Mockito.spy(new StatsConcurrentInsertIndex(engineConfiguration));
    }

    @Test
    void onEngineCreated() {
        // given
        when(engine.getMapperSet()).thenReturn(mapperSet);
        when(mapperSet.classToMapper(Stats.class)).thenReturn(statsMapper);
        when(statsMapper.getMapperListeners()).thenReturn(mapperListeners);

        // execute
        statsBitmapIndex.onEngineCreated(engine);

        // assert
        verify(mapperListeners).addSavingListener(statsBitmapIndex);
    }

    @Test
    void contains() {
        // given
        when(engine.getMapperSet()).thenReturn(mapperSet);
        when(mapperSet.classToMapper(Stats.class)).thenReturn(statsMapper);
        when(statsMapper.getMapperListeners()).thenReturn(mapperListeners);
        statsBitmapIndex.onEngineCreated(engine);
        statsBitmapIndex.savingComponent(88888, new Stats());
        statsBitmapIndex.savingComponent(888, new Stats());

        // execute
        boolean yes1 = statsBitmapIndex.contains(88888);
        boolean yes2 = statsBitmapIndex.contains(888);
        boolean no = statsBitmapIndex.contains(666);

        // assert
        Assertions.assertThat(yes1).isTrue();
        Assertions.assertThat(yes2).isTrue();
        Assertions.assertThat(no).isFalse();
    }

    @Test
    void ids() {
        // given
        when(engine.getMapperSet()).thenReturn(mapperSet);
        when(mapperSet.classToMapper(Stats.class)).thenReturn(statsMapper);
        when(statsMapper.getMapperListeners()).thenReturn(mapperListeners);
        statsBitmapIndex.onEngineCreated(engine);
        statsBitmapIndex.savingComponent(88888, new Stats());
        statsBitmapIndex.savingComponent(888, new Stats());

        // execute
        IntStream results = statsBitmapIndex.chunks().flatMapToInt(Chunk::ids);

        // assert
        Assertions.assertThat(results).containsExactlyInAnyOrder(888, 88888);
    }

}
