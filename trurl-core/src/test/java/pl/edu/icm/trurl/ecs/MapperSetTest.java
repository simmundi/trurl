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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.ecs.util.DynamicComponentAccessor;
import pl.edu.icm.trurl.exampledata.Looks;
import pl.edu.icm.trurl.exampledata.Stats;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MapperSetTest {

    @Spy
    ComponentAccessor componentAccessor = new DynamicComponentAccessor(Arrays.asList(Looks.class, Stats.class));
    @Spy
    Mappers mappers = new Mappers();

    @InjectMocks
    MapperSet mapperSet;

    @Test
    void classToMapper() {
        // execute
        Looks looks = mapperSet.classToMapper(Looks.class).create();
        Stats stats = mapperSet.classToMapper(Stats.class).create();

        // assert
        assertThat(looks).isExactlyInstanceOf(Looks.class);
        assertThat(stats).isExactlyInstanceOf(Stats.class);
    }

    @Test
    void indexToMapper() {
        // execute
        Object stats = mapperSet.indexToMapper(1).create();
        Object looks = mapperSet.indexToMapper(0).create();

        // assert
        assertThat(looks).isExactlyInstanceOf(Looks.class);
        assertThat(stats).isExactlyInstanceOf(Stats.class);
    }

    @Test
    void classToIndex() {
        // execute
        int statsIndex = mapperSet.classToIndex(Stats.class);
        int looksIndex = mapperSet.classToIndex(Looks.class);

        // assert
        assertThat(looksIndex).isEqualTo(0);
        assertThat(statsIndex).isEqualTo(1);
    }

    @Test
    void componentCount() {
        // execute
        int count = mapperSet.componentCount();

        // assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    void streamMappers() {
        // execute
        Stream<String> results = mapperSet.streamMappers().map(Mapper::name);

        // assert
        assertThat(results).containsExactly("looks", "stats");
    }
}
