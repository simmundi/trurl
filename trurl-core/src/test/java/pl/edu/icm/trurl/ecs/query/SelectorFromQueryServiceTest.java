/*
 * Copyright (c) 2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

package pl.edu.icm.trurl.ecs.query;

import net.snowyhollows.bento.Bento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.ecs.*;
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.exampledata.Person;
import pl.edu.icm.trurl.exampledata.PersonMapper;
import pl.edu.icm.trurl.exampledata.Stats;
import pl.edu.icm.trurl.exampledata.StatsMapper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class SelectorFromQueryServiceTest {

    EngineConfiguration engineConfiguration;
    Engine engine;
    Selectors selectors;
    SelectorFromQueryService service;
    Random random = new Random(0);

    @BeforeEach
    void prepare() {
        engineConfiguration = Bento.createRoot().get(EngineConfigurationFactory.IT);
        engineConfiguration.addComponentClasses(Person.class, Stats.class);
        engine = engineConfiguration.getEngine();
        selectors = new Selectors(engineConfiguration);

        engine.execute(sf -> {
            Session session = sf.create(10000);
            for (int i = 0; i < 1000; i++) {
                session.createEntity(
                        randomPerson(),
                        randomStats()
                );
            }
            session.close();
        });

        service = new SelectorFromQueryService(selectors, engineConfiguration);
    }

    @Test
    void fixedSelectorFromQuery() {
        // given
        Query queryForWise = (entity, result, label) -> {
            String name = entity.get(Person.class).getName();
            if (entity.get(Stats.class).getWis() == 5) {
                result.add(entity, "wise_" + name);
            } else {
                result.add(entity, "unwise_" + name);
            }
        };
        PersonMapper personMapper = (PersonMapper) engine.getMapperSet().classToMapper(Person.class);
        StatsMapper statsMapper = (StatsMapper) engine.getMapperSet().classToMapper(Stats.class);

        // execute
        Selector selector = service.fixedSelectorFromQuery(queryForWise);

        // assert
        AtomicInteger counter = new AtomicInteger();
        Set<String> foundNames = new HashSet<>();
        Set<String> foundWiseNames = new HashSet<>();

        selector.chunks().forEach(chunk -> {
            String label = chunk.getChunkInfo().getLabel();
            String name = label.split("_")[1];
            boolean wise = label.startsWith("wise_");
            if (wise) {
                foundWiseNames.add(name);
            } else {
                foundNames.add(name);
            }

            chunk.ids().forEach(id -> {
                counter.incrementAndGet();
                assertThat(personMapper.getName(id)).isEqualTo(name);
                if (wise) {
                    assertThat(statsMapper.getWis(id)).isEqualTo(5);
                } else {
                    assertThat(statsMapper.getWis(id)).isLessThan(5);
                }
            });
        });
        assertThat(counter.get()).isEqualTo(engine.getCount());
        assertThat(foundNames).containsAll(Arrays.asList(names));
        assertThat(foundWiseNames).containsAll(Arrays.asList(names));
    }

    private Stats randomStats() {
        return new Stats(
                random.nextInt(6),
                random.nextInt(6),
                random.nextInt(6)
        );
    }

    String[] names = {"ANNA", "BARBARA", "CELINA", "DANUTA", "EWELINA", "FILIPINA", "GRAŻYNA", "HANNA", "IRENA", "JANINA", "KRZYSZTOFA"};

    private Person randomPerson() {
        return new Person(names[random.nextInt(names.length)]);
    }
}