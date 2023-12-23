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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.ecs.*;
import pl.edu.icm.trurl.ecs.index.Index;
import pl.edu.icm.trurl.ecs.index.RandomAccessIndex;
import pl.edu.icm.trurl.ecs.util.Indices;
import pl.edu.icm.trurl.exampledata.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class QueryServiceTest {

    EngineConfiguration engineConfiguration;
    Engine engine;
    Indices indices;
    QueryService service;
    Random random = new Random(0);
    String[] names = {"ANNA", "BARBARA", "CELINA", "DANUTA", "EWELINA", "FILIPINA", "GRAŻYNA", "HANNA", "IRENA", "JANINA", "KRZYSZTOFA"};

    @BeforeEach
    void prepare() {
        engineConfiguration = Bento.createRoot().get(EngineConfigurationFactory.IT);
        engineConfiguration.addComponentClasses(Person.class, Stats.class, House.class);
        engine = engineConfiguration.getEngine();
        indices = new Indices(engineConfiguration);

        engine.execute(sf -> {
            NoCacheSession noCacheSession = sf.createOrGet(10000);
            for (int i = 0; i < 1000; i++) {
                DetachedEntity entity = noCacheSession.createEntity(
                        randomPerson(),
                        randomStats()
                );
                noCacheSession.createEntity(new House(entity));
            }
            noCacheSession.close();
        });

        service = new QueryService(indices, engineConfiguration);
    }

    @Test
    @Disabled
    void fixedSelectorFromQuery() {
        // given
        Query<?> queryForWise = (entity, result, label) -> {
            Person person = entity.get(Person.class);
            if (person == null) {
                return;
            }
            String name = person.getName();
            if (entity.get(Stats.class).getWis() == 5) {
                result.add(entity, "wise_" + name);
            } else {
                result.add(entity, "unwise_" + name);
            }
        };
        PersonDao personMapper = (PersonDao) engine.getDaoManager().classToMapper(Person.class);
        StatsDao statsMapper = (StatsDao) engine.getDaoManager().classToMapper(Stats.class);

        // execute
        Index index = service.fixedSelectorFromQuery(queryForWise);

        // assert
        AtomicInteger counter = new AtomicInteger();
        Set<String> foundNames = new HashSet<>();
        Set<String> foundWiseNames = new HashSet<>();

        index.chunks().forEach(chunk -> {
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
        assertThat(counter.get()).isEqualTo(engine.getCount() / 2);
        assertThat(foundNames).containsAll(asList(names));
        assertThat(foundWiseNames).containsAll(asList(names));
    }

    private enum SelectorType {
        PERSON, HOUSE
    }

    @Test
    @Disabled
    void fixedMultipleSelectorsFromRawQueryInParallel() {
        // given
        PersonDao personMapper = (PersonDao) engine.getDaoManager().classToMapper(Person.class);
        StatsDao statsMapper = (StatsDao) engine.getDaoManager().classToMapper(Stats.class);
        HouseDao houseMapper = (HouseDao) engine.getDaoManager().classToMapper(House.class);

        RawQuery<SelectorType> rawQuery = (entityId, result, label) -> {
            if (personMapper.isPresent(entityId) && statsMapper.isPresent(entityId)) {
                String name = personMapper.getName(entityId);
                if (statsMapper.getWis(entityId) == 5)
                    result.add(entityId, "wise_" + name, SelectorType.PERSON);
                else
                    result.add(entityId, "unwise_" + name, SelectorType.PERSON);
            }
            if (houseMapper.isPresent(entityId)) {
                result.add(entityId, label, SelectorType.HOUSE);
            }
        };


        // execute
        Map<SelectorType, RandomAccessIndex> selectors = service.
                fixedMultipleSelectorsFromRawQueryInParallel(asList(SelectorType.values()), rawQuery);

        // assert
        AtomicInteger counter = new AtomicInteger();
        Set<String> foundNames = new HashSet<>();
        Set<String> foundWiseNames = new HashSet<>();

        selectors.get(SelectorType.PERSON).chunks().forEach(chunk -> {
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

        selectors.get(SelectorType.HOUSE).chunks().forEach(chunk ->
                chunk.ids().forEach(id -> {
                    counter.incrementAndGet();
                    assertThat(houseMapper.isPresent(id)).isTrue();
                }));
        assertThat(counter.get()).isEqualTo(engine.getCount());
        assertThat(foundNames).containsAll(asList(names));
        assertThat(foundWiseNames).containsAll(asList(names));
    }

    private Stats randomStats() {
        return new Stats(
                random.nextInt(6),
                random.nextInt(6),
                random.nextInt(6)
        );
    }

    private Person randomPerson() {
        return new Person(names[random.nextInt(names.length)]);
    }
}