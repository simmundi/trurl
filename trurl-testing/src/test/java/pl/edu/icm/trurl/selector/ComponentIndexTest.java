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
import pl.edu.icm.trurl.exampledata.Stats;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComponentIndexTest {
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

    StatsIndex statsIndex;

    @BeforeEach
    void before() {
        statsIndex = Mockito.spy(new StatsIndex(engineConfiguration));
    }

    @Test
    void onEngineCreated() {
        // given
        when(engine.getMapperSet()).thenReturn(mapperSet);
        when(mapperSet.classToMapper(Stats.class)).thenReturn(statsMapper);
        when(statsMapper.getMapperListeners()).thenReturn(mapperListeners);

        // execute
        statsIndex.onEngineCreated(engine);

        // assert
        verify(mapperListeners).addSavingListener(statsIndex);
    }



}
