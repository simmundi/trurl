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

package pl.edu.icm.trurl.ecs.dao;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.config.Configurer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.exampledata.Coordinates;
import pl.edu.icm.trurl.exampledata.DaoOfThingFactory;
import pl.edu.icm.trurl.exampledata.Thing;
import pl.edu.icm.trurl.exampledata.ThingDao;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.basic.BasicAttributeFactory;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.join.Join;
import pl.edu.icm.trurl.store.join.SingleJoin;
import pl.edu.icm.trurl.store.join.SingleJoinWithReverse;
import pl.edu.icm.trurl.store.join.SingleJoinWithReverseOnly;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class EmbeddedDenseIT {
    public static final int SIZE = 10_000;
    Store store = new Store(new BasicAttributeFactory(), 10_000);
    ThingDao thingDao;

    @BeforeEach
    void beforeEach() {
        Bento config = new Configurer().setParam("daoPrefix", "").getConfig();
        thingDao = config.get(DaoOfThingFactory.IT);
        thingDao.configureAndAttach(store);
    }


    @Test
    void init() {
        // assert
        assertThat((Join)store.getJoin("coordinates")).isInstanceOf(SingleJoin.class);
        assertThat((Join)store.getJoin("coordinatesWithReverse")).isInstanceOf(SingleJoinWithReverse.class);
        assertThat((Join)store.getJoin("coordinatesWithReverseOnly")).isInstanceOf(SingleJoinWithReverseOnly.class);

        assertThat(store.getSubstores().stream().map(Store::getName)).containsExactlyInAnyOrder("coordinates",
                "coordinatesWithReverse",
                "coordinatesWithReverseOnly");
        assertThat(store.getSubstore("coordinates").getAllAttributes().stream().map(Attribute::name)).containsExactlyInAnyOrder("coordinates.x",
                "coordinates.y");
        assertThat(store.getSubstore("coordinatesWithReverse").getAllAttributes().stream().map(Attribute::name)).containsExactlyInAnyOrder("coordinatesWithReverse.x",
                "coordinatesWithReverse.y",
                "reverse");
        assertThat(store.getSubstore("coordinatesWithReverseOnly").getAllAttributes().stream().map(Attribute::name)).containsExactlyInAnyOrder("coordinatesWithReverseOnly.x",
                "coordinatesWithReverseOnly.y",
                "reverse");
        assertThat(store.getAllAttributes().stream().map(Attribute::name)).containsExactlyInAnyOrder("index",
                "coordinatesWithReverse",
                "coordinates");
    }

    @Test
    void create__fetch() {
        // execute
        IntStream.range(0, 10_000).forEach(i -> {
            Thing thing = new Thing();
            thing.setIndex(i);
            if (i % 50 == 0) {
                thing.setCoordinates(new Coordinates(i, -i));
            }
            if (i % 51 == 0) {
                thing.setCoordinatesWithReverse(new Coordinates(i + 1, -i + 1));
            }
            if (i % 52 == 0) {
                thing.setCoordinatesWithReverseOnly(new Coordinates(i + 2, -i + 2));
            }
            thingDao.save(thing, store.getCounter().next());
        });

        // assert
        assertThat(store.getCounter().getCount()).isEqualTo(SIZE);
        assertThat(store.getSubstore("coordinates").getCounter().getCount()).isEqualTo(SIZE / 50);
        assertThat(store.getSubstore("coordinatesWithReverse").getCounter().getCount()).isEqualTo(SIZE / 51 + 1);
        assertThat(store.getSubstore("coordinatesWithReverseOnly").getCounter().getCount()).isEqualTo(SIZE / 52 + 1);

        IntStream.range(0, SIZE).forEach(i -> {
            Thing thing = thingDao.createAndLoad(i);
            if (i % 50 == 0) {
                assertThat(thing.getCoordinates().getX()).isEqualTo(i);
                assertThat(thing.getCoordinates().getY()).isEqualTo(-i);
            } else {
                assertThat(thing.getCoordinates()).isNull();
            }
            if (i % 51 == 0) {
                assertThat(thing.getCoordinatesWithReverse().getX()).isEqualTo(i + 1);
                assertThat(thing.getCoordinatesWithReverse().getY()).isEqualTo(-i + 1);
            } else {
                assertThat(thing.getCoordinatesWithReverse()).isNull();
            }
            if (i % 52 == 0) {
                assertThat(thing.getCoordinatesWithReverseOnly().getX()).isEqualTo(i + 2);
                assertThat(thing.getCoordinatesWithReverseOnly().getY()).isEqualTo(-i + 2);
            } else {
                assertThat(thing.getCoordinatesWithReverseOnly()).isNull();
            }
        });
    }
}
