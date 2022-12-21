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

package pl.edu.icm.trurl.store;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.MapperSet;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.exampledata.BunchOfData;
import pl.edu.icm.trurl.exampledata.Looks;
import pl.edu.icm.trurl.exampledata.Stats;
import pl.edu.icm.trurl.store.attribute.Attribute;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StoreSplitterTest {

    @Mock Attribute attributeA1;
    @Mock Attribute attributeA2;
    @Mock Attribute attributeB1;
    @Mock Attribute attributeB2;
    @Mock Attribute attributeC;

    @Mock Mapper mapperA;

    @Mock Mapper mapperB;
    @Mock Mapper mapperC;

    @Mock
    MapperSet mapperSet;
    @Mock Engine engine;

    @Mock Store store;

    void prepareMappers() {
        when(attributeA1.name()).thenReturn("a1");
        when(attributeA2.name()).thenReturn("a2");
        when(attributeB1.name()).thenReturn("b1");
        when(attributeB2.name()).thenReturn("b2");
        when(attributeC.name()).thenReturn("c");
        when(store.getCount()).thenReturn(5);
        when(store.attributes()).thenAnswer(call -> Stream.of(attributeC, attributeA1, attributeA2, attributeB1, attributeB2));
        when(mapperA.getCount()).thenReturn(10);
        when(mapperB.getCount()).thenReturn(8);
        when(mapperA.attributes()).thenReturn(asList(attributeA1, attributeA2));
        when(mapperB.attributes()).thenReturn(asList(attributeB1, attributeB2));
        when(mapperC.attributes()).thenReturn(asList(attributeC));
    }

    @Test
    @DisplayName("Should split 5 attributes across two stores using mapper references")
    public void splitMappers() {
        // given
        prepareMappers();
        StoreSplitter from = StoreSplitter.from(store);

        // execute
        StoreInspector split = from.splitMappers(asList(mapperA, mapperB));
        StoreInspector reminder = from.getReminder();
        StoreInspector onlyC = from.splitMappers(asList(mapperC));
        StoreInspector empty = from.getReminder();

        // assert
        Assertions.assertThat(split.attributes()).containsExactly(attributeA1, attributeA2, attributeB1, attributeB2);
        Assertions.assertThat(reminder.attributes()).containsExactly(attributeC);
        Assertions.assertThat(onlyC.attributes()).containsExactly(attributeC);
        Assertions.assertThat(empty.attributes()).isEmpty();
    }

    @Test
    @DisplayName("Should split 5 attributes across two stores using components")
    public void splitComponents() {
        // given
        prepareMappers();
        when(engine.getStore()).thenReturn(store);
        when(engine.getMapperSet()).thenReturn(mapperSet);
        when(mapperSet.classToMapper(Stats.class)).thenReturn(mapperA);
        when(mapperSet.classToMapper(BunchOfData.class)).thenReturn(mapperB);
        when(mapperSet.classToMapper(Looks.class)).thenReturn(mapperC);
        StoreSplitter from = StoreSplitter.from(engine);

        // execute
        StoreInspector split = from.splitComponents(asList(Stats.class, BunchOfData.class));
        StoreInspector reminder = from.getReminder();
        StoreInspector onlyC = from.splitComponents(asList(Looks.class));
        StoreInspector empty = from.getReminder();

        // assert
        Assertions.assertThat(split.attributes()).containsExactly(attributeA1, attributeA2, attributeB1, attributeB2);
        Assertions.assertThat(reminder.attributes()).containsExactly(attributeC);
        Assertions.assertThat(onlyC.attributes()).containsExactly(attributeC);
        Assertions.assertThat(empty.attributes()).isEmpty();
    }

    @Test
    @DisplayName("Should throw IllegalStateException ")
    public void splitComponents__noMapperSet() {
        // given
        StoreSplitter from = StoreSplitter.from(store);

        // execute & assert
        Assertions.assertThatThrownBy(() -> from.splitComponents(singletonList(Looks.class)))
                .isInstanceOf(IllegalStateException.class);
    }
}
