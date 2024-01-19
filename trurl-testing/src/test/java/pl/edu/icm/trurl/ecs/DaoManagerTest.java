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

import net.snowyhollows.bento.BentoFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.dao.Dao;
import pl.edu.icm.trurl.ecs.dao.DaosCreator;
import pl.edu.icm.trurl.ecs.util.DynamicComponentAccessor;
import pl.edu.icm.trurl.exampledata.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DaoManagerTest {

    @Spy
    ComponentAccessor componentAccessor = new DynamicComponentAccessor(Arrays.asList(Looks.class, Stats.class));
    Map<Class<?>, BentoFactory<?>> factories = new HashMap<>(); {
        factories.put(Looks.class, DaoOfLooksFactory.IT);
        factories.put(Stats.class, DaoOfStatsFactory.IT);
    }
    @Spy
    DaosCreator daosCreator = new DaosCreator();

    DaoManager daoManager;

    @BeforeEach
    void init() {
        daoManager = new DaoManager(componentAccessor, factories, daosCreator);

    }

    @Test
    void classToMapper() {
        // execute
        Looks looks = daoManager.classToDao(Looks.class).create();
        Stats stats = daoManager.classToDao(Stats.class).create();

        // assert
        assertThat(looks).isExactlyInstanceOf(Looks.class);
        assertThat(stats).isExactlyInstanceOf(Stats.class);
    }

    @Test
    void indexToMapper() {
        // execute
        Object stats = daoManager.indexToDao(1).create();
        Object looks = daoManager.indexToDao(0).create();

        // assert
        assertThat(looks).isExactlyInstanceOf(Looks.class);
        assertThat(stats).isExactlyInstanceOf(Stats.class);
    }

    @Test
    void classToIndex() {
        // execute
        int statsIndex = daoManager.classToIndex(Stats.class);
        int looksIndex = daoManager.classToIndex(Looks.class);

        // assert
        assertThat(looksIndex).isEqualTo(0);
        assertThat(statsIndex).isEqualTo(1);
    }

    @Test
    void componentCount() {
        // execute
        int count = daoManager.componentCount();

        // assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    void streamMappers() {
        // execute
        Stream<String> results = daoManager.allDaos().stream().map(Dao::name);

        // assert
        assertThat(results).containsExactly("looks", "stats");
    }
}