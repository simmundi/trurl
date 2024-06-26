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
import pl.edu.icm.trurl.ecs.util.Indexes;
import pl.edu.icm.trurl.exampledata.*;
import pl.edu.icm.trurl.util.query.QueryService;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class QueryServiceTest {

    EngineBuilder engineBuilder;
    Engine engine;
    Indexes indexes;
    QueryService service;
    Random random = new Random(0);
    String[] names = {"ANNA", "BARBARA", "CELINA", "DANUTA", "EWELINA", "FILIPINA", "GRAŻYNA", "HANNA", "IRENA", "JANINA", "KRZYSZTOFA"};

    @BeforeEach
    void prepare() {
        engineBuilder = Bento.createRoot().get(EngineBuilderFactory.IT);
        engineBuilder.addComponentClasses(Person.class, Stats.class, House.class);
        engine = engineBuilder.getEngine();
        indexes = new Indexes(engineBuilder, 25000);

        engine.execute(sf -> {
            Session session = sf.createOrGet();
            for (int i = 0; i < 1000; i++) {
                Entity entity = session.createEntity(
                        randomPerson(),
                        randomStats()
                );
                session.createEntity(new House(entity));
            }
            session.close();
        });

        service = new QueryService(indexes, engineBuilder);
    }

    @Test
    @Disabled
    void fixedIndexFromQuery() {
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
        PersonDao personDao = (PersonDao) engine.getDaoManager().classToDao(Person.class);
        StatsDao statsDao = (StatsDao) engine.getDaoManager().classToDao(Stats.class);

        // execute
        Index index = service.fixedIndexFromQuery(queryForWise);

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
                assertThat(personDao.getName(id)).isEqualTo(name);
                if (wise) {
                    assertThat(statsDao.getWis(id)).isEqualTo(5);
                } else {
                    assertThat(statsDao.getWis(id)).isLessThan(5);
                }
            });
        });
        assertThat(counter.get()).isEqualTo(engine.getCount() / 2);
        assertThat(foundNames).containsAll(asList(names));
        assertThat(foundWiseNames).containsAll(asList(names));
    }

    private enum IndexType {
        PERSON, HOUSE
    }

    @Test
    @Disabled
    void fixedMultipleIndexesFromRawQueryInParallel() {
        // given
        PersonDao personDao = (PersonDao) engine.getDaoManager().classToDao(Person.class);
        StatsDao statsDao = (StatsDao) engine.getDaoManager().classToDao(Stats.class);
        HouseDao houseDao = (HouseDao) engine.getDaoManager().classToDao(House.class);

        RawQuery<IndexType> rawQuery = (entityId, result, label) -> {
            if (personDao.isPresent(entityId) && statsDao.isPresent(entityId)) {
                String name = personDao.getName(entityId);
                if (statsDao.getWis(entityId) == 5)
                    result.add(entityId, "wise_" + name, IndexType.PERSON);
                else
                    result.add(entityId, "unwise_" + name, IndexType.PERSON);
            }
            if (houseDao.isPresent(entityId)) {
                result.add(entityId, label, IndexType.HOUSE);
            }
        };


        // execute
        Map<IndexType, RandomAccessIndex> index = service.
                fixedMultipleIndexesFromRawQueryInParallel(asList(IndexType.values()), rawQuery);

        // assert
        AtomicInteger counter = new AtomicInteger();
        Set<String> foundNames = new HashSet<>();
        Set<String> foundWiseNames = new HashSet<>();

        index.get(IndexType.PERSON).chunks().forEach(chunk -> {
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
                assertThat(personDao.getName(id)).isEqualTo(name);
                if (wise) {
                    assertThat(statsDao.getWis(id)).isEqualTo(5);
                } else {
                    assertThat(statsDao.getWis(id)).isLessThan(5);
                }
            });
        });

        index.get(IndexType.HOUSE).chunks().forEach(chunk ->
                chunk.ids().forEach(id -> {
                    counter.incrementAndGet();
                    assertThat(houseDao.isPresent(id)).isTrue();
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
